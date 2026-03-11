import { useState, useEffect, useCallback } from 'react'
import Sidebar from './components/Sidebar.jsx'
import Dashboard from './pages/Dashboard.jsx'
import ServicesPage from './pages/ServicesPage.jsx'
import HostsPage from './pages/HostsPage.jsx'
import DaemonsPage from './pages/DaemonsPage.jsx'
import MetricsPage from './pages/MetricsPage.jsx'
import NotificationsPage from './pages/NotificationsPage.jsx'
import DiagnosticsPage from './pages/DiagnosticsPage.jsx'
import { getUnreadCount } from './api.js'

const PAGE_TITLES = {
  dashboard:     { title: 'Dashboard',      sub: 'Genel durum özeti' },
  services:      { title: 'Servisler',      sub: 'Tüm big data servisleri' },
  hosts:         { title: "Host'lar",       sub: 'Sunucu listesi ve metrikleri' },
  daemons:       { title: "Daemon'lar",     sub: 'Servis daemon yönetimi' },
  metrics:       { title: 'Metrikler',      sub: 'CPU, bellek, disk istatistikleri' },
  notifications: { title: 'Bildirimler',    sub: 'Sistem uyarıları ve olaylar' },
  diagnostics:   { title: 'Diagnostics',    sub: 'Sorun giderme ve sistem durumu' },
}

export default function App() {
  const [page,            setPage]            = useState('dashboard')
  const [unreadCount,     setUnreadCount]     = useState(0)
  const [selectedService, setSelectedService] = useState(null)
  const [backendOk,       setBackendOk]       = useState(true)

  const fetchUnread = useCallback(async () => {
    try {
      const data = await getUnreadCount()
      setUnreadCount(data.count ?? 0)
      setBackendOk(true)
    } catch {
      setBackendOk(false)
    }
  }, [])

  useEffect(() => {
    fetchUnread()
    const interval = setInterval(fetchUnread, 30_000)
    return () => clearInterval(interval)
  }, [fetchUnread])

  function navigate(target, extra) {
    setPage(target)
    if (extra?.service) setSelectedService(extra.service)
    else setSelectedService(null)
  }

  const meta = PAGE_TITLES[page] || { title: page, sub: '' }

  return (
    <div className="layout">
      <Sidebar
        page={page}
        onNavigate={navigate}
        unreadCount={unreadCount}
        backendOk={backendOk}
      />
      <div className="main">
        <div className="topbar">
          <div>
            <div className="topbar-title">{meta.title}</div>
            {meta.sub && <div className="topbar-sub">{meta.sub}</div>}
          </div>
          <div className="topbar-actions">
            {!backendOk ? (
              <div
                className="refresh-indicator"
                style={{ color: 'var(--danger)', cursor: 'pointer' }}
                onClick={() => navigate('diagnostics')}
                title="Backend bağlantı hatası — Diagnostics sayfasını aç"
              >
                <span className="dot" style={{ background: 'var(--danger)', animation: 'none' }} />
                <span>Backend Bağlanamıyor</span>
                <span style={{ fontSize: 11, marginLeft: 4 }}>→ Diagnostics</span>
              </div>
            ) : (
              <div className="refresh-indicator">
                <span className="dot" />
                <span>Canlı</span>
              </div>
            )}
          </div>
        </div>
        <div className="content">
          {page === 'dashboard'     && <Dashboard onNavigate={navigate} />}
          {page === 'services'      && <ServicesPage onNavigate={navigate} />}
          {page === 'hosts'         && <HostsPage />}
          {page === 'daemons'       && <DaemonsPage selectedService={selectedService} />}
          {page === 'metrics'       && <MetricsPage />}
          {page === 'notifications' && <NotificationsPage onUnreadChange={setUnreadCount} />}
          {page === 'diagnostics'   && <DiagnosticsPage />}
        </div>
      </div>
    </div>
  )
}
