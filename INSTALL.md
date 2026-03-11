# BigData Manager — Kurulum Kılavuzu (CentOS 8.x)

## Gereksinimler
# test

- Python 3.9+
- MySQL 8.x (cluster_manager veritabanı)
- Paramiko için OpenSSH anahtar çifti (`~/.ssh/id_rsa`)
- mcli (Pure Storage için)
- node_exporter 9100 portunda tüm yönetilen host'larda çalışıyor olmalı

---

## 1. Python Ortamı

```bash
python3.9 -m venv venv
source venv/bin/activate
pip install -r backend/requirements.txt
```

---

## 2. MySQL Veritabanı Oluşturma

```sql
CREATE DATABASE cluster_manager CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'cm_user'@'localhost' IDENTIFIED BY 'cm_password';
GRANT ALL PRIVILEGES ON cluster_manager.* TO 'cm_user'@'localhost';
FLUSH PRIVILEGES;
```

---

## 3. Konfigürasyon

`cluster_manager.cfg` dosyasını düzenleyin:

```ini
[mysql]
host     = 127.0.0.1
port     = 3306
database = cluster_manager
user     = cm_user
password = cm_password

[prometheus]
url     = https://bd-prometheus.hmb.gov.tr
timeout = 10

[ssh]
user     = cm_user
key_path = ~/.ssh/id_rsa
timeout  = 30

[metrics]
interval_seconds = 60

[mcli]
alias = pure

[general]
topology_file = topology.yaml
log_level     = INFO
```

---

## 4. SSH Erişimi

CM sunucusundan tüm yönetilen host'lara şifreli anahtar ile erişim sağlanmalıdır:

```bash
# Anahtar oluştur (yoksa)
ssh-keygen -t rsa -b 4096 -f ~/.ssh/id_rsa

# Her host için yetkilendir
ssh-copy-id -i ~/.ssh/id_rsa.pub cm_user@<hostname>
```

### Doris BE için sudo yetkisi

Doris BE host'larında `cm_user`'a sudo yetkisi:

```bash
# /etc/sudoers.d/cm_user dosyası:
cm_user ALL=(ALL) NOPASSWD: /usr/bin/systemctl start doris-be, \
                             /usr/bin/systemctl stop doris-be, \
                             /usr/bin/systemctl restart doris-be
```

---

## 5. topology.yaml Düzenleme

`topology.yaml` dosyasında gerçek hostname/IP değerlerinizi girin:

- `hostname`: SSH ile erişilecek hostname
- `ip`: node_exporter scrape için IP
- `jump_via`: Doris BE için FE hostname (CM → FE → BE)
- `systemctl`: systemd servis adı

---

## 6. mcli Kurulumu (Pure Storage)

```bash
# mcli indir
curl -O https://dl.min.io/client/mc/release/linux-amd64/mc
chmod +x mc
mv mc /usr/local/bin/mcli

# Pure Storage alias ekle
mcli alias set pure https://pure-s3.hmb.gov.tr ACCESS_KEY SECRET_KEY

# Test
mcli ls pure/
```

---

## 7. Frontend Build (React UI)

```bash
cd frontend
npm install
npm run build
# → Çıktı: ../frontend-dist/ klasörüne yazılır
cd ..
```

Backend, `frontend-dist/` klasörü varsa React uygulamasını, yoksa `standalone.html`'i sunar.

**Geliştirme modunda çalıştırma (hot-reload):**

```bash
# Terminal 1 — Backend
uvicorn backend.main:app --host 0.0.0.0 --port 8000 --reload

# Terminal 2 — Frontend dev server (proxy: localhost:8000)
cd frontend
npm run dev
# → http://localhost:3000
```

---

## 8. Uygulamayı Başlatma

```bash
# Ortam değişkeni (opsiyonel, varsayılan: ./cluster_manager.cfg)
export CM_CONFIG=/etc/bigdata-manager/cluster_manager.cfg

# Uvicorn ile başlat
source venv/bin/activate
uvicorn backend.main:app --host 0.0.0.0 --port 8000

# Üretim için (daemon modunda)
nohup uvicorn backend.main:app --host 0.0.0.0 --port 8000 --workers 2 &
```

### systemd ile Başlatma (Önerilen)

```ini
# /etc/systemd/system/bigdata-manager.service
[Unit]
Description=BigData Manager API
After=network.target mysqld.service

[Service]
Type=simple
User=cm_user
WorkingDirectory=/opt/bigdata-manager
Environment="CM_CONFIG=/opt/bigdata-manager/cluster_manager.cfg"
ExecStart=/opt/bigdata-manager/venv/bin/uvicorn backend.main:app --host 0.0.0.0 --port 8000
Restart=on-failure
RestartSec=5

[Install]
WantedBy=multi-user.target
```

```bash
systemctl daemon-reload
systemctl enable --now bigdata-manager
```

---

## 8. Arayüze Erişim

Tarayıcıda: `http://<cm-sunucu-ip>:8000`

Standalone (backend olmadan): `standalone.html` dosyasını çift tıklayarak açabilirsiniz.

---

## API Endpoints

| Method | Path | Açıklama |
|--------|------|----------|
| GET | /api/health | Sağlık kontrolü |
| GET | /api/services | Tüm servisler |
| GET | /api/services/{name} | Servis detayı |
| GET | /api/hosts | Tüm host'lar |
| GET | /api/daemons | Tüm daemon'lar |
| POST | /api/daemons/{id}/start | Daemon başlat |
| POST | /api/daemons/{id}/stop | Daemon durdur |
| POST | /api/daemons/{id}/restart | Daemon yeniden başlat |
| GET | /api/daemons/{id}/logs | SSE log stream |
| GET | /api/metrics/hosts | Host metrikleri |
| POST | /api/metrics/refresh | Manuel yenileme |
| GET | /api/notifications | Bildirimler |
| PATCH | /api/notifications/{id}/read | Okundu işaretle |

---

## Sorun Giderme

**SSH bağlantı hatası:**
```bash
ssh -i ~/.ssh/id_rsa cm_user@<hostname> systemctl is-active <service>
```

**DB bağlantı hatası:**
```bash
mysql -u cm_user -p cluster_manager -e "SELECT 1"
```

**mcli testi:**
```bash
mcli ls pure/raw-data
```

**Log seviyesini artır:**
```ini
# cluster_manager.cfg
[general]
log_level = DEBUG
```
