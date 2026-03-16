from flask import Flask, render_template, jsonify, request, session, redirect, url_for
import pymysql
import requests
import urllib3
from datetime import datetime
from decimal import Decimal
from functools import wraps
from concurrent.futures import ThreadPoolExecutor
from time import time
import threading

urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

app = Flask(__name__)
app.secret_key = 'adhsladhn31p4jp1j4x8d7ada2'

TRINO_URL = "https://localhost:8443"
ADMIN_USERS = ['bigdata', 'vahip.evcimen', 'beyza.yigit']
EXCLUDED_USERS = ['airflow']

MYSQL_CONFIG = {
    'host': 'ip',
    'port': 3306,
    'user': 'user',
    'password': 'sifre',
    'database': 'db',
    'charset': 'utf8mb4'
}

def get_db():
    """Simple connection - no pooling required"""
    return pymysql.connect(
        host=MYSQL_CONFIG['host'],
        port=MYSQL_CONFIG['port'],
        user=MYSQL_CONFIG['user'],
        password=MYSQL_CONFIG['password'],
        database=MYSQL_CONFIG['database'],
        charset=MYSQL_CONFIG['charset'],
        cursorclass=pymysql.cursors.DictCursor
    )

# Simple cache with TTL
_cache = {}
_cache_lock = threading.Lock()

def cache_get(key, ttl=3):
    """Get cached value if not expired"""
    with _cache_lock:
        if key in _cache:
            value, timestamp = _cache[key]
            if time() - timestamp < ttl:
                return value
    return None

def cache_set(key, value):
    """Store value in cache"""
    with _cache_lock:
        _cache[key] = (value, time())

# Thread pool for parallel queries
executor = ThreadPoolExecutor(max_workers=6)

def safe_num(val):
    if val is None:
        return 0
    if isinstance(val, Decimal):
        return float(val)
    return val

def is_admin():
    return session.get('user') in ADMIN_USERS

def login_required(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        if 'user' not in session:
            return redirect(url_for('login'))
        return f(*args, **kwargs)
    return decorated

def verify_ldap(username, password):
    try:
        resp = requests.get(
            f"{TRINO_URL}/v1/info",
            auth=(username, password),
            timeout=5,
            verify=False
        )
        return resp.status_code == 200
    except:
        return False

@app.route('/login', methods=['GET', 'POST'])
def login():
    error = None
    if request.method == 'POST':
        username = request.form['username']
        password = request.form['password']

        if verify_ldap(username, password):
            session['user'] = username
            session['password'] = password
            return redirect(url_for('dashboard'))
        else:
            error = 'Geçersiz kullanıcı adı veya şifre'

    return '''
    <!DOCTYPE html>
    <html>
    <head>
        <title>Trino Dashboard - Login</title>
        <style>
            body { font-family: Arial; background: #0f0f1a; color: #e0e0e0; display: flex; justify-content: center; align-item
s: center; height: 100vh; margin: 0; }
            .login-box { background: #1a1a2e; padding: 40px; border-radius: 10px; width: 300px; }
            h2 { color: #00bcd4; text-align: center; margin-bottom: 30px; }
            input { width: 100%; padding: 12px; margin: 10px 0; border: 1px solid #333; border-radius: 5px; background: #25254
2; color: #fff; box-sizing: border-box; }
            button { width: 100%; padding: 12px; background: #00bcd4; border: none; border-radius: 5px; color: #000; font-weig
ht: bold; cursor: pointer; margin-top: 20px; }
            button:hover { background: #00a0b4; }
            .error { color: #f87171; text-align: center; margin-top: 10px; }
        </style>
    </head>
    <body>
        <div class="login-box">
            <h2>🔐 Trino Dashboard</h2>
            <form method="post">
                <input type="text" name="username" placeholder="LDAP Kullanıcı Adı" required>
                <input type="password" name="password" placeholder="Şifre" required>
                <button type="submit">Giriş Yap</button>
            </form>
            ''' + (f'<p class="error">{error}</p>' if error else '') + '''
        </div>
    </body>
    </html>
    '''

@app.route('/logout')
def logout():
    session.clear()
    return redirect(url_for('login'))

@app.route('/')
@login_required
def dashboard():
    return render_template('dashboard.html', user=session.get('user'), is_admin=is_admin())

@app.route('/api/queries')
@login_required
def get_queries():
    try:
        user = session.get('user')
        password = session.get('password')
        admin = is_admin()

        # RUNNING - Trino API'den
        running = []
        try:
            headers = {'X-Trino-User': user}
            resp = requests.get(
                f"{TRINO_URL}/v1/query",
                headers=headers,
                auth=(user, password),
                timeout=10,
                verify=False
            )
            if resp.status_code == 200:
                all_queries = resp.json()

                for q in all_queries:
                    if q.get('state') != 'RUNNING':
                        continue

                    query_user = q.get('session', {}).get('user', '-')

                    if query_user in EXCLUDED_USERS:
                        continue

                    if not admin and query_user != user:
                        continue

                    query_stats = q.get('queryStats', {})

                    completed = query_stats.get('completedDrivers', 0)
                    total = query_stats.get('totalDrivers', 0)
                    progress = round((completed / total) * 100, 1) if total > 0 else 0

                    running.append({
                        'query_id': q.get('queryId', ''),
                        'query_state': 'RUNNING',
                        'user': query_user,
                        'source': q.get('session', {}).get('source', '-'),
                        'catalog': q.get('session', {}).get('catalog', '-'),
                        'query_short': q.get('query', '')[:150],
                        'wall_time': query_stats.get('elapsedTime', '-'),
                        'cpu_time': query_stats.get('totalCpuTime', '-'),
                        'memory': query_stats.get('peakUserMemoryReservation', '-'),
                        'rows': str(query_stats.get('processedRows', 0)),
                        'progress': progress
                    })
        except Exception as e:
            print(f"Trino API error: {e}")

        # Today prefix for faster queries (uses index)
        today_prefix = datetime.now().strftime('%Y%m%d') + '%'
        cache_key = f"queries_{user}_{admin}_{today_prefix[:8]}"

        # Check cache first (3 second TTL)
        cached = cache_get(cache_key, ttl=3)
        if cached:
            recent_raw, today_stats, catalog_stats, hourly_stats, slowest_queries, long_queries, top_users = cached
        else:
            # Helper functions for parallel execution
            def fetch_recent():
                conn = get_db()
                cur = conn.cursor()
                if admin:
                    cur.execute("""
                        SELECT query_id, query_state, user, source, catalog,
                               LEFT(query, 150) as query_short, cpu_time_millis,
                               wall_time_millis, peak_memory_bytes, total_rows, error_code
                        FROM trino_queries
                        WHERE user NOT IN ('airflow')
                        ORDER BY query_id DESC LIMIT 30
                    """)
                else:
                    cur.execute("""
                        SELECT query_id, query_state, user, source, catalog,
                               LEFT(query, 150) as query_short, cpu_time_millis,
                               wall_time_millis, peak_memory_bytes, total_rows, error_code
                        FROM trino_queries WHERE user = %s ORDER BY query_id DESC LIMIT 30
                    """, (user,))
                result = cur.fetchall()
                cur.close()
                conn.close()
                return result

            def fetch_today_stats():
                conn = get_db()
                cur = conn.cursor()
                if admin:
                    cur.execute("""
                        SELECT COUNT(*) as today_total,
                            SUM(CASE WHEN query_state = 'FINISHED' THEN 1 ELSE 0 END) as today_finished,
                            SUM(CASE WHEN query_state = 'FAILED' THEN 1 ELSE 0 END) as today_failed,
                            ROUND(AVG(wall_time_millis)/1000, 1) as avg_duration_sec,
                            ROUND(SUM(cpu_time_millis)/1000/60, 1) as total_cpu_min,
                            ROUND(SUM(peak_memory_bytes)/1024/1024/1024, 2) as total_memory_gb
                        FROM trino_queries
                        WHERE query_id LIKE %s AND user NOT IN ('airflow')
                    """, (today_prefix,))
                else:
                    cur.execute("""
                        SELECT COUNT(*) as today_total,
                            SUM(CASE WHEN query_state = 'FINISHED' THEN 1 ELSE 0 END) as today_finished,
                            SUM(CASE WHEN query_state = 'FAILED' THEN 1 ELSE 0 END) as today_failed,
                            ROUND(AVG(wall_time_millis)/1000, 1) as avg_duration_sec,
                            ROUND(SUM(cpu_time_millis)/1000/60, 1) as total_cpu_min,
                            ROUND(SUM(peak_memory_bytes)/1024/1024/1024, 2) as total_memory_gb
                        FROM trino_queries WHERE user = %s AND query_id LIKE %s
                    """, (user, today_prefix))
                result = cur.fetchone()
                cur.close()
                conn.close()
                return result

            def fetch_catalog_stats():
                if not admin:
                    return []
                conn = get_db()
                cur = conn.cursor()
                cur.execute("""
                    SELECT catalog, COUNT(*) as cnt, ROUND(AVG(wall_time_millis)/1000, 1) as avg_sec
                    FROM trino_queries
                    WHERE query_id LIKE %s AND user NOT IN ('airflow') AND catalog IS NOT NULL
                    GROUP BY catalog ORDER BY cnt DESC LIMIT 10
                """, (today_prefix,))
                result = cur.fetchall()
                cur.close()
                conn.close()
                return result

            def fetch_hourly_stats():
                if not admin:
                    return []
                conn = get_db()
                cur = conn.cursor()
                cur.execute("""
                    SELECT SUBSTRING(query_id, 10, 2) as hour, COUNT(*) as cnt
                    FROM trino_queries
                    WHERE query_id LIKE %s AND user NOT IN ('airflow')
                    GROUP BY SUBSTRING(query_id, 10, 2) ORDER BY hour
                """, (today_prefix,))
                result = cur.fetchall()
                cur.close()
                conn.close()
                return result

            def fetch_slowest():
                conn = get_db()
                cur = conn.cursor()
                if admin:
                    cur.execute("""
                        SELECT user, query_id, ROUND(wall_time_millis/1000, 1) as duration_sec,
                               ROUND(cpu_time_millis/1000, 1) as cpu_sec, LEFT(query, 100) as query_short
                        FROM trino_queries
                        WHERE query_id LIKE %s AND user NOT IN ('airflow')
                        ORDER BY wall_time_millis DESC LIMIT 10
                    """, (today_prefix,))
                else:
                    cur.execute("""
                        SELECT user, query_id, ROUND(wall_time_millis/1000, 1) as duration_sec,
                               ROUND(cpu_time_millis/1000, 1) as cpu_sec, LEFT(query, 100) as query_short
                        FROM trino_queries WHERE user = %s AND query_id LIKE %s
                        ORDER BY wall_time_millis DESC LIMIT 10
                    """, (user, today_prefix))
                result = cur.fetchall()
                cur.close()
                conn.close()
                return result

            def fetch_long_queries():
                conn = get_db()
                cur = conn.cursor()
                if admin:
                    cur.execute("""
                        SELECT user, query_id, query_state, ROUND(wall_time_millis/1000, 1) as duration_sec,
                               LEFT(query, 100) as query_short
                        FROM trino_queries
                        WHERE query_id LIKE %s AND user NOT IN ('airflow') AND wall_time_millis > 300000
                        ORDER BY wall_time_millis DESC LIMIT 50
                    """, (today_prefix,))
                else:
                    cur.execute("""
                        SELECT user, query_id, query_state, ROUND(wall_time_millis/1000, 1) as duration_sec,
                               LEFT(query, 100) as query_short
                        FROM trino_queries WHERE user = %s AND query_id LIKE %s AND wall_time_millis > 300000
                        ORDER BY wall_time_millis DESC LIMIT 50
                    """, (user, today_prefix))
                result = cur.fetchall()
                cur.close()
                conn.close()
                return result

            def fetch_top_users():
                if not admin:
                    return []
                conn = get_db()
                cur = conn.cursor()
                cur.execute("""
                    SELECT user, COUNT(*) as query_count,
                           SUM(CASE WHEN query_state = 'FAILED' THEN 1 ELSE 0 END) as failed_count,
                           ROUND(AVG(wall_time_millis)/1000, 1) as avg_duration,
                           ROUND(SUM(cpu_time_millis)/1000/60, 1) as total_cpu_min
                    FROM trino_queries
                    WHERE query_id LIKE %s AND user NOT IN ('airflow')
                    GROUP BY user ORDER BY query_count DESC LIMIT 10
                """, (today_prefix,))
                result = cur.fetchall()
                cur.close()
                conn.close()
                return result

            # Execute all queries in parallel
            futures = {
                'recent': executor.submit(fetch_recent),
                'today_stats': executor.submit(fetch_today_stats),
                'catalog': executor.submit(fetch_catalog_stats),
                'hourly': executor.submit(fetch_hourly_stats),
                'slowest': executor.submit(fetch_slowest),
                'long': executor.submit(fetch_long_queries),
                'top_users': executor.submit(fetch_top_users)
            }

            # Collect results
            recent_raw = futures['recent'].result(timeout=10)
            today_stats = futures['today_stats'].result(timeout=10)
            catalog_stats = futures['catalog'].result(timeout=10)
            hourly_stats = futures['hourly'].result(timeout=10)
            slowest_queries = futures['slowest'].result(timeout=10)
            long_queries = futures['long'].result(timeout=10)
            top_users = futures['top_users'].result(timeout=10)

            # Store in cache
            cache_set(cache_key, (recent_raw, today_stats, catalog_stats, hourly_stats, slowest_queries, long_queries, top_use
rs))

        def format_query(q):
            return {
                'query_id': q.get('query_id', ''),
                'query_state': q.get('query_state', ''),
                'user': q.get('user', '-'),
                'source': q.get('source', '-'),
                'catalog': q.get('catalog', '-'),
                'query_short': q.get('query_short', ''),
                'error_code': q.get('error_code', ''),
                'cpu_time': f"{safe_num(q.get('cpu_time_millis')) / 1000:.1f}s",
                'wall_time': f"{safe_num(q.get('wall_time_millis')) / 1000:.1f}s",
                'memory': f"{safe_num(q.get('peak_memory_bytes')) / 1024 / 1024:.1f} MB",
                'rows': f"{int(safe_num(q.get('total_rows'))):,}",
                'progress': 100
            }

        def format_slow(q):
            return {
                'user': q.get('user', '-'),
                'query_id': q.get('query_id', ''),
                'duration_sec': safe_num(q.get('duration_sec')),
                'cpu_sec': safe_num(q.get('cpu_sec')),
                'query_short': q.get('query_short', '')
            }

        def format_long(q):
            return {
                'user': q.get('user', '-'),
                'query_id': q.get('query_id', ''),
                'query_state': q.get('query_state', ''),
                'duration_sec': safe_num(q.get('duration_sec')),
                'query_short': q.get('query_short', '')
            }

        def format_top_user(q):
            return {
                'user': q.get('user', '-'),
                'query_count': int(safe_num(q.get('query_count'))),
                'failed_count': int(safe_num(q.get('failed_count'))),
                'avg_duration': safe_num(q.get('avg_duration')),
                'total_cpu_min': safe_num(q.get('total_cpu_min'))
            }

        def format_catalog(c):
            return {
                'catalog': c.get('catalog', '-'),
                'count': int(safe_num(c.get('cnt'))),
                'avg_sec': safe_num(c.get('avg_sec'))
            }

        def format_hourly(h):
            return {
                'hour': h.get('hour', '00'),
                'count': int(safe_num(h.get('cnt')))
            }

        # Calculate success rate
        total = int(safe_num(today_stats.get('today_total'))) if today_stats else 0
        finished = int(safe_num(today_stats.get('today_finished'))) if today_stats else 0
        success_rate = round((finished / total) * 100, 1) if total > 0 else 0

        return jsonify({
            'running': running,
            'recent': [format_query(q) for q in recent_raw],
            'today_stats': {
                'total': total,
                'finished': finished,
                'failed': int(safe_num(today_stats.get('today_failed'))) if today_stats else 0,
                'avg_duration': safe_num(today_stats.get('avg_duration_sec')) if today_stats else 0,
                'running': len(running),
                'success_rate': success_rate,
                'total_cpu_min': safe_num(today_stats.get('total_cpu_min')) if today_stats else 0,
                'total_memory_gb': safe_num(today_stats.get('total_memory_gb')) if today_stats else 0
            },
            'slowest_queries': [format_slow(q) for q in slowest_queries],
            'long_queries': [format_long(q) for q in long_queries],
            'top_users': [format_top_user(q) for q in top_users],
            'catalog_stats': [format_catalog(c) for c in catalog_stats] if admin else [],
            'hourly_stats': [format_hourly(h) for h in hourly_stats] if admin else [],
            'user': user,
            'is_admin': admin,
            'timestamp': datetime.now().isoformat()
        })

    except Exception as e:
        import traceback
        traceback.print_exc()
        return jsonify({'error': str(e)}), 500

@app.route('/api/query/<query_id>/kill', methods=['POST'])
@login_required
def kill_query(query_id):
    """Query'yi durdur - sadece admin"""
    if not is_admin():
        return jsonify({'error': 'Yetkisiz'}), 403

    try:
        user = session.get('user')
        password = session.get('password')
        headers = {'X-Trino-User': user}

        resp = requests.delete(
            f"{TRINO_URL}/v1/query/{query_id}",
            headers=headers,
            auth=(user, password),
            timeout=5,
            verify=False
        )

        if resp.status_code in [200, 204]:
            return jsonify({'success': True, 'message': f'Query {query_id} durduruldu'})
        else:
            return jsonify({'error': f'Query durdurulamadı: {resp.status_code}'}), 400
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route('/api/query/<query_id>')
@login_required
def get_query_detail(query_id):
    try:
        user = session.get('user')
        password = session.get('password')
        admin = is_admin()

        # Önce Trino API dene
        try:
            headers = {'X-Trino-User': user}
            resp = requests.get(
                f"{TRINO_URL}/v1/query/{query_id}",
                headers=headers,
                auth=(user, password),
                timeout=5,
                verify=False
            )
            if resp.status_code == 200:
                q = resp.json()
                query_user = q.get('session', {}).get('user')

                if not admin and query_user != user:
                    return jsonify({'error': 'Bu sorguyu görme yetkiniz yok'}), 403

                query_stats = q.get('queryStats', {})
                completed = query_stats.get('completedDrivers', 0)
                total = query_stats.get('totalDrivers', 0)
                progress = round((completed / total) * 100, 1) if total > 0 else 0

                return jsonify({
                    'query_id': q.get('queryId'),
                    'query_state': q.get('state'),
                    'user': query_user,
                    'source': q.get('session', {}).get('source'),
                    'catalog': q.get('session', {}).get('catalog'),
                    'query': q.get('query'),
                    'error_code': q.get('errorCode', {}).get('name') if q.get('errorCode') else None,
                    'failure_message': q.get('failureInfo', {}).get('message') if q.get('failureInfo') else None,
                    'cpu_time_millis': query_stats.get('totalCpuTime', '0'),
                    'wall_time_millis': query_stats.get('elapsedTime', '0'),
                    'queued_time_millis': query_stats.get('queuedTime', '0'),
                    'peak_memory_bytes': query_stats.get('peakUserMemoryReservation', '0'),
                    'total_rows': query_stats.get('processedRows', 0),
                    'output_rows': query_stats.get('outputPositions', 0),
                    'completed_splits': completed,
                    'total_splits': total,
                    'progress': progress
                })
        except:
            pass

        # MySQL'den dene
        conn = get_db()
        cursor = conn.cursor()
        cursor.execute("""
            SELECT query_id, query_state, user, source, catalog, `schema`,
                   query, error_code, failure_message,
                   cpu_time_millis, wall_time_millis, queued_time_millis,
                   peak_memory_bytes, total_rows, output_rows, completed_splits
            FROM trino_queries
            WHERE query_id = %s AND user NOT IN ('airflow')
        """, (query_id,))
        q = cursor.fetchone()
        cursor.close()
        conn.close()

        if q:
            if not admin and q.get('user') != user:
                return jsonify({'error': 'Bu sorguyu görme yetkiniz yok'}), 403

            return jsonify({
                'query_id': q.get('query_id'),
                'query_state': q.get('query_state'),
                'user': q.get('user'),
                'source': q.get('source'),
                'catalog': q.get('catalog'),
                'query': q.get('query'),
                'error_code': q.get('error_code'),
                'failure_message': q.get('failure_message'),
                'cpu_time_millis': safe_num(q.get('cpu_time_millis')),
                'wall_time_millis': safe_num(q.get('wall_time_millis')),
                'queued_time_millis': safe_num(q.get('queued_time_millis')),
                'peak_memory_bytes': safe_num(q.get('peak_memory_bytes')),
                'total_rows': safe_num(q.get('total_rows')),
                'output_rows': safe_num(q.get('output_rows')),
                'completed_splits': safe_num(q.get('completed_splits')),
                'progress': 100
            })
        return jsonify({'error': 'Query not found'}), 404

    except Exception as e:
        return jsonify({'error': str(e)}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, ssl_context=('dashboard.crt', 'dashboard.key'))
