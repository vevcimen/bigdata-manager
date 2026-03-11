import { useState, useEffect, useCallback } from 'react'
import { getServices, getService } from '../api.js'
import StatusBadge from '../components/StatusBadge.jsx'

export default function ServicesPage({ onNavigate }) {
  const [services, setServices] = useState([])
  const [selected, setSelected] = useState(null)
  const [detail,   setDetail]   = useState(null)
  const [loading,  setLoading]  = useState(true)
  const [loadingDetail, setLoadingDetail] = useState(false)
  const [error,    setError]    = useState(null)

  const fetchServices = useCallback(async () => {
    try {
      const data = await getServices()
      setServices(data)
      setError(null)
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchServices()
    const t = setInterval(fetchServices, 30_000)
    return () => clearInterval(t)
  }, [fetchServices])

  async function selectService(name) {
    setSelected(name)
    setLoadingDetail(true)
    try {
      const data = await getService(name)
      setDetail(data)
    } catch (e) {
      setDetail(null)
    } finally {
      setLoadingDetail(false)
    }
  }

  if (loading) return <div className="loading"><div className="spinner" /> Yükleniyor...</div>

  return (
    <div style={{ display: 'flex', gap: 16, height: 'calc(100vh - 100px)' }}>
      {/* ── Service list ── */}
      <div style={{ width: 320, minWidth: 320, overflowY: 'auto' }}>
        {error && <div className="error-banner">{error}</div>}
        <div className="section-title">
          <IconServices />
          {services.length} Servis
        </div>
        {services.map(s => (
          <div
            key={s.id}
            onClick={() => selectService(s.name)}
            style={{
              background: selected === s.name ? 'var(--hover)' : 'var(--card)',
              border: `1px solid ${selected === s.name ? 'var(--accent-dim)' : 'var(--border)'}`,
              borderRadius: 'var(--radius-lg)',
              padding: '14px 16px',
              marginBottom: 8,
              cursor: 'pointer',
              transition: 'all 0.15s',
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
              <div>
                <div style={{ fontWeight: 600, fontSize: 13, color: 'var(--text)', marginBottom: 4 }}>
                  {s.name}
                </div>
                <div style={{ fontSize: 11, color: 'var(--text-muted)' }}>{s.description}</div>
              </div>
              <span className={`chip ${s.type}`}>{s.type}</span>
            </div>
            <div style={{ display: 'flex', gap: 12, marginTop: 10, fontSize: 11, color: 'var(--text-subtle)' }}>
              <span>🖥 {s.host_count} host</span>
            </div>
          </div>
        ))}
      </div>

      {/* ── Service detail ── */}
      <div style={{ flex: 1, overflowY: 'auto' }}>
        {!selected && (
          <div className="empty">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
              <ellipse cx="12" cy="5" rx="9" ry="3"/>
              <path d="M21 12c0 1.66-4 3-9 3s-9-1.34-9-3"/>
              <path d="M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5"/>
            </svg>
            <p>Detay görmek için sol listeden bir servis seçin</p>
          </div>
        )}

        {selected && loadingDetail && (
          <div className="loading"><div className="spinner" /> Yükleniyor...</div>
        )}

        {selected && !loadingDetail && detail && (
          <div>
            <div className="flex items-center justify-between mb-24">
              <div>
                <h2 style={{ fontSize: 20, fontWeight: 700, color: 'var(--text)' }}>{detail.name}</h2>
                <p style={{ color: 'var(--text-muted)', fontSize: 13, marginTop: 4 }}>{detail.description}</p>
              </div>
              <div className="flex gap-8">
                <span className={`chip ${detail.type}`}>{detail.type}</span>
                <button
                  className="btn btn-primary btn-sm"
                  onClick={() => onNavigate('daemons', { service: detail.name })}
                >
                  Daemon'ları Yönet
                </button>
              </div>
            </div>

            {/* Extra info */}
            {detail.extra && Object.keys(detail.extra).length > 0 && (
              <div className="card mb-24">
                <div className="card-title">Konfigürasyon</div>
                <table style={{ fontSize: 12 }}>
                  <tbody>
                    {Object.entries(detail.extra).map(([k, v]) => (
                      <tr key={k}>
                        <td style={{ padding: '6px 0', color: 'var(--text-muted)', width: 180, fontWeight: 500 }}>
                          {k}
                        </td>
                        <td style={{ padding: '6px 0', fontFamily: 'var(--font-mono)', color: 'var(--text)' }}>
                          {typeof v === 'object' ? JSON.stringify(v) : String(v)}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}

            {/* Hosts */}
            <div className="section-title">
              <IconHosts />
              {detail.hosts?.length || 0} Host
            </div>

            {detail.hosts?.length === 0 && (
              <div className="card" style={{ color: 'var(--text-muted)', textAlign: 'center', padding: 32 }}>
                Bu servis için SSH üzerinden yönetilen host tanımlı değil.
              </div>
            )}

            <div className="grid-2">
              {detail.hosts?.map(h => (
                <div key={h.id} className="host-card">
                  <div className="host-card-header">
                    <div>
                      <div className="host-name">{h.hostname}</div>
                      <div className="host-ip">{h.ip}</div>
                    </div>
                    {h.jump_via && (
                      <span
                        style={{
                          fontSize: 10,
                          background: 'rgba(210,153,34,0.1)',
                          color: 'var(--warning)',
                          padding: '2px 6px',
                          borderRadius: 4,
                        }}
                      >
                        jump: {h.jump_via}
                      </span>
                    )}
                  </div>

                  {h.daemons?.length > 0 && (
                    <div>
                      <div style={{ fontSize: 11, color: 'var(--text-subtle)', marginBottom: 8 }}>DAEMON'LAR</div>
                      {h.daemons.map(d => (
                        <div
                          key={d.id}
                          style={{
                            display: 'flex',
                            justifyContent: 'space-between',
                            alignItems: 'center',
                            padding: '5px 0',
                            borderBottom: '1px solid var(--border)',
                          }}
                        >
                          <div>
                            <div style={{ fontSize: 12, fontFamily: 'var(--font-mono)', color: 'var(--text)' }}>
                              {d.systemctl_name}
                            </div>
                            <div style={{ fontSize: 11, color: 'var(--text-subtle)' }}>
                              {d.daemon_type}
                            </div>
                          </div>
                          <StatusBadge status={d.last_status} />
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  )
}

function IconServices() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
      <ellipse cx="12" cy="5" rx="9" ry="3"/>
      <path d="M21 12c0 1.66-4 3-9 3s-9-1.34-9-3"/>
      <path d="M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5"/>
    </svg>
  )
}

function IconHosts() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
      <rect x="2" y="2" width="20" height="8" rx="2" ry="2"/>
      <rect x="2" y="14" width="20" height="8" rx="2" ry="2"/>
      <line x1="6" y1="6" x2="6.01" y2="6"/>
      <line x1="6" y1="18" x2="6.01" y2="18"/>
    </svg>
  )
}
