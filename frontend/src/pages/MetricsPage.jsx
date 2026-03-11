import { useState, useEffect, useCallback } from 'react'
import { getHosts, getAllHostMetrics, getHostMetrics, refreshMetrics } from '../api.js'

function pct(used, total) {
  if (!used || !total) return null
  return (parseFloat(used) / parseFloat(total)) * 100
}

function fmtBytes(b) {
  if (b === undefined || b === null) return '—'
  const n = parseFloat(b)
  if (n >= 1e12) return `${(n / 1e12).toFixed(1)} TB`
  if (n >= 1e9)  return `${(n / 1e9).toFixed(1)} GB`
  if (n >= 1e6)  return `${(n / 1e6).toFixed(0)} MB`
  return `${n} B`
}

function fmtPct(v) {
  if (v === undefined || v === null) return '—'
  return `${parseFloat(v).toFixed(1)}%`
}

function BarCell({ value, max = 100 }) {
  if (value === null || value === undefined) {
    return <span style={{ color: 'var(--text-subtle)', fontSize: 11 }}>—</span>
  }
  const p = Math.min(100, Math.max(0, value))
  let color = 'var(--success)'
  if (p >= 85) color = 'var(--danger)'
  else if (p >= 60) color = 'var(--warning)'
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
      <div style={{ flex: 1, background: 'var(--bg-secondary)', borderRadius: 4, height: 6, overflow: 'hidden' }}>
        <div style={{ width: `${p}%`, background: color, height: '100%', borderRadius: 4 }} />
      </div>
      <span style={{ fontSize: 11, color: 'var(--text-muted)', width: 42, textAlign: 'right' }}>
        {p.toFixed(1)}%
      </span>
    </div>
  )
}

export default function MetricsPage() {
  const [hosts,       setHosts]       = useState([])
  const [metrics,     setMetrics]     = useState({})
  const [loading,     setLoading]     = useState(true)
  const [error,       setError]       = useState(null)
  const [selected,    setSelected]    = useState(null)
  const [history,     setHistory]     = useState([])
  const [loadingHist, setLoadingHist] = useState(false)
  const [refreshing,  setRefreshing]  = useState(false)

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

  async function selectHost(hostname) {
    setSelected(hostname)
    setLoadingHist(true)
    setHistory([])
    try {
      const data = await getHostMetrics(hostname, null, 60)
      setHistory(data)
    } catch {}
    finally { setLoadingHist(false) }
  }

  async function handleRefresh() {
    setRefreshing(true)
    try { await refreshMetrics() } catch {}
    await fetchAll()
    setRefreshing(false)
  }

  if (loading) return <div className="loading"><div className="spinner" /> Yükleniyor...</div>

  // Sort hosts: those with metrics first
  const sortedHosts = [...hosts].sort((a, b) => {
    const aHas = !!metrics[a.hostname]
    const bHas = !!metrics[b.hostname]
    return (bHas ? 1 : 0) - (aHas ? 1 : 0)
  })

  return (
    <div>
      {error && <div className="error-banner">⚠ {error}</div>}

      <div className="flex items-center justify-between mb-24">
        <div style={{ fontSize: 13, color: 'var(--text-muted)' }}>
          node_exporter üzerinden toplanan son metrikler · Her 60 saniyede güncellenir
        </div>
        <button
          className="btn btn-ghost"
          onClick={handleRefresh}
          disabled={refreshing}
        >
          {refreshing
            ? <><span className="spinner" style={{ width: 12, height: 12 }} /> Toplanıyor...</>
            : '↻ Metrikleri Yenile'
          }
        </button>
      </div>

      {/* ── Main metrics table ── */}
      <div className="card mb-24">
        <div className="card-title">Tüm Host'lar — Anlık Özet</div>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Host</th>
                <th>Servis</th>
                <th style={{ width: 200 }}>CPU Kullanımı</th>
                <th style={{ width: 200 }}>Bellek Kullanımı</th>
                <th style={{ width: 200 }}>Disk Kullanımı</th>
                <th>Bellek</th>
                <th>Disk</th>
                <th>Ağ ↓</th>
                <th>Ağ ↑</th>
              </tr>
            </thead>
            <tbody>
              {sortedHosts.map(h => {
                const m = metrics[h.hostname] || {}
                const cpuPct  = m['cpu_usage_percent'] !== undefined ? parseFloat(m['cpu_usage_percent']) : null
                const memPct  = pct(m['memory_used_bytes'], m['memory_total_bytes'])
                const diskPct = pct(m['disk_used_bytes'], m['disk_total_bytes'])
                const netRx   = m['network_receive_bytes_total']
                const netTx   = m['network_transmit_bytes_total']

                return (
                  <tr
                    key={h.id}
                    style={{ cursor: 'pointer', background: selected === h.hostname ? 'var(--hover)' : '' }}
                    onClick={() => selectHost(h.hostname)}
                  >
                    <td style={{ fontFamily: 'var(--font-mono)', fontSize: 12, fontWeight: 500 }}>
                      {h.hostname}
                    </td>
                    <td>
                      <span className={`chip ${(h.service_name || '').toLowerCase().split('-')[0].trim()}`}>
                        {h.service_name || '—'}
                      </span>
                    </td>
                    <td><BarCell value={cpuPct} /></td>
                    <td><BarCell value={memPct} /></td>
                    <td><BarCell value={diskPct} /></td>
                    <td style={{ fontSize: 11, color: 'var(--text-muted)' }}>
                      {fmtBytes(m['memory_used_bytes'])} / {fmtBytes(m['memory_total_bytes'])}
                    </td>
                    <td style={{ fontSize: 11, color: 'var(--text-muted)' }}>
                      {fmtBytes(m['disk_used_bytes'])} / {fmtBytes(m['disk_total_bytes'])}
                    </td>
                    <td style={{ fontSize: 11, fontFamily: 'var(--font-mono)', color: 'var(--text-muted)' }}>
                      {netRx !== undefined ? fmtBytes(netRx) : '—'}
                    </td>
                    <td style={{ fontSize: 11, fontFamily: 'var(--font-mono)', color: 'var(--text-muted)' }}>
                      {netTx !== undefined ? fmtBytes(netTx) : '—'}
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
      </div>

      {/* ── Host history ── */}
      {selected && (
        <div className="card">
          <div className="card-header">
            <div>
              <div className="card-title">Detaylı Metrik Geçmişi</div>
              <div style={{ fontSize: 12, color: 'var(--text-muted)', fontFamily: 'var(--font-mono)' }}>
                {selected}
              </div>
            </div>
            <button className="btn btn-ghost btn-sm" onClick={() => setSelected(null)}>
              Kapat ✕
            </button>
          </div>

          {loadingHist && <div className="loading"><div className="spinner" /> Yükleniyor...</div>}

          {!loadingHist && history.length === 0 && (
            <div className="empty"><p>Geçmiş metrik bulunamadı</p></div>
          )}

          {!loadingHist && history.length > 0 && (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Zaman</th>
                    <th>Metrik</th>
                    <th>Değer</th>
                    <th>Etiketler</th>
                  </tr>
                </thead>
                <tbody>
                  {history.slice(-100).reverse().map((row, i) => (
                    <tr key={i}>
                      <td className="td-muted" style={{ fontSize: 11, whiteSpace: 'nowrap' }}>
                        {new Date(row.ts).toLocaleString('tr-TR')}
                      </td>
                      <td className="td-mono" style={{ fontSize: 12 }}>{row.metric_name}</td>
                      <td style={{ fontSize: 12, fontFamily: 'var(--font-mono)' }}>
                        {typeof row.value === 'number' ? row.value.toFixed(3) : row.value}
                      </td>
                      <td className="td-muted" style={{ fontSize: 11 }}>
                        {row.labels ? JSON.stringify(row.labels) : '—'}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {hosts.length === 0 && (
        <div className="empty"><p>Host bulunamadı</p></div>
      )}
    </div>
  )
}
