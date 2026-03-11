"""
Metrik API'si.

GET /api/metrics/hosts              → Tüm host'ların son metrik snapshots
GET /api/metrics/hosts/{hostname}   → Belirli host metrikleri (zaman serisi)
GET /api/metrics/services           → Servis bazlı özet
GET /api/metrics/refresh            → Manuel zamanlayıcı tetikle
"""
import logging
from typing import Optional

from fastapi import APIRouter, Depends, Query
from sqlalchemy import func
from sqlalchemy.orm import Session

from ..db.models import MetricSnapshot
from .deps import get_db

log = logging.getLogger(__name__)
router = APIRouter(prefix="/api/metrics", tags=["metrics"])


# ─── GET /api/metrics/hosts ───────────────────────────────────────────────────

@router.get("/hosts")
def all_host_metrics(db: Session = Depends(get_db)):
    """
    Her host için en son metrik snapshot döndürür.
    Sonuç: {hostname: {metric_name: value, ...}}
    """
    # Her host+metric_name için en güncel ts
    subq = (
        db.query(
            MetricSnapshot.host,
            MetricSnapshot.metric_name,
            func.max(MetricSnapshot.ts).label("latest_ts")
        )
        .group_by(MetricSnapshot.host, MetricSnapshot.metric_name)
        .subquery()
    )

    rows = (
        db.query(MetricSnapshot)
        .join(subq, (MetricSnapshot.host == subq.c.host) &
                    (MetricSnapshot.metric_name == subq.c.metric_name) &
                    (MetricSnapshot.ts == subq.c.latest_ts))
        .all()
    )

    result: dict = {}
    for r in rows:
        result.setdefault(r.host, {})[r.metric_name] = r.value
    return result


# ─── GET /api/metrics/hosts/{hostname} ───────────────────────────────────────

@router.get("/hosts/{hostname}")
def host_metrics_history(
    hostname: str,
    metric: Optional[str] = Query(None, description="Metrik adı filtresi"),
    limit: int = Query(60, ge=1, le=1440),
    db: Session = Depends(get_db)
):
    """Son N adet snapshot döndürür (zaman serisi)."""
    q = db.query(MetricSnapshot).filter(MetricSnapshot.host == hostname)
    if metric:
        q = q.filter(MetricSnapshot.metric_name == metric)
    rows = q.order_by(MetricSnapshot.ts.desc()).limit(limit).all()
    return [
        {
            "metric_name": r.metric_name,
            "value":       r.value,
            "labels":      r.labels,
            "ts":          r.ts.isoformat()
        }
        for r in reversed(rows)
    ]


# ─── GET /api/metrics/services ───────────────────────────────────────────────

@router.get("/services")
def service_metrics(
    service_name: Optional[str] = Query(None),
    db: Session = Depends(get_db)
):
    """Servis bazlı son metrik özeti."""
    q = db.query(MetricSnapshot)
    if service_name:
        q = q.filter(MetricSnapshot.service_name == service_name)

    # Her service+metric_name için en son değer
    subq = (
        db.query(
            MetricSnapshot.service_name,
            MetricSnapshot.metric_name,
            func.max(MetricSnapshot.ts).label("latest_ts")
        )
        .group_by(MetricSnapshot.service_name, MetricSnapshot.metric_name)
        .subquery()
    )
    rows = (
        db.query(MetricSnapshot)
        .join(subq, (MetricSnapshot.service_name == subq.c.service_name) &
                    (MetricSnapshot.metric_name == subq.c.metric_name) &
                    (MetricSnapshot.ts == subq.c.latest_ts))
        .all()
    )

    result: dict = {}
    for r in rows:
        result.setdefault(r.service_name, {})[r.metric_name] = {
            "value":  r.value,
            "labels": r.labels,
            "ts":     r.ts.isoformat()
        }
    return result


# ─── POST /api/metrics/refresh ───────────────────────────────────────────────

@router.post("/refresh")
def manual_refresh():
    """Tüm zamanlayıcı görevlerini hemen tetikler."""
    try:
        from ..core.scheduler import trigger_all
        trigger_all()
        return {"status": "ok", "message": "Tüm görevler tetiklendi."}
    except Exception as e:
        log.error(f"Manuel refresh hatası: {e}")
        return {"status": "error", "message": str(e)}
