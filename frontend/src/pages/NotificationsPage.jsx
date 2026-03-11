import { useState, useEffect, useCallback } from 'react'
import {
  getNotifications, markRead, markAllRead, deleteNotification
} from '../api.js'

const LEVEL_LABEL = {
  info:    'Bilgi',
  warning: 'Uyarı',
  error:   'Hata',
  success: 'Başarı',
}

export default function NotificationsPage({ onUnreadChange }) {
  const [notifs,   setNotifs]   = useState([])
  const [loading,  setLoading]  = useState(true)
  const [error,    setError]    = useState(null)
  const [filter,   setFilter]   = useState('all') // all | unread | info | warning | error

  const fetchNotifs = useCallback(async () => {
    try {
      const data = await getNotifications(100)
      setNotifs(data)
      const unread = data.filter(n => !n.is_read).length
      onUnreadChange?.(unread)
      setError(null)
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }, [onUnreadChange])

  useEffect(() => {
    fetchNotifs()
    const t = setInterval(fetchNotifs, 30_000)
    return () => clearInterval(t)
  }, [fetchNotifs])

  async function handleMarkRead(id) {
    await markRead(id)
    setNotifs(prev => prev.map(n => n.id === id ? { ...n, is_read: true } : n))
    const unread = notifs.filter(n => !n.is_read && n.id !== id).length
    onUnreadChange?.(unread)
  }

  async function handleMarkAll() {
    await markAllRead()
    setNotifs(prev => prev.map(n => ({ ...n, is_read: true })))
    onUnreadChange?.(0)
  }

  async function handleDelete(id) {
    await deleteNotification(id)
    const next = notifs.filter(n => n.id !== id)
    setNotifs(next)
    onUnreadChange?.(next.filter(n => !n.is_read).length)
  }

  const filtered = notifs.filter(n => {
    if (filter === 'unread') return !n.is_read
    if (filter === 'all') return true
    return n.level === filter
  })

  const unreadCount = notifs.filter(n => !n.is_read).length

  if (loading) return <div className="loading"><div className="spinner" /> Yükleniyor...</div>

  return (
    <div>
      {error && <div className="error-banner">⚠ {error}</div>}

      {/* ── Header actions ── */}
      <div className="flex items-center justify-between mb-24" style={{ gap: 12, flexWrap: 'wrap' }}>
        <div style={{ fontSize: 13, color: 'var(--text-muted)' }}>
          <strong style={{ color: 'var(--text)' }}>{notifs.length}</strong> bildirim,{' '}
          <strong style={{ color: unreadCount > 0 ? 'var(--accent)' : 'var(--text)' }}>
            {unreadCount}
          </strong>{' '}okunmamış
        </div>
        <div className="flex gap-8">
          {unreadCount > 0 && (
            <button className="btn btn-ghost btn-sm" onClick={handleMarkAll}>
              ✓ Tümünü Okundu İşaretle
            </button>
          )}
          <button className="btn btn-ghost btn-sm" onClick={fetchNotifs}>↻ Yenile</button>
        </div>
      </div>

      {/* ── Filter tabs ── */}
      <div className="tabs">
        {[
          { key: 'all',     label: `Tümü (${notifs.length})` },
          { key: 'unread',  label: `Okunmamış (${unreadCount})` },
          { key: 'error',   label: 'Hata' },
          { key: 'warning', label: 'Uyarı' },
          { key: 'info',    label: 'Bilgi' },
          { key: 'success', label: 'Başarı' },
        ].map(t => (
          <div
            key={t.key}
            className={`tab${filter === t.key ? ' active' : ''}`}
            onClick={() => setFilter(t.key)}
          >
            {t.label}
          </div>
        ))}
      </div>

      {/* ── Notification list ── */}
      <div className="card">
        {filtered.length === 0 && (
          <div className="empty">
            <p>{filter === 'unread' ? 'Okunmamış bildirim yok' : 'Bildirim bulunamadı'}</p>
          </div>
        )}

        {filtered.map(n => (
          <div
            key={n.id}
            className={`notif-item${n.is_read ? '' : ' unread'}`}
            style={{ padding: '14px 0' }}
          >
            <span className={`notif-dot ${n.is_read ? 'read' : (n.level || 'info')}`} />
            <div className="notif-body">
              <div className="flex items-center gap-8" style={{ marginBottom: 4 }}>
                <span className="notif-title">{n.title}</span>
                {n.level && (
                  <span style={{
                    fontSize: 10,
                    padding: '1px 6px',
                    borderRadius: 10,
                    fontWeight: 600,
                    background: n.level === 'error' ? 'rgba(248,81,73,0.15)'
                      : n.level === 'warning' ? 'rgba(210,153,34,0.15)'
                      : n.level === 'success' ? 'rgba(63,185,80,0.15)'
                      : 'rgba(88,166,255,0.15)',
                    color: n.level === 'error' ? 'var(--danger)'
                      : n.level === 'warning' ? 'var(--warning)'
                      : n.level === 'success' ? 'var(--success)'
                      : 'var(--accent)',
                  }}>
                    {LEVEL_LABEL[n.level] || n.level}
                  </span>
                )}
                {!n.is_read && (
                  <span style={{
                    fontSize: 10, padding: '1px 6px', borderRadius: 10,
                    background: 'rgba(88,166,255,0.15)', color: 'var(--accent)', fontWeight: 600,
                  }}>
                    Yeni
                  </span>
                )}
              </div>
              <div className="notif-message">{n.message}</div>
              <div className="notif-meta">
                {n.source && <span>{n.source} · </span>}
                {n.created_at && new Date(n.created_at).toLocaleString('tr-TR')}
              </div>
            </div>
            <div className="notif-actions">
              {!n.is_read && (
                <button
                  className="btn btn-ghost btn-sm"
                  onClick={() => handleMarkRead(n.id)}
                  title="Okundu işaretle"
                >
                  ✓
                </button>
              )}
              <button
                className="btn btn-ghost btn-sm"
                style={{ color: 'var(--danger)' }}
                onClick={() => handleDelete(n.id)}
                title="Sil"
              >
                ✕
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
