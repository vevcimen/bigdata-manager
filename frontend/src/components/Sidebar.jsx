import { useState } from 'react'

const NAV = [
  {
    label: 'GENEL',
    items: [
      { id: 'dashboard', label: 'Dashboard', icon: IconDashboard },
      { id: 'notifications', label: 'Bildirimler', icon: IconBell, badge: true },
    ]
  },
  {
    label: 'YÖNETİM',
    items: [
      { id: 'services',  label: 'Servisler',  icon: IconServices },
      { id: 'hosts',     label: "Host'lar",   icon: IconHosts },
      { id: 'daemons',   label: "Daemon'lar", icon: IconDaemon },
    ]
  },
  {
    label: 'İZLEME',
    items: [
      { id: 'metrics',      label: 'Metrikler',    icon: IconMetrics },
    ]
  },
  {
    label: 'SİSTEM',
    items: [
      { id: 'diagnostics', label: 'Diagnostics', icon: IconDiag },
      { id: 'users', label: 'Kullanıcılar', icon: IconUsers },
    ]
  },
]

export default function Sidebar({ page, onNavigate, unreadCount, backendOk, user, onLogout }) {
  const [collapsed, setCollapsed] = useState(false)

  return (
    <nav className={`sidebar ${collapsed ? 'sidebar-collapsed' : ''}`}>
      <div className="sidebar-logo">
        <IconLayers />
        {!collapsed && <span>Kasırga</span>}
        <button
          className="sidebar-toggle"
          onClick={() => setCollapsed(c => !c)}
          title={collapsed ? 'Menüyü genişlet' : 'Menüyü daralt'}
        >
          {collapsed ? <IconMenuOpen /> : <IconMenuClose />}
        </button>
      </div>

      {NAV.map(section => (
        <div key={section.label} className="sidebar-section">
          {!collapsed && <div className="sidebar-label">{section.label}</div>}
          {section.items.map(item => (
            <div
              key={item.id}
              className={`sidebar-item${page === item.id ? ' active' : ''}`}
              onClick={() => onNavigate(item.id)}
              title={collapsed ? item.label : undefined}
            >
              <item.icon />
              {!collapsed && item.label}
              {!collapsed && item.badge && unreadCount > 0 && (
                <span className="badge">{unreadCount > 99 ? '99+' : unreadCount}</span>
              )}
              {collapsed && item.badge && unreadCount > 0 && (
                <span className="badge badge-dot" />
              )}
            </div>
          ))}
        </div>
      ))}

      <div className="sidebar-footer">
        {!collapsed ? (
          <>
            {user && (
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 8 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                  <IconUser />
                  <span style={{ fontSize: 12, color: 'var(--text)' }}>{user.username}</span>
                </div>
                <button
                  onClick={onLogout}
                  style={{
                    background: 'none', border: 'none', color: 'var(--text-muted)',
                    cursor: 'pointer', padding: '2px 6px', borderRadius: 4, fontSize: 11,
                    transition: 'all 0.15s',
                  }}
                  onMouseEnter={e => { e.target.style.color = 'var(--danger)'; e.target.style.background = 'rgba(248,81,73,0.1)' }}
                  onMouseLeave={e => { e.target.style.color = 'var(--text-muted)'; e.target.style.background = 'none' }}
                  title="Çıkış yap"
                >
                  Çıkış
                </button>
              </div>
            )}
            <div>Kasırga</div>
            <div className="version">v2026-03-12</div>
          </>
        ) : (
          <div style={{ textAlign: 'center' }}>
            {user && (
              <button
                onClick={onLogout}
                style={{
                  background: 'none', border: 'none', color: 'var(--text-muted)',
                  cursor: 'pointer', padding: 4, borderRadius: 4, display: 'flex',
                  alignItems: 'center', justifyContent: 'center', margin: '0 auto 4px',
                }}
                title="Çıkış yap"
              >
                <IconLogout />
              </button>
            )}
            <div className="version">v</div>
          </div>
        )}
      </div>
    </nav>
  )
}

// ── Icons (inline SVG) ────────────────────────────────────────────────────────

function IconLayers() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <polygon points="12 2 2 7 12 12 22 7 12 2"/>
      <polyline points="2 17 12 22 22 17"/>
      <polyline points="2 12 12 17 22 12"/>
    </svg>
  )
}

function IconMenuOpen() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <line x1="3" y1="12" x2="21" y2="12"/>
      <line x1="3" y1="6" x2="21" y2="6"/>
      <line x1="3" y1="18" x2="21" y2="18"/>
    </svg>
  )
}

function IconMenuClose() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <line x1="3" y1="12" x2="21" y2="12"/>
      <line x1="3" y1="6" x2="21" y2="6"/>
      <line x1="3" y1="18" x2="21" y2="18"/>
    </svg>
  )
}

function IconDashboard() {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/>
      <rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/>
    </svg>
  )
}

function IconBell() {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
      <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
    </svg>
  )
}

function IconServices() {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <ellipse cx="12" cy="5" rx="9" ry="3"/>
      <path d="M21 12c0 1.66-4 3-9 3s-9-1.34-9-3"/>
      <path d="M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5"/>
    </svg>
  )
}

function IconHosts() {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <rect x="2" y="2" width="20" height="8" rx="2" ry="2"/>
      <rect x="2" y="14" width="20" height="8" rx="2" ry="2"/>
      <line x1="6" y1="6" x2="6.01" y2="6"/>
      <line x1="6" y1="18" x2="6.01" y2="18"/>
    </svg>
  )
}

function IconDaemon() {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <circle cx="12" cy="12" r="3"/>
      <path d="M19.07 4.93a10 10 0 0 1 0 14.14M4.93 4.93a10 10 0 0 0 0 14.14"/>
      <path d="M15.54 8.46a5 5 0 0 1 0 7.07M8.46 8.46a5 5 0 0 0 0 7.07"/>
    </svg>
  )
}

function IconMetrics() {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <line x1="18" y1="20" x2="18" y2="10"/>
      <line x1="12" y1="20" x2="12" y2="4"/>
      <line x1="6" y1="20" x2="6" y2="14"/>
    </svg>
  )
}

function IconDiag() {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <circle cx="12" cy="12" r="10"/>
      <line x1="12" y1="8" x2="12" y2="12"/>
      <line x1="12" y1="16" x2="12.01" y2="16"/>
    </svg>
  )
}

function IconUsers() {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
      <circle cx="9" cy="7" r="4"/>
      <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
      <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
    </svg>
  )
}

function IconUser() {
  return (
    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
      <circle cx="12" cy="7" r="4"/>
    </svg>
  )
}

function IconLogout() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
      <polyline points="16 17 21 12 16 7"/>
      <line x1="21" y1="12" x2="9" y2="12"/>
    </svg>
  )
}
