import { useState, useEffect, useCallback } from 'react'
import Sidebar from './components/Sidebar.jsx'
import Dashboard from './pages/Dashboard.jsx'
import ServicesPage from './pages/ServicesPage.jsx'
import HostsPage from './pages/HostsPage.jsx'
import DaemonsPage from './pages/DaemonsPage.jsx'
import MetricsPage from './pages/MetricsPage.jsx'
import NotificationsPage from './pages/NotificationsPage.jsx'
import DiagnosticsPage from './pages/DiagnosticsPage.jsx'
import LoginPage from './pages/LoginPage.jsx'
import UserManagement from './pages/UserManagement.jsx'
import { getUnreadCount, getToken, setToken, getMe } from './api.js'

const PAGE_TITLES = {
  dashboard:     { title: 'Dashboard',      sub: 'Genel durum özeti' },
  services:      { title: 'Servisler',      sub: 'Tüm big data servisleri' },
  hosts:         { title: "Host'lar",       sub: 'Sunucu listesi ve metrikleri' },
  daemons:       { title: "Daemon'lar",     sub: 'Servis daemon yönetimi' },
  metrics:       { title: 'Metrikler',      sub: 'CPU, bellek, disk istatistikleri' },
  notifications: { title: 'Bildirimler',    sub: 'Sistem uyarıları ve olaylar' },
  diagnostics:   { title: 'Diagnostics',    sub: 'Sorun giderme ve sistem durumu' },
  users:         { title: 'Kullanıcılar',   sub: 'Kullanıcı yönetimi' },
}

export default function App() {
  const [page,            setPage]            = useState('dashboard')
  const [unreadCount,     setUnreadCount]     = useState(0)
  const [selectedService, setSelectedService] = useState(null)
  const [backendOk,       setBackendOk]       = useState(true)
  const [user,            setUser]            = useState(null)
  const [authChecked,     setAuthChecked]     = useState(false)

  // Check existing token on mount
  useEffect(() => {
    const token = getToken()
    if (token) {
      getMe().then(u => { setUser(u); setAuthChecked(true) })
        .catch(() => { setToken(null); setAuthChecked(true) })
    } else {
      setAuthChecked(true)
    }
  }, [])

  // Listen for auth-expired events
  useEffect(() => {
    function onExpired() { setUser(null) }
    window.addEventListener('auth-expired', onExpired)
    return () => window.removeEventListener('auth-expired', onExpired)
  }, [])

  const fetchUnread = useCallback(async () => {
    if (!user) return
    try {
      const data = await getUnreadCount()
      setUnreadCount(data.count ?? 0)
      setBackendOk(true)
    } catch {
      setBackendOk(false)
    }
  }, [user])

  useEffect(() => {
    if (!user) return
    fetchUnread()
    const interval = setInterval(fetchUnread, 30_000)
    return () => clearInterval(interval)
  }, [fetchUnread, user])

  function navigate(target, extra) {
    setPage(target)
    if (extra?.service) setSelectedService(extra.service)
    else setSelectedService(null)
  }

  function handleLogout() {
    setToken(null)
    setUser(null)
    setPage('dashboard')
  }

  if (!authChecked) {
    return (
      <div style={{ height: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', background: 'var(--bg)' }}>
        <div className="spinner" />
      </div>
    )
  }

  if (!user) {
    return <LoginPage onLogin={u => setUser(u)} />
  }

  const meta = PAGE_TITLES[page] || { title: page, sub: '' }

  return (
    <div className="layout">
      <Sidebar
        page={page}
        onNavigate={navigate}
        unreadCount={unreadCount}
        backendOk={backendOk}
        user={user}
        onLogout={handleLogout}
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
            <div style={{ marginLeft: 12, fontSize: 12, color: 'var(--text-muted)' }}>
              {user.username}
            </div>
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
          {page === 'users'         && <UserManagement currentUser={user} />}
        </div>
      </div>
    </div>
  )
}
