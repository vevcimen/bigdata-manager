"""
Diagnostics / Sorun giderme API'si

GET /api/diag          → Tam sistem durumu (DB, SSH, config, frontend, env)
GET /api/diag/logs     → Son uygulama log satırları (in-memory buffer)
"""
import logging
import os
import platform
import socket
import sys
import traceback
from datetime import datetime

from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from .deps import get_db, get_cfg

router = APIRouter(prefix="/api/diag", tags=["diagnostics"])
log    = logging.getLogger(__name__)

# ─── In-memory log buffer (son 500 satır) ────────────────────────────────────

class _LogBuffer(logging.Handler):
    def __init__(self, capacity: int = 500):
        super().__init__()
        self._buf: list[dict] = []
        self._cap = capacity

    def emit(self, record: logging.LogRecord):
        entry = {
            "ts":      datetime.utcnow().isoformat(),
            "level":   record.levelname,
            "logger":  record.name,
            "message": self.format(record),
        }
        self._buf.append(entry)
        if len(self._buf) > self._cap:
            self._buf = self._buf[-self._cap:]

    def get(self, last: int = 200) -> list[dict]:
        return list(self._buf[-last:])


_buffer = _LogBuffer()
_buffer.setFormatter(logging.Formatter("%(asctime)s  %(levelname)-8s  %(name)s  %(message)s"))
logging.getLogger().addHandler(_buffer)


# ─── GET /api/diag ────────────────────────────────────────────────────────────

@router.get("")
def full_diagnostics(
    db:  Session = Depends(get_db),
    cfg = Depends(get_cfg),
):
    result = {
        "timestamp": datetime.utcnow().isoformat(),
        "system":    _sys_info(),
        "config":    _cfg_check(cfg),
        "database":  _db_check(db),
        "filesystem": _fs_check(),
        "network":   _net_check(cfg),
        "errors":    [],
    }

    # Hata özetini topla
    for section, data in result.items():
        if isinstance(data, dict) and data.get("status") == "error":
            result["errors"].append(f"{section}: {data.get('detail','?')}")

    result["healthy"] = len(result["errors"]) == 0
    return result


# ─── GET /api/diag/logs ───────────────────────────────────────────────────────

@router.get("/logs")
def get_logs(last: int = 200):
    """Son N uygulama log satırını döndürür."""
    return {
        "count": min(last, 500),
        "logs": _buffer.get(last),
    }


# ─── Yardımcılar ─────────────────────────────────────────────────────────────

def _sys_info() -> dict:
    return {
        "status":       "ok",
        "python":       sys.version,
        "platform":     platform.platform(),
        "hostname":     socket.gethostname(),
        "pid":          os.getpid(),
        "cwd":          os.getcwd(),
        "uptime_start": _start_time.isoformat(),
    }


def _cfg_check(cfg) -> dict:
    try:
        required = [
            ("mysql",      "host"),
            ("mysql",      "user"),
            ("mysql",      "database"),
            ("prometheus", "url"),
            ("ssh",        "user"),
            ("ssh",        "key_path"),
        ]
        missing = [
            f"{s}.{k}" for s, k in required
            if not cfg.has_option(s, k)
        ]
        ssh_key = cfg.get("ssh", "key_path") if cfg.has_option("ssh", "key_path") else None
        ssh_key_exists = os.path.isfile(os.path.expanduser(ssh_key)) if ssh_key else False

        warnings = []
        if not ssh_key_exists and ssh_key:
            warnings.append(f"SSH key bulunamadı: {ssh_key}")

        return {
            "status":          "error" if missing else ("warning" if warnings else "ok"),
            "missing_keys":    missing,
            "warnings":        warnings,
            "topology_file":   cfg.get("general", "topology_file", fallback="topology.yaml"),
            "prometheus_url":  cfg.get("prometheus", "url", fallback="?"),
            "ssh_user":        cfg.get("ssh", "user", fallback="?"),
            "ssh_key_path":    ssh_key,
            "ssh_key_exists":  ssh_key_exists,
        }
    except Exception as e:
        return {"status": "error", "detail": str(e)}


def _db_check(db: Session) -> dict:
    try:
        from sqlalchemy import text
        result = db.execute(text("SELECT 1 AS ping")).fetchone()
        # Tablo sayısı
        from ..db.models import Service, Host, Daemon
        svc_count    = db.query(Service).count()
        host_count   = db.query(Host).count()
        daemon_count = db.query(Daemon).count()
        return {
            "status":       "ok",
            "ping":         result[0] == 1,
            "services":     svc_count,
            "hosts":        host_count,
            "daemons":      daemon_count,
        }
    except Exception as e:
        return {
            "status": "error",
            "detail": str(e),
            "hint":   "MySQL çalışıyor mu? Kullanıcı/şifre doğru mu? (INSTALL.md §2)",
        }


def _fs_check() -> dict:
    root = os.getcwd()
    dist_dir = os.path.join(root, "frontend-dist")
    topo_path = None
    try:
        import configparser
        cfg_path = os.environ.get("CM_CONFIG", "cluster_manager.cfg")
        cfg = configparser.ConfigParser()
        cfg.read(cfg_path)
        topo_path = cfg.get("general", "topology_file", fallback="topology.yaml")
    except Exception:
        pass

    return {
        "status":              "ok",
        "cwd":                 root,
        "frontend_dist":       os.path.isdir(dist_dir),
        "frontend_dist_path":  dist_dir,
        "topology_exists":     os.path.isfile(topo_path) if topo_path else False,
        "topology_path":       topo_path,
        "config_env":          os.environ.get("CM_CONFIG", "(varsayılan: cluster_manager.cfg)"),
    }


def _net_check(cfg) -> dict:
    checks = {}

    # Prometheus
    try:
        prom_url = cfg.get("prometheus", "url", fallback=None)
        if prom_url:
            host = prom_url.split("//")[-1].split("/")[0].split(":")[0]
            port = 443 if prom_url.startswith("https") else 80
            if ":" in prom_url.split("//")[-1].split("/")[0]:
                port = int(prom_url.split("//")[-1].split("/")[0].split(":")[1])
            s = socket.create_connection((host, port), timeout=3)
            s.close()
            checks["prometheus"] = {"status": "ok", "url": prom_url}
    except Exception as e:
        checks["prometheus"] = {
            "status": "error",
            "url":    cfg.get("prometheus", "url", fallback="?"),
            "detail": str(e),
            "hint":   "Prometheus'a ağ erişimi yok. VPN/firewall kontrol edin.",
        }

    # MySQL port
    try:
        mysql_host = cfg.get("mysql", "host", fallback="127.0.0.1")
        mysql_port = int(cfg.get("mysql", "port", fallback="3306"))
        s = socket.create_connection((mysql_host, mysql_port), timeout=3)
        s.close()
        checks["mysql_port"] = {"status": "ok", "host": mysql_host, "port": mysql_port}
    except Exception as e:
        checks["mysql_port"] = {
            "status": "error",
            "detail": str(e),
            "hint":   "mysqld çalışıyor mu? 'systemctl status mysqld'",
        }

    overall = "ok" if all(v.get("status") == "ok" for v in checks.values()) else "warning"
    return {"status": overall, "checks": checks}


_start_time = datetime.utcnow()
