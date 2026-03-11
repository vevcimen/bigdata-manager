import { useState, useEffect, useCallback } from 'react'
import { getServices, getDaemons, getHosts, getNotifications, refreshMetrics } from '../api.js'
import StatusBadge from '../components/StatusBadge.jsx'

export default function Dashboard({ onNavigate }) {
  const [services,   setServices]   = useState([])
  const [daemons,    setDaemons]    = useState([])
  const [hosts,      setHosts]      = useState([])
  const [notifs,     setNotifs]     = useState([])
  const [loading,    setLoading]    = useState(true)
  const [error,      setError]      = useState(null)
  const [refreshing, setRefreshing] = useState(false)
  const [lastUpdate, setLastUpdate] = useState(null)

  const fetchAll = useCallback(async () => {
    try {
      const [s, d, h, n] = await Promise.all([
        getServices(), getDaemons(), getHosts(), getNotifications(10)
      ])
      setServices(s)
      setDaemons(d)
      setHosts(h)
      setNotifs(n)
      setLastUpdate(new Date())
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

  async function handleRefresh() {
    setRefreshing(true)
    try { await refreshMetrics() } catch {}
    await fetchAll()
    setRefreshing(false)
  }

  // ── Computed stats ──────────────────────────────────────────────────────────
  const activeCount   = daemons.filter(d => d.last_status === 'active').length
  const failedCount   = daemons.filter(d => d.last_status === 'failed').length
  const inactiveCount = daemons.filter(d => !['active','failed'].includes(d.last_status)).length
  const unreadNotifs  = notifs.filter(n => !n.is_read)

  if (loading) {
    return <div className="loading"><div className="spinner" /> Yükleniyor...</div>
  }

  return (
    <div>
      {error && (
        <div className="error-banner">
          ⚠ Backend bağlantı hatası: {error}
        </div>
      )}

      {/* ── Stat cards ── */}
      <div className="stat-grid">
        <div className="stat-card" style={{ cursor: 'pointer' }} onClick={() => onNavigate('services')}>
          <div className="label">Toplam Servis</div>
          <div className="value accent">{services.length}</div>
          <div className="sub">Büyük veri bileşenleri</div>
        </div>
        <div className="stat-card" style={{ cursor: 'pointer' }} onClick={() => onNavigate('hosts')}>
          <div className="label">Toplam Host</div>
          <div className="value accent">{hosts.length}</div>
          <div className="sub">Yönetilen sunucular</div>
        </div>
        <div className="stat-card" style={{ cursor: 'pointer' }} onClick={() => onNavigate('daemons')}>
          <div className="label">Aktif Daemon</div>
          <div className="value success">{activeCount}</div>
          <div className="sub">Toplam {daemons.length} daemon</div>
        </div>
        <div className="stat-card" style={{ cursor: 'pointer' }} onClick={() => onNavigate('daemons')}>
          <div className="label">Hata</div>
          <div className={`value ${failedCount > 0 ? 'danger' : 'success'}`}>{failedCount}</div>
          <div className="sub">Hata veren daemon</div>
        </div>
        <div className="stat-card" style={{ cursor: 'pointer' }} onClick={() => onNavigate('daemons')}>
          <div className="label">Pasif</div>
          <div className={`value ${inactiveCount > 0 ? 'warning' : 'success'}`}>{inactiveCount}</div>
          <div className="sub">Durdurulmuş daemon</div>
        </div>
        <div className="stat-card" style={{ cursor: 'pointer' }} onClick={() => onNavigate('notifications')}>
          <div className="label">Okunmamış Bildirim</div>
          <div className={`value ${unreadNotifs.length > 0 ? 'warning' : 'success'}`}>{unreadNotifs.length}</div>
          <div className="sub">Toplam {notifs.length} bildirim</div>
        </div>
      </div>

      {/* ── Actions ── */}
      <div className="flex items-center justify-between mb-24" style={{ gap: 12 }}>
        <div style={{ fontSize: 12, color: 'var(--text-subtle)' }}>
          {lastUpdate && `Son güncelleme: ${lastUpdate.toLocaleTimeString('tr-TR')}`}
        </div>
        <button
          className="btn btn-ghost"
          onClick={handleRefresh}
          disabled={refreshing}
        >
          {refreshing ? <><span className="spinner" style={{ width: 12, height: 12 }} /> Yenileniyor...</> : '↻ Metrikleri Yenile'}
        </button>
      </div>

      <div className="grid-2">
        {/* ── Services table ── */}
        <div className="card">
          <div className="card-header">
            <div className="card-title">Servisler</div>
            <button className="btn btn-ghost btn-sm" onClick={() => onNavigate('services')}>
              Tümünü Gör →
            </button>
          </div>
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Servis</th>
                  <th>Tip</th>
                  <th>Host</th>
                </tr>
              </thead>
              <tbody>
                {services.length === 0 && (
                  <tr><td colSpan={3} className="td-muted" style={{ textAlign: 'center', padding: 24 }}>
                    Servis bulunamadı
                  </td></tr>
                )}
                {services.map(s => (
                  <tr
                    key={s.id}
                    style={{ cursor: 'pointer' }}
                    onClick={() => onNavigate('daemons', { service: s.name })}
                  >
                    <td style={{ fontWeight: 500 }}>{s.name}</td>
                    <td><span className={`chip ${s.type}`}>{s.type}</span></td>
                    <td className="td-muted">{s.host_count} host</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* ── Failed / Warning daemons ── */}
        <div className="card">
          <div className="card-header">
            <div className="card-title">Sorunlu Daemon'lar</div>
            <button className="btn btn-ghost btn-sm" onClick={() => onNavigate('daemons')}>
              Tümünü Gör →
            </button>
          </div>
          {failedCount + inactiveCount === 0 ? (
            <div className="empty" style={{ padding: '24px 0' }}>
              <div style={{ fontSize: 28, marginBottom: 8 }}>✓</div>
              <p style={{ color: 'var(--success)' }}>Tüm daemon'lar sağlıklı</p>
            </div>
          ) : (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr><th>Daemon</th><th>Host</th><th>Durum</th></tr>
                </thead>
                <tbody>
                  {daemons
                    .filter(d => d.last_status !== 'active')
                    .slice(0, 8)
                    .map(d => (
                      <tr key={d.id}>
                        <td className="td-mono">{d.systemctl_name}</td>
                        <td className="td-muted">{d.host?.hostname}</td>
                        <td><StatusBadge status={d.last_status} /></td>
                      </tr>
                    ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {/* ── Recent notifications ── */}
        <div className="card">
          <div className="card-header">
            <div className="card-title">Son Bildirimler</div>
            <button className="btn btn-ghost btn-sm" onClick={() => onNavigate('notifications')}>
              Tümünü Gör →
            </button>
          </div>
          {notifs.length === 0 ? (
            <div className="empty" style={{ padding: '24px 0' }}>
              <p>Bildirim yok</p>
            </div>
          ) : (
            notifs.slice(0, 5).map(n => (
              <div key={n.id} className={`notif-item${n.is_read ? '' : ' unread'}`}>
                <span className={`notif-dot ${n.is_read ? 'read' : (n.level || 'info')}`} />
                <div className="notif-body">
                  <div className="notif-title">{n.title}</div>
                  <div className="notif-message">{n.message}</div>
                  <div className="notif-meta">
                    {n.source && <span>{n.source} · </span>}
                    {n.created_at && new Date(n.created_at).toLocaleString('tr-TR')}
                  </div>
                </div>
              </div>
            ))
          )}
        </div>

        {/* ── Hosts quick view ── */}
        <div className="card">
          <div className="card-header">
            <div className="card-title">Host'lar</div>
            <button className="btn btn-ghost btn-sm" onClick={() => onNavigate('hosts')}>
              Tümünü Gör →
            </button>
          </div>
          <div className="table-wrap">
            <table>
              <thead>
                <tr><th>Hostname</th><th>IP</th><th>Servis</th></tr>
              </thead>
              <tbody>
                {hosts.length === 0 && (
                  <tr><td colSpan={3} className="td-muted" style={{ textAlign: 'center', padding: 24 }}>
                    Host bulunamadı
                  </td></tr>
                )}
                {hosts.slice(0, 8).map(h => (
                  <tr key={h.id}>
                    <td style={{ fontWeight: 500, fontSize: 12, fontFamily: 'var(--font-mono)' }}>
                      {h.hostname}
                    </td>
                    <td className="td-mono td-muted">{h.ip}</td>
                    <td><span className={`chip ${h.service_name?.toLowerCase() || ''}`}>{h.service_name}</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  )
}
