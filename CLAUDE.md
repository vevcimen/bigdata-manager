# Kasırga — Claude Ajan Bilgilendirmesi

## Proje Nedir?
Cloudera Manager benzeri bir araç. On-prem ~150+ fiziksel sunucu üzerinde açık kaynak big data tool'ların yönetimi ve monitoring'i için hazırlanmıştır.

## Takip Edilen Araçlar
Trino, Spark, Kafka, Kafka Connect, Apache Doris, Apache HDFS, Pure Storage, Airflow, Zookeeper, MySQL

## Teknik Stack
- **Backend**: Python FastAPI (backend/)
- **Frontend**: React 18 + Vite (frontend/)
- **Veritabanı**: MySQL (SQLAlchemy ORM)
- **OS**: CentOS 8
- **İletişim**: Passwordless SSH (Paramiko) + atlama sunucusu (jump host) desteği
- **Auth**: JWT tabanlı (pyjwt + passlib/bcrypt), default kullanıcı: admin/admin

## Deploy Süreci (KRİTİK)
- Hedef sunucu **kapalı network** (air-gapped). Sunucuda npm/node YOKTUR.
- `backend/`, `frontend/`, `frontend-dist/` dizinleri olduğu gibi sunucuya taşınır.
- **frontend-dist/ repo'da tutulur** ve her frontend değişikliğinde `cd frontend && npm run build` ile yeniden build edilmelidir.
- Build çıktısı `frontend-dist/` klasörüne gider (vite.config.js'de tanımlı).

## Proje Yapısı

```
backend/
├── main.py                    # FastAPI giriş noktası, startup/shutdown, router kayıtları
├── requirements.txt           # Python bağımlılıkları
├── api/
│   ├── auth.py                # JWT login/logout, kullanıcı CRUD (/api/auth/*)
│   ├── hosts.py               # Host CRUD + Servis CRUD + topology export (/api/hosts/*, /api/services/*, /api/topology/export)
│   ├── daemons.py             # Daemon start/stop/restart/logs (/api/daemons/*)
│   ├── metrics.py             # Metrik API'leri (/api/metrics/*)
│   ├── notifications.py       # Bildirim CRUD (/api/notifications/*)
│   ├── service_detail.py      # Servis-spesifik detay: HDFS, Spark, Trino, Airflow (/api/services/{name}/detail)
│   ├── diag.py                # Sistem diagnostikleri (/api/diag)
│   └── deps.py                # Dependency injection (DB session, config)
├── core/
│   ├── topology.py            # topology.yaml yükleyici (sadece ilk kurulumda kullanılır)
│   ├── ssh.py                 # SSH bağlantı havuzu, komut çalıştırma, log streaming
│   └── scheduler.py           # APScheduler: metrik toplama, daemon durum kontrolü
├── db/
│   ├── models.py              # SQLAlchemy modelleri: Service, Host, Daemon, DaemonEvent, MetricSnapshot, Notification, User
│   └── init_db.py             # DB init: tablo oluşturma, ilk topology sync, default admin oluşturma
└── services/
    └── doris.py               # Apache Doris HTTP API client

frontend/src/
├── main.jsx                   # React giriş noktası
├── App.jsx                    # Ana layout: auth kontrolü, sayfa routing, topbar
├── api.js                     # API client: token yönetimi, tüm endpoint fonksiyonları
├── index.css                  # Global stiller (dark tema)
├── pages/
│   ├── LoginPage.jsx          # Giriş ekranı
│   ├── Dashboard.jsx          # Genel durum özeti
│   ├── ServicesPage.jsx       # Servis listesi + CRUD + detay panelleri (HDFS/Spark/Trino/Airflow)
│   ├── HostsPage.jsx          # Host yönetimi (tablo/grid, ekleme/düzenleme/silme)
│   ├── DaemonsPage.jsx        # Daemon yönetimi (start/stop/restart, canlı log)
│   ├── MetricsPage.jsx        # CPU/bellek/disk metrikleri
│   ├── NotificationsPage.jsx  # Bildirim merkezi
│   ├── DiagnosticsPage.jsx    # Sistem sağlık kontrolleri
│   └── UserManagement.jsx     # Kullanıcı yönetimi (ekleme/düzenleme/silme, şifre değiştirme)
└── components/
    ├── Sidebar.jsx            # Sol menü (collapsible, kullanıcı bilgisi, çıkış)
    ├── StatusBadge.jsx        # Durum göstergesi (active/inactive/failed)
    ├── LogViewer.jsx          # SSE ile canlı log izleme
    └── ErrorBoundary.jsx      # React error boundary
```

## Veritabanı Modelleri
- **Service**: name (unique), type, description, extra (JSON — webui_url, username, password vb.)
- **Host**: hostname, ip, jump_via, service_id (FK)
- **Daemon**: daemon_type, systemctl_name, log_path, last_status, host_id (FK), service_id (FK)
- **DaemonEvent**: action, triggered_by, status, output, daemon_id (FK)
- **MetricSnapshot**: host, service_name, metric_name, value, labels (JSON), ts
- **Notification**: title, message, level, source, is_read
- **User**: username, password_hash, role (admin/viewer)

## Önemli Mimari Kararlar
1. **Topology.yaml sadece ilk kurulumda kullanılır.** DB'de servis varsa restart'larda topology.yaml okunmaz, DB configleri kullanılır. (init_db.py)
2. **DB configleri `GET /api/topology/export` ile topology.yaml olarak export edilebilir.** Arayüzde Servisler sayfasında "Export" butonu var.
3. **JWT auth zorunlu.** Tüm API çağrıları Authorization header'ı taşır. 401 alınca frontend otomatik logout yapar.
4. **Servis detay panelleri servis tipine göre dinamik.** HDFS→Namenode JMX, Spark→REST API, Trino→query API, Airflow→DAG API. Bağlantı bilgileri servisin `extra` alanında tutulur.
5. **SSH ile daemon yönetimi.** systemctl start/stop/restart + tail -f log streaming (SSE).
6. **APScheduler** ile periyodik metrik toplama (node_exporter, Prometheus, mcli, daemon status).

## Config Dosyaları
- **cluster_manager.cfg**: MySQL, Prometheus, SSH, metrics ayarları (INI format)
- **topology.yaml**: Servis/host/daemon tanımları (sadece ilk init için, sonra DB'den yönetilir)
- Her ikisi de repo'da dummy/örnek veridir, gerçek bilgi içermez.

## Geliştirme Notları
- Frontend değişikliği yaptıktan sonra mutlaka `cd frontend && npm run build` çalıştır.
- Backend'e yeni router eklerken: (1) router dosyasını yaz, (2) main.py'de import et ve include_router() ile ekle.
- Yeni DB modeli eklerken: models.py'ye yaz, init_db.py'de gerekli migration/seed mantığını ekle.
- Servis tipi eklerken: service_detail.py'de handler fonksiyonu yaz, ServicesPage.jsx'e detay panel componenti ekle.
