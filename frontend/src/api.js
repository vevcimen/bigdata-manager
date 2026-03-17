const BASE = '/api'

export function getToken() {
  return localStorage.getItem('kasirga_token')
}
export function setToken(token) {
  if (token) localStorage.setItem('kasirga_token', token)
  else localStorage.removeItem('kasirga_token')
}

async function request(path, options = {}) {
  const token = getToken()
  const headers = { 'Content-Type': 'application/json', ...options.headers }
  if (token) headers['Authorization'] = `Bearer ${token}`
  const res = await fetch(BASE + path, { headers, ...options })
  if (res.status === 401) {
    setToken(null)
    window.dispatchEvent(new Event('auth-expired'))
  }
  if (!res.ok) {
    const text = await res.text().catch(() => '')
    throw new Error(`HTTP ${res.status}: ${text || res.statusText}`)
  }
  return res.json()
}

// ── Health ──────────────────────────────────────────────────────────────────
export const getHealth   = () => request('/health')
export const getVersion  = () => request('/version')

// ── Auth ─────────────────────────────────────────────────────────────────────
export const login = (username, password) =>
  request('/auth/login', { method: 'POST', body: JSON.stringify({ username, password }) })
export const getMe = () => request('/auth/me')
export const getUsers = () => request('/auth/users')
export const createUser = (data) => request('/auth/users', { method: 'POST', body: JSON.stringify(data) })
export const updateUser = (id, data) => request(`/auth/users/${id}`, { method: 'PUT', body: JSON.stringify(data) })
export const deleteUser = (id) => request(`/auth/users/${id}`, { method: 'DELETE' })

// ── Services ────────────────────────────────────────────────────────────────
export const getServices = () => request('/services')
export const getService  = (name) => request(`/services/${encodeURIComponent(name)}`)
export const createService = (data) => request('/services', { method: 'POST', body: JSON.stringify(data) })
export const updateService = (id, data) => request(`/services/${id}`, { method: 'PUT', body: JSON.stringify(data) })
export const deleteService = (id) => request(`/services/${id}`, { method: 'DELETE' })

// ── Topology Export ──────────────────────────────────────────────────────────
export const exportTopology = async () => {
  const token = getToken()
  const headers = token ? { 'Authorization': `Bearer ${token}` } : {}
  const res = await fetch(BASE + '/topology/export', { headers })
  if (!res.ok) throw new Error(`HTTP ${res.status}`)
  const blob = await res.blob()
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'topology.yaml'
  a.click()
  URL.revokeObjectURL(url)
}

// ── Service Detail ───────────────────────────────────────────────────────────
export const getServiceDetail = (name) => request(`/services/${encodeURIComponent(name)}/detail`)

// ── HDFS Tablo Analizi ───────────────────────────────────────────────────────
export const getHdfsSchemas    = (name) => request(`/services/${encodeURIComponent(name)}/hdfs/schemas`)
export const getHdfsTables     = (name, schema) => request(`/services/${encodeURIComponent(name)}/hdfs/schemas/${encodeURIComponent(schema)}/tables`)
export const analyzeHdfsTables = (name, data) => request(`/services/${encodeURIComponent(name)}/hdfs/analyze`, { method: 'POST', body: JSON.stringify(data) })
export const testHmsConnection = (name) => request(`/services/${encodeURIComponent(name)}/hdfs/hms-test`)
export const getTrinoQueryDetail = (serviceName, queryId) =>
  request(`/services/${encodeURIComponent(serviceName)}/trino/query/${encodeURIComponent(queryId)}`)
export const killTrinoQuery = (serviceName, queryId) =>
  request(`/services/${encodeURIComponent(serviceName)}/trino/query/${encodeURIComponent(queryId)}`, { method: 'DELETE' })
export const killSparkApp = (serviceName, appId) =>
  request(`/services/${encodeURIComponent(serviceName)}/spark/app/${encodeURIComponent(appId)}`, { method: 'DELETE' })

// ── Hosts ────────────────────────────────────────────────────────────────────
export const getHosts = () => request('/hosts')
export const getHost  = (hostname) => request(`/hosts/${encodeURIComponent(hostname)}`)
export const createHost = (data) => request('/hosts', { method: 'POST', body: JSON.stringify(data) })
export const updateHost = (id, data) => request(`/hosts/${id}`, { method: 'PUT', body: JSON.stringify(data) })
export const deleteHost = (id) => request(`/hosts/${id}`, { method: 'DELETE' })

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
