import { useEffect, useRef, useState, useCallback } from 'react'
import { openLogStream } from '../api.js'

export default function LogViewer({ daemonId, daemonName }) {
  const [lines, setLines]         = useState([])
  const [connected, setConnected] = useState(false)
  const [error, setError]         = useState(null)
  const bottomRef = useRef(null)
  const esRef     = useRef(null)
  const retryRef  = useRef(null)

  const connect = useCallback(() => {
    if (!daemonId) return
    // Önceki bağlantıyı temizle
    if (esRef.current) {
      esRef.current.close()
      esRef.current = null
    }
    if (retryRef.current) {
      clearTimeout(retryRef.current)
      retryRef.current = null
    }

    setError(null)
    setConnected(false)

    const es = openLogStream(daemonId, 200)
    esRef.current = es

    es.onopen = () => {
      setConnected(true)
      setError(null)
    }

    es.onmessage = (evt) => {
      const line = evt.data
      setLines(prev => {
        const next = [...prev, line]
        return next.length > 2000 ? next.slice(-2000) : next
      })
    }

    es.onerror = () => {
      setConnected(false)
      es.close()
      esRef.current = null
      // 3 saniye sonra otomatik yeniden bağlan
      setError('Log akışı kesildi. Yeniden bağlanılıyor...')
      retryRef.current = setTimeout(() => {
        connect()
      }, 3000)
    }
  }, [daemonId])

  useEffect(() => {
    setLines([])
    connect()

    return () => {
      if (esRef.current) esRef.current.close()
      if (retryRef.current) clearTimeout(retryRef.current)
    }
  }, [connect])

  // Auto-scroll to bottom on new lines
  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [lines])

  function handleRefresh() {
    setLines([])
    connect()
  }

  function classifyLine(line) {
    const low = line.toLowerCase()
    if (low.includes('error') || low.includes('exception') || low.includes('fatal')) return 'log-error'
    if (low.includes('warn')) return 'log-warn'
    if (low.includes('info')) return 'log-info'
    return ''
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-16">
        <div className="flex items-center gap-8">
          <span style={{ fontSize: 13, fontWeight: 600, color: 'var(--text)' }}>
            {daemonName || `Daemon #${daemonId}`} — Canlı Log
          </span>
          {connected && (
            <span className="badge-status badge-active" style={{ fontSize: 10 }}>
              Bağlı
            </span>
          )}
          {!connected && !error && (
            <span className="badge-status badge-unknown" style={{ fontSize: 10 }}>
              Bağlanıyor...
            </span>
          )}
        </div>
        <div className="btn-group">
          <button
            className="btn btn-ghost btn-sm"
            onClick={handleRefresh}
            title="Yeniden bağlan"
          >
            ↻ Yenile
          </button>
          <button
            className="btn btn-ghost btn-sm"
            onClick={() => setLines([])}
          >
            Temizle
          </button>
        </div>
      </div>

      {error && (
        <div className="error-banner" style={{ marginBottom: 8 }}>
          {error}
          <button
            className="btn btn-ghost btn-sm"
            style={{ marginLeft: 'auto' }}
            onClick={handleRefresh}
          >
            ↻ Şimdi Bağlan
          </button>
        </div>
      )}

      <div className="log-viewer">
        {lines.length === 0 && (
          <span style={{ color: 'var(--text-subtle)' }}>
            Log satırları bekleniyor...
          </span>
        )}
        {lines.map((line, i) => (
          <span key={i} className={`log-line ${classifyLine(line)}`}>
            {line + '\n'}
          </span>
        ))}
        <div ref={bottomRef} />
      </div>
    </div>
  )
}
