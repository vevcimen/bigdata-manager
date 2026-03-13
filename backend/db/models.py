from datetime import datetime
from sqlalchemy import (
    Column, Integer, String, Text, DateTime, Boolean,
    Float, ForeignKey, Enum, JSON
)
from sqlalchemy.orm import declarative_base, relationship

Base = declarative_base()


class Service(Base):
    """Topology'den yüklenen servis instance'ları (örn: HDFS-İÇ, Spark-DIŞ)"""
    __tablename__ = "services"

    id          = Column(Integer, primary_key=True, autoincrement=True)
    name        = Column(String(128), nullable=False, unique=True)
    type        = Column(String(64), nullable=False)   # hdfs, spark, kafka ...
    description = Column(String(255))
    extra       = Column(JSON)                          # topology extra blok
    created_at  = Column(DateTime, default=datetime.utcnow)
    updated_at  = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)

    hosts       = relationship("Host", back_populates="service", cascade="all, delete-orphan")

    def __repr__(self):
        return f"<Service {self.name} ({self.type})>"


class Host(Base):
    """Topology'deki her bir host girişi"""
    __tablename__ = "hosts"

    id          = Column(Integer, primary_key=True, autoincrement=True)
    service_id  = Column(Integer, ForeignKey("services.id"), nullable=False)
    hostname    = Column(String(255), nullable=False)
    ip          = Column(String(64))
    jump_via    = Column(String(255))   # Doris BE için: FE hostname
    created_at  = Column(DateTime, default=datetime.utcnow)

    service     = relationship("Service", back_populates="hosts")
    daemons     = relationship("Daemon", back_populates="host", cascade="all, delete-orphan")

    def __repr__(self):
        return f"<Host {self.hostname} ({self.ip})>"


class Daemon(Base):
    """Her host üzerindeki daemon rol tanımı"""
    __tablename__ = "daemons"

    id              = Column(Integer, primary_key=True, autoincrement=True)
    host_id         = Column(Integer, ForeignKey("hosts.id"), nullable=False)
    service_id      = Column(Integer, ForeignKey("services.id"), nullable=False)
    daemon_type     = Column(String(64), nullable=False)    # namenode, datanode, worker ...
    systemctl_name  = Column(String(128), nullable=False)
    log_path        = Column(String(512))
    last_status     = Column(
        Enum("active", "inactive", "failed", "activating", "deactivating", "unknown"),
        default="unknown"
    )
    last_checked    = Column(DateTime)
    created_at      = Column(DateTime, default=datetime.utcnow)

    host    = relationship("Host", back_populates="daemons")
    events  = relationship("DaemonEvent", back_populates="daemon", cascade="all, delete-orphan")

    def __repr__(self):
        return f"<Daemon {self.daemon_type} on {self.host.hostname if self.host else '?'}>"


class DaemonEvent(Base):
    """Daemon start/stop/restart işlem geçmişi (audit log)"""
    __tablename__ = "daemon_events"

    id          = Column(Integer, primary_key=True, autoincrement=True)
    daemon_id   = Column(Integer, ForeignKey("daemons.id"), nullable=False)
    action      = Column(Enum("start", "stop", "restart", "status"), nullable=False)
    triggered_by = Column(String(64), default="ui")  # ui / scheduler / api
    status      = Column(Enum("success", "failed", "timeout"), nullable=False)
    output      = Column(Text)
    created_at  = Column(DateTime, default=datetime.utcnow)

    daemon      = relationship("Daemon", back_populates="events")


class MetricSnapshot(Base):
    """node_exporter ve Prometheus'tan toplanan metrik anlık görüntüler"""
    __tablename__ = "metric_snapshots"

    id          = Column(Integer, primary_key=True, autoincrement=True)
    host        = Column(String(255), nullable=False)
    service_name = Column(String(128))
    metric_name = Column(String(128), nullable=False)
    value       = Column(Float)
    labels      = Column(JSON)   # Prometheus label dict
    ts          = Column(DateTime, default=datetime.utcnow, index=True)


class User(Base):
    """Kullanıcı tablosu (login/auth)"""
    __tablename__ = "users"

    id          = Column(Integer, primary_key=True, autoincrement=True)
    username    = Column(String(64), nullable=False, unique=True)
    password_hash = Column(String(255), nullable=False)
    role        = Column(String(32), default="admin")  # admin / viewer
    created_at  = Column(DateTime, default=datetime.utcnow)
    updated_at  = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)


class Notification(Base):
    """Bildirim merkezi"""
    __tablename__ = "notifications"

    id          = Column(Integer, primary_key=True, autoincrement=True)
    title       = Column(String(255), nullable=False)
    message     = Column(Text)
    level       = Column(Enum("info", "warning", "critical"), default="info")
    source      = Column(String(128))   # hangi servis/host'tan geldi
    is_read     = Column(Boolean, default=False)
    created_at  = Column(DateTime, default=datetime.utcnow)
