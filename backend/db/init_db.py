"""
DB başlatma modülü.
İlk çalıştırmada tabloları oluşturur, topology.yaml'ı okuyarak
services/hosts/daemons tablolarını doldurur.
"""
import logging
from sqlalchemy import create_engine, text
from sqlalchemy.orm import sessionmaker
from passlib.hash import bcrypt
from .models import Base, Service, Host, Daemon, User

log = logging.getLogger(__name__)


def get_engine(cfg):
    host     = cfg.get("mysql", "host")
    port     = cfg.get("mysql", "port")
    database = cfg.get("mysql", "database")
    user     = cfg.get("mysql", "user")
    password = cfg.get("mysql", "password")
    url = f"mysql+pymysql://{user}:{password}@{host}:{port}/{database}?charset=utf8mb4"
    return create_engine(url, pool_pre_ping=True, pool_recycle=3600, echo=False)


def init_database(engine, topology: dict):
    """
    Tabloları oluştur.
    Sadece DB'de hiç servis yoksa (ilk kurulum) topology.yaml'dan sync et.
    Normal restart'larda DB'deki configler korunur.
    """
    Base.metadata.create_all(engine)
    log.info("DB tabloları kontrol edildi / oluşturuldu.")

    Session = sessionmaker(bind=engine)
    session = Session()
    try:
        existing_count = session.query(Service).count()
        if existing_count == 0:
            log.info("DB boş — topology.yaml'dan ilk yükleme yapılıyor…")
            _sync_topology(session, topology)
            session.commit()
            log.info("Topology senkronizasyonu tamamlandı.")
        else:
            log.info(f"DB'de {existing_count} servis mevcut — topology.yaml atlanıyor, DB configleri kullanılacak.")

        # Default admin kullanıcı yoksa oluştur
        admin = session.query(User).filter_by(username="admin").first()
        if not admin:
            admin = User(
                username="admin",
                password_hash=bcrypt.hash("admin"),
                role="admin",
            )
            session.add(admin)
            session.commit()
            log.info("Default admin kullanıcı oluşturuldu (admin/admin).")
    except Exception as e:
        session.rollback()
        log.error(f"DB init hatası: {e}")
        raise
    finally:
        session.close()


def _sync_topology(session, topology: dict):
    """Topology değişimlerini DB'ye yansıt (ekleme/güncelleme, silme yok)."""
    for svc_def in topology.get("services", []):
        name  = svc_def["name"]
        stype = svc_def["type"]

        svc = session.query(Service).filter_by(name=name).first()
        if not svc:
            svc = Service(
                name=name,
                type=stype,
                description=svc_def.get("description", ""),
                extra=svc_def.get("extra", {})
            )
            session.add(svc)
            session.flush()
            log.info(f"Yeni servis eklendi: {name}")
        else:
            svc.description = svc_def.get("description", svc.description)
            svc.extra       = svc_def.get("extra", svc.extra)

        for host_def in svc_def.get("hosts", []):
            hostname = host_def["hostname"]
            host = session.query(Host).filter_by(
                service_id=svc.id, hostname=hostname
            ).first()

            if not host:
                host = Host(
                    service_id=svc.id,
                    hostname=hostname,
                    ip=host_def.get("ip"),
                    jump_via=host_def.get("jump_via")
                )
                session.add(host)
                session.flush()
                log.info(f"  Host eklendi: {hostname}")
            else:
                host.ip       = host_def.get("ip", host.ip)
                host.jump_via = host_def.get("jump_via", host.jump_via)

            for role_def in host_def.get("roles", []):
                dtype = role_def["type"]
                daemon = session.query(Daemon).filter_by(
                    host_id=host.id, daemon_type=dtype
                ).first()

                if not daemon:
                    daemon = Daemon(
                        host_id=host.id,
                        service_id=svc.id,
                        daemon_type=dtype,
                        systemctl_name=role_def["systemctl"],
                        log_path=role_def.get("log_path", "")
                    )
                    session.add(daemon)
                    log.info(f"    Daemon eklendi: {dtype} @ {hostname}")
                else:
                    daemon.systemctl_name = role_def["systemctl"]
                    daemon.log_path       = role_def.get("log_path", daemon.log_path)
