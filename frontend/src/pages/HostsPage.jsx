import { useState, useEffect, useCallback } from 'react'
import { getHosts, getAllHostMetrics, getServices, createHost, updateHost, deleteHost, daemonAction } from '../api.js'
import StatusBadge from '../components/StatusBadge.jsx'
import LogViewer from '../components/LogViewer.jsx'

// ── Yardımcı bileşenler ──────────────────────────────────────────────────────

function ProgressBar({ value }) {
  const pct = Math.min(100, Math.max(0, value))
  return (
    <div className="progress-bar-wrap">
      <div className="progress-bar" style={{ width: `${pct}%` }} />
    </div>
  )
}

function MetricRow({ label, value, unit = '%' }) {
  if (value === undefined || value === null) return null
  const num = parseFloat(value)
  return (
    <div className="metric-row">
      <span className="metric-label">{label}</span>
      <ProgressBar value={num} />
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

// ── Host Düzenleme Modal ─────────────────────────────────────────────────────

function HostEditModal({ host, services, onSave, onClose }) {
  const isNew = !host
  const [form, setForm] = useState({
    hostname: host?.hostname || '',
    ip: host?.ip || '',
    service_name: host?.service_name || (services[0]?.name || ''),
    jump_via: host?.jump_via || '',
    useJumpHost: !!(host?.jump_via),
    daemons: host?.daemons?.map(d => ({
      daemon_type: d.daemon_type,
      systemctl_name: d.systemctl_name,
      log_path: d.log_path || '',
    })) || [],
  })
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState(null)

  function addDaemon() {
    setForm(f => ({ ...f, daemons: [...f.daemons, { daemon_type: '', systemctl_name: '', log_path: '' }] }))
  }

  function removeDaemon(idx) {
    setForm(f => ({ ...f, daemons: f.daemons.filter((_, i) => i !== idx) }))
  }

  function updateDaemon(idx, field, value) {
    setForm(f => ({
      ...f,
      daemons: f.daemons.map((d, i) => i === idx ? { ...d, [field]: value } : d)
    }))
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setSaving(true)
    setError(null)
    try {
      const payload = {
        hostname: form.hostname,
        ip: form.ip || null,
        service_name: form.service_name,
        jump_via: form.useJumpHost ? form.jump_via : null,
        daemons: form.daemons.filter(d => d.daemon_type && d.systemctl_name),
      }
      if (isNew) {
        await createHost(payload)
      } else {
        await updateHost(host.id, payload)
      }
      onSave()
    } catch (err) {
      setError(err.message)
    } finally {
      setSaving(false)
    }
  }

  const inputStyle = {
    background: 'var(--bg-secondary)',
    border: '1px solid var(--border)',
    borderRadius: 'var(--radius)',
    padding: '6px 10px',
    color: 'var(--text)',
    fontSize: 13,
    width: '100%',
    outline: 'none',
  }

  return (
    <div
      style={{
        position: 'fixed', inset: 0,
        background: 'rgba(0,0,0,0.7)',
        zIndex: 100,
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        padding: 24,
      }}
      onClick={e => { if (e.target === e.currentTarget) onClose() }}
    >
      <div style={{
        background: 'var(--sidebar)',
        border: '1px solid var(--border)',
        borderRadius: 'var(--radius-lg)',
        padding: 24,
        width: '100%',
        maxWidth: 600,
        maxHeight: '85vh',
        overflow: 'auto',
      }}>
        <div style={{ fontSize: 15, fontWeight: 600, color: 'var(--text)', marginBottom: 16 }}>
          {isNew ? 'Yeni Host Ekle' : `Host Düzenle — ${host.hostname}`}
        </div>

        {error && <div className="error-banner" style={{ marginBottom: 12 }}>{error}</div>}

        <form onSubmit={handleSubmit}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12, marginBottom: 16 }}>
            <div>
              <label style={{ fontSize: 11, color: 'var(--text-muted)', display: 'block', marginBottom: 4 }}>Hostname *</label>
              <input style={inputStyle} required value={form.hostname} onChange={e => setForm(f => ({ ...f, hostname: e.target.value }))} placeholder="ornek-host-01.internal" />
            </div>
            <div>
              <label style={{ fontSize: 11, color: 'var(--text-muted)', display: 'block', marginBottom: 4 }}>IP Adresi</label>
              <input style={inputStyle} value={form.ip} onChange={e => setForm(f => ({ ...f, ip: e.target.value }))} placeholder="10.0.1.100" />
            </div>
          </div>

          <div style={{ marginBottom: 16 }}>
            <label style={{ fontSize: 11, color: 'var(--text-muted)', display: 'block', marginBottom: 4 }}>Servis *</label>
            <select
              style={{ ...inputStyle, cursor: 'pointer' }}
              value={form.service_name}
              onChange={e => setForm(f => ({ ...f, service_name: e.target.value }))}
            >
              {services.map(s => <option key={s.name} value={s.name}>{s.name}</option>)}
            </select>
          </div>

          <div style={{ marginBottom: 16 }}>
            <label style={{ display: 'flex', alignItems: 'center', gap: 8, fontSize: 13, color: 'var(--text)', cursor: 'pointer' }}>
              <input
                type="checkbox"
                checked={form.useJumpHost}
                onChange={e => setForm(f => ({ ...f, useJumpHost: e.target.checked }))}
              />
              Jump Host kullan (Doris BE vb.)
            </label>
            {form.useJumpHost && (
              <input
                style={{ ...inputStyle, marginTop: 8 }}
                value={form.jump_via}
                onChange={e => setForm(f => ({ ...f, jump_via: e.target.value }))}
                placeholder="doris-fe-01.internal"
              />
            )}
          </div>

          <div style={{ marginBottom: 16 }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 8 }}>
              <label style={{ fontSize: 12, fontWeight: 600, color: 'var(--text)' }}>Daemon / Roller</label>
              <button type="button" className="btn btn-ghost btn-sm" onClick={addDaemon}>+ Rol Ekle</button>
            </div>
            {form.daemons.map((d, idx) => (
              <div key={idx} style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr auto', gap: 8, marginBottom: 8 }}>
                <input style={inputStyle} placeholder="Tip (namenode, worker...)" value={d.daemon_type} onChange={e => updateDaemon(idx, 'daemon_type', e.target.value)} />
                <input style={inputStyle} placeholder="systemctl adı" value={d.systemctl_name} onChange={e => updateDaemon(idx, 'systemctl_name', e.target.value)} />
                <input style={inputStyle} placeholder="Log dosya yolu" value={d.log_path} onChange={e => updateDaemon(idx, 'log_path', e.target.value)} />
                <button type="button" className="btn btn-danger btn-sm" onClick={() => removeDaemon(idx)} title="Sil">✕</button>
              </div>
            ))}
            {form.daemons.length === 0 && (
              <div style={{ fontSize: 12, color: 'var(--text-subtle)', padding: 8 }}>Henüz rol eklenmedi.</div>
            )}
          </div>

          <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
            <button type="button" className="btn btn-ghost" onClick={onClose}>İptal</button>
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? 'Kaydediliyor...' : (isNew ? 'Ekle' : 'Kaydet')}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

// ── Host Satırı (Table view, genişletilebilir) ───────────────────────────────

function HostRow({ host, metrics, onEdit, onDelete, onRefresh }) {
  const [expanded, setExpanded] = useState(false)
  const [actioning, setActioning] = useState({})
  const [logDaemon, setLogDaemon] = useState(null)

  const m = metrics[host.hostname] || {}
  const cpuUsage = m['cpu_usage_percent']
  const memUsed = m['memory_used_bytes']
  const memTotal = m['memory_total_bytes']
  const diskUsed = m['disk_used_bytes']
  const diskTotal = m['disk_total_bytes']
  const memPct = memTotal && memUsed ? (memUsed / memTotal) * 100 : null
  const diskPct = diskTotal && diskUsed ? (diskUsed / diskTotal) * 100 : null
  const daemons = host.daemons || []

  async function handleAction(daemon, action) {
    setActioning(prev => ({ ...prev, [daemon.id]: action }))
    try {
      await daemonAction(daemon.id, action)
      onRefresh()
    } catch { }
    finally {
      setActioning(prev => { const n = { ...prev }; delete n[daemon.id]; return n })
    }
  }

  return (
    <>
      <tr
        style={{ cursor: 'pointer' }}
        onClick={() => setExpanded(e => !e)}
      >
        <td>
          <span style={{ marginRight: 8, fontSize: 10, color: 'var(--text-subtle)' }}>{expanded ? '▼' : '▶'}</span>
          <span style={{ fontWeight: 600 }}>{host.hostname}</span>
        </td>
        <td className="td-mono">{host.ip || '—'}</td>
        <td>
          <span className={`chip ${(host.service_name || '').toLowerCase().split('-')[0].trim()}`}>
            {host.service_name || '—'}
          </span>
        </td>
        <td style={{ fontSize: 12, color: 'var(--text-muted)' }}>{daemons.length} rol</td>
        <td>
          {cpuUsage !== undefined ? (
            <span style={{ fontSize: 12 }}>CPU {parseFloat(cpuUsage).toFixed(0)}%</span>
          ) : (
            <span style={{ fontSize: 11, color: 'var(--text-subtle)' }}>—</span>
          )}
        </td>
        <td>
          {host.jump_via && (
            <span style={{ fontSize: 10, background: 'rgba(210,153,34,0.1)', color: 'var(--warning)', padding: '2px 6px', borderRadius: 4 }}>
              ⤳ {host.jump_via}
            </span>
          )}
        </td>
        <td onClick={e => e.stopPropagation()}>
          <div className="btn-group">
            <button className="btn btn-ghost btn-sm" onClick={() => onEdit(host)} title="Düzenle">✎</button>
            <button className="btn btn-danger btn-sm" onClick={() => onDelete(host)} title="Sil">✕</button>
          </div>
        </td>
      </tr>

      {expanded && (
        <tr>
          <td colSpan={7} style={{ padding: 0, background: 'var(--bg-secondary)' }}>
            <div style={{ padding: '12px 24px' }}>
              {/* Metrikler */}
              {(cpuUsage !== undefined || memPct !== null || diskPct !== null) && (
                <div style={{ display: 'flex', gap: 24, marginBottom: 12, maxWidth: 500 }}>
                  <div style={{ flex: 1 }}><MetricRow label="CPU" value={cpuUsage} /></div>
                  {memPct !== null && <div style={{ flex: 1 }}><MetricRow label="Bellek" value={memPct} /></div>}
                  {diskPct !== null && <div style={{ flex: 1 }}><MetricRow label="Disk" value={diskPct} /></div>}
                </div>
              )}
              {memTotal && (
                <div style={{ fontSize: 11, color: 'var(--text-subtle)', marginBottom: 12 }}>
                  Bellek: {formatBytes(memUsed)} / {formatBytes(memTotal)}
                </div>
              )}

              {/* Daemon / Roller */}
              {daemons.length > 0 ? (
                <table style={{ width: '100%' }}>
                  <thead>
                    <tr>
                      <th>Rol / Daemon</th>
                      <th>systemctl</th>
                      <th>Log Yolu</th>
                      <th>Durum</th>
                      <th>İşlemler</th>
                    </tr>
                  </thead>
                  <tbody>
                    {daemons.map(d => {
                      const busy = actioning[d.id]
                      return (
                        <tr key={d.id}>
                          <td style={{ fontWeight: 500 }}>{d.daemon_type}</td>
                          <td className="td-mono">{d.systemctl_name}</td>
                          <td className="td-muted" style={{ fontSize: 11 }}>{d.log_path || '—'}</td>
                          <td><StatusBadge status={d.last_status} /></td>
                          <td>
                            <div className="btn-group">
                              <button className="btn btn-success btn-sm" disabled={!!busy} onClick={() => handleAction(d, 'start')}>
                                {busy === 'start' ? '...' : '▶ Başlat'}
                              </button>
                              <button className="btn btn-danger btn-sm" disabled={!!busy} onClick={() => handleAction(d, 'stop')}>
                                {busy === 'stop' ? '...' : '■ Durdur'}
                              </button>
                              <button className="btn btn-warning btn-sm" disabled={!!busy} onClick={() => handleAction(d, 'restart')}>
                                {busy === 'restart' ? '...' : '↺ Yeniden'}
                              </button>
                              {d.log_path && (
                                <button className="btn btn-ghost btn-sm" onClick={() => setLogDaemon(d)}>
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
              ) : (
                <div style={{ fontSize: 12, color: 'var(--text-subtle)' }}>Bu host'ta tanımlı rol yok.</div>
              )}
            </div>

            {/* Log viewer modal */}
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
                  <button className="btn btn-ghost" style={{ marginTop: 12 }} onClick={() => setLogDaemon(null)}>Kapat</button>
                </div>
              </div>
            )}
          </td>
        </tr>
      )}
    </>
  )
}

// ── Host Kartı (Grid view, genişletilebilir) ─────────────────────────────────

function HostCard({ host, metrics, onEdit, onDelete, onRefresh }) {
  const [expanded, setExpanded] = useState(false)
  const [actioning, setActioning] = useState({})
  const [logDaemon, setLogDaemon] = useState(null)

  const m = metrics[host.hostname] || {}
  const cpuUsage = m['cpu_usage_percent']
  const memUsed = m['memory_used_bytes']
  const memTotal = m['memory_total_bytes']
  const diskUsed = m['disk_used_bytes']
  const diskTotal = m['disk_total_bytes']
  const memPct = memTotal && memUsed ? (memUsed / memTotal) * 100 : null
  const diskPct = diskTotal && diskUsed ? (diskUsed / diskTotal) * 100 : null
  const daemons = host.daemons || []

  async function handleAction(daemon, action) {
    setActioning(prev => ({ ...prev, [daemon.id]: action }))
    try {
      await daemonAction(daemon.id, action)
      onRefresh()
    } catch { }
    finally {
      setActioning(prev => { const n = { ...prev }; delete n[daemon.id]; return n })
    }
  }

  return (
    <div className="host-card">
      <div className="host-card-header" style={{ cursor: 'pointer' }} onClick={() => setExpanded(e => !e)}>
        <div>
          <div className="host-name">
            <span style={{ marginRight: 6, fontSize: 10, color: 'var(--text-subtle)' }}>{expanded ? '▼' : '▶'}</span>
            {host.hostname}
          </div>
          <div className="host-ip">{host.ip}</div>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
          {host.jump_via && (
            <span style={{ fontSize: 10, background: 'rgba(210,153,34,0.1)', color: 'var(--warning)', padding: '2px 6px', borderRadius: 4, whiteSpace: 'nowrap' }}>
              ⤳ jump
            </span>
          )}
          <button className="btn btn-ghost btn-sm" onClick={e => { e.stopPropagation(); onEdit(host) }} title="Düzenle">✎</button>
          <button className="btn btn-danger btn-sm" onClick={e => { e.stopPropagation(); onDelete(host) }} title="Sil">✕</button>
        </div>
      </div>

      {(cpuUsage !== undefined || memPct !== null || diskPct !== null) ? (
        <div style={{ marginTop: 8 }}>
          {cpuUsage !== undefined && <MetricRow label="CPU" value={cpuUsage} />}
          {memPct !== null && <MetricRow label="Bellek" value={memPct} />}
          {diskPct !== null && <MetricRow label="Disk" value={diskPct} />}
          {memTotal && (
            <div style={{ fontSize: 11, color: 'var(--text-subtle)', marginTop: 4 }}>
              Bellek: {formatBytes(memUsed)} / {formatBytes(memTotal)}
            </div>
          )}
        </div>
      ) : (
        <div style={{ fontSize: 11, color: 'var(--text-subtle)', marginTop: 8 }}>
          Metrik verisi yok
        </div>
      )}

      <div style={{ fontSize: 11, color: 'var(--text-muted)', marginTop: 8 }}>
        {daemons.length} rol
      </div>

      {expanded && daemons.length > 0 && (
        <div style={{ marginTop: 12, borderTop: '1px solid var(--border)', paddingTop: 12 }}>
          {daemons.map(d => {
            const busy = actioning[d.id]
            return (
              <div key={d.id} style={{ marginBottom: 10, padding: '8px 10px', background: 'var(--bg-secondary)', borderRadius: 'var(--radius)' }}>
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 4 }}>
                  <span style={{ fontWeight: 600, fontSize: 12 }}>{d.daemon_type}</span>
                  <StatusBadge status={d.last_status} />
                </div>
                <div style={{ fontSize: 11, color: 'var(--text-muted)', fontFamily: 'var(--font-mono)', marginBottom: 6 }}>
                  {d.systemctl_name}
                </div>
                <div className="btn-group">
                  <button className="btn btn-success btn-sm" disabled={!!busy} onClick={() => handleAction(d, 'start')}>
                    {busy === 'start' ? '...' : '▶'}
                  </button>
                  <button className="btn btn-danger btn-sm" disabled={!!busy} onClick={() => handleAction(d, 'stop')}>
                    {busy === 'stop' ? '...' : '■'}
                  </button>
                  <button className="btn btn-warning btn-sm" disabled={!!busy} onClick={() => handleAction(d, 'restart')}>
                    {busy === 'restart' ? '...' : '↺'}
                  </button>
                  {d.log_path && (
                    <button className="btn btn-ghost btn-sm" onClick={() => setLogDaemon(d)}>≡ Log</button>
                  )}
                </div>
              </div>
            )
          })}
        </div>
      )}

      {expanded && daemons.length === 0 && (
        <div style={{ marginTop: 12, fontSize: 12, color: 'var(--text-subtle)' }}>Bu host'ta tanımlı rol yok.</div>
      )}

      {/* Log viewer modal */}
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
            <button className="btn btn-ghost" style={{ marginTop: 12 }} onClick={() => setLogDaemon(null)}>Kapat</button>
          </div>
        </div>
      )}
    </div>
  )
}

// ── Ana Sayfa Bileşeni ───────────────────────────────────────────────────────

export default function HostsPage() {
  const [hosts, setHosts] = useState([])
  const [metrics, setMetrics] = useState({})
  const [services, setServices] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [filter, setFilter] = useState('')
  const [viewMode, setViewMode] = useState('table') // 'table' | 'grid'
  const [editHost, setEditHost] = useState(undefined) // undefined=closed, null=new, host=edit
  const [deleteConfirm, setDeleteConfirm] = useState(null)

  const fetchAll = useCallback(async () => {
    try {
      const [h, m, s] = await Promise.all([getHosts(), getAllHostMetrics(), getServices()])
      setHosts(h)
      setMetrics(m)
      setServices(s)
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

  async function handleDelete(host) {
    try {
      await deleteHost(host.id)
      setDeleteConfirm(null)
      fetchAll()
    } catch (e) {
      setError(e.message)
    }
  }

  const filtered = hosts.filter(h =>
    !filter ||
    h.hostname.toLowerCase().includes(filter.toLowerCase()) ||
    (h.ip || '').includes(filter) ||
    (h.service_name || '').toLowerCase().includes(filter.toLowerCase())
  )

  // Servise göre grupla
  const grouped = {}
  for (const h of filtered) {
    const svc = h.service_name || 'Diğer'
    if (!grouped[svc]) grouped[svc] = []
    grouped[svc].push(h)
  }

  if (loading) return <div className="loading"><div className="spinner" /> Yükleniyor...</div>

  return (
    <div>
      {error && <div className="error-banner">{error}</div>}

      {/* Toolbar */}
      <div className="flex items-center justify-between mb-24" style={{ gap: 12, flexWrap: 'wrap' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <div style={{ fontSize: 13, color: 'var(--text-muted)' }}>
            Toplam <strong style={{ color: 'var(--text)' }}>{hosts.length}</strong> host
          </div>
          <button className="btn btn-primary btn-sm" onClick={() => setEditHost(null)}>+ Host Ekle</button>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
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
          {/* View mode toggle */}
          <div style={{ display: 'flex', border: '1px solid var(--border)', borderRadius: 'var(--radius)', overflow: 'hidden' }}>
            <button
              style={{
                padding: '5px 10px', fontSize: 12, border: 'none', cursor: 'pointer',
                background: viewMode === 'table' ? 'var(--accent-dim)' : 'var(--bg-secondary)',
                color: viewMode === 'table' ? '#fff' : 'var(--text-muted)',
              }}
              onClick={() => setViewMode('table')}
              title="Tablo görünümü"
            >
              ☰
            </button>
            <button
              style={{
                padding: '5px 10px', fontSize: 12, border: 'none', cursor: 'pointer',
                background: viewMode === 'grid' ? 'var(--accent-dim)' : 'var(--bg-secondary)',
                color: viewMode === 'grid' ? '#fff' : 'var(--text-muted)',
              }}
              onClick={() => setViewMode('grid')}
              title="Kart görünümü"
            >
              ⊞
            </button>
          </div>
          <button className="btn btn-ghost btn-sm" onClick={fetchAll}>↻ Yenile</button>
        </div>
      </div>

      {/* Host listesi */}
      {Object.entries(grouped).map(([svcName, svcHosts]) => (
        <div key={svcName} className="section">
          <div className="section-title">
            <span className={`chip ${svcName.toLowerCase().split('-')[0].trim()}`}>{svcName}</span>
            <span style={{ fontSize: 12, color: 'var(--text-muted)', fontWeight: 400 }}>
              {svcHosts.length} host
            </span>
          </div>

          {viewMode === 'table' ? (
            <div className="card">
              <div className="table-wrap">
                <table>
                  <thead>
                    <tr>
                      <th>Hostname</th>
                      <th>IP</th>
                      <th>Servis</th>
                      <th>Roller</th>
                      <th>Metrik</th>
                      <th>Jump</th>
                      <th>İşlemler</th>
                    </tr>
                  </thead>
                  <tbody>
                    {svcHosts.map(h => (
                      <HostRow
                        key={h.id}
                        host={h}
                        metrics={metrics}
                        onEdit={setEditHost}
                        onDelete={setDeleteConfirm}
                        onRefresh={fetchAll}
                      />
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          ) : (
            <div className="grid-3">
              {svcHosts.map(h => (
                <HostCard
                  key={h.id}
                  host={h}
                  metrics={metrics}
                  onEdit={setEditHost}
                  onDelete={setDeleteConfirm}
                  onRefresh={fetchAll}
                />
              ))}
            </div>
          )}
        </div>
      ))}

      {filtered.length === 0 && (
        <div className="empty">
          <p>{filter ? `"${filter}" ile eşleşen host bulunamadı` : 'Host bulunamadı'}</p>
        </div>
      )}

      {/* Edit / Add modal */}
      {editHost !== undefined && (
        <HostEditModal
          host={editHost}
          services={services}
          onSave={() => { setEditHost(undefined); fetchAll() }}
          onClose={() => setEditHost(undefined)}
        />
      )}

      {/* Delete confirm modal */}
      {deleteConfirm && (
        <div
          style={{
            position: 'fixed', inset: 0,
            background: 'rgba(0,0,0,0.7)',
            zIndex: 100,
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            padding: 24,
          }}
          onClick={e => { if (e.target === e.currentTarget) setDeleteConfirm(null) }}
        >
          <div style={{
            background: 'var(--sidebar)',
            border: '1px solid var(--danger-dim)',
            borderRadius: 'var(--radius-lg)',
            padding: 24,
            maxWidth: 400,
          }}>
            <div style={{ fontSize: 15, fontWeight: 600, color: 'var(--danger)', marginBottom: 12 }}>
              Host Silme Onayı
            </div>
            <p style={{ fontSize: 13, color: 'var(--text-muted)', marginBottom: 16 }}>
              <strong style={{ color: 'var(--text)' }}>{deleteConfirm.hostname}</strong> hostunu ve
              üzerindeki tüm daemon tanımlarını silmek istediğinize emin misiniz?
            </p>
            <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
              <button className="btn btn-ghost" onClick={() => setDeleteConfirm(null)}>İptal</button>
              <button className="btn btn-danger" onClick={() => handleDelete(deleteConfirm)}>Sil</button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
