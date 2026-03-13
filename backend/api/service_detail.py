"""
Servis-spesifik detay API'leri.

GET /api/services/{service_name}/detail → Servise özel metrik/bilgi (HDFS, Spark, Trino, Airflow)
"""
import logging
from typing import Optional

import httpx
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from ..db.models import Service, MetricSnapshot
from .deps import get_db

log = logging.getLogger(__name__)
router = APIRouter(tags=["service-detail"])

TIMEOUT = httpx.Timeout(10.0)


async def _fetch_json(url: str, auth: Optional[tuple] = None) -> dict:
    """URL'den JSON çek."""
    try:
        async with httpx.AsyncClient(timeout=TIMEOUT, verify=False) as client:
            kwargs = {}
            if auth:
                kwargs['auth'] = auth
            resp = await client.get(url, **kwargs)
            resp.raise_for_status()
            return resp.json()
    except Exception as e:
        log.warning(f"Servis detay fetch hatası ({url}): {e}")
        return {"error": str(e)}


async def _fetch_text(url: str, auth: Optional[tuple] = None) -> str:
    """URL'den text çek."""
    try:
        async with httpx.AsyncClient(timeout=TIMEOUT, verify=False) as client:
            kwargs = {}
            if auth:
                kwargs['auth'] = auth
            resp = await client.get(url, **kwargs)
            resp.raise_for_status()
            return resp.text
    except Exception as e:
        log.warning(f"Servis detay fetch hatası ({url}): {e}")
        return ""


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


async def _hdfs_detail(svc: Service, extra: dict, db: Session) -> dict:
    """HDFS detay: namenode overview + historik doluluk."""
    data = {}

    # Namenode web UI'dan overview
    webui_url = extra.get("webui_url", "")
    if webui_url:
        jmx_url = webui_url.rstrip("/") + "/jmx?qry=Hadoop:service=NameNode,name=FSNamesystemState"
        jmx = await _fetch_json(jmx_url)
        beans = jmx.get("beans", [])
        if beans:
            bean = beans[0]
            data["summary"] = {
                "CapacityTotal": bean.get("CapacityTotal"),
                "CapacityUsed": bean.get("CapacityUsed"),
                "CapacityRemaining": bean.get("CapacityRemaining"),
                "BlocksTotal": bean.get("BlocksTotal"),
                "FilesTotal": bean.get("FilesTotal"),
                "NumLiveDataNodes": bean.get("NumLiveDataNodes"),
                "NumDeadDataNodes": bean.get("NumDeadDataNodes"),
                "NumStaleDataNodes": bean.get("NumStaleDataNodes"),
            }
            if data["summary"]["CapacityTotal"] and data["summary"]["CapacityUsed"]:
                total = data["summary"]["CapacityTotal"]
                used = data["summary"]["CapacityUsed"]
                data["summary"]["UsedPercent"] = round(used / total * 100, 2) if total > 0 else 0
        else:
            data["summary"] = jmx  # hata varsa error dict döner

    # Historik doluluk (MetricSnapshot'tan)
    snapshots = db.query(MetricSnapshot).filter(
        MetricSnapshot.service_name == svc.name,
        MetricSnapshot.metric_name.in_(["hdfs_capacity_used_percent", "disk_used_percent"])
    ).order_by(MetricSnapshot.ts.desc()).limit(100).all()

    data["history"] = [
        {"ts": s.ts.isoformat(), "metric": s.metric_name, "value": s.value}
        for s in reversed(snapshots)
    ]

    return data


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


async def _trino_detail(svc: Service, extra: dict) -> dict:
    """Trino detay: running queries."""
    data = {"queries": []}

    webui_url = extra.get("webui_url", "")
    username = extra.get("username", "")
    password = extra.get("password", "")

    if webui_url:
        auth = (username, password) if username else None
        queries_url = webui_url.rstrip("/") + "/v1/query"
        queries = await _fetch_json(queries_url, auth=auth)
        if isinstance(queries, list):
            # Sadece RUNNING ve QUEUED query'leri
            active = [q for q in queries if q.get("state") in ("RUNNING", "QUEUED", "PLANNING")]
            data["queries"] = [
                {
                    "queryId": q.get("queryId"),
                    "state": q.get("state"),
                    "query": (q.get("query", "")[:200]),
                    "user": q.get("session", {}).get("user", ""),
                    "elapsedTime": q.get("queryStats", {}).get("elapsedTime", ""),
                    "progress": q.get("queryStats", {}).get("progressPercentage", 0),
                    "completedDrivers": q.get("queryStats", {}).get("completedDrivers", 0),
                    "totalDrivers": q.get("queryStats", {}).get("totalDrivers", 0),
                }
                for q in active
            ]

    return data


async def _airflow_detail(svc: Service, extra: dict) -> dict:
    """Airflow detay: running DAG'lar."""
    data = {"dag_runs": []}

    webui_url = extra.get("webui_url", "")
    username = extra.get("username", "")
    password = extra.get("password", "")

    if webui_url:
        auth = (username, password) if username else None
        runs_url = webui_url.rstrip("/") + "/api/v1/dags/~/dagRuns?state=running&limit=50"
        result = await _fetch_json(runs_url, auth=auth)
        if isinstance(result, dict):
            runs = result.get("dag_runs", [])
            data["dag_runs"] = [
                {
                    "dag_id": r.get("dag_id"),
                    "run_id": r.get("dag_run_id"),
                    "state": r.get("state"),
                    "start_date": r.get("start_date"),
                    "execution_date": r.get("execution_date"),
                }
                for r in runs
            ]

    return data
