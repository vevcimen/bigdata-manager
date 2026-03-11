import { useState, useEffect, useCallback } from 'react'
import { getHosts, getAllHostMetrics } from '../api.js'
import StatusBadge from '../components/StatusBadge.jsx'

function ProgressBar({ value, max = 100 }) {
  const pct = Math.min(100, Math.max(0, (value / max) * 100))
  let cls = 'low'
  if (pct >= 85) cls = 'high'
  else if (pct >= 60) cls = 'medium'
  return (
    <div className="progress-bar-wrap">
      <div className="progress-bar" style={{ width: `${pct}%` }} />
    </div>
  )
}

function MetricRow({ label, value, unit = '%', max = 100 }) {
  if (value === undefined || value === null) return null
  const num = parseFloat(value)
  return (
    <div className="metric-row">
      <span className="metric-label">{label}</span>
      <ProgressBar value={num} max={max} />
      <span className="metric-value">{isNaN(num) ? '—' : `${num.toFixed(1)}${unit}`}</span>
    </div>
  )
}

function formatBytes(bytes) {
  if (!bytes) return '—'
  const gb = parseFloat(bytes) / (1024 ** 3)
  if (gb >= 1) return `${gb.toFixed(1)} GB`
  const mb = parseFloat(bytes) / (1024 ** 2)
  return `${mb.toFixed(0)} MB`
}

export default function HostsPage() {
  const [hosts,   setHosts]   = useState([])
  const [metrics, setMetrics] = useState({})
  const [loading, setLoading] = useState(true)
  const [error,   setError]   = useState(null)
  const [filter,  setFilter]  = useState('')

  const fetchAll = useCallback(async () => {
    try {
      const [h, m] = await Promise.all([getHosts(), getAllHostMetrics()])
      setHosts(h)
      setMetrics(m)
      setError(null)
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchAll()
    const t = setInterval(fetchAll, 30_000)
    return () => clearInterval(t)
  }, [fetchAll])

  const filtered = hosts.filter(h =>
    !filter ||
    h.hostname.toLowerCase().includes(filter.toLowerCase()) ||
    (h.ip || '').includes(filter) ||
    (h.service_name || '').toLowerCase().includes(filter.toLowerCase())
  )

  // Group by service
  const grouped = {}
  for (const h of filtered) {
    const svc = h.service_name || 'Diğer'
    if (!grouped[svc]) grouped[svc] = []
    grouped[svc].push(h)
  }

  if (loading) return <div className="loading"><div className="spinner" /> Yükleniyor...</div>

  return (
    <div>
      {error && <div className="error-banner">⚠ {error}</div>}

      <div className="flex items-center justify-between mb-24" style={{ gap: 12 }}>
        <div style={{ fontSize: 13, color: 'var(--text-muted)' }}>
          Toplam <strong style={{ color: 'var(--text)' }}>{hosts.length}</strong> host
        </div>
        <input
          type="text"
          placeholder="Host, IP veya servis ara..."
          value={filter}
          onChange={e => setFilter(e.target.value)}
          style={{
            background: 'var(--bg-secondary)',
            border: '1px solid var(--border)',
            borderRadius: 'var(--radius)',
            padding: '6px 12px',
            color: 'var(--text)',
            fontSize: 13,
            width: 260,
            outline: 'none',
          }}
        />
        <button className="btn btn-ghost btn-sm" onClick={fetchAll}>↻ Yenile</button>
      </div>

      {Object.entries(grouped).map(([svcName, svcHosts]) => (
        <div key={svcName} className="section">
          <div className="section-title">
            <span className={`chip ${svcName.toLowerCase().split('-')[0].trim()}`}>{svcName}</span>
            <span style={{ fontSize: 12, color: 'var(--text-muted)', fontWeight: 400 }}>
              {svcHosts.length} host
            </span>
          </div>

          <div className="grid-3">
            {svcHosts.map(h => {
              const m = metrics[h.hostname] || {}
              const cpuUsage  = m['cpu_usage_percent']
              const memUsed   = m['memory_used_bytes']
              const memTotal  = m['memory_total_bytes']
              const diskUsed  = m['disk_used_bytes']
              const diskTotal = m['disk_total_bytes']
              const memPct = memTotal && memUsed ? (memUsed / memTotal) * 100 : null
              const diskPct = diskTotal && diskUsed ? (diskUsed / diskTotal) * 100 : null
              const hasMetrics = cpuUsage !== undefined || memPct !== null || diskPct !== null

              return (
                <div key={h.id} className="host-card">
                  <div className="host-card-header">
                    <div>
                      <div className="host-name">{h.hostname}</div>
                      <div className="host-ip">{h.ip}</div>
                    </div>
                    {h.jump_via && (
                      <span style={{
                        fontSize: 10,
                        background: 'rgba(210,153,34,0.1)',
                        color: 'var(--warning)',
                        padding: '2px 6px',
                        borderRadius: 4,
                        whiteSpace: 'nowrap',
                      }}>
                        ⤳ jump
                      </span>
                    )}
                  </div>

                  {hasMetrics ? (
                    <div style={{ marginTop: 8 }}>
                      {cpuUsage !== undefined && (
                        <MetricRow label="CPU" value={cpuUsage} />
                      )}
                      {memPct !== null && (
                        <MetricRow
                          label="Bellek"
                          value={memPct}
                          unit="%"
                        />
                      )}
                      {diskPct !== null && (
                        <MetricRow
                          label="Disk"
                          value={diskPct}
                          unit="%"
                        />
                      )}
                      {memTotal && (
                        <div style={{ fontSize: 11, color: 'var(--text-subtle)', marginTop: 4 }}>
                          Bellek: {formatBytes(memUsed)} / {formatBytes(memTotal)}
                        </div>
                      )}
                    </div>
                  ) : (
                    <div style={{ fontSize: 11, color: 'var(--text-subtle)', marginTop: 8 }}>
                      Metrik verisi yok — node_exporter çalışıyor mu?
                    </div>
                  )}
                </div>
              )
            })}
          </div>
        </div>
      ))}

      {filtered.length === 0 && (
        <div className="empty">
          <p>{filter ? `"${filter}" ile eşleşen host bulunamadı` : 'Host bulunamadı'}</p>
        </div>
      )}
    </div>
  )
}
