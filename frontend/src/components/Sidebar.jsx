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
      { id: 'hosts',     label: 'Host\'lar',   icon: IconHosts },
      { id: 'daemons',   label: 'Daemon\'lar', icon: IconDaemon },
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
    ]
  },
]

export default function Sidebar({ page, onNavigate, unreadCount, backendOk }) {
  return (
    <nav className="sidebar">
      <div className="sidebar-logo">
        <IconLayers />
        BigData Manager
      </div>

      {NAV.map(section => (
        <div key={section.label} className="sidebar-section">
          <div className="sidebar-label">{section.label}</div>
          {section.items.map(item => (
            <div
              key={item.id}
              className={`sidebar-item${page === item.id ? ' active' : ''}`}
              onClick={() => onNavigate(item.id)}
            >
              <item.icon />
              {item.label}
              {item.badge && unreadCount > 0 && (
                <span className="badge">{unreadCount > 99 ? '99+' : unreadCount}</span>
              )}
            </div>
          ))}
        </div>
      ))}

      <div className="sidebar-footer">
        <div>BigData Manager</div>
        <div className="version">v1.0.0</div>
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
