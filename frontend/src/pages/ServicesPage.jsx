import { useState, useEffect, useCallback, useRef } from 'react'
import { getServices, getService, createService, updateService, deleteService, getServiceDetail, exportTopology, getTrinoQueryDetail, killTrinoQuery, killSparkApp, getHdfsSchemas, getHdfsTables, analyzeHdfsTables, testHmsConnection } from '../api.js'
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
            {detail.type === 'hdfs' && <HdfsDetailPanel detail={svcDetail} loading={loadingSvcDetail} serviceName={detail.name} />}
            {detail.type === 'spark' && <SparkDetailPanel detail={svcDetail} loading={loadingSvcDetail} serviceName={detail.name} />}
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
                {extraFields.map((f, i) => {
                  const isPassword = f.key.toLowerCase().includes('password') || f.key.toLowerCase().includes('secret')
                  return (
                    <div key={i} style={{ display: 'flex', gap: 8, marginBottom: 6 }}>
                      <input type="text" value={f.key} placeholder="Anahtar" style={{ ...inputStyle, flex: 1 }}
                        onChange={e => { const nf = [...extraFields]; nf[i] = { ...nf[i], key: e.target.value }; setExtraFields(nf) }} />
                      <div style={{ flex: 2, position: 'relative', display: 'flex' }}>
                        <input
                          type={isPassword && !f.show ? 'password' : 'text'}
                          value={f.value} placeholder="Değer"
                          style={{ ...inputStyle, flex: 1, paddingRight: isPassword ? 32 : undefined }}
                          onChange={e => { const nf = [...extraFields]; nf[i] = { ...nf[i], value: e.target.value }; setExtraFields(nf) }}
                        />
                        {isPassword && (
                          <button type="button" onClick={() => { const nf = [...extraFields]; nf[i] = { ...nf[i], show: !nf[i].show }; setExtraFields(nf) }}
                            style={{ position: 'absolute', right: 6, top: '50%', transform: 'translateY(-50%)', background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-muted)', fontSize: 14, padding: 0, lineHeight: 1 }}
                            title={f.show ? 'Gizle' : 'Göster'}>
                            {f.show ? '🙈' : '👁'}
                          </button>
                        )}
                      </div>
                      <button type="button" className="btn btn-danger btn-sm"
                        onClick={() => setExtraFields(ef => ef.filter((_, j) => j !== i))}>X</button>
                    </div>
                  )
                })}
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

function HdfsDetailPanel({ detail, loading, serviceName }) {
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

      {serviceName && <HdfsTableAnalysis serviceName={serviceName} />}
    </div>
  )
}

// ─── HDFS Tablo Analizi ───────────────────────────────────────────────────────

function HdfsTableAnalysis({ serviceName }) {
  const [schemas,        setSchemas]        = useState(null)
  const [schemasLoading, setSchemasLoading] = useState(false)
  const [schemasError,   setSchemasError]   = useState(null)
  const [selectedSchema, setSelectedSchema] = useState('')
  const [tables,         setTables]         = useState(null)
  const [orphans,        setOrphans]        = useState([])
  const [tablesLoading,  setTablesLoading]  = useState(false)
  const [hmsEnabled,     setHmsEnabled]     = useState(false)
  const [hmsTestResult,  setHmsTestResult]  = useState(null)
  const [hmsTesting,     setHmsTesting]     = useState(false)
  const [checked,        setChecked]        = useState({})
  const [search,         setSearch]         = useState('')
  const [analyzing,      setAnalyzing]      = useState(false)
  const [result,         setResult]         = useState(null)
  const [analyzeError,   setAnalyzeError]   = useState(null)

  const fmtBytes = (b) => {
    if (!b && b !== 0) return '-'
    if (b === 0) return '0 B'
    const units = ['B', 'KB', 'MB', 'GB', 'TB', 'PB']
    let i = 0, v = b
    while (v >= 1024 && i < units.length - 1) { v /= 1024; i++ }
    return v.toFixed(i === 0 ? 0 : 1) + ' ' + units[i]
  }

  const loadSchemas = async () => {
    setSchemasLoading(true)
    setSchemasError(null)
    try {
      const data = await getHdfsSchemas(serviceName)
      setSchemas(data.schemas || [])
    } catch (e) {
      setSchemasError(e.message)
    } finally {
      setSchemasLoading(false)
    }
  }

  const loadTables = async (schema) => {
    setSelectedSchema(schema)
    setTables(null)
    setOrphans([])
    setChecked({})
    setSearch('')
    setResult(null)
    setAnalyzeError(null)
    setHmsEnabled(false)
    if (!schema) return
    setTablesLoading(true)
    try {
      const data = await getHdfsTables(serviceName, schema)
      setTables(data.tables || [])
      setOrphans(data.orphans || [])
      setHmsEnabled(data.hmsEnabled || false)
    } catch (e) {
      setTables([])
    } finally {
      setTablesLoading(false)
    }
  }

  // Arama filtresine uyan tablolar
  const filteredTables = (tables || []).filter(t => {
    if (!search) return true
    const q = search.toLowerCase()
    return (
      t.name.toLowerCase().includes(q) ||
      (t.hmsName && t.hmsName.toLowerCase().includes(q))
    )
  })

  const toggleAll = (val) => {
    const next = {}
    filteredTables.forEach(t => { next[t.name] = val })
    setChecked(prev => ({ ...prev, ...next }))
  }

  const analyze = async () => {
    const selected = Object.keys(checked).filter(k => checked[k])
    if (!selected.length) return
    setAnalyzing(true)
    setResult(null)
    setAnalyzeError(null)
    try {
      const data = await analyzeHdfsTables(serviceName, { schema_name: selectedSchema, tables: selected })
      setResult(data)
    } catch (e) {
      setAnalyzeError(e.message)
    } finally {
      setAnalyzing(false)
    }
  }

  const selectedCount = Object.values(checked).filter(Boolean).length

  const barColor = (pct) => {
    if (pct > 50) return 'linear-gradient(90deg,#ef4444,#f87171)'
    if (pct > 20) return 'linear-gradient(90deg,#f59e0b,#fbbf24)'
    return 'linear-gradient(90deg,#3b82f6,#60a5fa)'
  }

  // Tablo satırı display adı
  const displayName = (t) => t.hmsName && t.hmsName !== t.name ? t.hmsName : t.name
  const hasAlias    = (t) => t.hmsName && t.hmsName !== t.name

  const runHmsTest = async () => {
    setHmsTesting(true)
    setHmsTestResult(null)
    try {
      const r = await testHmsConnection(serviceName)
      setHmsTestResult(r)
    } catch (e) {
      setHmsTestResult({ ok: false, error: e.message })
    } finally {
      setHmsTesting(false)
    }
  }

  return (
    <div className="card mb-24">
      <div style={{ display:'flex', alignItems:'center', justifyContent:'space-between', marginBottom:16, flexWrap:'wrap', gap:8 }}>
        <div className="card-title" style={{ margin:0 }}>HDFS Tablo Analizi</div>
        <div style={{ display:'flex', alignItems:'center', gap:8 }}>
          {hmsEnabled && (
            <span style={{ fontSize:11, background:'#1e3a5f', color:'#60a5fa', borderRadius:4, padding:'2px 8px', fontWeight:600 }}>
              HMS ✓
            </span>
          )}
          <button className="btn btn-sm" onClick={runHmsTest} disabled={hmsTesting} title="HMS bağlantısını test et">
            {hmsTesting ? <><span className="spinner" style={{ width:10, height:10 }} /> Test…</> : 'HMS Test'}
          </button>
        </div>
      </div>
      {hmsTestResult && (
        <div style={{
          marginBottom:12, padding:'10px 14px', borderRadius:6, fontSize:12,
          background: hmsTestResult.ok ? 'rgba(34,197,94,0.08)' : 'rgba(239,68,68,0.08)',
          border: `1px solid ${hmsTestResult.ok ? 'rgba(34,197,94,0.3)' : 'rgba(239,68,68,0.3)'}`,
        }}>
          {hmsTestResult.ok ? (
            <span style={{ color:'#4ade80' }}>
              ✓ Bağlantı başarılı — {hmsTestResult.host}:{hmsTestResult.port}/{hmsTestResult.database} &nbsp;|&nbsp;
              {hmsTestResult.dbCount} veritabanı, {hmsTestResult.tblCount} tablo
            </span>
          ) : (
            <span style={{ color:'#f87171' }}>
              ✗ Bağlantı hatası: <strong>{hmsTestResult.error}</strong>
              {hmsTestResult.host && <span style={{ color:'var(--text-muted)' }}> ({hmsTestResult.host}:{hmsTestResult.port}/{hmsTestResult.database})</span>}
            </span>
          )}
        </div>
      )}

      {/* Şema seçimi */}
      {!schemas ? (
        <div style={{ marginBottom: 16 }}>
          <button className="btn" onClick={loadSchemas} disabled={schemasLoading}>
            {schemasLoading
              ? <><span className="spinner" style={{ width:12, height:12 }} /> Yükleniyor…</>
              : '⊕ Şemaları Yükle'}
          </button>
          {schemasError && <div className="error-banner" style={{ marginTop:8 }}>{schemasError}</div>}
        </div>
      ) : (
        <div style={{ display:'flex', alignItems:'center', gap:8, marginBottom:16, flexWrap:'wrap' }}>
          <div style={{ position:'relative' }}>
            <select
              style={{
                appearance:'none', background:'var(--card)', color:'var(--text)',
                border:'1px solid var(--border)', borderRadius:6,
                padding:'7px 32px 7px 12px', fontSize:13, cursor:'pointer',
                minWidth:200, outline:'none',
              }}
              value={selectedSchema}
              onChange={e => loadTables(e.target.value)}
            >
              <option value="">— Şema seçin —</option>
              {schemas.map(s => <option key={s.name} value={s.name}>{s.name}</option>)}
            </select>
            <span style={{ position:'absolute', right:10, top:'50%', transform:'translateY(-50%)', pointerEvents:'none', color:'var(--text-muted)', fontSize:11 }}>▼</span>
          </div>
          <button className="btn btn-sm" onClick={loadSchemas} disabled={schemasLoading} title="Listeyi yenile" style={{ fontSize:14 }}>↻</button>
          <span style={{ fontSize:12, color:'var(--text-muted)' }}>{schemas.length} şema</span>
        </div>
      )}

      {/* Tablo yükleniyor */}
      {tablesLoading && (
        <div style={{ color:'var(--text-muted)', fontSize:13, display:'flex', alignItems:'center', gap:8, padding:'12px 0' }}>
          <div className="spinner" /> Tablolar yükleniyor…
        </div>
      )}

      {/* Tablo listesi */}
      {tables && !tablesLoading && (
        <div>
          {tables.length === 0 ? (
            <div style={{ color:'var(--text-muted)', fontSize:13, marginBottom:12 }}>Bu şemada tablo bulunamadı.</div>
          ) : (
            <>
              {/* Arama + kontroller */}
              <div style={{ display:'flex', gap:8, marginBottom:10, alignItems:'center', flexWrap:'wrap' }}>
                <div style={{ position:'relative', flex:1, minWidth:160 }}>
                  <span style={{ position:'absolute', left:10, top:'50%', transform:'translateY(-50%)', color:'var(--text-muted)', fontSize:13, pointerEvents:'none' }}>🔍</span>
                  <input
                    type="text"
                    placeholder="Tablo ara…"
                    value={search}
                    onChange={e => setSearch(e.target.value)}
                    style={{
                      width:'100%', boxSizing:'border-box',
                      background:'var(--bg)', color:'var(--text)',
                      border:'1px solid var(--border)', borderRadius:6,
                      padding:'7px 10px 7px 30px', fontSize:13, outline:'none',
                    }}
                  />
                  {search && (
                    <button onClick={() => setSearch('')} style={{ position:'absolute', right:8, top:'50%', transform:'translateY(-50%)', background:'none', border:'none', color:'var(--text-muted)', cursor:'pointer', fontSize:14 }}>✕</button>
                  )}
                </div>
                <button className="btn btn-sm" onClick={() => toggleAll(true)}>Tümünü Seç</button>
                <button className="btn btn-sm" onClick={() => toggleAll(false)}>Temizle</button>
                <span style={{ fontSize:12, color:'var(--text-muted)', whiteSpace:'nowrap' }}>
                  {filteredTables.length}/{tables.length} tablo
                  {selectedCount > 0 && <span style={{ color:'var(--accent)', marginLeft:6 }}>{selectedCount} seçili</span>}
                </span>
              </div>

              {/* Tablo grid */}
              <div style={{
                maxHeight:260, overflowY:'auto',
                background:'var(--bg)', border:'1px solid var(--border)',
                borderRadius:8, padding:'6px 8px', marginBottom:12,
              }}>
                {filteredTables.length === 0 ? (
                  <div style={{ color:'var(--text-muted)', fontSize:12, padding:'8px 4px' }}>"{search}" ile eşleşen tablo yok.</div>
                ) : (
                  <div style={{ display:'grid', gridTemplateColumns:'repeat(auto-fill, minmax(240px, 1fr))', gap:2 }}>
                    {filteredTables.map(t => (
                      <label key={t.name} style={{
                        display:'flex', alignItems:'center', gap:7,
                        cursor:'pointer', padding:'4px 6px', borderRadius:5,
                        background: checked[t.name] ? 'rgba(59,130,246,0.12)' : 'transparent',
                        transition:'background 0.15s',
                      }}>
                        <input type="checkbox"
                          checked={!!checked[t.name]}
                          onChange={e => setChecked(prev => ({ ...prev, [t.name]: e.target.checked }))}
                          style={{ accentColor:'var(--accent)', flexShrink:0 }}
                        />
                        <span style={{ fontSize:12, overflow:'hidden', minWidth:0 }}>
                          <span style={{ fontFamily:'monospace', display:'block', overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap' }}
                            title={displayName(t)}>
                            {displayName(t)}
                          </span>
                          {hasAlias(t) && (
                            <span style={{ fontSize:10, color:'var(--text-muted)', fontFamily:'monospace', display:'block', overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap' }}
                              title={t.name}>
                              ↳ {t.name}
                            </span>
                          )}
                        </span>
                      </label>
                    ))}
                  </div>
                )}
              </div>

              <button className="btn" onClick={analyze} disabled={analyzing || selectedCount === 0}
                style={{ minWidth:140 }}>
                {analyzing
                  ? <><span className="spinner" style={{ width:12, height:12 }} /> Analiz ediliyor…</>
                  : `Analiz Et  (${selectedCount} tablo)`}
              </button>
            </>
          )}
        </div>
      )}

      {analyzeError && <div className="error-banner" style={{ marginTop:12 }}>{analyzeError}</div>}

      {/* ─── Orphan Tablo Uyarısı (tablolar yüklendikten sonra, analiz olmasa da) ── */}
      {hmsEnabled && orphans.length > 0 && (
        <div style={{ marginTop:16, background:'rgba(245,158,11,0.08)', border:'1px solid rgba(245,158,11,0.3)', borderRadius:8, padding:'12px 16px' }}>
          <div style={{ display:'flex', alignItems:'center', gap:8, marginBottom:8 }}>
            <span style={{ fontSize:16 }}>⚠</span>
            <span style={{ fontWeight:700, fontSize:13, color:'#f59e0b' }}>HDFS'de Var, HMS'de Yok — {orphans.length} Orphan Klasör</span>
          </div>
          <div style={{ display:'flex', flexWrap:'wrap', gap:6 }}>
            {orphans.map((o,i) => (
              <span key={i} style={{ background:'rgba(245,158,11,0.15)', border:'1px solid rgba(245,158,11,0.3)', borderRadius:4, padding:'2px 8px', fontSize:11, fontFamily:'monospace', color:'#fbbf24' }}>
                {o.name}
              </span>
            ))}
          </div>
        </div>
      )}

      {/* ─── Analiz Sonuçları ─────────────────────────────────────────── */}
      {result && (() => {
        const emptyTables     = result.tables.filter(t => t.isEmpty)
        const smallFileTables = result.tables.filter(t => t.smallFiles)
        const nonEmptyTables  = result.tables.filter(t => !t.isEmpty)
        const maxB            = nonEmptyTables[0]?.sizeBytes || 1

        return (
          <div style={{ marginTop:24 }}>
            {/* Özet kartlar */}
            <div style={{ display:'grid', gridTemplateColumns:'repeat(auto-fit, minmax(130px,1fr))', gap:10, marginBottom:16 }}>
              {[
                { label:'ŞEMA',          value: result.schema,    mono:true },
                { label:'TOPLAM BOYUT',  value: fmtBytes(result.totalBytes) },
                { label:'ANALİZ EDİLEN', value: `${result.tables.length} tablo` },
                { label:'TOPLAM DOSYA',  value: result.tables.reduce((s,t)=>s+t.fileCount,0).toLocaleString('tr-TR') },
                { label:'BOŞ TABLO',     value: emptyTables.length, warn: emptyTables.length > 0 },
                { label:'KÜÇÜK DOSYA',   value: smallFileTables.length, warn: smallFileTables.length > 0 },
              ].map((c,i) => (
                <div key={i} style={{ background:'var(--bg)', borderRadius:7, padding:'10px 14px', border:`1px solid ${c.warn ? 'rgba(239,68,68,0.4)' : 'var(--border)'}` }}>
                  <div style={{ fontSize:10, color: c.warn ? '#f87171' : 'var(--text-muted)', letterSpacing:'0.05em', marginBottom:4 }}>{c.label}</div>
                  <div style={{ fontSize:14, fontWeight:700, fontFamily: c.mono ? 'monospace' : undefined, color: c.warn ? '#f87171' : undefined }}>{c.value}</div>
                </div>
              ))}
            </div>

            {/* Bar chart başlık satırı */}
            <div style={{ display:'flex', alignItems:'center', gap:10, marginBottom:4, paddingLeft:6 }}>
              <div style={{ width:190, minWidth:190, fontSize:10, color:'var(--text-muted)' }}>TABLO</div>
              <div style={{ flex:1 }} />
              <div style={{ width:72, textAlign:'right', fontSize:10, color:'var(--text-muted)' }}>BOYUT</div>
              <div style={{ width:72, textAlign:'right', fontSize:10, color:'var(--text-muted)' }}>DOSYA SAYISI</div>
              <div style={{ width:72, textAlign:'right', fontSize:10, color:'var(--text-muted)' }}>ORT. DOSYA</div>
              {result.hmsEnabled && <div style={{ width:64, textAlign:'right', fontSize:10, color:'var(--text-muted)' }}>PARTİSYON</div>}
              <div style={{ width:40, textAlign:'right', fontSize:10, color:'var(--text-muted)' }}>% (SEÇİLEN)</div>
            </div>
            {/* Bar chart */}
            <div style={{ display:'flex', flexDirection:'column', gap:6 }}>
              {result.tables.map((t, i) => {
                const pct      = maxB > 0 ? (t.sizeBytes / maxB * 100) : 0
                const totalPct = result.totalBytes > 0 ? (t.sizeBytes / result.totalBytes * 100) : 0
                const label    = t.hmsName && t.hmsName !== t.table ? t.hmsName : t.table
                const subLabel = t.hmsName && t.hmsName !== t.table ? t.table : null

                return (
                  <div key={i} style={{
                    display:'flex', alignItems:'center', gap:10,
                    background: t.isEmpty ? 'rgba(239,68,68,0.06)' : t.smallFiles ? 'rgba(245,158,11,0.05)' : 'transparent',
                    borderRadius:6, padding:'3px 6px',
                    border: t.isEmpty ? '1px solid rgba(239,68,68,0.2)' : t.smallFiles ? '1px solid rgba(245,158,11,0.15)' : '1px solid transparent',
                  }}>
                    {/* Tablo adı */}
                    <div style={{ width:190, minWidth:190 }}>
                      <div style={{ display:'flex', alignItems:'center', gap:5 }}>
                        <span style={{ fontSize:12, fontFamily:'monospace', overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap', flex:1, color:'var(--text)' }} title={label}>{label}</span>
                        {t.isEmpty    && <span style={{ fontSize:10, background:'rgba(239,68,68,0.2)', color:'#f87171', borderRadius:3, padding:'0 4px', whiteSpace:'nowrap', flexShrink:0 }}>Boş</span>}
                        {t.smallFiles && <span style={{ fontSize:10, background:'rgba(245,158,11,0.2)', color:'#fbbf24', borderRadius:3, padding:'0 4px', whiteSpace:'nowrap', flexShrink:0 }}>⚠ Küçük Dosya</span>}
                      </div>
                      {subLabel && <div style={{ fontSize:10, fontFamily:'monospace', color:'var(--text-muted)', overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap' }}>↳ {subLabel}</div>}
                    </div>
                    {/* Bar */}
                    <div style={{ flex:1, background:'var(--border)', borderRadius:4, height:16, overflow:'hidden', minWidth:0 }}>
                      {!t.isEmpty && (
                        <div style={{
                          width: pct + '%', height:'100%',
                          background: barColor(totalPct),
                          borderRadius:4,
                          transition:'width 0.5s cubic-bezier(0.4,0,0.2,1)',
                          minWidth: pct > 0 ? 4 : 0,
                        }} />
                      )}
                    </div>
                    {/* Boyut */}
                    <div style={{ width:72, textAlign:'right', fontSize:12, fontWeight:600, whiteSpace:'nowrap', color: t.isEmpty ? '#f87171' : undefined }}>
                      {t.isEmpty ? '—' : fmtBytes(t.sizeBytes)}
                    </div>
                    {/* Dosya sayısı */}
                    <div style={{ width:72, textAlign:'right', fontSize:11, color:'var(--text-muted)', whiteSpace:'nowrap' }}>
                      {t.fileCount.toLocaleString('tr-TR')} dosya
                    </div>
                    {/* Ortalama dosya boyutu */}
                    <div style={{ width:72, textAlign:'right', fontSize:11, color: t.smallFiles ? '#fbbf24' : 'var(--text-muted)', whiteSpace:'nowrap' }}>
                      {t.fileCount > 0 ? 'ø ' + fmtBytes(t.avgFileBytes) : '—'}
                    </div>
                    {/* Partition sayısı */}
                    {result.hmsEnabled && (
                      <div style={{ width:64, textAlign:'right', fontSize:11, color:'var(--text-muted)', whiteSpace:'nowrap' }}>
                        {t.partitionCount > 0 ? `${t.partitionCount.toLocaleString('tr-TR')} part.` : 'part. yok'}
                      </div>
                    )}
                    {/* Yüzde */}
                    <div style={{ width:40, textAlign:'right', fontSize:11, color:'var(--text-muted)', whiteSpace:'nowrap' }}>
                      %{totalPct.toFixed(1)}
                    </div>
                  </div>
                )
              })}
            </div>

            {/* Küçük dosya sorunu açıklaması */}
            {smallFileTables.length > 0 && (
              <div style={{ marginTop:12, fontSize:11, color:'var(--text-muted)', background:'rgba(245,158,11,0.06)', border:'1px solid rgba(245,158,11,0.2)', borderRadius:6, padding:'8px 12px' }}>
                ⚠ <strong style={{ color:'#fbbf24' }}>Küçük Dosya Sorunu</strong>: Ortalama dosya boyutu &lt;128 MB olan tablolar işaretlendi. Bu tablolarda compaction veya birleştirme işlemi önerilir.
              </div>
            )}
          </div>
        )
      })()}
    </div>
  )
}


// ─── Spark Detail Panel ──────────────────────────────────────────────────────

const SPARK_REFRESH_INTERVAL = 30

function SparkDetailPanel({ detail: initialDetail, loading: initialLoading, serviceName }) {
  const [detail, setDetail]           = useState(initialDetail)
  const [loading, setLoading]         = useState(initialLoading)
  const [countdown, setCountdown]     = useState(SPARK_REFRESH_INTERVAL)
  const [autoRefresh, setAutoRefresh] = useState(true)
  const countdownRef                  = useRef(SPARK_REFRESH_INTERVAL)
  const autoRefreshRef                = useRef(true)

  useEffect(() => { setDetail(initialDetail) }, [initialDetail])
  useEffect(() => { setLoading(initialLoading) }, [initialLoading])

  const refresh = useCallback(async () => {
    if (!serviceName) return
    setLoading(true)
    try {
      const sd = await getServiceDetail(serviceName)
      setDetail(sd)
    } catch {}
    finally {
      setLoading(false)
      countdownRef.current = SPARK_REFRESH_INTERVAL
      setCountdown(SPARK_REFRESH_INTERVAL)
    }
  }, [serviceName])

  useEffect(() => {
    const tick = setInterval(() => {
      if (!autoRefreshRef.current) return
      countdownRef.current -= 1
      setCountdown(countdownRef.current)
      if (countdownRef.current <= 0) refresh()
    }, 1000)
    return () => clearInterval(tick)
  }, [refresh])

  const toggleAutoRefresh = () => {
    const next = !autoRefreshRef.current
    autoRefreshRef.current = next
    setAutoRefresh(next)
    if (next) { countdownRef.current = SPARK_REFRESH_INTERVAL; setCountdown(SPARK_REFRESH_INTERVAL) }
  }

  const [killing, setKilling] = useState(null)

  if (loading && !detail) return <div className="loading"><div className="spinner" /> Spark verileri yükleniyor...</div>
  if (!detail?.data) return null
  const { running, completed, cluster } = detail.data

  const fmtTime = (ts) => {
    if (!ts) return '-'
    const d = typeof ts === 'number' ? new Date(ts) : new Date(ts)
    return isNaN(d) ? '-' : d.toLocaleString('tr-TR')
  }
  const fmtMem = (mb) => {
    if (!mb && mb !== 0) return '-'
    if (mb >= 1024) return (mb / 1024).toFixed(1) + ' GB'
    return mb + ' MB'
  }
  const pct = (used, total) => total > 0 ? Math.round(used / total * 100) : 0

  const handleKill = async (app) => {
    if (!window.confirm(`"${app.name}" (${app.id}) uygulamasını durdurmak istediğinize emin misiniz?`)) return
    setKilling(app.id)
    try {
      await killSparkApp(serviceName, app.id)
      setTimeout(refresh, 1500)
    } catch (e) {
      alert('Kill başarısız: ' + e.message)
    } finally {
      setKilling(null)
    }
  }

  return (
    <div>
      {/* Toolbar */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 12 }}>
        <button className="btn btn-sm" onClick={refresh} disabled={loading}>
          {loading ? <span className="spinner" style={{ width: 12, height: 12 }} /> : '↻'} Yenile
        </button>
        <button className="btn btn-sm" onClick={toggleAutoRefresh}
          style={{ background: autoRefresh ? 'var(--accent)' : undefined }}>
          {autoRefresh ? `Otomatik (${countdown}s)` : 'Otomatik: Kapalı'}
        </button>
      </div>

      {/* Cluster Kaynakları */}
      {cluster && (cluster.totalCores > 0 || cluster.workers > 0) && (
        <div className="card mb-24">
          <div className="card-title">Cluster Kaynakları</div>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(160px, 1fr))', gap: 12 }}>
            <div style={{ background: 'var(--bg)', borderRadius: 6, padding: '10px 14px' }}>
              <div style={{ fontSize: 11, color: 'var(--text-muted)', marginBottom: 4 }}>WORKER SAYISI</div>
              <div style={{ fontSize: 22, fontWeight: 700 }}>{cluster.workers}</div>
            </div>
            <div style={{ background: 'var(--bg)', borderRadius: 6, padding: '10px 14px' }}>
              <div style={{ fontSize: 11, color: 'var(--text-muted)', marginBottom: 4 }}>CPU CORE</div>
              <div style={{ fontSize: 18, fontWeight: 700 }}>{cluster.usedCores} <span style={{ fontSize: 12, color: 'var(--text-muted)', fontWeight: 400 }}>/ {cluster.totalCores} ({pct(cluster.usedCores, cluster.totalCores)}%)</span></div>
              <div style={{ background: 'var(--border)', borderRadius: 3, height: 4, marginTop: 6 }}>
                <div style={{ background: 'var(--accent)', borderRadius: 3, height: 4, width: pct(cluster.usedCores, cluster.totalCores) + '%' }} />
              </div>
            </div>
            <div style={{ background: 'var(--bg)', borderRadius: 6, padding: '10px 14px' }}>
              <div style={{ fontSize: 11, color: 'var(--text-muted)', marginBottom: 4 }}>BELLEK</div>
              <div style={{ fontSize: 18, fontWeight: 700 }}>{fmtMem(cluster.usedMemoryMB)} <span style={{ fontSize: 12, color: 'var(--text-muted)', fontWeight: 400 }}>/ {fmtMem(cluster.totalMemoryMB)} ({pct(cluster.usedMemoryMB, cluster.totalMemoryMB)}%)</span></div>
              <div style={{ background: 'var(--border)', borderRadius: 3, height: 4, marginTop: 6 }}>
                <div style={{ background: 'var(--warning, #f59e0b)', borderRadius: 3, height: 4, width: pct(cluster.usedMemoryMB, cluster.totalMemoryMB) + '%' }} />
              </div>
            </div>
            <div style={{ background: 'var(--bg)', borderRadius: 6, padding: '10px 14px' }}>
              <div style={{ fontSize: 11, color: 'var(--text-muted)', marginBottom: 4 }}>AKTİF UYGULAMA</div>
              <div style={{ fontSize: 22, fontWeight: 700 }}>{running?.length || 0}</div>
            </div>
          </div>
        </div>
      )}

      <div className="card mb-24">
        <div className="card-title">Aktif Uygulamalar ({running?.length || 0})</div>
        {(!running || running.length === 0) ? (
          <div style={{ color: 'var(--text-muted)', fontSize: 13, textAlign: 'center', padding: 20 }}>
            Çalışan uygulama yok
          </div>
        ) : (
          <div className="table-wrap">
            <table>
              <thead><tr><th>App ID</th><th>Ad</th><th>Kullanıcı</th><th>Başlangıç</th><th>Süre</th><th>Core</th><th>Mem/Exec</th><th></th></tr></thead>
              <tbody>
                {running.map((app, i) => (
                  <tr key={i}>
                    <td className="td-mono" style={{ fontSize: 11 }}>{app.id}</td>
                    <td>{app.name}</td>
                    <td className="td-muted">{app.user || '-'}</td>
                    <td className="td-muted">{fmtTime(app.startTime)}</td>
                    <td className="td-muted">{app.duration || '-'}</td>
                    <td className="td-muted">{app.cores ?? '-'}</td>
                    <td className="td-muted">{app.memoryMB ? fmtMem(app.memoryMB) : '-'}</td>
                    <td>
                      <button className="btn btn-sm" style={{ background: '#dc2626', color: '#fff', padding: '2px 8px', fontSize: 11 }}
                        onClick={() => handleKill(app)} disabled={killing === app.id}>
                        {killing === app.id ? '...' : 'Durdur'}
                      </button>
                    </td>
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
              <thead><tr><th>App ID</th><th>Ad</th><th>Kullanıcı</th><th>Başlangıç</th><th>Bitiş</th><th>Süre</th><th>Durum</th></tr></thead>
              <tbody>
                {completed.map((app, i) => (
                  <tr key={i}>
                    <td className="td-mono" style={{ fontSize: 11 }}>{app.id}</td>
                    <td>{app.name}</td>
                    <td className="td-muted">{app.user || '-'}</td>
                    <td className="td-muted">{fmtTime(app.startTime)}</td>
                    <td className="td-muted">{fmtTime(app.endTime)}</td>
                    <td className="td-muted">{app.duration || '-'}</td>
                    <td>
                      {app.completed === true
                        ? <span className="badge-status badge-active">Başarılı</span>
                        : app.completed === false
                          ? <span className="badge-status badge-failed">Başarısız</span>
                          : <span className="badge-status badge-unknown">Bilinmiyor</span>}
                    </td>
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

const REFRESH_INTERVAL = 30

function TrinoDetailPanel({ detail: initialDetail, loading: initialLoading, serviceName }) {
  const [detail, setDetail]           = useState(initialDetail)
  const [loading, setLoading]         = useState(initialLoading)
  const [countdown, setCountdown]     = useState(REFRESH_INTERVAL)
  const [autoRefresh, setAutoRefresh] = useState(true)
  const [planQuery, setPlanQuery]     = useState(null)
  const [planData, setPlanData]       = useState(null)
  const [planLoading, setPlanLoading] = useState(false)
  const [planError, setPlanError]     = useState(null)
  const [killing, setKilling]         = useState(null)
  const countdownRef                  = useRef(REFRESH_INTERVAL)
  const autoRefreshRef                = useRef(true)

  // initialDetail/loading dışarıdan ilk kez gelince senkronize et
  useEffect(() => { setDetail(initialDetail) }, [initialDetail])
  useEffect(() => { setLoading(initialLoading) }, [initialLoading])

  const refresh = useCallback(async () => {
    if (!serviceName) return
    setLoading(true)
    try {
      const sd = await getServiceDetail(serviceName)
      setDetail(sd)
    } catch {}
    finally {
      setLoading(false)
      countdownRef.current = REFRESH_INTERVAL
      setCountdown(REFRESH_INTERVAL)
    }
  }, [serviceName])

  // Countdown tick + auto-refresh
  useEffect(() => {
    const tick = setInterval(() => {
      if (!autoRefreshRef.current) return
      countdownRef.current -= 1
      setCountdown(countdownRef.current)
      if (countdownRef.current <= 0) refresh()
    }, 1000)
    return () => clearInterval(tick)
  }, [refresh])

  function toggleAutoRefresh() {
    autoRefreshRef.current = !autoRefreshRef.current
    setAutoRefresh(autoRefreshRef.current)
    if (autoRefreshRef.current) {
      countdownRef.current = REFRESH_INTERVAL
      setCountdown(REFRESH_INTERVAL)
    }
  }

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

  async function handleKill(q) {
    if (!confirm(`"${q.queryId}" sorgusunu durdurmak istediğinize emin misiniz?`)) return
    setKilling(q.queryId)
    try {
      await killTrinoQuery(serviceName, q.queryId)
    } catch (e) {
      alert('Sorgu durdurulamadı: ' + e.message)
    } finally {
      setKilling(null)
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
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
          <div className="card-title" style={{ marginBottom: 0 }}>
            Trino Çalışan Sorgular ({queries?.length || 0})
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            {autoRefresh && (
              <span style={{ fontSize: 11, color: 'var(--text-subtle)' }}>
                {countdown}s
              </span>
            )}
            <button
              className="btn btn-ghost btn-sm"
              style={{ fontSize: 11 }}
              onClick={refresh}
              disabled={loading}
            >
              {loading ? '⟳' : '↻'} Yenile
            </button>
            <button
              className="btn btn-ghost btn-sm"
              style={{ fontSize: 11, color: autoRefresh ? 'var(--accent)' : 'var(--text-muted)' }}
              onClick={toggleAutoRefresh}
            >
              {autoRefresh ? '⏸ Otomatik' : '▶ Otomatik'}
            </button>
          </div>
        </div>
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
                      <div>{_toGB(q.currentMemory)} GB</div>
                      <div style={{ fontSize: 10, color: 'var(--text-subtle)' }}>peak: {_toGB(q.peakMemory)} GB</div>
                    </td>
                    <td style={{ whiteSpace: 'nowrap', fontSize: 11 }}>
                      <div>{q.rawInputDataSize || '0B'}</div>
                      <div style={{ fontSize: 10, color: 'var(--text-subtle)' }}>
                        {q.processedRows?.toLocaleString() || 0} satır
                      </div>
                    </td>
                    <td style={{ maxWidth: 240, fontSize: 11 }}>
                      {_isExecute(q.query) ? (
                        <div>
                          <span style={{
                            fontSize: 10, background: 'rgba(210,153,34,0.15)', color: 'var(--warning)',
                            padding: '1px 6px', borderRadius: 4, marginBottom: 2, display: 'inline-block'
                          }}>Hazırlanmış Sorgu</span>
                          <div style={{ fontFamily: 'var(--font-mono)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', color: 'var(--text-subtle)' }}
                            title={q.query}>{q.query}</div>
                        </div>
                      ) : (
                        <div style={{ fontFamily: 'var(--font-mono)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}
                          title={q.query}>{q.query}</div>
                      )}
                    </td>
                    <td>
                      <div style={{ display: 'flex', gap: 4 }}>
                        <button
                          className="btn btn-ghost btn-sm"
                          style={{ fontSize: 11, padding: '2px 8px' }}
                          onClick={() => openPlan(q)}
                        >
                          Plan
                        </button>
                        <button
                          className="btn btn-danger btn-sm"
                          style={{ fontSize: 11, padding: '2px 8px' }}
                          disabled={killing === q.queryId}
                          onClick={() => handleKill(q)}
                        >
                          {killing === q.queryId ? '...' : 'Durdur'}
                        </button>
                      </div>
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
            <div>
              {detail?.expandedQuery && _isExecute(detail?.query) && (
                <div style={{ marginBottom: 12 }}>
                  <div style={{ fontSize: 11, color: 'var(--warning)', marginBottom: 6 }}>
                    ⚠ Hazırlanmış sorgu — gerçek SQL:
                  </div>
                  <pre style={{
                    background: 'var(--bg)', border: '1px solid var(--warning)', borderRadius: 'var(--radius)',
                    padding: 16, fontSize: 12, fontFamily: 'var(--font-mono)', color: 'var(--text)',
                    whiteSpace: 'pre-wrap', wordBreak: 'break-word', margin: 0,
                  }}>{detail.expandedQuery}</pre>
                </div>
              )}
              <pre style={{
                background: 'var(--bg)', border: '1px solid var(--border)', borderRadius: 'var(--radius)',
                padding: 16, fontSize: 12, fontFamily: 'var(--font-mono)', color: detail?.expandedQuery && _isExecute(detail?.query) ? 'var(--text-subtle)' : 'var(--text)',
                whiteSpace: 'pre-wrap', wordBreak: 'break-word', margin: 0,
              }}>
                {detail?.query || query.query}
              </pre>
            </div>
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

const PLAN_NODE_LABELS = {
  'Output': 'Çıktı',
  'Project': 'Projeksiyon',
  'Filter': 'Filtre',
  'TableScan': 'Tablo Tarama',
  'Aggregate': 'Gruplama',
  'Join': 'Birleştirme',
  'Sort': 'Sıralama',
  'Limit': 'Limit',
  'TopN': 'İlk N',
  'Exchange': 'Veri Transferi',
  'RemoteSource': 'Uzak Kaynak',
  'Window': 'Pencere Fonksiyonu',
  'Distinct': 'Tekil',
  'Union': 'Birleşim',
  'Values': 'Sabit Değerler',
}

function PlanNode({ node, depth = 0 }) {
  const [collapsed, setCollapsed] = useState(false)
  if (!node) return null

  const children = node.children || []
  const estimates = node.estimates?.[0]
  const details = Object.entries(node.details || {}).slice(0, 4)
  const label = PLAN_NODE_LABELS[node.name] || node.name

  const nodeColor = {
    'TableScan': 'var(--accent)', 'Join': 'var(--warning)',
    'Aggregate': '#a78bfa', 'Filter': 'var(--success)',
    'Sort': '#fb923c', 'Exchange': 'var(--text-muted)',
  }[node.name] || 'var(--text)'

  return (
    <div style={{ marginLeft: depth * 20, borderLeft: depth > 0 ? '2px solid var(--border)' : 'none', paddingLeft: depth > 0 ? 12 : 0, marginTop: 8 }}>
      <div
        style={{ cursor: children.length > 0 ? 'pointer' : 'default', display: 'flex', alignItems: 'center', gap: 8 }}
        onClick={() => children.length > 0 && setCollapsed(c => !c)}
      >
        <span style={{
          fontSize: 11, fontWeight: 700, color: nodeColor,
          background: `${nodeColor}18`, border: `1px solid ${nodeColor}40`,
          borderRadius: 4, padding: '1px 8px', whiteSpace: 'nowrap',
        }}>
          {children.length > 0 && <span style={{ marginRight: 4 }}>{collapsed ? '▶' : '▼'}</span>}
          {label}
        </span>
        {estimates && estimates.outputRowCount != null && (
          <span style={{ fontSize: 10, color: 'var(--text-subtle)' }}>
            ~{Math.round(estimates.outputRowCount).toLocaleString()} satır
            {estimates.outputSizeInBytes != null && ` · ${_fmtBytes(estimates.outputSizeInBytes)}`}
          </span>
        )}
      </div>
      {!collapsed && details.length > 0 && (
        <div style={{ fontSize: 10, color: 'var(--text-muted)', marginTop: 4, marginLeft: 8 }}>
          {details.map(([k, v], i) => (
            <div key={i} style={{ marginBottom: 2 }}>
              <span style={{ color: 'var(--text-subtle)', fontWeight: 500 }}>{k}: </span>
              <span style={{ fontFamily: 'var(--font-mono)' }}>{String(v).slice(0, 100)}</span>
            </div>
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

function _isExecute(query) {
  return query && query.trim().toUpperCase().startsWith('EXECUTE')
}

function _toGB(trinoStr) {
  if (!trinoStr || trinoStr === '0B') return '0.00'
  const units = { 'TB': 1024, 'GB': 1, 'MB': 1/1024, 'KB': 1/1024/1024, 'B': 1/1024/1024/1024 }
  for (const [unit, mult] of Object.entries(units)) {
    if (trinoStr.toUpperCase().endsWith(unit)) {
      const val = parseFloat(trinoStr) * mult
      return val.toFixed(2)
    }
  }
  return '0.00'
}

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
