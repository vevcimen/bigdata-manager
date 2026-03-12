"""
BigData Manager — FastAPI uygulaması giriş noktası.

Başlatma:
    uvicorn backend.main:app --host 0.0.0.0 --port 8000 --reload

Ortam değişkeni:
    CM_CONFIG   → cluster_manager.cfg dosyasının yolu (varsayılan: ./cluster_manager.cfg)
"""
import configparser
import logging
import os

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles

from .core.topology import load_topology
from .db.init_db    import get_engine, init_database
from .api           import deps
from .api.daemons       import router as daemons_router
from .api.metrics       import router as metrics_router
from .api.hosts         import router as hosts_router
from .api.notifications import router as notif_router
from .api.diag          import router as diag_router


# ─── Logging ──────────────────────────────────────────────────────────────────

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s  %(levelname)-8s  %(name)s  %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S"
)
log = logging.getLogger(__name__)


# ─── Config yükleme ───────────────────────────────────────────────────────────

def _load_cfg() -> configparser.ConfigParser:
    cfg_path = os.environ.get("CM_CONFIG", "cluster_manager.cfg")
    cfg = configparser.ConfigParser()
    read = cfg.read(cfg_path)
    if not read:
        raise FileNotFoundError(f"Config dosyası okunamadı: {cfg_path}")
    log.info(f"Config yüklendi: {cfg_path}")
    return cfg


# ─── FastAPI app ──────────────────────────────────────────────────────────────

app = FastAPI(
    title="Kasırga API",
    description="Büyük veri ortamı servis ve daemon yönetim API'si",
    version="v2026-03-12",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

# API router'ları
app.include_router(daemons_router)
app.include_router(metrics_router)
app.include_router(hosts_router)
app.include_router(notif_router)
app.include_router(diag_router)

# React frontend dist klasörü (npm run build sonrası oluşur)
# Yoksa standalone.html'i kök dizinden sun (fallback)
_root_dir  = os.path.join(os.path.dirname(__file__), "..")
_dist_dir  = os.path.join(_root_dir, "frontend-dist")
_serve_dir = _dist_dir if os.path.isdir(_dist_dir) else _root_dir
app.mount("/", StaticFiles(directory=_serve_dir, html=True), name="static")


# ─── Startup / Shutdown ───────────────────────────────────────────────────────

@app.on_event("startup")
async def on_startup():
    log.info("Uygulama başlatılıyor…")

    # Config
    cfg = _load_cfg()

    # Log seviyesi ayarla
    if cfg.has_option("general", "log_level"):
        logging.getLogger().setLevel(cfg.get("general", "log_level").upper())

    # Topology
    topo_file = cfg.get("general", "topology_file") if cfg.has_option("general", "topology_file") else "topology.yaml"
    topology  = load_topology(topo_file)

    # DB
    engine = get_engine(cfg)
    init_database(engine, topology)

    # Dependency injection
    deps.set_engine(engine)
    deps.set_cfg(cfg)

    # Zamanlayıcı
    from .core.scheduler import init_scheduler
    init_scheduler(engine, cfg, topology)

    log.info("Uygulama hazır.")


@app.on_event("shutdown")
async def on_shutdown():
    from .core.scheduler import shutdown_scheduler
    shutdown_scheduler()
    log.info("Uygulama durduruldu.")


# ─── Sağlık kontrolü ─────────────────────────────────────────────────────────

@app.get("/api/health", tags=["system"])
def health():
    return {"status": "ok"}


@app.get("/api/version", tags=["system"])
def version():
    return {"version": "v2026-03-12", "name": "Kasırga"}
