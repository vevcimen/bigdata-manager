import { useState, useEffect, useCallback } from 'react'
import { getServices, getService, createService, updateService, deleteService, getServiceDetail, exportTopology, getTrinoQueryDetail } from '../api.js'
import StatusBadge from '../components/StatusBadge.jsx'

const SERVICE_TYPES = ['hdfs', 'spark', 'kafka', 'trino', 'airflow', 'doris', 'mysql', 'zookeeper', 'pure_storage', 'kafka_connect']

export default function ServicesPage({ onNavigate }) {
  const [services, setServices] = useState([])
  const [selected, setSelected] = useState(null)
  const [detail, setDetail]     = useState(null)
  const [svcDetail, setSvcDetail] = useState(null)
  const [loading, setLoading]   = useState(true)
  const [loadingDetail, setLoadingDetail] = useState(false)
  const [loadingSvcDetail, setLoadingSvcDetail] = useState(false)
  const [error, setError]       = useState(null)
  const [showModal, setShowModal] = useState(false)
  const [editSvc, setEditSvc]   = useState(null)
  const [form, setForm]         = useState({ name: '', type: 'hdfs', description: '', extra: {} })
  const [extraFields, setExtraFields] = useState([])
  const [saving, setSaving]     = useState(false)
  const [modalError, setModalError] = useState(null)

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
    setSvcDetail(null)
    try {
      const data = await getService(name)
      setDetail(data)
      // Servis detayını da çek
      setLoadingSvcDetail(true)
      try {
        const sd = await getServiceDetail(name)
        setSvcDetail(sd)
      } catch {} finally { setLoadingSvcDetail(false) }
    } catch (e) {
      setDetail(null)
    } finally {
      setLoadingDetail(false)
    }
  }

  function openCreate() {
    setEditSvc(null)
    setForm({ name: '', type: 'hdfs', description: '', extra: {} })
    setExtraFields([])
    setModalError(null)
    setShowModal(true)
  }

  function openEdit(svc) {
    setEditSvc(svc)
    const extra = svc.extra || {}
    setForm({ name: svc.name, type: svc.type, description: svc.description || '', extra })
    setExtraFields(Object.entries(extra).map(([k, v]) => ({ key: k, value: typeof v === 'object' ? JSON.stringify(v) : String(v) })))
    setModalError(null)
    setShowModal(true)
  }

  async function handleSave(e) {
    e.preventDefault()
    setSaving(true)
    setModalError(null)
    const extraObj = {}
    extraFields.forEach(f => {
      if (f.key.trim()) {
        try { extraObj[f.key.trim()] = JSON.parse(f.value) } catch { extraObj[f.key.trim()] = f.value }
      }
    })
    try {
      if (editSvc) {
        await updateService(editSvc.id, { ...form, extra: extraObj })
      } else {
        await createService({ ...form, extra: extraObj })
      }
      setShowModal(false)
      fetchServices()
      if (selected === form.name || selected === editSvc?.name) selectService(form.name)
    } catch (err) {
      setModalError(err.message)
    }
    setSaving(false)
  }

  async function handleDelete(svc) {
    if (!confirm(`"${svc.name}" servisini ve tüm host/daemon'larını silmek istediğinize emin misiniz?`)) return
    try {
      await deleteService(svc.id)
      if (selected === svc.name) { setSelected(null); setDetail(null) }
      fetchServices()
    } catch (err) { alert(err.message) }
  }

  async function handleExport() {
    try { await exportTopology() } catch (err) { alert('Export hatası: ' + err.message) }
  }

  if (loading) return <div className="loading"><div className="spinner" /> Yükleniyor...</div>

  return (
    <div style={{ display: 'flex', gap: 16, height: 'calc(100vh - 100px)' }}>
      {/* ── Service list ── */}
      <div style={{ width: 320, minWidth: 320, overflowY: 'auto' }}>
        {error && <div className="error-banner">{error}</div>}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
          <div className="section-title" style={{ marginBottom: 0 }}>
            <IconServices />
            {services.length} Servis
          </div>
          <div className="btn-group">
            <button className="btn btn-ghost btn-sm" onClick={handleExport} title="Topology Export">Export</button>
            <button className="btn btn-primary btn-sm" onClick={openCreate}>+ Servis Ekle</button>
          </div>
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
              <span>{s.host_count} host</span>
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
                <button className="btn btn-ghost btn-sm" onClick={() => openEdit(detail)}>Düzenle</button>
                <button className="btn btn-danger btn-sm" onClick={() => handleDelete(detail)}>Sil</button>
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
                          {k === 'password' ? '••••••••' : (typeof v === 'object' ? JSON.stringify(v) : String(v))}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}

            {/* ── Servis-spesifik detay panelleri ── */}
            {detail.type === 'hdfs' && <HdfsDetailPanel detail={svcDetail} loading={loadingSvcDetail} />}
            {detail.type === 'spark' && <SparkDetailPanel detail={svcDetail} loading={loadingSvcDetail} />}
            {detail.type === 'trino' && <TrinoDetailPanel detail={svcDetail} loading={loadingSvcDetail} serviceName={detail.name} />}
            {detail.type === 'airflow' && <AirflowDetailPanel detail={svcDetail} loading={loadingSvcDetail} />}

            {/* Hosts */}
            <div className="section-title mt-24">
              <IconHosts />
              {detail.hosts?.length || 0} Host
            </div>

            {detail.hosts?.length === 0 && (
              <div className="card" style={{ color: 'var(--text-muted)', textAlign: 'center', padding: 32 }}>
                Bu servis için host tanımlı değil.
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
                      <span style={{
                        fontSize: 10, background: 'rgba(210,153,34,0.1)', color: 'var(--warning)',
                        padding: '2px 6px', borderRadius: 4,
                      }}>
                        jump: {h.jump_via}
                      </span>
                    )}
                  </div>
                  {h.daemons?.length > 0 && (
                    <div>
                      <div style={{ fontSize: 11, color: 'var(--text-subtle)', marginBottom: 8 }}>DAEMON'LAR</div>
                      {h.daemons.map(d => (
                        <div key={d.id} style={{
                          display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                          padding: '5px 0', borderBottom: '1px solid var(--border)',
                        }}>
                          <div>
                            <div style={{ fontSize: 12, fontFamily: 'var(--font-mono)', color: 'var(--text)' }}>{d.systemctl_name}</div>
                            <div style={{ fontSize: 11, color: 'var(--text-subtle)' }}>{d.daemon_type}</div>
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

      {/* ── Servis Ekleme/Düzenleme Modal ── */}
      {showModal && (
        <div style={{
          position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.6)', display: 'flex',
          alignItems: 'center', justifyContent: 'center', zIndex: 1000,
        }} onClick={() => setShowModal(false)}>
          <div style={{
            background: 'var(--card)', border: '1px solid var(--border)', borderRadius: 'var(--radius-lg)',
            padding: 24, width: 520, maxHeight: '80vh', overflowY: 'auto', boxShadow: 'var(--shadow-lg)',
          }} onClick={e => e.stopPropagation()}>
            <h3 style={{ fontSize: 16, fontWeight: 600, marginBottom: 20, color: 'var(--text)' }}>
              {editSvc ? 'Servis Düzenle' : 'Yeni Servis Ekle'}
            </h3>
            {modalError && <div className="error-banner" style={{ marginBottom: 12 }}>{modalError}</div>}
            <form onSubmit={handleSave}>
              <div style={{ marginBottom: 14 }}>
                <label style={{ display: 'block', fontSize: 12, color: 'var(--text-muted)', marginBottom: 4 }}>Servis Adı</label>
                <input type="text" value={form.name} onChange={e => setForm(f => ({ ...f, name: e.target.value }))}
                  required style={inputStyle} placeholder="ör: HDFS-İÇ" />
              </div>
              <div style={{ marginBottom: 14 }}>
                <label style={{ display: 'block', fontSize: 12, color: 'var(--text-muted)', marginBottom: 4 }}>Tip</label>
                <select value={form.type} onChange={e => setForm(f => ({ ...f, type: e.target.value }))} style={inputStyle}>
                  {SERVICE_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
                </select>
              </div>
              <div style={{ marginBottom: 14 }}>
                <label style={{ display: 'block', fontSize: 12, color: 'var(--text-muted)', marginBottom: 4 }}>Açıklama</label>
                <input type="text" value={form.description} onChange={e => setForm(f => ({ ...f, description: e.target.value }))}
                  style={inputStyle} placeholder="ör: İç ağ HDFS cluster'ı" />
              </div>

              {/* Extra fields */}
              <div style={{ marginBottom: 14 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
                  <label style={{ fontSize: 12, color: 'var(--text-muted)' }}>
                    Ek Ayarlar (webui_url, username, password vb.)
                  </label>
                  <button type="button" className="btn btn-ghost btn-sm"
                    onClick={() => setExtraFields(f => [...f, { key: '', value: '' }])}>
                    + Alan Ekle
                  </button>
                </div>
                {extraFields.map((f, i) => (
                  <div key={i} style={{ display: 'flex', gap: 8, marginBottom: 6 }}>
                    <input type="text" value={f.key} placeholder="Anahtar" style={{ ...inputStyle, flex: 1 }}
                      onChange={e => { const nf = [...extraFields]; nf[i] = { ...nf[i], key: e.target.value }; setExtraFields(nf) }} />
                    <input type={f.key === 'password' ? 'password' : 'text'} value={f.value} placeholder="Değer" style={{ ...inputStyle, flex: 2 }}
                      onChange={e => { const nf = [...extraFields]; nf[i] = { ...nf[i], value: e.target.value }; setExtraFields(nf) }} />
                    <button type="button" className="btn btn-danger btn-sm"
                      onClick={() => setExtraFields(ef => ef.filter((_, j) => j !== i))}>X</button>
                  </div>
                ))}
                {(form.type === 'trino' || form.type === 'airflow') && extraFields.length === 0 && (
                  <div style={{ fontSize: 11, color: 'var(--warning)', marginTop: 4 }}>
                    Bu servis tipi için webui_url, username ve password alanları gerekebilir.
                  </div>
                )}
              </div>

              <div className="flex gap-8" style={{ justifyContent: 'flex-end', marginTop: 20 }}>
                <button type="button" className="btn btn-ghost" onClick={() => setShowModal(false)}>İptal</button>
                <button type="submit" className="btn btn-primary" disabled={saving}>
                  {saving ? 'Kaydediliyor...' : 'Kaydet'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}

const inputStyle = {
  width: '100%', padding: '8px 10px', background: 'var(--bg)', border: '1px solid var(--border)',
  borderRadius: 'var(--radius)', color: 'var(--text)', fontSize: 13, outline: 'none',
}

// ─── HDFS Detail Panel ───────────────────────────────────────────────────────

function HdfsDetailPanel({ detail, loading }) {
  if (loading) return <div className="loading"><div className="spinner" /> HDFS verileri yükleniyor...</div>
  if (!detail?.data) return null
  const { summary, history } = detail.data

  function formatBytes(b) {
    if (!b) return '0 B'
    const units = ['B', 'KB', 'MB', 'GB', 'TB', 'PB']
    let i = 0
    let val = b
    while (val >= 1024 && i < units.length - 1) { val /= 1024; i++ }
    return val.toFixed(1) + ' ' + units[i]
  }

  return (
    <div>
      {summary && !summary.error && (
        <div className="card mb-24">
          <div className="card-title">HDFS Namenode Özet</div>
          <div className="grid-3">
            <SummaryCard label="Toplam Kapasite" value={formatBytes(summary.CapacityTotal)} />
            <SummaryCard label="Kullanılan" value={formatBytes(summary.CapacityUsed)} accent={summary.UsedPercent > 80 ? 'danger' : 'accent'} />
            <SummaryCard label="Kalan" value={formatBytes(summary.CapacityRemaining)} accent="success" />
            <SummaryCard label="Doluluk" value={`${summary.UsedPercent ?? '-'}%`} accent={summary.UsedPercent > 80 ? 'danger' : summary.UsedPercent > 60 ? 'warning' : 'success'} />
            <SummaryCard label="Toplam Blok" value={summary.BlocksTotal?.toLocaleString() ?? '-'} />
            <SummaryCard label="Toplam Dosya" value={summary.FilesTotal?.toLocaleString() ?? '-'} />
            <SummaryCard label="Canlı DataNode" value={summary.NumLiveDataNodes ?? '-'} accent="success" />
            <SummaryCard label="Ölü DataNode" value={summary.NumDeadDataNodes ?? '-'} accent={summary.NumDeadDataNodes > 0 ? 'danger' : 'success'} />
            <SummaryCard label="Bayat DataNode" value={summary.NumStaleDataNodes ?? '-'} accent={summary.NumStaleDataNodes > 0 ? 'warning' : 'success'} />
          </div>
          {summary.UsedPercent != null && (
            <div style={{ marginTop: 16 }}>
              <div style={{ fontSize: 12, color: 'var(--text-muted)', marginBottom: 6 }}>Doluluk Oranı</div>
              <div className="progress-bar-wrap" style={{ height: 12 }}>
                <div className={`progress-bar ${summary.UsedPercent > 85 ? 'high' : summary.UsedPercent > 60 ? 'medium' : 'low'}`}
                  style={{ width: `${Math.min(summary.UsedPercent, 100)}%` }} />
              </div>
            </div>
          )}
        </div>
      )}
      {summary?.error && (
        <div className="error-banner mb-24">HDFS bağlantı hatası: {summary.error}</div>
      )}
      {history && history.length > 0 && (
        <div className="card mb-24">
          <div className="card-title">Historik HDFS Doluluk</div>
          <div className="table-wrap" style={{ maxHeight: 300, overflowY: 'auto' }}>
            <table>
              <thead><tr><th>Zaman</th><th>Metrik</th><th>Değer</th></tr></thead>
              <tbody>
                {history.map((h, i) => (
                  <tr key={i}>
                    <td className="td-muted">{new Date(h.ts).toLocaleString('tr-TR')}</td>
                    <td className="td-mono">{h.metric}</td>
                    <td>{typeof h.value === 'number' ? h.value.toFixed(2) + '%' : h.value}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  )
}

// ─── Spark Detail Panel ──────────────────────────────────────────────────────

function SparkDetailPanel({ detail, loading }) {
  if (loading) return <div className="loading"><div className="spinner" /> Spark verileri yükleniyor...</div>
  if (!detail?.data) return null
  const { running, completed } = detail.data

  return (
    <div>
      <div className="card mb-24">
        <div className="card-title">Spark Running Applications ({running?.length || 0})</div>
        {(!running || running.length === 0) ? (
          <div style={{ color: 'var(--text-muted)', fontSize: 13, textAlign: 'center', padding: 20 }}>
            Çalışan uygulama yok
          </div>
        ) : (
          <div className="table-wrap">
            <table>
              <thead><tr><th>App ID</th><th>Ad</th><th>Durum</th><th>Başlangıç</th></tr></thead>
              <tbody>
                {running.map((app, i) => (
                  <tr key={i}>
                    <td className="td-mono">{app.id}</td>
                    <td>{app.name}</td>
                    <td><span className="badge-status badge-active">{app.attempts?.[0]?.completed ? 'Bitti' : 'Çalışıyor'}</span></td>
                    <td className="td-muted">{app.attempts?.[0]?.startTime ? new Date(app.attempts[0].startTime).toLocaleString('tr-TR') : '-'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <div className="card mb-24">
        <div className="card-title">Son Tamamlanan Uygulamalar ({completed?.length || 0})</div>
        {(!completed || completed.length === 0) ? (
          <div style={{ color: 'var(--text-muted)', fontSize: 13, textAlign: 'center', padding: 20 }}>
            Tamamlanan uygulama yok
          </div>
        ) : (
          <div className="table-wrap">
            <table>
              <thead><tr><th>App ID</th><th>Ad</th><th>Başlangıç</th><th>Bitiş</th></tr></thead>
              <tbody>
                {completed.map((app, i) => (
                  <tr key={i}>
                    <td className="td-mono">{app.id}</td>
                    <td>{app.name}</td>
                    <td className="td-muted">{app.attempts?.[0]?.startTime ? new Date(app.attempts[0].startTime).toLocaleString('tr-TR') : '-'}</td>
                    <td className="td-muted">{app.attempts?.[0]?.endTime ? new Date(app.attempts[0].endTime).toLocaleString('tr-TR') : '-'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}

// ─── Trino Detail Panel ──────────────────────────────────────────────────────

function TrinoDetailPanel({ detail, loading, serviceName }) {
  const [planQuery, setPlanQuery]   = useState(null)   // query obje (satır)
  const [planData, setPlanData]     = useState(null)   // backend'den gelen detay
  const [planLoading, setPlanLoading] = useState(false)
  const [planError, setPlanError]   = useState(null)

  async function openPlan(q) {
    setPlanQuery(q)
    setPlanData(null)
    setPlanError(null)
    setPlanLoading(true)
    try {
      const d = await getTrinoQueryDetail(serviceName, q.queryId)
      setPlanData(d)
    } catch (e) {
      setPlanError(e.message)
    } finally {
      setPlanLoading(false)
    }
  }

  if (loading) return <div className="loading"><div className="spinner" /> Trino verileri yükleniyor...</div>
  if (!detail?.data) return null

  const { queries, cluster, cluster_error, queries_error } = detail.data

  return (
    <div>
      {/* ── Cluster İstatistikleri ── */}
      {cluster && Object.keys(cluster).length > 0 && (
        <div className="card mb-24">
          <div className="card-title">Trino Cluster Durumu</div>
          <div className="grid-3">
            <SummaryCard label="Çalışan Sorgular"  value={cluster.runningQueries}  accent={cluster.runningQueries > 0 ? 'accent' : 'success'} />
            <SummaryCard label="Kuyruktaki Sorgular" value={cluster.queuedQueries} accent={cluster.queuedQueries > 0 ? 'warning' : 'success'} />
            <SummaryCard label="Bloklu Sorgular"   value={cluster.blockedQueries}  accent={cluster.blockedQueries > 0 ? 'danger' : 'success'} />
            <SummaryCard label="Aktif Worker"      value={cluster.activeWorkers}   accent="accent" />
            <SummaryCard label="Çalışan Driver"    value={cluster.runningDrivers}  accent="accent" />
            <SummaryCard label="Kullanılan Bellek" value={_fmtBytes(cluster.reservedMemory)} accent="accent" />
          </div>
        </div>
      )}
      {cluster_error && (
        <div className="error-banner mb-24">Cluster bilgisi alınamadı: {cluster_error}</div>
      )}

      {/* ── Çalışan Sorgular ── */}
      <div className="card mb-24">
        <div className="card-title">Trino Çalışan Sorgular ({queries?.length || 0})</div>
        {queries_error && <div className="error-banner" style={{ marginBottom: 12 }}>{queries_error}</div>}
        {(!queries || queries.length === 0) ? (
          <div style={{ color: 'var(--text-muted)', fontSize: 13, textAlign: 'center', padding: 20 }}>
            Aktif sorgu yok
          </div>
        ) : (
          <div className="table-wrap" style={{ maxHeight: 480, overflowY: 'auto' }}>
            <table>
              <thead>
                <tr>
                  <th>Query ID</th>
                  <th>Kullanıcı</th>
                  <th>Kaynak</th>
                  <th>Durum</th>
                  <th>İlerleme</th>
                  <th>Süre</th>
                  <th>CPU</th>
                  <th>Bellek</th>
                  <th>Okunan</th>
                  <th>Sorgu</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {queries.map((q, i) => (
                  <tr key={i}>
                    <td className="td-mono" style={{ fontSize: 10 }}>{q.queryId}</td>
                    <td style={{ fontWeight: 600, fontSize: 12 }}>{q.user || '-'}</td>
                    <td className="td-muted" style={{ fontSize: 11 }}>{q.source || '-'}</td>
                    <td>
                      <span className={`badge-status ${_trinoStateBadge(q.state)}`}>{q.state}</span>
                    </td>
                    <td style={{ minWidth: 110 }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                        <div className="progress-bar-wrap" style={{ height: 5, flex: 1 }}>
                          <div className="progress-bar low" style={{ width: `${q.progress || 0}%` }} />
                        </div>
                        <span style={{ fontSize: 10, color: 'var(--text-muted)', whiteSpace: 'nowrap' }}>
                          {Math.round(q.progress || 0)}%
                        </span>
                      </div>
                      <div style={{ fontSize: 10, color: 'var(--text-subtle)', marginTop: 2 }}>
                        {q.completedDrivers}/{q.totalDrivers} drv
                      </div>
                    </td>
                    <td className="td-muted" style={{ whiteSpace: 'nowrap' }}>{q.elapsedTime || '-'}</td>
                    <td className="td-muted" style={{ whiteSpace: 'nowrap' }}>{q.cpuTime || '-'}</td>
                    <td style={{ whiteSpace: 'nowrap', fontSize: 11 }}>
                      <div>{q.currentMemory || '0B'}</div>
                      <div style={{ fontSize: 10, color: 'var(--text-subtle)' }}>peak: {q.peakMemory || '0B'}</div>
                    </td>
                    <td style={{ whiteSpace: 'nowrap', fontSize: 11 }}>
                      <div>{q.rawInputDataSize || '0B'}</div>
                      <div style={{ fontSize: 10, color: 'var(--text-subtle)' }}>
                        {q.rawInputPositions?.toLocaleString() || 0} satır
                      </div>
                    </td>
                    <td style={{
                      maxWidth: 240, overflow: 'hidden', textOverflow: 'ellipsis',
                      whiteSpace: 'nowrap', fontSize: 11, fontFamily: 'var(--font-mono)',
                    }} title={q.query}>{q.query}</td>
                    <td>
                      <button
                        className="btn btn-ghost btn-sm"
                        style={{ fontSize: 11, padding: '2px 8px', whiteSpace: 'nowrap' }}
                        onClick={() => openPlan(q)}
                      >
                        Plan
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* ── Sorgu Plan Modal ── */}
      {planQuery && (
        <TrinoQueryPlanModal
          query={planQuery}
          detail={planData}
          loading={planLoading}
          error={planError}
          onClose={() => { setPlanQuery(null); setPlanData(null) }}
        />
      )}
    </div>
  )
}

// ─── Trino Sorgu Plan Modal ───────────────────────────────────────────────────

function TrinoQueryPlanModal({ query, detail, loading, error, onClose }) {
  const [tab, setTab] = useState('stats')

  return (
    <div style={{
      position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.7)',
      display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1100,
    }} onClick={onClose}>
      <div style={{
        background: 'var(--card)', border: '1px solid var(--border)',
        borderRadius: 'var(--radius-lg)', padding: 0, width: '900px', maxWidth: '95vw',
        maxHeight: '88vh', display: 'flex', flexDirection: 'column',
        boxShadow: 'var(--shadow-lg)',
      }} onClick={e => e.stopPropagation()}>

        {/* Header */}
        <div style={{
          padding: '16px 20px', borderBottom: '1px solid var(--border)',
          display: 'flex', justifyContent: 'space-between', alignItems: 'center',
        }}>
          <div>
            <div style={{ fontWeight: 700, fontSize: 14, color: 'var(--text)' }}>
              Sorgu Detayı — {query.queryId}
            </div>
            <div style={{ fontSize: 11, color: 'var(--text-muted)', marginTop: 2 }}>
              {query.user && <span style={{ marginRight: 12 }}>Kullanıcı: <strong>{query.user}</strong></span>}
              {query.source && <span>Kaynak: {query.source}</span>}
            </div>
          </div>
          <button className="btn btn-ghost btn-sm" onClick={onClose}>✕ Kapat</button>
        </div>

        {/* Tabs */}
        <div style={{ display: 'flex', gap: 0, borderBottom: '1px solid var(--border)', padding: '0 20px' }}>
          {['stats', 'sql', 'plan'].map(t => (
            <button key={t} onClick={() => setTab(t)} style={{
              background: 'none', border: 'none', cursor: 'pointer', padding: '10px 16px',
              fontSize: 12, fontWeight: 600, color: tab === t ? 'var(--accent)' : 'var(--text-muted)',
              borderBottom: tab === t ? '2px solid var(--accent)' : '2px solid transparent',
              transition: 'color 0.15s',
            }}>
              {t === 'stats' ? 'İstatistikler' : t === 'sql' ? 'SQL' : 'Sorgu Planı'}
            </button>
          ))}
        </div>

        {/* Body */}
        <div style={{ flex: 1, overflowY: 'auto', padding: 20 }}>
          {loading && <div className="loading"><div className="spinner" /> Sorgu detayı yükleniyor...</div>}
          {error && <div className="error-banner">{error}</div>}

          {!loading && !error && !detail && (
            <div style={{ color: 'var(--text-muted)', textAlign: 'center', padding: 20 }}>Veri yok</div>
          )}

          {!loading && detail && tab === 'stats' && (
            <TrinoQueryStats stats={detail.stats} warnings={detail.warnings} failureInfo={detail.failureInfo} />
          )}

          {!loading && tab === 'sql' && (
            <pre style={{
              background: 'var(--bg)', border: '1px solid var(--border)', borderRadius: 'var(--radius)',
              padding: 16, fontSize: 12, fontFamily: 'var(--font-mono)', color: 'var(--text)',
              whiteSpace: 'pre-wrap', wordBreak: 'break-word', margin: 0,
            }}>
              {detail?.query || query.query}
            </pre>
          )}

          {!loading && detail && tab === 'plan' && (
            <TrinoStagePlan stage={detail.outputStage} />
          )}
        </div>
      </div>
    </div>
  )
}

function TrinoQueryStats({ stats, warnings, failureInfo }) {
  if (!stats) return <div style={{ color: 'var(--text-muted)' }}>İstatistik yok</div>

  const rows = [
    ['Oluşturma Zamanı',    stats.createTime],
    ['Yürütme Başlangıcı',  stats.executionStartTime],
    ['Toplam Süre',         stats.elapsedTime],
    ['Kuyruk Süresi',       stats.queuedTime],
    ['Planlama Süresi',     stats.planningTime],
    ['CPU Süresi',          stats.totalCpuTime],
    ['Zamanlanmış Süre',    stats.totalScheduledTime],
    ['Bloklu Süre',         stats.totalBlockedTime],
    ['Mevcut Bellek',       stats.userMemoryReservation],
    ['Peak Kullanıcı Bellek', stats.peakUserMemoryReservation],
    ['Toplam Bellek',       stats.totalMemoryReservation],
    ['Peak Toplam Bellek',  stats.peakTotalMemoryReservation],
    ['Ham Girdi Boyutu',    stats.rawInputDataSize],
    ['Ham Girdi Satırı',    stats.rawInputPositions?.toLocaleString()],
    ['Fiziksel Girdi',      stats.physicalInputDataSize],
    ['Çıktı Boyutu',        stats.outputDataSize],
    ['Çıktı Satırı',        stats.outputPositions?.toLocaleString()],
    ['Dökülen Veri',        stats.spilledDataSize],
    ['Tamamlanan Driver',   stats.completedDrivers],
    ['Toplam Driver',       stats.totalDrivers],
    ['İlerleme',            stats.progressPercentage != null ? `${Math.round(stats.progressPercentage)}%` : null],
  ]

  return (
    <div>
      <div className="card mb-16" style={{ padding: '12px 16px' }}>
        <table style={{ width: '100%', fontSize: 12 }}>
          <tbody>
            {rows.filter(([, v]) => v != null && v !== '').map(([label, value], i) => (
              <tr key={i}>
                <td style={{ padding: '5px 0', color: 'var(--text-muted)', width: '50%', fontWeight: 500 }}>{label}</td>
                <td style={{ padding: '5px 0', fontFamily: 'var(--font-mono)', color: 'var(--text)' }}>{value}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {warnings?.length > 0 && (
        <div style={{ marginBottom: 12 }}>
          {warnings.map((w, i) => (
            <div key={i} className="error-banner" style={{ background: 'rgba(210,153,34,0.12)', borderColor: 'var(--warning)', color: 'var(--warning)', marginBottom: 6 }}>
              ⚠ {w.message || JSON.stringify(w)}
            </div>
          ))}
        </div>
      )}
      {failureInfo && (
        <div className="error-banner">
          <div style={{ fontWeight: 600, marginBottom: 4 }}>Hata: {failureInfo.type}</div>
          <div style={{ fontSize: 11, fontFamily: 'var(--font-mono)' }}>{failureInfo.message}</div>
        </div>
      )}
    </div>
  )
}

function TrinoStagePlan({ stage, depth = 0 }) {
  const [collapsed, setCollapsed] = useState(false)

  if (!stage) return (
    <div style={{ color: 'var(--text-muted)', fontSize: 13, textAlign: 'center', padding: 20 }}>
      Sorgu planı mevcut değil (sorgu henüz planlanmıyor olabilir)
    </div>
  )

  const stageId  = stage.stageId || stage.plan?.id || `stage-${depth}`
  const planRoot = stage.plan?.root
  const children = stage.subStages || []
  const stats    = stage.stageStats || {}

  return (
    <div style={{ marginLeft: depth * 20, marginBottom: 8 }}>
      <div style={{
        background: 'var(--bg)', border: '1px solid var(--border)', borderRadius: 'var(--radius)',
        padding: '10px 14px', cursor: 'pointer',
      }} onClick={() => setCollapsed(c => !c)}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <span style={{ fontSize: 10, color: 'var(--text-subtle)', fontFamily: 'var(--font-mono)' }}>
              {collapsed ? '▶' : '▼'}
            </span>
            <span style={{ fontWeight: 600, fontSize: 12, color: 'var(--accent)' }}>Stage {stageId}</span>
            {planRoot && (
              <span style={{ fontSize: 11, color: 'var(--text)' }}>{planRoot.name}</span>
            )}
          </div>
          <div style={{ display: 'flex', gap: 16, fontSize: 11, color: 'var(--text-muted)' }}>
            {stats.state && <span>{stats.state}</span>}
            {stats.totalMemoryReservation && <span>Bellek: {stats.totalMemoryReservation}</span>}
            {stats.totalCpuTime && <span>CPU: {stats.totalCpuTime}</span>}
            {stats.rawInputDataSize && <span>Girdi: {stats.rawInputDataSize}</span>}
          </div>
        </div>

        {!collapsed && planRoot && (
          <div style={{ marginTop: 10 }}>
            <PlanNode node={planRoot} />
          </div>
        )}
      </div>

      {!collapsed && children.map((child, i) => (
        <TrinoStagePlan key={i} stage={child} depth={depth + 1} />
      ))}
    </div>
  )
}

function PlanNode({ node, depth = 0 }) {
  const [collapsed, setCollapsed] = useState(false)
  if (!node) return null

  const children = node.children || []
  const estimates = node.estimates?.[0]
  const details = Object.entries(node.details || {})

  return (
    <div style={{ marginLeft: depth * 16, borderLeft: depth > 0 ? '1px solid var(--border)' : 'none', paddingLeft: depth > 0 ? 12 : 0, marginTop: 6 }}>
      <div style={{ cursor: children.length > 0 ? 'pointer' : 'default' }}
        onClick={() => children.length > 0 && setCollapsed(c => !c)}>
        <span style={{ fontSize: 11, fontWeight: 600, color: 'var(--text)', fontFamily: 'var(--font-mono)' }}>
          {children.length > 0 && <span style={{ color: 'var(--text-subtle)', marginRight: 4 }}>{collapsed ? '▶' : '▼'}</span>}
          {node.name}
        </span>
        {estimates && (
          <span style={{ fontSize: 10, color: 'var(--text-subtle)', marginLeft: 10 }}>
            ~{estimates.outputRowCount != null ? Math.round(estimates.outputRowCount).toLocaleString() : '?'} satır
            {estimates.outputSizeInBytes != null && ` / ${_fmtBytes(estimates.outputSizeInBytes)}`}
          </span>
        )}
      </div>
      {!collapsed && details.length > 0 && (
        <div style={{ fontSize: 10, color: 'var(--text-muted)', fontFamily: 'var(--font-mono)', marginTop: 2, marginLeft: 12 }}>
          {details.map(([k, v], i) => (
            <div key={i}><span style={{ color: 'var(--text-subtle)' }}>{k}:</span> {String(v).slice(0, 120)}</div>
          ))}
        </div>
      )}
      {!collapsed && children.map((child, i) => (
        <PlanNode key={i} node={child} depth={depth + 1} />
      ))}
    </div>
  )
}

// ─── Trino yardımcıları ───────────────────────────────────────────────────────

function _trinoStateBadge(state) {
  if (state === 'RUNNING')  return 'badge-active'
  if (state === 'BLOCKED')  return 'badge-unknown'
  if (state === 'FAILED')   return 'badge-failed'
  if (state === 'FINISHED') return 'badge-inactive'
  return 'badge-unknown'
}

function _fmtBytes(bytes) {
  if (!bytes && bytes !== 0) return '-'
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  let val = Number(bytes)
  let i = 0
  while (val >= 1024 && i < units.length - 1) { val /= 1024; i++ }
  return val.toFixed(1) + ' ' + units[i]
}

// ─── Airflow Detail Panel ────────────────────────────────────────────────────

function AirflowDetailPanel({ detail, loading }) {
  if (loading) return <div className="loading"><div className="spinner" /> Airflow verileri yükleniyor...</div>
  if (!detail?.data) return null
  const { dag_runs } = detail.data

  return (
    <div className="card mb-24">
      <div className="card-title">Airflow Çalışan DAG'lar ({dag_runs?.length || 0})</div>
      {(!dag_runs || dag_runs.length === 0) ? (
        <div style={{ color: 'var(--text-muted)', fontSize: 13, textAlign: 'center', padding: 20 }}>
          Çalışan DAG yok
        </div>
      ) : (
        <div className="table-wrap">
          <table>
            <thead><tr><th>DAG ID</th><th>Run ID</th><th>Durum</th><th>Başlangıç</th></tr></thead>
            <tbody>
              {dag_runs.map((r, i) => (
                <tr key={i}>
                  <td style={{ fontWeight: 600 }}>{r.dag_id}</td>
                  <td className="td-mono" style={{ fontSize: 11 }}>{r.run_id}</td>
                  <td><span className="badge-status badge-active">{r.state}</span></td>
                  <td className="td-muted">{r.start_date ? new Date(r.start_date).toLocaleString('tr-TR') : '-'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}

// ─── Yardımcı bileşenler ─────────────────────────────────────────────────────

function SummaryCard({ label, value, accent }) {
  const color = accent ? `var(--${accent})` : 'var(--text)'
  return (
    <div style={{
      background: 'var(--bg)', border: '1px solid var(--border)', borderRadius: 'var(--radius)',
      padding: '12px 14px',
    }}>
      <div style={{ fontSize: 11, color: 'var(--text-subtle)', marginBottom: 4 }}>{label}</div>
      <div style={{ fontSize: 18, fontWeight: 700, color }}>{value}</div>
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
