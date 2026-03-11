"""
Daemon yönetim API'si.

GET  /api/daemons                  → Tüm daemon'lar + durum
GET  /api/daemons/{id}             → Tek daemon
POST /api/daemons/{id}/{action}    → start | stop | restart | status
GET  /api/daemons/{id}/logs        → SSE log stream (tail -f)
"""
import logging
from datetime import datetime
from typing import Optional

from fastapi import APIRouter, Depends, HTTPException, Query
from fastapi.responses import StreamingResponse
from sqlalchemy.orm import Session

from ..db.models import Daemon, DaemonEvent, Host, Service
from ..core import ssh as ssh_pool
from .deps import get_db, get_cfg

log = logging.getLogger(__name__)
router = APIRouter(prefix="/api/daemons", tags=["daemons"])

VALID_ACTIONS = {"start", "stop", "restart", "status"}


# ─── GET /api/daemons ─────────────────────────────────────────────────────────

@router.get("")
def list_daemons(
    service_name: Optional[str] = Query(None),
    db: Session = Depends(get_db)
):
    q = db.query(Daemon).join(Host).join(Service)
    if service_name:
        q = q.filter(Service.name == service_name)
    daemons = q.all()
    return [_daemon_to_dict(d) for d in daemons]


# ─── GET /api/daemons/{id} ────────────────────────────────────────────────────

@router.get("/{daemon_id}")
def get_daemon(daemon_id: int, db: Session = Depends(get_db)):
    d = db.query(Daemon).filter_by(id=daemon_id).first()
    if not d:
        raise HTTPException(status_code=404, detail="Daemon bulunamadı")
    return _daemon_to_dict(d)


# ─── POST /api/daemons/{id}/{action} ─────────────────────────────────────────

@router.post("/{daemon_id}/{action}")
def daemon_action(
    daemon_id: int,
    action: str,
    db: Session = Depends(get_db),
    cfg=Depends(get_cfg)
):
    if action not in VALID_ACTIONS:
        raise HTTPException(status_code=400, detail=f"Geçersiz aksiyon. Geçerliler: {VALID_ACTIONS}")

    daemon = db.query(Daemon).filter_by(id=daemon_id).first()
    if not daemon:
        raise HTTPException(status_code=404, detail="Daemon bulunamadı")

    host      = daemon.host
    hostname  = host.hostname
    jump_host = host.jump_via
    ssh_user  = cfg.get("ssh", "user")
    ssh_key   = cfg.get("ssh", "key_path")
    ssh_to    = int(cfg.get("ssh", "timeout") if cfg.has_option("ssh", "timeout") else 30)

    if action == "status":
        cmd = f"systemctl is-active {daemon.systemctl_name}"
    else:
        cmd = f"sudo systemctl {action} {daemon.systemctl_name}"

    exit_code, out, err = ssh_pool.run_command(
        hostname, ssh_user, ssh_key, cmd,
        timeout=ssh_to,
        jump_host=jump_host,
        jump_user=ssh_user if jump_host else None
    )

    ev_status = "success" if exit_code == 0 else "failed"
    output    = (out + "\n" + err).strip()

    # Audit log kaydı
    event = DaemonEvent(
        daemon_id    = daemon_id,
        action       = action,
        triggered_by = "ui",
        status       = ev_status,
        output       = output[:4000]
    )
    db.add(event)

    # Durum güncelle
    if action != "status":
        status_after_action = {"start": "active", "stop": "inactive", "restart": "active"}
        daemon.last_status  = status_after_action.get(action, "unknown") if ev_status == "success" else "failed"
        daemon.last_checked = datetime.utcnow()
    else:
        daemon.last_status  = out.strip().lower() or "unknown"
        daemon.last_checked = datetime.utcnow()

    db.commit()

    return {
        "daemon_id":  daemon_id,
        "action":     action,
        "exit_code":  exit_code,
        "output":     output,
        "status":     ev_status,
        "last_status": daemon.last_status
    }


# ─── GET /api/daemons/{id}/logs  (SSE) ───────────────────────────────────────

@router.get("/{daemon_id}/logs")
def stream_daemon_logs(
    daemon_id: int,
    lines: int = Query(100, ge=1, le=1000),
    db: Session = Depends(get_db),
    cfg=Depends(get_cfg)
):
    daemon = db.query(Daemon).filter_by(id=daemon_id).first()
    if not daemon:
        raise HTTPException(status_code=404, detail="Daemon bulunamadı")

    if not daemon.log_path:
        raise HTTPException(status_code=400, detail="Bu daemon için log_path tanımlanmamış")

    host      = daemon.host
    hostname  = host.hostname
    jump_host = host.jump_via
    ssh_user  = cfg.get("ssh", "user")
    ssh_key   = cfg.get("ssh", "key_path")
    ssh_to    = int(cfg.get("ssh", "timeout") if cfg.has_option("ssh", "timeout") else 30)

    def event_gen():
        try:
            for line in ssh_pool.stream_log(
                hostname, ssh_user, ssh_key,
                daemon.log_path, lines=lines, timeout=ssh_to,
                jump_host=jump_host,
                jump_user=ssh_user if jump_host else None
            ):
                yield f"data: {line}\n\n"
        except GeneratorExit:
            pass

    return StreamingResponse(
        event_gen(),
        media_type="text/event-stream",
        headers={
            "Cache-Control": "no-cache",
            "X-Accel-Buffering": "no"
        }
    )


# ─── GET /api/daemons/{id}/events ────────────────────────────────────────────

@router.get("/{daemon_id}/events")
def daemon_events(
    daemon_id: int,
    limit: int = Query(50, ge=1, le=200),
    db: Session = Depends(get_db)
):
    events = (
        db.query(DaemonEvent)
        .filter_by(daemon_id=daemon_id)
        .order_by(DaemonEvent.created_at.desc())
        .limit(limit)
        .all()
    )
    return [
        {
            "id":           e.id,
            "action":       e.action,
            "triggered_by": e.triggered_by,
            "status":       e.status,
            "output":       e.output,
            "created_at":   e.created_at.isoformat() if e.created_at else None
        }
        for e in events
    ]


# ─── Yardımcı ─────────────────────────────────────────────────────────────────

def _daemon_to_dict(d: Daemon) -> dict:
    return {
        "id":             d.id,
        "daemon_type":    d.daemon_type,
        "systemctl_name": d.systemctl_name,
        "log_path":       d.log_path,
        "last_status":    d.last_status,
        "last_checked":   d.last_checked.isoformat() if d.last_checked else None,
        "host": {
            "id":       d.host.id,
            "hostname": d.host.hostname,
            "ip":       d.host.ip,
            "jump_via": d.host.jump_via,
        } if d.host else None,
        "service": {
            "id":   d.service_id,
            "name": d.host.service.name if d.host and d.host.service else None,
        }
    }
