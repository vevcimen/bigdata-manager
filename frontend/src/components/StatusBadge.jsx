export default function StatusBadge({ status }) {
  const s = (status || 'unknown').toLowerCase()
  let cls = 'badge-unknown'
  if (s === 'active' || s === 'running') cls = 'badge-active'
  else if (s === 'inactive' || s === 'stopped') cls = 'badge-inactive'
  else if (s === 'failed' || s === 'error') cls = 'badge-failed'
  else if (s === 'ok') cls = 'badge-ok'

  const labels = {
    active: 'Aktif', running: 'Çalışıyor', inactive: 'Pasif',
    stopped: 'Durduruldu', failed: 'Hata', error: 'Hata',
    unknown: 'Bilinmiyor', ok: 'OK',
  }

  return (
    <span className={`badge-status ${cls}`}>
      {labels[s] || status}
    </span>
  )
}
