"""
Host ve servis listeleme API'si.

GET /api/services                     → Tüm servisler + host/daemon özeti
GET /api/services/{service_name}      → Tek servis detay
GET /api/hosts                        → Tüm host'lar
GET /api/hosts/{hostname}             → Host detayı + daemon'lar
"""
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from ..db.models import Service, Host, Daemon
from .deps import get_db

router = APIRouter(tags=["hosts"])


# ─── /api/services ────────────────────────────────────────────────────────────

@router.get("/api/services")
def list_services(db: Session = Depends(get_db)):
    services = db.query(Service).all()
    return [_service_to_dict(s, summary=True) for s in services]


@router.get("/api/services/{service_name}")
def get_service(service_name: str, db: Session = Depends(get_db)):
    svc = db.query(Service).filter_by(name=service_name).first()
    if not svc:
        raise HTTPException(status_code=404, detail="Servis bulunamadı")
    return _service_to_dict(svc, summary=False)


# ─── /api/hosts ───────────────────────────────────────────────────────────────

@router.get("/api/hosts")
def list_hosts(db: Session = Depends(get_db)):
    hosts = db.query(Host).all()
    return [_host_to_dict(h, full=False) for h in hosts]


@router.get("/api/hosts/{hostname}")
def get_host(hostname: str, db: Session = Depends(get_db)):
    host = db.query(Host).filter_by(hostname=hostname).first()
    if not host:
        raise HTTPException(status_code=404, detail="Host bulunamadı")
    return _host_to_dict(host, full=True)


# ─── Yardımcılar ──────────────────────────────────────────────────────────────

def _service_to_dict(svc: Service, summary: bool) -> dict:
    hosts = svc.hosts if not summary else []
    result = {
        "id":          svc.id,
        "name":        svc.name,
        "type":        svc.type,
        "description": svc.description,
        "extra":       svc.extra,
        "host_count":  len(svc.hosts),
        "created_at":  svc.created_at.isoformat() if svc.created_at else None,
    }
    if not summary:
        result["hosts"] = [_host_to_dict(h, full=True) for h in svc.hosts]
    return result


def _host_to_dict(host: Host, full: bool) -> dict:
    result = {
        "id":         host.id,
        "hostname":   host.hostname,
        "ip":         host.ip,
        "jump_via":   host.jump_via,
        "service_id": host.service_id,
        "service_name": host.service.name if host.service else None,
    }
    if full:
        result["daemons"] = [
            {
                "id":             d.id,
                "daemon_type":    d.daemon_type,
                "systemctl_name": d.systemctl_name,
                "log_path":       d.log_path,
                "last_status":    d.last_status,
                "last_checked":   d.last_checked.isoformat() if d.last_checked else None,
            }
            for d in host.daemons
        ]
    return result
