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


def _parse_bytes(s: str) -> int:
    """Trino bellek string'ini byte'a çevirir (ör: '512MB' → 536870912)."""
    try:
        s = s.strip()
        units = {"B": 1, "KB": 1024, "MB": 1024**2, "GB": 1024**3, "TB": 1024**4}
        for unit, mult in sorted(units.items(), key=lambda x: -len(x[0])):
            if s.upper().endswith(unit):
                return int(float(s[:-len(unit)]) * mult)
        return int(float(s))
    except Exception:
        return 0


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

@router.delete("/api/services/{service_name}/trino/query/{query_id}")
async def trino_kill_query(
    service_name: str,
    query_id: str,
    db: Session = Depends(get_db),
):
    """Çalışan bir Trino sorgusunu durdurur (DELETE /v1/query/{id})."""
    svc = db.query(Service).filter_by(name=service_name).first()
    if not svc:
        raise HTTPException(status_code=404, detail="Servis bulunamadı")

    extra = svc.extra or {}
    webui_url  = extra.get("webui_url", "")
    username   = extra.get("username", "")
    password   = extra.get("password", "")
    trino_user = extra.get("trino_user", username)

    if not webui_url:
        raise HTTPException(status_code=400, detail="webui_url tanımlı değil")

    url = webui_url.rstrip("/") + f"/v1/query/{query_id}"
    try:
        headers = {}
        if trino_user:
            headers["X-Trino-User"] = trino_user
        async with httpx.AsyncClient(timeout=TIMEOUT, verify=False) as client:
            kwargs: dict = {"headers": headers}
            if username and password:
                kwargs["auth"] = (username, password)
            resp = await client.delete(url, **kwargs)
            if resp.status_code in (200, 204):
                return {"success": True, "queryId": query_id}
            raise HTTPException(status_code=resp.status_code, detail=f"Trino yanıtı: {resp.status_code}")
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=502, detail=str(e))


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
        "outputStage":   detail.get("outputStage"),
        "failureInfo":   detail.get("failureInfo"),
        "warnings":      detail.get("warnings", []),
        "expandedQuery": detail.get("expandedQuery"),  # prepared statement gerçek SQL
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
    """Spark detay: running (Master UI /json/) + son 100 completed (History Server)."""
    data = {"running": [], "completed": []}

    # webui_url → Spark Master UI (port 8080)
    # history_url → Spark History Server (port 18080) — opsiyonel
    master_url  = extra.get("webui_url", "").rstrip("/")
    history_url = extra.get("history_url", "").rstrip("/")

    # ── Çalışan işler: Master UI /json/ → activeapps ─────────────────────────
    if master_url:
        master_json = await _fetch_json(master_url + "/json/")
        if isinstance(master_json, dict):
            for app in master_json.get("activeapps", []):
                duration_ms = app.get("duration", 0)
                data["running"].append({
                    "id":           app.get("id", ""),
                    "name":         app.get("name", ""),
                    "user":         app.get("user", ""),
                    "startTime":    app.get("starttime", ""),
                    "endTime":      "",
                    "durationMs":   duration_ms,
                    "duration":     _fmt_duration(duration_ms),
                    "completed":    False,
                    "sparkVersion": "",
                    "cores":        app.get("cores", ""),
                    "memoryPerSlave": app.get("memoryperslave", ""),
                })

    # ── Tamamlanan işler: History Server /api/v1/applications ─────────────────
    hist_base = history_url or master_url  # history_url yoksa master_url'yi dene
    if hist_base:
        def _fmt_hist(app: dict) -> dict:
            attempt = (app.get("attempts") or [{}])[0]
            duration_ms = attempt.get("duration", 0)
            return {
                "id":           app.get("id", ""),
                "name":         app.get("name", ""),
                "user":         attempt.get("sparkUser", ""),
                "startTime":    attempt.get("startTime", ""),
                "endTime":      attempt.get("endTime", ""),
                "durationMs":   duration_ms,
                "duration":     _fmt_duration(duration_ms),
                "completed":    attempt.get("completed", False),
                "sparkVersion": attempt.get("appSparkVersion", ""),
            }

        completed_raw = await _fetch_json(hist_base + "/api/v1/applications?status=completed&limit=100")
        if isinstance(completed_raw, list):
            data["completed"] = [_fmt_hist(a) for a in completed_raw]

    return data


def _fmt_duration(ms: int) -> str:
    if not ms:
        return "-"
    s = ms // 1000
    if s < 60:
        return f"{s}s"
    m, s = divmod(s, 60)
    if m < 60:
        return f"{m}d {s}s"
    h, m = divmod(m, 60)
    return f"{h}s {m}d"


# ─── Trino ───────────────────────────────────────────────────────────────────

async def _trino_detail(svc: Service, extra: dict) -> dict:
    """Trino detay: cluster stats + çalışan sorgular."""
    data = {"queries": [], "cluster": {}}

    webui_url      = extra.get("webui_url", "")
    username       = extra.get("username", "")
    password       = extra.get("password", "")
    trino_user     = extra.get("trino_user", username)

    # allowed_users: JSON dizi veya virgülle ayrılmış string
    allowed_users = extra.get("allowed_users", [])
    if isinstance(allowed_users, str):
        allowed_users = [u.strip() for u in allowed_users.split(",") if u.strip()]

    # excluded_users: sistem kullanıcılarını filtrele (ör: airflow)
    excluded_users = extra.get("excluded_users", [])
    if isinstance(excluded_users, str):
        excluded_users = [u.strip() for u in excluded_users.split(",") if u.strip()]

    if not webui_url:
        return data

    auth = (username, password) if username and password else None
    base = webui_url.rstrip("/")

    # Worker sayısı (/v1/node)
    nodes = await _fetch_trino_json(base + "/v1/node", trino_user, auth)
    active_workers = len(nodes) if isinstance(nodes, list) else 0

    # Tüm sorgular (/v1/query)
    queries = await _fetch_trino_json(base + "/v1/query", trino_user, auth)
    if isinstance(queries, list):

        # Cluster istatistiklerini tüm query listesinden türet
        all_states = [q.get("state") for q in queries]
        running_queries = [q for q in queries if q.get("state") == "RUNNING"]
        data["cluster"] = {
            "runningQueries": all_states.count("RUNNING"),
            "blockedQueries": all_states.count("BLOCKED"),
            "queuedQueries":  all_states.count("QUEUED"),
            "activeWorkers":  active_workers,
            "runningDrivers": sum(q.get("queryStats", {}).get("runningDrivers", 0) for q in running_queries),
            "reservedMemory": sum(
                _parse_bytes(q.get("queryStats", {}).get("userMemoryReservation", "0B"))
                for q in running_queries
            ),
        }

        # Aktif sorgular
        active = [q for q in queries if q.get("state") in ("RUNNING", "QUEUED", "PLANNING", "STARTING", "FINISHING", "BLOCKED")]

        # Sistem kullanıcılarını çıkar
        if excluded_users:
            active = [q for q in active if q.get("session", {}).get("user", "") not in excluded_users]

        def _progress(q):
            completed = q.get("queryStats", {}).get("completedDrivers", 0)
            total     = q.get("queryStats", {}).get("totalDrivers", 0)
            return round((completed / total) * 100, 1) if total > 0 else 0

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
                "processedRows":    q.get("queryStats", {}).get("processedRows", 0),
                "rawInputDataSize": q.get("queryStats", {}).get("rawInputDataSize", "0B"),
                "progress":         _progress(q),
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
