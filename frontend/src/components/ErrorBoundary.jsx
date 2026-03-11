import { Component } from 'react'

export default class ErrorBoundary extends Component {
  constructor(props) {
    super(props)
    this.state = { error: null, info: null }
  }

  static getDerivedStateFromError(error) {
    return { error }
  }

  componentDidCatch(error, info) {
    this.setState({ info })
    console.error('[ErrorBoundary]', error, info)
  }

  render() {
    if (this.state.error) {
      return (
        <div style={{
          padding: 40,
          fontFamily: 'Consolas, monospace',
          background: '#0f1117',
          color: '#e6edf3',
          minHeight: '100vh',
        }}>
          <div style={{
            maxWidth: 700,
            margin: '0 auto',
            background: '#161b22',
            border: '1px solid #f85149',
            borderRadius: 10,
            padding: 32,
          }}>
            <div style={{ fontSize: 20, fontWeight: 700, color: '#f85149', marginBottom: 8 }}>
              ✗ Uygulama Hatası
            </div>
            <div style={{ fontSize: 13, color: '#8b949e', marginBottom: 20 }}>
              React bileşeni çöktü. Aşağıdaki hata bilgisiyle backend log'larını kontrol edin.
            </div>

            <div style={{ background: '#0d1117', borderRadius: 6, padding: 16, marginBottom: 16 }}>
              <div style={{ fontSize: 12, color: '#f85149', fontWeight: 600, marginBottom: 8 }}>HATA:</div>
              <pre style={{ fontSize: 12, color: '#ff7b72', whiteSpace: 'pre-wrap', wordBreak: 'break-all', margin: 0 }}>
                {this.state.error?.toString()}
              </pre>
            </div>

            {this.state.info?.componentStack && (
              <div style={{ background: '#0d1117', borderRadius: 6, padding: 16, marginBottom: 16 }}>
                <div style={{ fontSize: 12, color: '#8b949e', fontWeight: 600, marginBottom: 8 }}>COMPONENT STACK:</div>
                <pre style={{ fontSize: 11, color: '#6e7681', whiteSpace: 'pre-wrap', margin: 0 }}>
                  {this.state.info.componentStack}
                </pre>
              </div>
            )}

            <div style={{ fontSize: 12, color: '#8b949e', marginBottom: 16 }}>
              Diagnostics API'si çalışıyor mu?{' '}
              <a
                href="/api/diag"
                target="_blank"
                style={{ color: '#58a6ff' }}
              >
                /api/diag
              </a>
              {' · '}
              <a
                href="/api/diag/logs"
                target="_blank"
                style={{ color: '#58a6ff' }}
              >
                /api/diag/logs
              </a>
              {' · '}
              <a
                href="/api/health"
                target="_blank"
                style={{ color: '#58a6ff' }}
              >
                /api/health
              </a>
            </div>

            <button
              onClick={() => this.setState({ error: null, info: null })}
              style={{
                background: 'rgba(88,166,255,0.1)',
                border: '1px solid #1f6feb',
                borderRadius: 6,
                color: '#58a6ff',
                padding: '7px 16px',
                fontSize: 13,
                cursor: 'pointer',
                marginRight: 8,
              }}
            >
              ↺ Tekrar Dene
            </button>
            <button
              onClick={() => window.location.reload()}
              style={{
                background: 'transparent',
                border: '1px solid #30363d',
                borderRadius: 6,
                color: '#8b949e',
                padding: '7px 16px',
                fontSize: 13,
                cursor: 'pointer',
              }}
            >
              Sayfayı Yenile
            </button>
          </div>
        </div>
      )
    }
    return this.props.children
  }
}
