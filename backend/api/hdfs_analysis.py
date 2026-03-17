"""
HDFS Tablo Analizi API'si.

GET  /api/services/{name}/hdfs/schemas                       → Warehouse şema listesi (SSH)
GET  /api/services/{name}/hdfs/schemas/{schema}/tables       → Şema altındaki tablo listesi + HMS adları
POST /api/services/{name}/hdfs/analyze                       → Seçili tabloların boyut + dosya sayısı + partition analizi

HMS tablo adı / partition sayısı için servis extra alanları:
  hms_host, hms_port (3306), hms_db (metastore), hms_user, hms_password
"""
import asyncio
import logging
from typing import List, Optional

import pymysql
from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel
from sqlalchemy.orm import Session

from ..db.models import Host, Service
from ..core import ssh as ssh_pool
from .deps import get_db, get_cfg

log = logging.getLogger(__name__)
router = APIRouter(tags=["hdfs-analysis"])

HDFS_CMD_TIMEOUT = 120
SMALL_FILE_THRESHOLD = 128 * 1024 * 1024  # 128 MB


# ─── SSH yardımcıları ─────────────────────────────────────────────────────────

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


# ─── HMS yardımcıları ─────────────────────────────────────────────────────────

def _hms_conn(extra: dict):
    """HMS MySQL bağlantısı kurar. hms_host yoksa None döner."""
    if not extra.get("hms_host"):
        return None
    try:
        return pymysql.connect(
            host=extra.get("hms_host"),
            port=int(extra.get("hms_port", 3306)),
            db=extra.get("hms_db", "metastore"),
            user=extra.get("hms_user", ""),
            password=extra.get("hms_password", ""),
            connect_timeout=10,
            read_timeout=30,
            charset="utf8mb4",
        )
    except Exception as e:
        log.warning(f"HMS bağlantı hatası: {e}")
        return None


def _hms_path_to_name(extra: dict, schema_name: str) -> dict:
    """
    {hdfs_dir_name: hms_table_name} eşlemesi.
    HMS yoksa boş dict döner.
    """
    conn = _hms_conn(extra)
    if not conn:
        return {}
    try:
        with conn:
            with conn.cursor() as cur:
                cur.execute("""
                    SELECT s.LOCATION, t.TBL_NAME
                    FROM   TBLS t
                    JOIN   SDS  s ON t.SD_ID = s.SD_ID
                    JOIN   DBS  d ON t.DB_ID  = d.DB_ID
                    WHERE  d.NAME = %s
                """, (schema_name,))
                rows = cur.fetchall()
        mapping = {}
        for location, tbl_name in rows:
            if location:
                dir_name = location.rstrip("/").split("/")[-1]
                mapping[dir_name] = tbl_name
        return mapping
    except Exception as e:
        log.warning(f"HMS path→name sorgu hatası: {e}")
        return {}


def _hms_partition_counts(extra: dict, schema_name: str, table_names: List[str]) -> dict:
    """
    {hms_table_name: partition_count} döner.
    HMS yoksa veya partitioned değilse 0.
    """
    if not table_names:
        return {}
    conn = _hms_conn(extra)
    if not conn:
        return {}
    try:
        placeholders = ",".join(["%s"] * len(table_names))
        with conn:
            with conn.cursor() as cur:
                cur.execute(f"""
                    SELECT t.TBL_NAME, COUNT(p.PART_ID) AS part_count
                    FROM   TBLS t
                    JOIN   DBS  d ON t.DB_ID = d.DB_ID
                    LEFT JOIN PARTITIONS p ON t.TBL_ID = p.TBL_ID
                    WHERE  d.NAME = %s
                      AND  t.TBL_NAME IN ({placeholders})
                    GROUP  BY t.TBL_NAME
                """, [schema_name] + list(table_names))
                return {row[0]: row[1] for row in cur.fetchall()}
    except Exception as e:
        log.warning(f"HMS partition count sorgu hatası: {e}")
        return {}


# ─── HMS bağlantı testi ───────────────────────────────────────────────────────

@router.get("/api/services/{service_name}/hdfs/hms-test")
async def hms_test(service_name: str, db: Session = Depends(get_db)):
    svc = db.query(Service).filter_by(name=service_name).first()
    if not svc:
        raise HTTPException(404, "Servis bulunamadı")
    extra = svc.extra or {}

    hms_host = extra.get("hms_host", "")
    if not hms_host:
        return {"ok": False, "error": "hms_host tanımlı değil"}

    hms_port = int(extra.get("hms_port", 3306))
    hms_db   = extra.get("hms_db", "metastore")
    hms_user = extra.get("hms_user", "")
    hms_pass = extra.get("hms_password", "")

    try:
        conn = pymysql.connect(
            host=hms_host, port=hms_port, db=hms_db,
            user=hms_user, password=hms_pass,
            connect_timeout=10, read_timeout=10,
            charset="utf8mb4",
        )
        with conn:
            with conn.cursor() as cur:
                cur.execute("SELECT COUNT(*) FROM DBS")
                db_count = cur.fetchone()[0]
                cur.execute("SELECT COUNT(*) FROM TBLS")
                tbl_count = cur.fetchone()[0]
        return {
            "ok":       True,
            "host":     hms_host,
            "port":     hms_port,
            "database": hms_db,
            "dbCount":  db_count,
            "tblCount": tbl_count,
        }
    except Exception as e:
        return {"ok": False, "error": str(e), "host": hms_host, "port": hms_port, "database": hms_db}


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

    # HDFS listesi + HMS eşlemesi paralel
    hdfs_result, hms_map = await asyncio.gather(
        asyncio.to_thread(_run, host, sp, cmd),
        asyncio.to_thread(_hms_path_to_name, extra, schema_name),
    )
    _, out, _ = hdfs_result

    tables  = []
    orphans = []  # HDFS'de var, HMS'de yok
    for line in out.splitlines():
        line = line.strip()
        if not line:
            continue
        dir_name = line.rstrip("/").split("/")[-1]
        if not dir_name:
            continue
        hms_name = hms_map.get(dir_name) if hms_map else None
        entry = {"name": dir_name, "hmsName": hms_name, "path": line}
        tables.append(entry)
        if hms_map and hms_name is None:
            orphans.append(entry)

    return {
        "schema":     schema_name,
        "tables":     tables,
        "orphans":    orphans,
        "hmsEnabled": bool(hms_map),
    }


# ─── Boyut + dosya sayısı + partition analizi ─────────────────────────────────

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

    # hdfs dfs -count: DIR_COUNT  FILE_COUNT  CONTENT_SIZE  PATH
    paths     = [f"{warehouse}/{body.schema_name}/{t}" for t in body.tables]
    path_list = " ".join(f"'{p}'" for p in paths)
    cmd = (
        f"for p in {path_list}; do "
        f"  res=$(hdfs dfs -count \"$p\" 2>/dev/null); "
        f"  if [ -n \"$res\" ]; then echo \"$res\"; else echo \"0 0 0 $p\"; fi; "
        f"done"
    )

    # HDFS count + HMS eşlemesi + HMS partition sayısı paralel
    hdfs_result, hms_map = await asyncio.gather(
        asyncio.to_thread(_run, host, sp, cmd),
        asyncio.to_thread(_hms_path_to_name, extra, body.schema_name),
    )
    _, out, _ = hdfs_result

    # Önce HDFS sonuçlarını parse et
    raw_results = []
    for line in out.splitlines():
        parts = line.split()
        if len(parts) < 4:
            continue
        try:
            dir_count  = int(parts[0])
            file_count = int(parts[1])
            size_bytes = int(parts[2])
            path       = parts[3]
            dir_name   = path.rstrip("/").split("/")[-1]
            hms_name   = hms_map.get(dir_name) if hms_map else None
            raw_results.append({
                "table":     dir_name,
                "hmsName":   hms_name,
                "path":      path,
                "sizeBytes": size_bytes,
                "fileCount": file_count,
                "dirCount":  dir_count,
            })
        except (ValueError, IndexError):
            continue

    # HMS partition sayısı sorgusunu gerçek tablo adları üzerinden yap
    hms_names_in_result = [r["hmsName"] for r in raw_results if r["hmsName"]]
    part_counts = {}
    if hms_names_in_result:
        part_counts = await asyncio.to_thread(
            _hms_partition_counts, extra, body.schema_name, hms_names_in_result
        )

    # Son sonuçları oluştur
    total_bytes = 0
    results     = []
    for r in raw_results:
        size_bytes = r["sizeBytes"]
        file_count = r["fileCount"]
        total_bytes += size_bytes

        # Boş tablo
        is_empty = size_bytes == 0

        # Ortalama dosya boyutu ve küçük dosya uyarısı
        avg_file_bytes = (size_bytes // file_count) if file_count > 0 else 0
        small_files    = (not is_empty) and file_count > 0 and (avg_file_bytes < SMALL_FILE_THRESHOLD)

        # Partition sayısı
        hms_name       = r["hmsName"]
        partition_count = part_counts.get(hms_name, 0) if hms_name else 0

        results.append({
            **r,
            "isEmpty":        is_empty,
            "avgFileBytes":   avg_file_bytes,
            "smallFiles":     small_files,
            "partitionCount": partition_count,
        })

    # Sıralama: önce boş olmayanlar (büyükten küçüğe), sonra boşlar
    results.sort(key=lambda x: (x["isEmpty"], -x["sizeBytes"]))

    return {
        "schema":     body.schema_name,
        "totalBytes": total_bytes,
        "tables":     results,
        "hmsEnabled": bool(hms_map),
    }
