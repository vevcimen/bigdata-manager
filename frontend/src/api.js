const BASE = '/api'

async function request(path, options = {}) {
  const res = await fetch(BASE + path, {
    headers: { 'Content-Type': 'application/json', ...options.headers },
    ...options,
  })
  if (!res.ok) {
    const text = await res.text().catch(() => '')
    throw new Error(`HTTP ${res.status}: ${text || res.statusText}`)
  }
  return res.json()
}

// ── Health ──────────────────────────────────────────────────────────────────
export const getHealth   = () => request('/health')
export const getVersion  = () => request('/version')

// ── Services ────────────────────────────────────────────────────────────────
export const getServices = () => request('/services')
export const getService  = (name) => request(`/services/${encodeURIComponent(name)}`)

// ── Hosts ────────────────────────────────────────────────────────────────────
export const getHosts = () => request('/hosts')
export const getHost  = (hostname) => request(`/hosts/${encodeURIComponent(hostname)}`)

// ── Daemons ──────────────────────────────────────────────────────────────────
export const getDaemons    = (serviceName) =>
  request('/daemons' + (serviceName ? `?service_name=${encodeURIComponent(serviceName)}` : ''))
export const getDaemon     = (id) => request(`/daemons/${id}`)
export const daemonAction  = (id, action) => request(`/daemons/${id}/${action}`, { method: 'POST' })
export const getDaemonEvents = (id, limit = 50) => request(`/daemons/${id}/events?limit=${limit}`)

// ── Metrics ──────────────────────────────────────────────────────────────────
export const getAllHostMetrics  = () => request('/metrics/hosts')
export const getHostMetrics    = (hostname, metric, limit = 60) => {
  const params = new URLSearchParams({ limit })
  if (metric) params.set('metric', metric)
  return request(`/metrics/hosts/${encodeURIComponent(hostname)}?${params}`)
}
export const getServiceMetrics = (serviceName) =>
  request('/metrics/services' + (serviceName ? `?service_name=${encodeURIComponent(serviceName)}` : ''))
export const refreshMetrics    = () => request('/metrics/refresh', { method: 'POST' })

// ── Notifications ─────────────────────────────────────────────────────────────
export const getNotifications  = (limit = 50) => request(`/notifications?limit=${limit}`)
export const getUnreadCount    = () => request('/notifications/unread')
export const markRead          = (id) => request(`/notifications/${id}/read`, { method: 'PATCH' })
export const markAllRead       = () => request('/notifications/read-all', { method: 'PATCH' })
export const deleteNotification = (id) => request(`/notifications/${id}`, { method: 'DELETE' })

// ── SSE log stream (returns EventSource) ────────────────────────────────────
export const openLogStream = (daemonId, lines = 200) =>
  new EventSource(`${BASE}/daemons/${daemonId}/logs?lines=${lines}`)
