"""
topology.yaml dosyasını yükler ve yapılandırılmış dict olarak döndürür.
"""
import logging
import os
from typing import Dict, Any
import yaml

log = logging.getLogger(__name__)


def load_topology(path: str) -> Dict[str, Any]:
    """
    topology.yaml'ı okur, temel yapı doğrulaması yapar ve dict döndürür.
    """
    expanded = os.path.expanduser(path)
    if not os.path.isfile(expanded):
        raise FileNotFoundError(f"Topology dosyası bulunamadı: {expanded}")

    with open(expanded, "r", encoding="utf-8") as f:
        data = yaml.safe_load(f)

    if not data or "services" not in data:
        raise ValueError("topology.yaml geçersiz: 'services' anahtarı bulunamadı")

    for svc in data["services"]:
        if "name" not in svc or "type" not in svc:
            raise ValueError(f"Servis tanımında 'name' veya 'type' eksik: {svc}")

    log.info(f"Topology yüklendi: {len(data['services'])} servis, dosya: {expanded}")
    return data


def get_service(topology: Dict[str, Any], name: str) -> Dict[str, Any]:
    """Topology içinden isime göre servis döndürür."""
    for svc in topology.get("services", []):
        if svc["name"] == name:
            return svc
    return {}


def get_services_by_type(topology: Dict[str, Any], svc_type: str) -> list:
    """Belirli tipte tüm servisleri döndürür."""
    return [s for s in topology.get("services", []) if s["type"] == svc_type]


def list_all_daemons(topology: Dict[str, Any]) -> list:
    """
    Topology'deki tüm daemon tanımlarını düz liste olarak döndürür.
    Her eleman: {service_name, service_type, hostname, ip, jump_via, role_type,
                 systemctl, log_path}
    """
    result = []
    for svc in topology.get("services", []):
        for host in svc.get("hosts", []):
            for role in host.get("roles", []):
                result.append({
                    "service_name": svc["name"],
                    "service_type": svc["type"],
                    "hostname":     host["hostname"],
                    "ip":           host.get("ip"),
                    "jump_via":     host.get("jump_via"),
                    "role_type":    role["type"],
                    "systemctl":    role["systemctl"],
                    "log_path":     role.get("log_path", ""),
                })
    return result
