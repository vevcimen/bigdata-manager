"""
FastAPI dependency injection yardımcıları.
"""
from sqlalchemy.orm import Session
from ..db.models import Base


# Uygulama başladığında main.py tarafından atanır
_engine  = None
_Session = None
_cfg     = None


def set_engine(engine):
    global _engine, _Session
    from sqlalchemy.orm import sessionmaker
    _engine  = engine
    _Session = sessionmaker(bind=engine)


def set_cfg(cfg):
    global _cfg
    _cfg = cfg


def get_db():
    if _Session is None:
        raise RuntimeError("DB engine henüz başlatılmadı")
    db = _Session()
    try:
        yield db
    finally:
        db.close()


def get_cfg():
    if _cfg is None:
        raise RuntimeError("Config henüz yüklenmedi")
    return _cfg
