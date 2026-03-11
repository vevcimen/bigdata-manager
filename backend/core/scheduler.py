"""
APScheduler tabanlı arka plan görev zamanlayıcı.

Görevler:
  - collect_node_metrics   : node_exporter (port 9100) HTTP scrape — her host
  - collect_daemon_statuses: systemctl is-active SSH sorgusu — tüm daemon'lar
  - collect_pure_storage   : mcli ile Pure Storage bucket metrik toplama
  - collect_prometheus     : Prometheus HTTP API'den ek metrikler
"""
import logging
import subprocess
import threading
from datetime import datetime
from typing import Optional

import httpx
from apscheduler.schedulers.background import BackgroundScheduler

from ..db.models import Daemon, Host, MetricSnapshot, Notification
from ..core import ssh as ssh_pool

log = logging.getLogger(__name__)

_scheduler: Optional[BackgroundScheduler] = None
_engine     = None
_cfg        = None
_topology   = None
_Session    = None   # sessionmaker instance

# APScheduler job ID sabitleri
JOB_NODE    = "collect_node_metrics"
JOB_DAEMON  = "collect_daemon_statuses"
JOB_PURE    = "collect_pure_storage"
JOB_PROM    = "collect_prometheus"


# ─── Init ─────────────────────────────────────────────────────────────────────

def init_scheduler(engine, cfg, topology):
    """Zamanlayıcıyı başlatır, tüm periyodik görevleri ekler."""
    global _scheduler, _engine, _cfg, _topology, _Session

    from sqlalchemy.orm import sessionmaker
    _engine   = engine
    _cfg      = cfg
    _topology = topology
    _Session  = sessionmaker(bind=engine)

    interval = int(cfg.get("metrics", "interval_seconds") if cfg.has_option("metrics", "interval_seconds") else 60)

    _scheduler = BackgroundScheduler(timezone="UTC")
    _scheduler.add_job(collect_node_metrics,    "interval", seconds=interval, id=JOB_NODE,   replace_existing=True)
    _scheduler.add_job(collect_daemon_statuses, "interval", seconds=interval, id=JOB_DAEMON, replace_existing=True)
    _scheduler.add_job(collect_pure_storage,    "interval", seconds=interval, id=JOB_PURE,   replace_existing=True)
    _scheduler.add_job(collect_prometheus,      "interval", seconds=interval, id=JOB_PROM,   replace_existing=True)
    _scheduler.start()
    log.info(f"Zamanlayıcı başlatıldı — interval: {interval}s")


def shutdown_scheduler():
    global _scheduler
    if _scheduler and _scheduler.running:
        _scheduler.shutdown(wait=False)
        log.info("Zamanlayıcı durduruldu.")


def trigger_all():
    """Manuel refresh için tüm görevleri hemen tetikler."""
    if not _scheduler:
        return
    for jid in [JOB_NODE, JOB_DAEMON, JOB_PURE, JOB_PROM]:
        try:
            _scheduler.get_job(jid).modify(next_run_time=datetime.utcnow())
        except Exception as e:
            log.warning(f"Görev tetiklenemedi [{jid}]: {e}")


# ─── node_exporter metrik toplama ─────────────────────────────────────────────

def collect_node_metrics():
    """
    Her host için node_exporter :9100/metrics endpoint'ini scrape eder.
    CPU, bellek, disk metriklerini MetricSnapshot olarak kaydeder.
    """
    if not _topology or not _Session:
        return
    session = _Session()
    try:
        for svc in _topology.get("services", []):
            for host_def in svc.get("hosts", []):
                ip       = host_def.get("ip") or host_def["hostname"]
                hostname = host_def["hostname"]
                url      = f"http://{ip}:9100/metrics"
                try:
                    resp = httpx.get(url, timeout=5)
                    if resp.status_code != 200:
                        continue
                    _parse_node_exporter(session, hostname, svc["name"], resp.text)
                except Exception as e:
                    log.debug(f"node_exporter erişim hatası [{hostname}]: {e}")
        session.commit()
    except Exception as e:
        session.rollback()
        log.error(f"collect_node_metrics hatası: {e}")
    finally:
        session.close()


def _parse_node_exporter(session, hostname: str, service_name: str, text: str):
    """
    Prometheus metin formatından kritik metrikleri ayıklar ve kaydeder.
    Ayrıştırılan metrikler: cpu_usage_percent, mem_used_percent, disk_used_percent
    """
    metrics_to_grab = {
        "node_cpu_seconds_total":            None,
        "node_memory_MemAvailable_bytes":    None,
        "node_memory_MemTotal_bytes":        None,
        "node_filesystem_avail_bytes":       None,
        "node_filesystem_size_bytes":        None,
    }
    raw = {}
    for line in text.splitlines():
        if line.startswith("#"):
            continue
        for key in metrics_to_grab:
            if line.startswith(key):
                parts = line.rsplit(" ", 1)
                if len(parts) == 2:
                    try:
                        raw.setdefault(key, []).append(float(parts[1]))
                    except ValueError:
                        pass

    now = datetime.utcnow()
    snapshots = []

    # Bellek kullanımı
    mem_total = sum(raw.get("node_memory_MemTotal_bytes", [0]))
    mem_avail = sum(raw.get("node_memory_MemAvailable_bytes", [0]))
    if mem_total > 0:
        mem_pct = 100.0 * (1 - mem_avail / mem_total)
        snapshots.append(MetricSnapshot(
            host=hostname, service_name=service_name,
            metric_name="mem_used_percent", value=round(mem_pct, 2), ts=now
        ))

    # Disk kullanımı (ilk filesystem)
    disk_avail_list = raw.get("node_filesystem_avail_bytes", [])
    disk_size_list  = raw.get("node_filesystem_size_bytes", [])
    if disk_avail_list and disk_size_list and disk_size_list[0] > 0:
        disk_pct = 100.0 * (1 - disk_avail_list[0] / disk_size_list[0])
        snapshots.append(MetricSnapshot(
            host=hostname, service_name=service_name,
            metric_name="disk_used_percent", value=round(disk_pct, 2), ts=now
        ))

    for snap in snapshots:
        session.add(snap)


# ─── Daemon durum kontrolü ────────────────────────────────────────────────────

def collect_daemon_statuses():
    """
    Tüm daemon'lar için SSH ile `systemctl is-active <unit>` komutunu çalıştırır,
    sonucu DB'ye kaydeder.
    """
    if not _Session or not _cfg:
        return

    ssh_user    = _cfg.get("ssh", "user")
    ssh_key     = _cfg.get("ssh", "key_path")
    ssh_timeout = int(_cfg.get("ssh", "timeout") if _cfg.has_option("ssh", "timeout") else 30)

    session = _Session()
    try:
        daemons = session.query(Daemon).all()
        for daemon in daemons:
            host_obj  = daemon.host
            hostname  = host_obj.hostname
            jump_host = host_obj.jump_via  # Doris BE için

            try:
                cmd = f"systemctl is-active {daemon.systemctl_name}"
                exit_code, out, _ = ssh_pool.run_command(
                    hostname, ssh_user, ssh_key, cmd,
                    timeout=ssh_timeout,
                    jump_host=jump_host,
                    jump_user=ssh_user if jump_host else None
                )
                status_map = {
                    "active": "active", "inactive": "inactive",
                    "failed": "failed", "activating": "activating",
                    "deactivating": "deactivating",
                }
                out_clean = out.strip().lower()
                daemon.last_status  = status_map.get(out_clean, "unknown")
                daemon.last_checked = datetime.utcnow()

                # Kritik durum bildirimi
                if daemon.last_status == "failed":
                    _create_notification(
                        session,
                        title=f"Daemon FAILED: {daemon.daemon_type}",
                        message=f"{daemon.daemon_type} on {hostname} is in FAILED state.",
                        level="critical",
                        source=hostname
                    )
            except Exception as e:
                log.debug(f"Daemon durum hatası [{daemon.daemon_type}@{hostname}]: {e}")
                daemon.last_status  = "unknown"
                daemon.last_checked = datetime.utcnow()

        session.commit()
    except Exception as e:
        session.rollback()
        log.error(f"collect_daemon_statuses hatası: {e}")
    finally:
        session.close()


# ─── Pure Storage (mcli) ──────────────────────────────────────────────────────

def collect_pure_storage():
    """
    mcli du komutunu çalıştırarak Pure Storage bucket metriklerini toplar.
    """
    if not _cfg or not _Session:
        return

    alias = _cfg.get("mcli", "alias") if _cfg.has_option("mcli", "alias") else "pure"
    session = _Session()
    try:
        # Bucket listesi al
        result = subprocess.run(
            ["mcli", "ls", f"{alias}/", "--json"],
            capture_output=True, text=True, timeout=30
        )
        if result.returncode != 0:
            log.debug(f"mcli ls hatası: {result.stderr}")
            return

        import json
        now = datetime.utcnow()
        for line in result.stdout.strip().splitlines():
            try:
                obj = json.loads(line)
                bucket_name = obj.get("key", "").rstrip("/")
                if not bucket_name:
                    continue

                # Bucket boyutu al
                du_result = subprocess.run(
                    ["mcli", "du", f"{alias}/{bucket_name}", "--json"],
                    capture_output=True, text=True, timeout=30
                )
                if du_result.returncode == 0:
                    for du_line in du_result.stdout.strip().splitlines():
                        try:
                            du_obj = json.loads(du_line)
                            size_bytes = du_obj.get("size", 0)
                            obj_count  = du_obj.get("objects", 0)
                            session.add(MetricSnapshot(
                                host="pure-storage", service_name="Pure-Storage",
                                metric_name="bucket_size_bytes",
                                value=float(size_bytes),
                                labels={"bucket": bucket_name},
                                ts=now
                            ))
                            session.add(MetricSnapshot(
                                host="pure-storage", service_name="Pure-Storage",
                                metric_name="bucket_object_count",
                                value=float(obj_count),
                                labels={"bucket": bucket_name},
                                ts=now
                            ))
                        except Exception:
                            pass
            except Exception:
                pass

        session.commit()
    except subprocess.TimeoutExpired:
        log.warning("mcli komutu zaman aşımına uğradı.")
    except FileNotFoundError:
        log.debug("mcli bulunamadı — Pure Storage metrikleri atlanıyor.")
    except Exception as e:
        session.rollback()
        log.error(f"collect_pure_storage hatası: {e}")
    finally:
        session.close()


# ─── Prometheus ───────────────────────────────────────────────────────────────

def collect_prometheus():
    """
    Prometheus HTTP API'den servis bazlı metrikler toplar.
    """
    if not _cfg or not _Session or not _topology:
        return

    prom_url     = _cfg.get("prometheus", "url")
    prom_timeout = int(_cfg.get("prometheus", "timeout") if _cfg.has_option("prometheus", "timeout") else 10)
    session      = _Session()

    try:
        # Her servis için prometheus_job varsa metrik çek
        for svc in _topology.get("services", []):
            prom_job = svc.get("extra", {}).get("prometheus_job")
            if not prom_job:
                continue
            _query_prometheus(session, prom_url, prom_job, svc["name"], prom_timeout)
        session.commit()
    except Exception as e:
        session.rollback()
        log.error(f"collect_prometheus hatası: {e}")
    finally:
        session.close()


def _query_prometheus(session, base_url: str, job: str, svc_name: str, timeout: int):
    """Bir Prometheus job için UP metriğini sorgular."""
    query = f'up{{job="{job}"}}'
    try:
        resp = httpx.get(
            f"{base_url}/api/v1/query",
            params={"query": query},
            timeout=timeout,
            verify=False   # self-signed cert toleransı
        )
        if resp.status_code != 200:
            return
        data = resp.json()
        now = datetime.utcnow()
        for result in data.get("data", {}).get("result", []):
            instance = result.get("metric", {}).get("instance", "unknown")
            value    = float(result["value"][1]) if "value" in result else 0
            session.add(MetricSnapshot(
                host=instance, service_name=svc_name,
                metric_name="up", value=value,
                labels=result.get("metric", {}),
                ts=now
            ))
    except Exception as e:
        log.debug(f"Prometheus sorgu hatası [{job}]: {e}")


# ─── Yardımcı ────────────────────────────────────────────────────────────────

def _create_notification(session, title: str, message: str, level: str, source: str):
    """DB'de bildirim oluşturur — aynı title+source için tekrar ekleme yapmaz."""
    from ..db.models import Notification
    exists = session.query(Notification).filter_by(
        title=title, source=source, is_read=False
    ).first()
    if not exists:
        session.add(Notification(
            title=title, message=message, level=level, source=source
        ))
