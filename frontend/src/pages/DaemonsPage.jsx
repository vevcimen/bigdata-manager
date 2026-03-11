import { useState, useEffect, useCallback } from 'react'
import { getDaemons, daemonAction } from '../api.js'
import StatusBadge from '../components/StatusBadge.jsx'
import LogViewer from '../components/LogViewer.jsx'

export default function DaemonsPage({ selectedService }) {
  const [daemons,      setDaemons]      = useState([])
  const [loading,      setLoading]      = useState(true)
  const [error,        setError]        = useState(null)
  const [filter,       setFilter]       = useState('')
  const [serviceFilter, setServiceFilter] = useState(selectedService || '')
  const [actioning,    setActioning]    = useState({})   // {daemonId: 'start'|'stop'|'restart'}
  const [actionResult, setActionResult] = useState(null) // {id, status, output}
  const [logDaemon,    setLogDaemon]    = useState(null) // daemon for log view

  const fetchDaemons = useCallback(async () => {
    try {
      const data = await getDaemons()
      setDaemons(data)
      setError(null)
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchDaemons()
    const t = setInterval(fetchDaemons, 15_000)
    return () => clearInterval(t)
  }, [fetchDaemons])

  useEffect(() => {
    if (selectedService) setServiceFilter(selectedService)
  }, [selectedService])

  async function handleAction(daemon, action) {
    setActioning(prev => ({ ...prev, [daemon.id]: action }))
    setActionResult(null)
    try {
      const result = await daemonAction(daemon.id, action)
      setActionResult({ id: daemon.id, ...result })
      // Refresh list after action
      await fetchDaemons()
    } catch (e) {
      setActionResult({ id: daemon.id, status: 'failed', output: e.message })
    } finally {
      setActioning(prev => { const n = { ...prev }; delete n[daemon.id]; return n })
    }
  }

  // Unique services for filter dropdown
  const serviceNames = [...new Set(daemons.map(d => d.service?.name).filter(Boolean))].sort()

  const filtered = daemons.filter(d => {
    const matchService = !serviceFilter || d.service?.name === serviceFilter
    const matchText = !filter ||
      (d.systemctl_name || '').toLowerCase().includes(filter.toLowerCase()) ||
      (d.host?.hostname || '').toLowerCase().includes(filter.toLowerCase()) ||
      (d.daemon_type || '').toLowerCase().includes(filter.toLowerCase())
    return matchService && matchText
  })

  // Group by service
  const grouped = {}
  for (const d of filtered) {
    const svc = d.service?.name || 'Diğer'
    if (!grouped[svc]) grouped[svc] = []
    grouped[svc].push(d)
  }

  const totalActive   = filtered.filter(d => d.last_status === 'active').length
  const totalFailed   = filtered.filter(d => d.last_status === 'failed').length
  const totalInactive = filtered.filter(d => !['active','failed'].includes(d.last_status)).length

  if (loading) return <div className="loading"><div className="spinner" /> Yükleniyor...</div>

  return (
    <div>
      {error && <div className="error-banner">⚠ {error}</div>}

      {/* ── Filters & stats ── */}
      <div style={{ display: 'flex', gap: 12, alignItems: 'center', marginBottom: 20, flexWrap: 'wrap' }}>
        <div style={{ display: 'flex', gap: 16, fontSize: 12, color: 'var(--text-muted)' }}>
          <span>Toplam: <strong style={{ color: 'var(--text)' }}>{filtered.length}</strong></span>
          <span style={{ color: 'var(--success)' }}>✓ {totalActive} aktif</span>
          {totalFailed > 0 && <span style={{ color: 'var(--danger)' }}>✗ {totalFailed} hata</span>}
          {totalInactive > 0 && <span style={{ color: 'var(--warning)' }}>⏸ {totalInactive} pasif</span>}
        </div>
        <div style={{ marginLeft: 'auto', display: 'flex', gap: 8, flexWrap: 'wrap' }}>
          <select
            value={serviceFilter}
            onChange={e => setServiceFilter(e.target.value)}
            style={{
              background: 'var(--bg-secondary)',
              border: '1px solid var(--border)',
              borderRadius: 'var(--radius)',
              padding: '5px 10px',
              color: 'var(--text)',
              fontSize: 12,
              cursor: 'pointer',
            }}
          >
            <option value="">Tüm Servisler</option>
            {serviceNames.map(n => <option key={n} value={n}>{n}</option>)}
          </select>
          <input
            type="text"
            placeholder="Daemon, host ara..."
            value={filter}
            onChange={e => setFilter(e.target.value)}
            style={{
              background: 'var(--bg-secondary)',
              border: '1px solid var(--border)',
              borderRadius: 'var(--radius)',
              padding: '5px 10px',
              color: 'var(--text)',
              fontSize: 12,
              width: 200,
              outline: 'none',
            }}
          />
          <button className="btn btn-ghost btn-sm" onClick={fetchDaemons}>↻ Yenile</button>
        </div>
      </div>

      {/* ── Action result banner ── */}
      {actionResult && (
        <div
          className="card"
          style={{
            marginBottom: 16,
            borderColor: actionResult.status === 'success' ? 'var(--success-dim)' : 'var(--danger-dim)',
          }}
        >
          <div style={{
            display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8,
            color: actionResult.status === 'success' ? 'var(--success)' : 'var(--danger)',
            fontWeight: 600, fontSize: 13,
          }}>
            {actionResult.status === 'success' ? '✓' : '✗'}
            Daemon #{actionResult.id} — {actionResult.action} →{' '}
            <StatusBadge status={actionResult.last_status || actionResult.status} />
          </div>
          {actionResult.output && (
            <pre style={{
              fontFamily: 'var(--font-mono)',
              fontSize: 11,
              color: 'var(--text-muted)',
              whiteSpace: 'pre-wrap',
              wordBreak: 'break-all',
              maxHeight: 100,
              overflow: 'auto',
            }}>
              {actionResult.output}
            </pre>
          )}
          <button
            className="btn btn-ghost btn-sm"
            style={{ marginTop: 8 }}
            onClick={() => setActionResult(null)}
          >
            Kapat
          </button>
        </div>
      )}

      {/* ── Log viewer modal ── */}
      {logDaemon && (
        <div
          style={{
            position: 'fixed', inset: 0,
            background: 'rgba(0,0,0,0.7)',
            zIndex: 100,
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            padding: 24,
          }}
          onClick={e => { if (e.target === e.currentTarget) setLogDaemon(null) }}
        >
          <div style={{
            background: 'var(--sidebar)',
            border: '1px solid var(--border)',
            borderRadius: 'var(--radius-lg)',
            padding: 24,
            width: '100%',
            maxWidth: 900,
            maxHeight: '80vh',
            overflow: 'auto',
          }}>
            <LogViewer daemonId={logDaemon.id} daemonName={logDaemon.systemctl_name} />
            <button
              className="btn btn-ghost"
              style={{ marginTop: 12 }}
              onClick={() => setLogDaemon(null)}
            >
              Kapat
            </button>
          </div>
        </div>
      )}

      {/* ── Grouped daemon tables ── */}
      {Object.entries(grouped).map(([svcName, svcDaemons]) => (
        <div key={svcName} className="section">
          <div className="section-title">
            <span className={`chip ${svcName.toLowerCase().split('-')[0].trim()}`}>{svcName}</span>
            <span style={{ fontSize: 12, color: 'var(--text-muted)', fontWeight: 400 }}>
              {svcDaemons.filter(d => d.last_status === 'active').length}/{svcDaemons.length} aktif
            </span>
          </div>

          <div className="card">
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Daemon / Servis Adı</th>
                    <th>Tip</th>
                    <th>Host</th>
                    <th>Durum</th>
                    <th>Son Kontrol</th>
                    <th>İşlemler</th>
                  </tr>
                </thead>
                <tbody>
                  {svcDaemons.map(d => {
                    const busy = actioning[d.id]
                    return (
                      <tr key={d.id}>
                        <td>
                          <span style={{ fontFamily: 'var(--font-mono)', fontSize: 12, color: 'var(--text)' }}>
                            {d.systemctl_name}
                          </span>
                        </td>
                        <td className="td-muted">{d.daemon_type}</td>
                        <td>
                          <div style={{ fontFamily: 'var(--font-mono)', fontSize: 11, color: 'var(--text-muted)' }}>
                            {d.host?.hostname}
                          </div>
                          {d.host?.jump_via && (
                            <div style={{ fontSize: 10, color: 'var(--warning)' }}>⤳ {d.host.jump_via}</div>
                          )}
                        </td>
                        <td><StatusBadge status={d.last_status} /></td>
                        <td className="td-muted" style={{ fontSize: 11 }}>
                          {d.last_checked
                            ? new Date(d.last_checked).toLocaleString('tr-TR')
                            : '—'}
                        </td>
                        <td>
                          <div className="btn-group">
                            <button
                              className="btn btn-success btn-sm"
                              disabled={!!busy}
                              onClick={() => handleAction(d, 'start')}
                            >
                              {busy === 'start' ? <><span className="spinner" style={{ width: 10, height: 10 }} />Başlatılıyor</> : '▶ Başlat'}
                            </button>
                            <button
                              className="btn btn-danger btn-sm"
                              disabled={!!busy}
                              onClick={() => handleAction(d, 'stop')}
                            >
                              {busy === 'stop' ? <><span className="spinner" style={{ width: 10, height: 10 }} />Durduruluyor</> : '■ Durdur'}
                            </button>
                            <button
                              className="btn btn-warning btn-sm"
                              disabled={!!busy}
                              onClick={() => handleAction(d, 'restart')}
                            >
                              {busy === 'restart' ? <><span className="spinner" style={{ width: 10, height: 10 }} />Yeniden başlatılıyor</> : '↺ Yeniden Başlat'}
                            </button>
                            {d.log_path && (
                              <button
                                className="btn btn-ghost btn-sm"
                                onClick={() => setLogDaemon(d)}
                              >
                                ≡ Log
                              </button>
                            )}
                          </div>
                        </td>
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      ))}

      {filtered.length === 0 && (
        <div className="empty">
          <p>{filter || serviceFilter ? 'Filtreyle eşleşen daemon bulunamadı' : 'Daemon bulunamadı'}</p>
        </div>
      )}
    </div>
  )
}
