"""
Host ve servis listeleme / CRUD API'si.

GET    /api/services                     → Tüm servisler + host/daemon özeti
GET    /api/services/{service_name}      → Tek servis detay
GET    /api/hosts                        → Tüm host'lar
GET    /api/hosts/{hostname}             → Host detayı + daemon'lar
POST   /api/hosts                        → Yeni host ekle
PUT    /api/hosts/{host_id}              → Host güncelle
DELETE /api/hosts/{host_id}              → Host sil
GET    /api/topology/export              → DB configlerini topology.yaml formatında export et
POST   /api/services                     → Yeni servis ekle
PUT    /api/services/{service_id}        → Servis güncelle
DELETE /api/services/{service_id}        → Servis sil
"""
from fastapi import APIRouter, Depends, HTTPException
from fastapi.responses import Response
from pydantic import BaseModel
from typing import Optional, List, Dict, Any
from sqlalchemy.orm import Session
import yaml

from ..db.models import Service, Host, Daemon
from .deps import get_db

router = APIRouter(tags=["hosts"])


# ─── Pydantic modeller ────────────────────────────────────────────────────────

class DaemonInput(BaseModel):
    daemon_type: str
    systemctl_name: str
    log_path: Optional[str] = None

class HostCreate(BaseModel):
    hostname: str
    ip: Optional[str] = None
    service_name: str
    jump_via: Optional[str] = None
    daemons: Optional[List[DaemonInput]] = []

class HostUpdate(BaseModel):
    hostname: Optional[str] = None
    ip: Optional[str] = None
    service_name: Optional[str] = None
    jump_via: Optional[str] = None
    daemons: Optional[List[DaemonInput]] = None

class ServiceCreate(BaseModel):
    name: str
    type: str
    description: Optional[str] = ""
    extra: Optional[Dict[str, Any]] = {}

class ServiceUpdate(BaseModel):
    name: Optional[str] = None
    type: Optional[str] = None
    description: Optional[str] = None
    extra: Optional[Dict[str, Any]] = None


# ─── /api/topology/export ─────────────────────────────────────────────────────

@router.get("/api/topology/export")
def export_topology(db: Session = Depends(get_db)):
    """DB'deki tüm configleri topology.yaml formatında export eder."""
    services = db.query(Service).all()
    result = {"services": []}
    for svc in services:
        svc_def = {
            "name": svc.name,
            "type": svc.type,
            "description": svc.description or "",
            "hosts": [],
        }
        if svc.extra:
            svc_def["extra"] = svc.extra
        for host in svc.hosts:
            host_def = {
                "hostname": host.hostname,
                "ip": host.ip or "",
            }
            if host.jump_via:
                host_def["jump_via"] = host.jump_via
            roles = []
            for d in host.daemons:
                role = {
                    "type": d.daemon_type,
                    "systemctl": d.systemctl_name,
                }
                if d.log_path:
                    role["log_path"] = d.log_path
                roles.append(role)
            if roles:
                host_def["roles"] = roles
            svc_def["hosts"].append(host_def)
        result["services"].append(svc_def)
    yaml_content = yaml.dump(result, allow_unicode=True, default_flow_style=False, sort_keys=False)
    return Response(content=yaml_content, media_type="text/yaml",
                    headers={"Content-Disposition": "attachment; filename=topology.yaml"})


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


@router.post("/api/services", status_code=201)
def create_service(body: ServiceCreate, db: Session = Depends(get_db)):
    existing = db.query(Service).filter_by(name=body.name).first()
    if existing:
        raise HTTPException(status_code=409, detail=f"Bu servis zaten mevcut: {body.name}")
    svc = Service(name=body.name, type=body.type, description=body.description, extra=body.extra or {})
    db.add(svc)
    db.commit()
    db.refresh(svc)
    return _service_to_dict(svc, summary=True)


@router.put("/api/services/{service_id}")
def update_service(service_id: int, body: ServiceUpdate, db: Session = Depends(get_db)):
    svc = db.query(Service).filter_by(id=service_id).first()
    if not svc:
        raise HTTPException(status_code=404, detail="Servis bulunamadı")
    if body.name is not None:
        dup = db.query(Service).filter(Service.name == body.name, Service.id != service_id).first()
        if dup:
            raise HTTPException(status_code=409, detail=f"Bu isimde servis zaten var: {body.name}")
        svc.name = body.name
    if body.type is not None:
        svc.type = body.type
    if body.description is not None:
        svc.description = body.description
    if body.extra is not None:
        svc.extra = body.extra
    db.commit()
    db.refresh(svc)
    return _service_to_dict(svc, summary=True)


@router.delete("/api/services/{service_id}")
def delete_service(service_id: int, db: Session = Depends(get_db)):
    svc = db.query(Service).filter_by(id=service_id).first()
    if not svc:
        raise HTTPException(status_code=404, detail="Servis bulunamadı")
    db.delete(svc)
    db.commit()
    return {"status": "deleted", "id": service_id}


# ─── /api/hosts ───────────────────────────────────────────────────────────────

@router.get("/api/hosts")
def list_hosts(db: Session = Depends(get_db)):
    hosts = db.query(Host).all()
    return [_host_to_dict(h, full=True) for h in hosts]


@router.get("/api/hosts/{hostname}")
def get_host(hostname: str, db: Session = Depends(get_db)):
    host = db.query(Host).filter_by(hostname=hostname).first()
    if not host:
        raise HTTPException(status_code=404, detail="Host bulunamadı")
    return _host_to_dict(host, full=True)


@router.post("/api/hosts", status_code=201)
def create_host(body: HostCreate, db: Session = Depends(get_db)):
    svc = db.query(Service).filter_by(name=body.service_name).first()
    if not svc:
        raise HTTPException(status_code=404, detail=f"Servis bulunamadı: {body.service_name}")
    existing = db.query(Host).filter_by(hostname=body.hostname, service_id=svc.id).first()
    if existing:
        raise HTTPException(status_code=409, detail=f"Bu host zaten mevcut: {body.hostname}")
    host = Host(
        hostname=body.hostname,
        ip=body.ip,
        service_id=svc.id,
        jump_via=body.jump_via,
    )
    db.add(host)
    db.flush()
    for d in (body.daemons or []):
        daemon = Daemon(
            host_id=host.id,
            service_id=svc.id,
            daemon_type=d.daemon_type,
            systemctl_name=d.systemctl_name,
            log_path=d.log_path,
        )
        db.add(daemon)
    db.commit()
    db.refresh(host)
    return _host_to_dict(host, full=True)


@router.put("/api/hosts/{host_id}")
def update_host(host_id: int, body: HostUpdate, db: Session = Depends(get_db)):
    host = db.query(Host).filter_by(id=host_id).first()
    if not host:
        raise HTTPException(status_code=404, detail="Host bulunamadı")
    if body.hostname is not None:
        host.hostname = body.hostname
    if body.ip is not None:
        host.ip = body.ip
    if body.jump_via is not None:
        host.jump_via = body.jump_via if body.jump_via else None
    if body.service_name is not None:
        svc = db.query(Service).filter_by(name=body.service_name).first()
        if not svc:
            raise HTTPException(status_code=404, detail=f"Servis bulunamadı: {body.service_name}")
        host.service_id = svc.id
    if body.daemons is not None:
        # Mevcut daemon'ları sil ve yenilerini ekle
        db.query(Daemon).filter_by(host_id=host.id).delete()
        for d in body.daemons:
            daemon = Daemon(
                host_id=host.id,
                service_id=host.service_id,
                daemon_type=d.daemon_type,
                systemctl_name=d.systemctl_name,
                log_path=d.log_path,
            )
            db.add(daemon)
    db.commit()
    db.refresh(host)
    return _host_to_dict(host, full=True)


@router.delete("/api/hosts/{host_id}")
def delete_host(host_id: int, db: Session = Depends(get_db)):
    host = db.query(Host).filter_by(id=host_id).first()
    if not host:
        raise HTTPException(status_code=404, detail="Host bulunamadı")
    db.delete(host)
    db.commit()
    return {"status": "deleted", "id": host_id}


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
