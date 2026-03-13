import { useState, useEffect, useCallback } from 'react'
import { getUsers, createUser, updateUser, deleteUser } from '../api.js'

export default function UserManagement({ currentUser }) {
  const [users, setUsers]       = useState([])
  const [loading, setLoading]   = useState(true)
  const [showModal, setShowModal] = useState(false)
  const [editUser, setEditUser] = useState(null)
  const [form, setForm]         = useState({ username: '', password: '', role: 'admin' })
  const [error, setError]       = useState(null)
  const [saving, setSaving]     = useState(false)

  const fetchUsers = useCallback(async () => {
    try {
      const data = await getUsers()
      setUsers(data)
    } catch {}
    setLoading(false)
  }, [])

  useEffect(() => { fetchUsers() }, [fetchUsers])

  function openCreate() {
    setEditUser(null)
    setForm({ username: '', password: '', role: 'admin' })
    setError(null)
    setShowModal(true)
  }

  function openEdit(u) {
    setEditUser(u)
    setForm({ username: u.username, password: '', role: u.role })
    setError(null)
    setShowModal(true)
  }

  async function handleSave(e) {
    e.preventDefault()
    setSaving(true)
    setError(null)
    try {
      if (editUser) {
        const body = { username: form.username, role: form.role }
        if (form.password) body.password = form.password
        await updateUser(editUser.id, body)
      } else {
        if (!form.password) { setError('Şifre zorunlu'); setSaving(false); return }
        await createUser(form)
      }
      setShowModal(false)
      fetchUsers()
    } catch (err) {
      setError(err.message)
    }
    setSaving(false)
  }

  async function handleDelete(u) {
    if (!confirm(`"${u.username}" kullanıcısını silmek istediğinize emin misiniz?`)) return
    try {
      await deleteUser(u.id)
      fetchUsers()
    } catch (err) {
      alert(err.message)
    }
  }

  if (loading) return <div className="loading"><div className="spinner" /> Yükleniyor...</div>

  return (
    <div>
      <div className="flex items-center justify-between mb-24">
        <div className="section-title" style={{ marginBottom: 0 }}>
          <IconUsers />
          Kullanıcı Yönetimi
        </div>
        <button className="btn btn-primary" onClick={openCreate}>+ Yeni Kullanıcı</button>
      </div>

      <div className="card">
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Kullanıcı Adı</th>
                <th>Rol</th>
                <th>Oluşturulma</th>
                <th>İşlemler</th>
              </tr>
            </thead>
            <tbody>
              {users.map(u => (
                <tr key={u.id}>
                  <td className="td-muted">{u.id}</td>
                  <td style={{ fontWeight: 600 }}>{u.username}</td>
                  <td><span className="chip">{u.role}</span></td>
                  <td className="td-muted">{u.created_at ? new Date(u.created_at).toLocaleDateString('tr-TR') : '-'}</td>
                  <td>
                    <div className="btn-group">
                      <button className="btn btn-ghost btn-sm" onClick={() => openEdit(u)}>Düzenle</button>
                      {u.id !== currentUser?.id && (
                        <button className="btn btn-danger btn-sm" onClick={() => handleDelete(u)}>Sil</button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Modal */}
      {showModal && (
        <div style={{
          position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.6)', display: 'flex',
          alignItems: 'center', justifyContent: 'center', zIndex: 1000,
        }} onClick={() => setShowModal(false)}>
          <div style={{
            background: 'var(--card)', border: '1px solid var(--border)', borderRadius: 'var(--radius-lg)',
            padding: 24, width: 400, boxShadow: 'var(--shadow-lg)',
          }} onClick={e => e.stopPropagation()}>
            <h3 style={{ fontSize: 16, fontWeight: 600, marginBottom: 20, color: 'var(--text)' }}>
              {editUser ? 'Kullanıcı Düzenle' : 'Yeni Kullanıcı'}
            </h3>
            {error && <div className="error-banner" style={{ marginBottom: 12 }}>{error}</div>}
            <form onSubmit={handleSave}>
              <div style={{ marginBottom: 14 }}>
                <label style={{ display: 'block', fontSize: 12, color: 'var(--text-muted)', marginBottom: 4 }}>Kullanıcı Adı</label>
                <input
                  type="text"
                  value={form.username}
                  onChange={e => setForm(f => ({ ...f, username: e.target.value }))}
                  required
                  style={{
                    width: '100%', padding: '8px 10px', background: 'var(--bg)', border: '1px solid var(--border)',
                    borderRadius: 'var(--radius)', color: 'var(--text)', fontSize: 13,
                  }}
                />
              </div>
              <div style={{ marginBottom: 14 }}>
                <label style={{ display: 'block', fontSize: 12, color: 'var(--text-muted)', marginBottom: 4 }}>
                  {editUser ? 'Yeni Şifre (boş bırakılırsa değişmez)' : 'Şifre'}
                </label>
                <input
                  type="password"
                  value={form.password}
                  onChange={e => setForm(f => ({ ...f, password: e.target.value }))}
                  required={!editUser}
                  style={{
                    width: '100%', padding: '8px 10px', background: 'var(--bg)', border: '1px solid var(--border)',
                    borderRadius: 'var(--radius)', color: 'var(--text)', fontSize: 13,
                  }}
                />
              </div>
              <div style={{ marginBottom: 20 }}>
                <label style={{ display: 'block', fontSize: 12, color: 'var(--text-muted)', marginBottom: 4 }}>Rol</label>
                <select
                  value={form.role}
                  onChange={e => setForm(f => ({ ...f, role: e.target.value }))}
                  style={{
                    width: '100%', padding: '8px 10px', background: 'var(--bg)', border: '1px solid var(--border)',
                    borderRadius: 'var(--radius)', color: 'var(--text)', fontSize: 13,
                  }}
                >
                  <option value="admin">Admin</option>
                  <option value="viewer">Viewer</option>
                </select>
              </div>
              <div className="flex gap-8" style={{ justifyContent: 'flex-end' }}>
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

function IconUsers() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
      <circle cx="9" cy="7" r="4"/>
      <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
      <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
    </svg>
  )
}
