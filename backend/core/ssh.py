"""
SSH bağlantı havuzu ve komut yürütme.
Paramiko kullanır, bağlantılar yeniden kullanılır.
Doris BE için jump-host (proxy) desteği mevcuttur.
"""
import logging
import threading
import time
from typing import Tuple, Optional, Generator
import paramiko

log = logging.getLogger(__name__)

_lock        = threading.Lock()
_pool: dict  = {}   # key: (host, user) -> {"client": SSHClient, "ts": float}
_POOL_TTL    = 300  # 5 dakikadan eski bağlantıları temizle


def _make_client(
    host: str,
    user: str,
    key_path: str,
    timeout: int = 30,
    jump_host: Optional[str] = None,
    jump_user: Optional[str] = None,
) -> paramiko.SSHClient:
    """
    Yeni SSH bağlantısı kurar.
    jump_host verilmişse önce oraya bağlanır, oradan hedef host'a atlar.
    """
    expanded_key = key_path.replace("~", str(__import__("pathlib").Path.home()))

    if jump_host:
        # ── Jump (Proxy) SSH ────────────────────────────────────────────
        jump_client = paramiko.SSHClient()
        jump_client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
        jump_client.connect(
            jump_host,
            username=jump_user or user,
            key_filename=expanded_key,
            timeout=timeout
        )
        jump_transport = jump_client.get_transport()
        dest_addr  = (host, 22)
        local_addr = ("127.0.0.1", 0)
        channel = jump_transport.open_channel("direct-tcpip", dest_addr, local_addr)

        target_client = paramiko.SSHClient()
        target_client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
        target_client.connect(
            host,
            username=user,
            key_filename=expanded_key,
            sock=channel,
            timeout=timeout
        )
        # jump_client referansını canlı tutmak için target'a ekliyoruz
        target_client._jump_client = jump_client
        return target_client
    else:
        client = paramiko.SSHClient()
        client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
        client.connect(host, username=user, key_filename=expanded_key, timeout=timeout)
        return client


def _is_alive(client: paramiko.SSHClient) -> bool:
    try:
        transport = client.get_transport()
        if transport and transport.is_active():
            transport.send_ignore()
            return True
    except Exception:
        pass
    return False


def get_connection(
    host: str,
    user: str,
    key_path: str,
    timeout: int = 30,
    jump_host: Optional[str] = None,
    jump_user: Optional[str] = None,
) -> paramiko.SSHClient:
    """Pool'dan bağlantı döndürür, yoksa yenisini kurar."""
    pool_key = (host, user, jump_host or "")
    with _lock:
        entry = _pool.get(pool_key)
        if entry and _is_alive(entry["client"]):
            entry["ts"] = time.time()
            return entry["client"]
        # Eski/kopuk bağlantıyı temizle
        if entry:
            try:
                entry["client"].close()
            except Exception:
                pass

    client = _make_client(host, user, key_path, timeout, jump_host, jump_user)
    with _lock:
        _pool[pool_key] = {"client": client, "ts": time.time()}
    log.debug(f"SSH bağlantısı kuruldu: {user}@{host}" + (f" (via {jump_host})" if jump_host else ""))
    return client


def run_command(
    host: str,
    user: str,
    key_path: str,
    command: str,
    timeout: int = 30,
    jump_host: Optional[str] = None,
    jump_user: Optional[str] = None,
) -> Tuple[int, str, str]:
    """
    SSH ile komut çalıştırır.
    Returns: (exit_code, stdout, stderr)
    """
    client = get_connection(host, user, key_path, timeout, jump_host, jump_user)
    try:
        stdin, stdout, stderr = client.exec_command(command, timeout=timeout)
        exit_code = stdout.channel.recv_exit_status()
        out = stdout.read().decode("utf-8", errors="replace").strip()
        err = stderr.read().decode("utf-8", errors="replace").strip()
        return exit_code, out, err
    except Exception as e:
        log.error(f"SSH komut hatası [{host}]: {e}")
        # Bağlantıyı pool'dan çıkar
        pool_key = (host, user, jump_host or "")
        with _lock:
            _pool.pop(pool_key, None)
        return 1, "", str(e)


def stream_log(
    host: str,
    user: str,
    key_path: str,
    log_path: str,
    lines: int = 100,
    timeout: int = 30,
    jump_host: Optional[str] = None,
    jump_user: Optional[str] = None,
) -> Generator[str, None, None]:
    """
    tail -f ile log dosyasını okur, satır satır yield eder.
    SSE endpoint'i için kullanılır.
    """
    cmd = f"tail -n {lines} -f {log_path}"
    try:
        client = get_connection(host, user, key_path, timeout, jump_host, jump_user)
        # stream için ayrı channel aç (exec_command bloklamaz)
        transport = client.get_transport()
        channel = transport.open_session()
        channel.settimeout(2.0)
        channel.exec_command(cmd)

        buffer = b""
        while True:
            try:
                chunk = channel.recv(4096)
                if not chunk:
                    break
                buffer += chunk
                while b"\n" in buffer:
                    line, buffer = buffer.split(b"\n", 1)
                    yield line.decode("utf-8", errors="replace")
            except Exception:
                # timeout veya bağlantı koptu
                break
        channel.close()
    except Exception as e:
        yield f"[SSH HATA] {e}"


def cleanup_pool():
    """Eski bağlantıları pool'dan temizler."""
    now = time.time()
    with _lock:
        dead = [k for k, v in _pool.items() if now - v["ts"] > _POOL_TTL]
        for k in dead:
            try:
                _pool[k]["client"].close()
            except Exception:
                pass
            del _pool[k]
    if dead:
        log.debug(f"SSH pool: {len(dead)} eski bağlantı temizlendi.")
