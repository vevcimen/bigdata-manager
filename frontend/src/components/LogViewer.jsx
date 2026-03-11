import { useEffect, useRef, useState } from 'react'
import { openLogStream } from '../api.js'

export default function LogViewer({ daemonId, daemonName }) {
  const [lines, setLines]       = useState([])
  const [connected, setConnected] = useState(false)
  const [error, setError]       = useState(null)
  const bottomRef = useRef(null)
  const esRef     = useRef(null)

  useEffect(() => {
    if (!daemonId) return
    setLines([])
    setError(null)
    setConnected(false)

    const es = openLogStream(daemonId, 200)
    esRef.current = es

    es.onopen = () => setConnected(true)

    es.onmessage = (evt) => {
      const line = evt.data
      setLines(prev => {
        const next = [...prev, line]
        return next.length > 2000 ? next.slice(-2000) : next
      })
    }

    es.onerror = () => {
      setError('Log akışı kesildi.')
      setConnected(false)
      es.close()
    }

    return () => { es.close() }
  }, [daemonId])

  // Auto-scroll to bottom on new lines
  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [lines])

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
        <button
          className="btn btn-ghost btn-sm"
          onClick={() => setLines([])}
        >
          Temizle
        </button>
      </div>

      {error && (
        <div className="error-banner" style={{ marginBottom: 8 }}>
          ⚠ {error}
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
