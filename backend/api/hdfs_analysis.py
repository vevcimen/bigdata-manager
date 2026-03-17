"""
HDFS Tablo Analizi API'si.

GET  /api/services/{name}/hdfs/schemas                       → Warehouse şema listesi (SSH)
GET  /api/services/{name}/hdfs/schemas/{schema}/tables       → Şema altındaki tablo listesi
POST /api/services/{name}/hdfs/analyze                       → Seçili tabloların boyut + dosya sayısı
"""
import asyncio
import logging
from typing import List

from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel
from sqlalchemy.orm import Session

from ..db.models import Host, Service
from ..core import ssh as ssh_pool
from .deps import get_db, get_cfg

log = logging.getLogger(__name__)
router = APIRouter(tags=["hdfs-analysis"])

HDFS_CMD_TIMEOUT = 120  # hdfs dfs komutları yavaş olabilir


def _ssh_params(cfg):
    return {
        "user":     cfg.get("ssh", "user"),
        "key_path": cfg.get("ssh", "key_path"),
        "timeout":  int(cfg.get("ssh", "timeout") if cfg.has_option("ssh", "timeout") else 30),
    }


def _get_svc_and_host(service_name: str, db: Session):
    svc = db.query(Service).filter_by(name=service_name).first()
    if not svc:
        raise HTTPException(404, "Servis bulunamadı")
    host = db.query(Host).filter_by(service_id=svc.id).first()
    if not host:
        raise HTTPException(400, "Servis için kayıtlı host yok")
    return svc, host


def _run(host: Host, sp: dict, cmd: str):
    jump = host.jump_via or None
    return ssh_pool.run_command(
        host.ip or host.hostname,
        sp["user"], sp["key_path"], cmd,
        timeout=HDFS_CMD_TIMEOUT,
        jump_host=jump,
        jump_user=sp["user"] if jump else None,
    )


# ─── Şema listesi ─────────────────────────────────────────────────────────────

@router.get("/api/services/{service_name}/hdfs/schemas")
async def list_schemas(service_name: str, db: Session = Depends(get_db), cfg=Depends(get_cfg)):
    svc, host = _get_svc_and_host(service_name, db)
    extra     = svc.extra or {}
    warehouse = extra.get("warehouse_path", "").rstrip("/")
    if not warehouse:
        raise HTTPException(400, "Servis extra alanına 'warehouse_path' ekleyin (örn: hdfs://GOLDEN/user/hive/warehouse)")

    sp  = _ssh_params(cfg)
    cmd = f"hdfs dfs -ls '{warehouse}' 2>/dev/null | tail -n +2 | awk '{{print $NF}}'"
    _, out, err = await asyncio.to_thread(_run, host, sp, cmd)

    schemas = []
    for line in out.splitlines():
        line = line.strip()
        if not line:
            continue
        name = line.rstrip("/").split("/")[-1]
        if name:
            schemas.append({"name": name, "path": line})

    if not schemas and err:
        log.warning(f"HDFS schema list hatası [{service_name}]: {err}")

    return {"warehouse": warehouse, "schemas": schemas}


# ─── Tablo listesi ────────────────────────────────────────────────────────────

@router.get("/api/services/{service_name}/hdfs/schemas/{schema_name}/tables")
async def list_tables(
    service_name: str, schema_name: str,
    db: Session = Depends(get_db), cfg=Depends(get_cfg)
):
    svc, host = _get_svc_and_host(service_name, db)
    extra     = svc.extra or {}
    warehouse = extra.get("warehouse_path", "").rstrip("/")
    if not warehouse:
        raise HTTPException(400, "warehouse_path tanımlı değil")

    schema_path = f"{warehouse}/{schema_name}"
    sp  = _ssh_params(cfg)
    cmd = f"hdfs dfs -ls '{schema_path}' 2>/dev/null | tail -n +2 | awk '{{print $NF}}'"
    _, out, err = await asyncio.to_thread(_run, host, sp, cmd)

    tables = []
    for line in out.splitlines():
        line = line.strip()
        if not line:
            continue
        name = line.rstrip("/").split("/")[-1]
        if name:
            tables.append({"name": name, "path": line})

    return {"schema": schema_name, "tables": tables}


# ─── Boyut + dosya sayısı analizi ────────────────────────────────────────────

class AnalyzeRequest(BaseModel):
    schema_name: str
    tables: List[str]


@router.post("/api/services/{service_name}/hdfs/analyze")
async def analyze_tables(
    service_name: str, body: AnalyzeRequest,
    db: Session = Depends(get_db), cfg=Depends(get_cfg)
):
    if not body.tables:
        raise HTTPException(400, "En az bir tablo seçin")

    svc, host = _get_svc_and_host(service_name, db)
    extra     = svc.extra or {}
    warehouse = extra.get("warehouse_path", "").rstrip("/")
    if not warehouse:
        raise HTTPException(400, "warehouse_path tanımlı değil")

    sp = _ssh_params(cfg)

    # Tek SSH çağrısında tüm tabloları sırayla say:
    # hdfs dfs -count çıktısı: DIR_COUNT  FILE_COUNT  CONTENT_SIZE  PATH
    paths = [f"{warehouse}/{body.schema_name}/{t}" for t in body.tables]
    path_list = " ".join(f"'{p}'" for p in paths)
    cmd = (
        f"for p in {path_list}; do "
        f"  res=$(hdfs dfs -count \"$p\" 2>/dev/null); "
        f"  if [ -n \"$res\" ]; then echo \"$res\"; else echo \"0 0 0 $p\"; fi; "
        f"done"
    )
    _, out, _ = await asyncio.to_thread(_run, host, sp, cmd)

    results    = []
    total_bytes = 0
    for line in out.splitlines():
        parts = line.split()
        if len(parts) < 4:
            continue
        try:
            dir_count  = int(parts[0])
            file_count = int(parts[1])
            size_bytes = int(parts[2])
            path       = parts[3]
            table_name = path.rstrip("/").split("/")[-1]
            total_bytes += size_bytes
            results.append({
                "table":     table_name,
                "path":      path,
                "sizeBytes": size_bytes,
                "fileCount": file_count,
                "dirCount":  dir_count,
            })
        except (ValueError, IndexError):
            continue

    results.sort(key=lambda x: x["sizeBytes"], reverse=True)
    return {
        "schema":     body.schema_name,
        "totalBytes": total_bytes,
        "tables":     results,
    }
