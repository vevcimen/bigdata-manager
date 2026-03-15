"""
Servis-spesifik detay API'leri.

GET /api/services/{service_name}/detail
    → Servise özel metrik/bilgi (HDFS, Spark, Trino, Airflow)

GET /api/services/{service_name}/trino/query/{query_id}
    → Belirli bir Trino sorgusunun tam detayı ve sorgu planı
"""
import logging
from typing import Optional, Any

import httpx
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from ..db.models import Service, MetricSnapshot
from .deps import get_db

log = logging.getLogger(__name__)
router = APIRouter(tags=["service-detail"])

TIMEOUT = httpx.Timeout(15.0)


# ─── HTTP yardımcıları ────────────────────────────────────────────────────────

async def _fetch_json(url: str, auth: Optional[tuple] = None) -> dict:
    """URL'den JSON çek (genel)."""
    try:
        async with httpx.AsyncClient(timeout=TIMEOUT, verify=False) as client:
            kwargs = {}
            if auth:
                kwargs["auth"] = auth
            resp = await client.get(url, **kwargs)
            resp.raise_for_status()
            return resp.json()
    except Exception as e:
        log.warning(f"Servis detay fetch hatası ({url}): {e}")
        return {"error": str(e)}


async def _fetch_trino_json(
    url: str,
    trino_user: str = "",
    auth: Optional[tuple] = None,
) -> Any:
    """Trino REST API'den JSON çek (X-Trino-User header dahil)."""
    try:
        headers = {}
        if trino_user:
            headers["X-Trino-User"] = trino_user
        async with httpx.AsyncClient(timeout=TIMEOUT, verify=False) as client:
            kwargs: dict = {"headers": headers}
            if auth:
                kwargs["auth"] = auth
            resp = await client.get(url, **kwargs)
            resp.raise_for_status()
            return resp.json()
    except Exception as e:
        log.warning(f"Trino fetch hatası ({url}): {e}")
        return {"error": str(e)}


async def _fetch_text(url: str, auth: Optional[tuple] = None) -> str:
    """URL'den text çek."""
    try:
        async with httpx.AsyncClient(timeout=TIMEOUT, verify=False) as client:
            kwargs = {}
            if auth:
                kwargs["auth"] = auth
            resp = await client.get(url, **kwargs)
            resp.raise_for_status()
            return resp.text
    except Exception as e:
        log.warning(f"Servis detay fetch hatası ({url}): {e}")
        return ""


# ─── Ana detay endpoint ───────────────────────────────────────────────────────

@router.get("/api/services/{service_name}/detail")
async def service_detail(service_name: str, db: Session = Depends(get_db)):
    """Servis tipine göre detay bilgilerini döndürür."""
    svc = db.query(Service).filter_by(name=service_name).first()
    if not svc:
        raise HTTPException(status_code=404, detail="Servis bulunamadı")

    extra = svc.extra or {}
    result = {"service_name": svc.name, "service_type": svc.type, "data": {}}

    if svc.type == "hdfs":
        result["data"] = await _hdfs_detail(svc, extra, db)
    elif svc.type == "spark":
        result["data"] = await _spark_detail(svc, extra)
    elif svc.type == "trino":
        result["data"] = await _trino_detail(svc, extra)
    elif svc.type == "airflow":
        result["data"] = await _airflow_detail(svc, extra)
    else:
        result["data"] = {"info": f"{svc.type} tipi için henüz detay paneli tanımlı değil."}

    return result


# ─── Trino sorgu planı endpoint ───────────────────────────────────────────────

@router.get("/api/services/{service_name}/trino/query/{query_id}")
async def trino_query_detail(
    service_name: str,
    query_id: str,
    db: Session = Depends(get_db),
):
    """Belirli bir Trino sorgusunun tam detayını ve sorgu planını döndürür."""
    svc = db.query(Service).filter_by(name=service_name).first()
    if not svc:
        raise HTTPException(status_code=404, detail="Servis bulunamadı")

    extra = svc.extra or {}
    webui_url = extra.get("webui_url", "")
    if not webui_url:
        raise HTTPException(status_code=400, detail="webui_url tanımlı değil")

    username   = extra.get("username", "")
    password   = extra.get("password", "")
    trino_user = extra.get("trino_user", username)
    auth       = (username, password) if username and password else None

    url    = webui_url.rstrip("/") + f"/v1/query/{query_id}"
    detail = await _fetch_trino_json(url, trino_user, auth)

    if isinstance(detail, dict) and "error" in detail:
        raise HTTPException(status_code=502, detail=detail["error"])

    stats = detail.get("queryStats", {})
    session = detail.get("session", {})

    return {
        "queryId":   detail.get("queryId"),
        "state":     detail.get("state"),
        "query":     detail.get("query", ""),
        "user":      session.get("user", ""),
        "source":    session.get("source", ""),
        "catalog":   session.get("catalog", ""),
        "schema":    session.get("schema", ""),
        "stats": {
            "createTime":                stats.get("createTime"),
            "executionStartTime":         stats.get("executionStartTime"),
            "elapsedTime":               stats.get("elapsedTime"),
            "queuedTime":                stats.get("queuedTime"),
            "planningTime":              stats.get("planningTime"),
            "finishingTime":             stats.get("finishingTime"),
            "totalCpuTime":              stats.get("totalCpuTime"),
            "totalScheduledTime":        stats.get("totalScheduledTime"),
            "totalBlockedTime":          stats.get("totalBlockedTime"),
            "userMemoryReservation":     stats.get("userMemoryReservation"),
            "peakUserMemoryReservation": stats.get("peakUserMemoryReservation"),
            "totalMemoryReservation":    stats.get("totalMemoryReservation"),
            "peakTotalMemoryReservation":stats.get("peakTotalMemoryReservation"),
            "rawInputDataSize":          stats.get("rawInputDataSize"),
            "rawInputPositions":         stats.get("rawInputPositions"),
            "outputDataSize":            stats.get("outputDataSize"),
            "outputPositions":           stats.get("outputPositions"),
            "physicalInputDataSize":     stats.get("physicalInputDataSize"),
            "spilledDataSize":           stats.get("spilledDataSize"),
            "completedDrivers":          stats.get("completedDrivers"),
            "totalDrivers":              stats.get("totalDrivers"),
            "progressPercentage":        stats.get("progressPercentage"),
        },
        "outputStage":  detail.get("outputStage"),
        "failureInfo":  detail.get("failureInfo"),
        "warnings":     detail.get("warnings", []),
    }


# ─── HDFS ────────────────────────────────────────────────────────────────────

async def _hdfs_detail(svc: Service, extra: dict, db: Session) -> dict:
    """HDFS detay: namenode overview + historik doluluk."""
    data = {}

    webui_url = extra.get("webui_url", "")
    if webui_url:
        jmx_url = webui_url.rstrip("/") + "/jmx?qry=Hadoop:service=NameNode,name=FSNamesystemState"
        jmx = await _fetch_json(jmx_url)
        beans = jmx.get("beans", [])
        if beans:
            bean = beans[0]
            data["summary"] = {
                "CapacityTotal":     bean.get("CapacityTotal"),
                "CapacityUsed":      bean.get("CapacityUsed"),
                "CapacityRemaining": bean.get("CapacityRemaining"),
                "BlocksTotal":       bean.get("BlocksTotal"),
                "FilesTotal":        bean.get("FilesTotal"),
                "NumLiveDataNodes":  bean.get("NumLiveDataNodes"),
                "NumDeadDataNodes":  bean.get("NumDeadDataNodes"),
                "NumStaleDataNodes": bean.get("NumStaleDataNodes"),
            }
            if data["summary"]["CapacityTotal"] and data["summary"]["CapacityUsed"]:
                total = data["summary"]["CapacityTotal"]
                used  = data["summary"]["CapacityUsed"]
                data["summary"]["UsedPercent"] = round(used / total * 100, 2) if total > 0 else 0
        else:
            data["summary"] = jmx

    snapshots = db.query(MetricSnapshot).filter(
        MetricSnapshot.service_name == svc.name,
        MetricSnapshot.metric_name.in_(["hdfs_capacity_used_percent", "disk_used_percent"])
    ).order_by(MetricSnapshot.ts.desc()).limit(100).all()

    data["history"] = [
        {"ts": s.ts.isoformat(), "metric": s.metric_name, "value": s.value}
        for s in reversed(snapshots)
    ]

    return data


# ─── Spark ───────────────────────────────────────────────────────────────────

async def _spark_detail(svc: Service, extra: dict) -> dict:
    """Spark detay: running + completed applications."""
    data = {"running": [], "completed": []}

    webui_url = extra.get("webui_url", "")
    if webui_url:
        apps_url = webui_url.rstrip("/") + "/api/v1/applications?status=running"
        running = await _fetch_json(apps_url)
        if isinstance(running, list):
            data["running"] = running

        completed_url = webui_url.rstrip("/") + "/api/v1/applications?status=completed&limit=10"
        completed = await _fetch_json(completed_url)
        if isinstance(completed, list):
            data["completed"] = completed[:10]

    return data


# ─── Trino ───────────────────────────────────────────────────────────────────

async def _trino_detail(svc: Service, extra: dict) -> dict:
    """Trino detay: cluster stats + çalışan sorgular."""
    data = {"queries": [], "cluster": {}}

    webui_url  = extra.get("webui_url", "")
    username   = extra.get("username", "")
    password   = extra.get("password", "")
    trino_user = extra.get("trino_user", username)

    # allowed_users: JSON dizi veya virgülle ayrılmış string
    allowed_users = extra.get("allowed_users", [])
    if isinstance(allowed_users, str):
        allowed_users = [u.strip() for u in allowed_users.split(",") if u.strip()]

    if not webui_url:
        return data

    auth = (username, password) if username and password else None
    base = webui_url.rstrip("/")

    # Cluster istatistikleri (/v1/cluster)
    cluster = await _fetch_trino_json(base + "/v1/cluster", trino_user, auth)
    if isinstance(cluster, dict) and "error" not in cluster:
        data["cluster"] = {
            "runningQueries":         cluster.get("runningQueries", 0),
            "blockedQueries":         cluster.get("blockedQueries", 0),
            "queuedQueries":          cluster.get("queuedQueries", 0),
            "activeWorkers":          cluster.get("activeWorkers", 0),
            "runningDrivers":         cluster.get("runningDrivers", 0),
            "reservedMemory":         cluster.get("reservedMemory", 0),
            "totalAvailableMemory":   cluster.get("totalAvailableMemory", 0),
            "totalInputRows":         cluster.get("totalInputRows", 0),
            "totalInputBytes":        cluster.get("totalInputBytes", 0),
            "totalCpuTimeSecs":       cluster.get("totalCpuTimeSecs", 0),
        }
    elif isinstance(cluster, dict) and "error" in cluster:
        data["cluster_error"] = cluster["error"]

    # Çalışan sorgular (/v1/query)
    queries = await _fetch_trino_json(base + "/v1/query", trino_user, auth)
    if isinstance(queries, list):
        active = [
            q for q in queries
            if q.get("state") in ("RUNNING", "QUEUED", "PLANNING", "STARTING", "FINISHING", "BLOCKED")
        ]
        # Kullanıcı filtresi
        if allowed_users:
            active = [q for q in active if q.get("session", {}).get("user", "") in allowed_users]

        data["queries"] = [
            {
                "queryId":          q.get("queryId"),
                "state":            q.get("state"),
                "query":            q.get("query", "")[:300],
                "user":             q.get("session", {}).get("user", ""),
                "source":           q.get("session", {}).get("source", ""),
                "catalog":          q.get("session", {}).get("catalog", ""),
                "schema":           q.get("session", {}).get("schema", ""),
                "elapsedTime":      q.get("queryStats", {}).get("elapsedTime", ""),
                "cpuTime":          q.get("queryStats", {}).get("totalCpuTime", ""),
                "currentMemory":    q.get("queryStats", {}).get("userMemoryReservation", "0B"),
                "peakMemory":       q.get("queryStats", {}).get("peakUserMemoryReservation", "0B"),
                "totalMemory":      q.get("queryStats", {}).get("totalMemoryReservation", "0B"),
                "rawInputDataSize": q.get("queryStats", {}).get("rawInputDataSize", "0B"),
                "rawInputPositions":q.get("queryStats", {}).get("rawInputPositions", 0),
                "progress":         q.get("queryStats", {}).get("progressPercentage", 0),
                "completedDrivers": q.get("queryStats", {}).get("completedDrivers", 0),
                "totalDrivers":     q.get("queryStats", {}).get("totalDrivers", 0),
            }
            for q in active
        ]
    elif isinstance(queries, dict) and "error" in queries:
        data["queries_error"] = queries["error"]

    return data


# ─── Airflow ─────────────────────────────────────────────────────────────────

async def _airflow_detail(svc: Service, extra: dict) -> dict:
    """Airflow detay: running DAG'lar."""
    data = {"dag_runs": []}

    webui_url = extra.get("webui_url", "")
    username  = extra.get("username", "")
    password  = extra.get("password", "")

    if webui_url:
        auth = (username, password) if username else None
        runs_url = webui_url.rstrip("/") + "/api/v1/dags/~/dagRuns?state=running&limit=50"
        result = await _fetch_json(runs_url, auth=auth)
        if isinstance(result, dict):
            runs = result.get("dag_runs", [])
            data["dag_runs"] = [
                {
                    "dag_id":         r.get("dag_id"),
                    "run_id":         r.get("dag_run_id"),
                    "state":          r.get("state"),
                    "start_date":     r.get("start_date"),
                    "execution_date": r.get("execution_date"),
                }
                for r in runs
            ]

    return data
