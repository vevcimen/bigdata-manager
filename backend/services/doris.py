"""
Apache Doris servis katmanı.

Doris FE HTTP API üzerinden cluster durumu, tablet durumu ve BE istatistikleri alır.
Doris BE'ye erişim jump-SSH ile sağlanır (CM → FE → BE).
"""
import logging
from typing import Optional, Dict, Any

import httpx

log = logging.getLogger(__name__)

# Doris HTTP API timeout
_TIMEOUT = 10


def get_fe_status(fe_url: str, user: str = "root", password: str = "") -> Dict[str, Any]:
    """
    Doris FE HTTP API'den cluster durumunu döndürür.
    Endpoint: GET /api/health (Doris 1.x / 2.x)
    """
    try:
        resp = httpx.get(
            f"{fe_url}/api/health",
            auth=(user, password),
            timeout=_TIMEOUT,
            follow_redirects=True
        )
        if resp.status_code == 200:
            return {"status": "healthy", "data": resp.json()}
        return {"status": "error", "code": resp.status_code}
    except Exception as e:
        log.debug(f"Doris FE health hatası [{fe_url}]: {e}")
        return {"status": "unreachable", "error": str(e)}


def get_backends(fe_url: str, user: str = "root", password: str = "") -> list:
    """
    FE HTTP API'den BE listesi döndürür.
    Endpoint: GET /api/show_proc?path=/backends
    """
    try:
        resp = httpx.get(
            f"{fe_url}/api/show_proc",
            params={"path": "/backends"},
            auth=(user, password),
            timeout=_TIMEOUT,
            follow_redirects=True
        )
        if resp.status_code == 200:
            data = resp.json()
            return data.get("rows", [])
        return []
    except Exception as e:
        log.debug(f"Doris backends hatası [{fe_url}]: {e}")
        return []


def get_frontends(fe_url: str, user: str = "root", password: str = "") -> list:
    """FE listesi (HA durumu)."""
    try:
        resp = httpx.get(
            f"{fe_url}/api/show_proc",
            params={"path": "/frontends"},
            auth=(user, password),
            timeout=_TIMEOUT,
            follow_redirects=True
        )
        if resp.status_code == 200:
            data = resp.json()
            return data.get("rows", [])
        return []
    except Exception as e:
        log.debug(f"Doris frontends hatası [{fe_url}]: {e}")
        return []


def get_cluster_summary(topology_svc: Dict[str, Any]) -> Dict[str, Any]:
    """
    topology servis tanımından Doris özet durumu toplar.
    topology_svc: topology.yaml'daki tek bir servis dict'i
    """
    extra       = topology_svc.get("extra", {})
    fe_url      = extra.get("fe_http_url", "")
    fe_user     = extra.get("fe_user", "root")
    fe_password = extra.get("fe_password", "")

    if not fe_url:
        return {"error": "fe_http_url topology'de tanımlanmamış"}

    health    = get_fe_status(fe_url, fe_user, fe_password)
    backends  = get_backends(fe_url, fe_user, fe_password)
    frontends = get_frontends(fe_url, fe_user, fe_password)

    return {
        "fe_url":     fe_url,
        "health":     health,
        "backends":   backends,
        "frontends":  frontends,
        "be_count":   len(backends),
        "fe_count":   len(frontends),
    }
