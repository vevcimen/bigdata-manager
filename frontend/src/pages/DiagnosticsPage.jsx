import { useState, useEffect, useCallback } from 'react'

const BASE = '/api/diag'

async function fetchDiag() {
  const res = await fetch(BASE)
  if (!res.ok) throw new Error(`HTTP ${res.status}`)
  return res.json()
}

async function fetchLogs(last = 200) {
  const res = await fetch(`${BASE}/logs?last=${last}`)
  if (!res.ok) throw new Error(`HTTP ${res.status}`)
  return res.json()
}

function StatusChip({ status }) {
  const map = {
    ok:      { bg: 'rgba(63,185,80,0.15)',   color: '#3fb950', label: '✓ OK' },
    warning: { bg: 'rgba(210,153,34,0.15)',  color: '#d29922', label: '⚠ Uyarı' },
    error:   { bg: 'rgba(248,81,73,0.15)',   color: '#f85149', label: '✗ Hata' },
  }
  const s = map[status] || map.warning
  return (
    <span style={{
      background: s.bg, color: s.color,
      padding: '2px 8px', borderRadius: 20,
      fontSize: 11, fontWeight: 700,
    }}>
      {s.label}
    </span>
  )
}

function Section({ title, status, children }) {
  const [open, setOpen] = useState(true)
  return (
    <div style={{
      background: 'var(--card)',
      border: '1px solid var(--border)',
      borderRadius: 'var(--radius-lg)',
      marginBottom: 16,
      overflow: 'hidden',
    }}>
      <div
        onClick={() => setOpen(o => !o)}
        style={{
          display: 'flex', alignItems: 'center', justifyContent: 'space-between',
          padding: '12px 20px', cursor: 'pointer',
          borderBottom: open ? '1px solid var(--border)' : 'none',
          background: status === 'error' ? 'rgba(248,81,73,0.05)' : 'transparent',
        }}
      >
        <span style={{ fontWeight: 600, fontSize: 13 }}>{title}</span>
        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
          <StatusChip status={status} />
          <span style={{ color: 'var(--text-subtle)', fontSize: 11 }}>{open ? '▲' : '▼'}</span>
        </div>
      </div>
      {open && <div style={{ padding: '16px 20px' }}>{children}</div>}
    </div>
  )
}

function KV({ label, value, mono, danger, warn }) {
  return (
    <div style={{ display: 'flex', gap: 12, padding: '4px 0', fontSize: 13 }}>
      <span style={{ color: 'var(--text-muted)', width: 200, flexShrink: 0 }}>{label}</span>
      <span style={{
        fontFamily: mono ? 'var(--font-mono)' : 'inherit',
        fontSize: mono ? 12 : 13,
        color: danger ? 'var(--danger)' : warn ? 'var(--warning)' : 'var(--text)',
        wordBreak: 'break-all',
      }}>
        {String(value ?? '—')}
      </span>
    </div>
  )
}

function LogLevel({ level }) {
  const colors = {
    ERROR:    '#f85149', CRITICAL: '#f85149',
    WARNING:  '#d29922',
    INFO:     '#58a6ff',
    DEBUG:    '#8b949e',
  }
  return (
    <span style={{
      color: colors[level] || '#8b949e',
      width: 60, display: 'inline-block',
      fontWeight: 700, fontSize: 11,
    }}>
      {level}
    </span>
  )
}

export default function DiagnosticsPage() {
  const [diag,       setDiag]       = useState(null)
  const [logs,       setLogs]       = useState(null)
  const [loading,    setLoading]    = useState(true)
  const [loadingLog, setLoadingLog] = useState(false)
  const [error,      setError]      = useState(null)
  const [tab,        setTab]        = useState('status')

  const loadDiag = useCallback(async () => {
    setLoading(true)
    try {
      const data = await fetchDiag()
      setDiag(data)
      setError(null)
    } catch (e) {
      setError(`Backend bağlantısı kurulamadı: ${e.message}`)
    } finally {
      setLoading(false)
    }
  }, [])

  const loadLogs = useCallback(async () => {
    setLoadingLog(true)
    try {
      const data = await fetchLogs(300)
      setLogs(data)
    } catch (e) {
      setLogs({ error: e.message })
    } finally {
      setLoadingLog(false)
    }
  }, [])

  useEffect(() => { loadDiag() }, [loadDiag])
  useEffect(() => { if (tab === 'logs') loadLogs() }, [tab, loadLogs])

  if (loading) return <div className="loading"><div className="spinner" /> Diagnostics yükleniyor...</div>

  if (error) {
    return (
      <div>
        <div className="error-banner" style={{ fontSize: 14 }}>
          ✗ {error}
        </div>
        <div className="card" style={{ marginTop: 16 }}>
          <div className="card-title">Olası Sebepler</div>
          {[
            ['Backend çalışmıyor', 'sudo systemctl status bigdata-manager', 'veya: uvicorn backend.main:app --host 0.0.0.0 --port 8000'],
            ['Config dosyası eksik', 'ls -la cluster_manager.cfg', 'CM_CONFIG ortam değişkeni doğru mu?'],
            ['Port 8000 kapalı', 'ss -tlnp | grep 8000', 'firewall-cmd --add-port=8000/tcp --permanent'],
            ['MySQL çalışmıyor', 'systemctl status mysqld', 'INSTALL.md §2 adımlarını kontrol et'],
          ].map(([title, cmd1, hint]) => (
            <div key={title} style={{ marginBottom: 16, paddingBottom: 16, borderBottom: '1px solid var(--border)' }}>
              <div style={{ fontWeight: 600, color: 'var(--danger)', marginBottom: 6 }}>✗ {title}</div>
              <pre style={{
                background: 'var(--bg-secondary)',
                border: '1px solid var(--border)',
                borderRadius: 6,
                padding: '8px 12px',
                fontSize: 12,
                fontFamily: 'var(--font-mono)',
                color: 'var(--text-muted)',
                marginBottom: 4,
              }}>
                {cmd1}
              </pre>
              <div style={{ fontSize: 12, color: 'var(--text-subtle)' }}>{hint}</div>
            </div>
          ))}
        </div>
      </div>
    )
  }

  return (
    <div>
      {/* ── Header ── */}
      <div className="flex items-center justify-between mb-24">
        <div>
          <div style={{ fontSize: 13, color: 'var(--text-muted)' }}>
            {diag?.timestamp && new Date(diag.timestamp).toLocaleString('tr-TR')} ·{' '}
            {diag?.system?.hostname}
          </div>
          {diag?.errors?.length > 0 && (
            <div style={{ marginTop: 8 }}>
              {diag.errors.map((e, i) => (
                <div key={i} style={{ color: 'var(--danger)', fontSize: 12, display: 'flex', gap: 6 }}>
                  <span>✗</span><span>{e}</span>
                </div>
              ))}
            </div>
          )}
        </div>
        <div className="flex gap-8">
          <span style={{
            fontSize: 13, fontWeight: 700,
            color: diag?.healthy ? 'var(--success)' : 'var(--danger)',
          }}>
            {diag?.healthy ? '✓ Sistem Sağlıklı' : '✗ Sorun Tespit Edildi'}
          </span>
          <button className="btn btn-ghost btn-sm" onClick={loadDiag}>↻ Yenile</button>
          <a
            href="/api/diag"
            target="_blank"
            className="btn btn-ghost btn-sm"
          >
            ↗ Ham JSON
          </a>
        </div>
      </div>

      {/* ── Tabs ── */}
      <div className="tabs">
        <div className={`tab${tab === 'status' ? ' active' : ''}`} onClick={() => setTab('status')}>
          Sistem Durumu
        </div>
        <div className={`tab${tab === 'logs' ? ' active' : ''}`} onClick={() => setTab('logs')}>
          Uygulama Logları
        </div>
        <div className={`tab${tab === 'tunnel' ? ' active' : ''}`} onClick={() => setTab('tunnel')}>
          SSH Tunnel / Erişim
        </div>
      </div>

      {/* ── Status tab ── */}
      {tab === 'status' && diag && (
        <div>
          <Section title="Sistem" status={diag.system?.status}>
            <KV label="Python" value={diag.system?.python} mono />
            <KV label="Platform" value={diag.system?.platform} />
            <KV label="Hostname" value={diag.system?.hostname} mono />
            <KV label="PID" value={diag.system?.pid} mono />
            <KV label="Çalışma Dizini" value={diag.system?.cwd} mono />
            <KV label="Başlangıç Zamanı" value={diag.system?.uptime_start} />
          </Section>

          <Section title="Konfigürasyon" status={diag.config?.status}>
            {diag.config?.missing_keys?.length > 0 && (
              <div className="error-banner" style={{ marginBottom: 12 }}>
                Eksik config anahtarları: {diag.config.missing_keys.join(', ')}
              </div>
            )}
            {diag.config?.warnings?.map((w, i) => (
              <div key={i} style={{ color: 'var(--warning)', fontSize: 12, marginBottom: 8 }}>⚠ {w}</div>
            ))}
            <KV label="Topology Dosyası" value={diag.config?.topology_file} mono />
            <KV label="Prometheus URL" value={diag.config?.prometheus_url} mono />
            <KV label="SSH User" value={diag.config?.ssh_user} mono />
            <KV label="SSH Key" value={diag.config?.ssh_key_path} mono />
            <KV
              label="SSH Key Mevcut mu?"
              value={diag.config?.ssh_key_exists ? 'Evet ✓' : 'Hayır ✗ — ssh-copy-id gerekli!'}
              danger={!diag.config?.ssh_key_exists}
            />
          </Section>

          <Section title="Veritabanı (MySQL)" status={diag.database?.status}>
            {diag.database?.status === 'error' ? (
              <>
                <div className="error-banner">{diag.database.detail}</div>
                {diag.database.hint && (
                  <div style={{ marginTop: 8, fontSize: 12, color: 'var(--warning)' }}>
                    💡 {diag.database.hint}
                  </div>
                )}
              </>
            ) : (
              <>
                <KV label="Bağlantı" value="Başarılı ✓" />
                <KV label="Servis Sayısı" value={diag.database?.services} />
                <KV label="Host Sayısı" value={diag.database?.hosts} />
                <KV label="Daemon Sayısı" value={diag.database?.daemons} />
                {diag.database?.daemons === 0 && (
                  <div style={{ marginTop: 8, fontSize: 12, color: 'var(--warning)' }}>
                    ⚠ Daemon bulunamadı — topology.yaml doğru yüklendi mi?
                  </div>
                )}
              </>
            )}
          </Section>

          <Section title="Dosya Sistemi" status={diag.filesystem?.status}>
            <KV label="Çalışma Dizini" value={diag.filesystem?.cwd} mono />
            <KV
              label="frontend-dist/"
              value={diag.filesystem?.frontend_dist ? 'Mevcut ✓' : 'Yok ✗ — npm run build çalıştırıldı mı?'}
              danger={!diag.filesystem?.frontend_dist}
            />
            <KV label="frontend-dist Yolu" value={diag.filesystem?.frontend_dist_path} mono />
            <KV
              label="topology.yaml"
              value={diag.filesystem?.topology_exists ? 'Mevcut ✓' : 'Yok ✗'}
              danger={!diag.filesystem?.topology_exists}
            />
            <KV label="CM_CONFIG" value={diag.filesystem?.config_env} mono />
          </Section>

          <Section title="Ağ Bağlantıları" status={diag.network?.status}>
            {Object.entries(diag.network?.checks || {}).map(([name, check]) => (
              <div key={name} style={{ marginBottom: 12, paddingBottom: 12, borderBottom: '1px solid var(--border)' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 6 }}>
                  <span style={{ fontWeight: 600, fontSize: 13 }}>{name}</span>
                  <StatusChip status={check.status} />
                </div>
                {check.url  && <KV label="URL" value={check.url} mono />}
                {check.host && <KV label="Host" value={`${check.host}:${check.port}`} mono />}
                {check.detail && (
                  <div style={{ color: 'var(--danger)', fontSize: 12, marginTop: 4 }}>
                    {check.detail}
                  </div>
                )}
                {check.hint && (
                  <div style={{ color: 'var(--warning)', fontSize: 12, marginTop: 4 }}>💡 {check.hint}</div>
                )}
              </div>
            ))}
          </Section>
        </div>
      )}

      {/* ── Logs tab ── */}
      {tab === 'logs' && (
        <div className="card">
          <div className="card-header">
            <div className="card-title">Uygulama Logları (Son {logs?.count || '...'} satır)</div>
            <div className="flex gap-8">
              <button className="btn btn-ghost btn-sm" onClick={loadLogs} disabled={loadingLog}>
                ↻ Yenile
              </button>
              <a href="/api/diag/logs" target="_blank" className="btn btn-ghost btn-sm">
                ↗ Ham JSON
              </a>
            </div>
          </div>

          {loadingLog && <div className="loading"><div className="spinner" /></div>}

          {!loadingLog && logs?.logs && (
            <div className="log-viewer" style={{ height: 600 }}>
              {logs.logs.length === 0 && (
                <span style={{ color: 'var(--text-subtle)' }}>Henüz log yok</span>
              )}
              {[...logs.logs].reverse().map((entry, i) => (
                <span key={i} className={`log-line ${
                  entry.level === 'ERROR' || entry.level === 'CRITICAL' ? 'log-error'
                  : entry.level === 'WARNING' ? 'log-warn'
                  : entry.level === 'INFO' ? 'log-info'
                  : ''
                }`}>
                  {entry.message + '\n'}
                </span>
              ))}
            </div>
          )}

          {logs?.error && (
            <div className="error-banner">Log alınamadı: {logs.error}</div>
          )}
        </div>
      )}

      {/* ── Tunnel tab ── */}
      {tab === 'tunnel' && (
        <div>
          <div className="card mb-24">
            <div className="card-title">SSH Tunnel ile Tarayıcıdan Erişim</div>
            <p style={{ fontSize: 13, color: 'var(--text-muted)', marginBottom: 16 }}>
              Sunucuya doğrudan tarayıcıdan erişemiyorsanız SSH tüneli kullanın.
              Windows'ta açtığınız <strong style={{ color: 'var(--text)' }}>PowerShell / CMD / Git Bash</strong>'te şu komutu çalıştırın:
            </p>

            <div style={{ background: 'var(--bg-secondary)', border: '1px solid var(--border)', borderRadius: 6, padding: 16, marginBottom: 16 }}>
              <div style={{ fontSize: 11, color: 'var(--text-subtle)', marginBottom: 8 }}>SSH TUNNEL KOMUTU</div>
              <pre style={{ fontFamily: 'var(--font-mono)', fontSize: 13, color: '#58a6ff', margin: 0 }}>
{`ssh -L 8000:localhost:8000 cm_user@<SUNUCU_IP>
# veya arka planda (Windows Terminal):
ssh -L 8000:localhost:8000 -N cm_user@<SUNUCU_IP>`}
              </pre>
            </div>

            <p style={{ fontSize: 13, color: 'var(--text-muted)', marginBottom: 12 }}>
              Tunnel açıkken tarayıcınızda:
            </p>
            <div style={{ background: 'var(--bg-secondary)', border: '1px solid var(--border)', borderRadius: 6, padding: 16, marginBottom: 16 }}>
              <pre style={{ fontFamily: 'var(--font-mono)', fontSize: 16, color: '#3fb950', margin: 0 }}>
                http://localhost:8000
              </pre>
            </div>

            <div style={{ fontSize: 12, color: 'var(--text-subtle)' }}>
              💡 Tunnel komutu çalıştığı sürece bağlantı açık kalır. Terminal penceresini kapatmayın.
            </div>
          </div>

          <div className="card mb-24">
            <div className="card-title">Alternatif: Serveri Dış IP'ye Bağla</div>
            <p style={{ fontSize: 13, color: 'var(--text-muted)', marginBottom: 12 }}>
              Eğer sunucu ağ üzerinde erişilebilirse (VPN, iç ağ):
            </p>
            <pre style={{ background: 'var(--bg-secondary)', borderRadius: 6, padding: 16, fontSize: 12, fontFamily: 'var(--font-mono)', color: 'var(--text-muted)', border: '1px solid var(--border)' }}>
{`# Backend'i tüm arayüzlere bağla
uvicorn backend.main:app --host 0.0.0.0 --port 8000

# Firewall'da portu aç (CentOS/RHEL)
sudo firewall-cmd --permanent --add-port=8000/tcp
sudo firewall-cmd --reload

# Tarayıcıda erişim:
http://<SUNUCU_IP>:8000`}
            </pre>
          </div>

          <div className="card">
            <div className="card-title">CentOS'ta Node.js Olmadan Kurulum</div>
            <p style={{ fontSize: 13, color: 'var(--text-muted)', marginBottom: 12 }}>
              Frontend <code style={{ fontFamily: 'var(--font-mono)', background: 'var(--bg-secondary)', padding: '1px 4px', borderRadius: 3 }}>frontend-dist/</code> klasörü
              git'e commit edilmiştir. Sunucuda npm kurmanıza gerek yok.
            </p>
            <pre style={{ background: 'var(--bg-secondary)', borderRadius: 6, padding: 16, fontSize: 12, fontFamily: 'var(--font-mono)', color: 'var(--text-muted)', border: '1px solid var(--border)' }}>
{`# Sunucuda sadece:
git clone https://github.com/vevcimen/bigdata-manager.git
cd bigdata-manager

# Python ortamı
python3.9 -m venv venv
source venv/bin/activate
pip install -r backend/requirements.txt

# Config düzenle
cp cluster_manager.cfg.example cluster_manager.cfg  # veya direkt düzenle

# Çalıştır
uvicorn backend.main:app --host 0.0.0.0 --port 8000
# npm install GEREKMİYOR — frontend-dist/ hazır gelir`}
            </pre>
          </div>
        </div>
      )}
    </div>
  )
}
