# Kasırga — İş Günlüğü (Changelog)

Bu dosya, projeye yapılan değişiklikleri tarih bazlı takip etmek için tutulur.
Yeni bir geliştirme döngüsünde en üste yeni bir gün bloğu eklenir.

---

## v2026-03-12

### Yapılanlar

#### 1. Rebrand: "BigData Manager" → "Kasırga"
- Sidebar logo metni, footer, HTML `<title>`, backend FastAPI title ve `/api/version` endpoint'i güncellendi.
- Tüm kullanıcı-görünür referanslar "Kasırga" olarak değiştirildi.

#### 2. Versiyonlama Formatı Güncellendi
- Eski format: `v1.0.0` (semver)
- Yeni format: `v{yyyy-MM-dd}` — geliştirme gününün tarihi kullanılır.
- Güncellenen yerler: `Sidebar.jsx` footer, `backend/main.py` (FastAPI version + `/api/version` endpoint).

#### 3. Sidebar — Hamburger Menü (Collapse/Expand)
- **Dosyalar:** `frontend/src/components/Sidebar.jsx`, `frontend/src/index.css`
- Sidebar'a toggle butonu eklendi. Daraltıldığında 56px genişliğe iner, sadece ikonlar görünür.
- CSS transition ile animasyonlu geçiş, collapsed durumda tooltip desteği.
- Badge (bildirim sayısı) collapsed modda küçük kırmızı nokta olarak gösterilir.

#### 4. Hosts Sayfası — Yeniden Tasarım
- **Dosyalar:** `frontend/src/pages/HostsPage.jsx`, `frontend/src/index.css`
- **Table / Grid görünüm seçici** eklendi (toolbar'da ☰ / ⊞ butonları).
- Her host satırına/kartına tıklayınca **roller (daemon'lar) genişler**:
  - Her rol için: daemon tipi, systemctl adı, log yolu, durum badge'i.
  - Aksiyon butonları: **Başlat / Durdur / Yeniden Başlat / Log Görüntüle**.
- Metrikler (CPU, Bellek, Disk) genişletilmiş bölümde detaylı gösterilir.

#### 5. Host Düzenleme (Edit) Modalı
- **Dosyalar:** `frontend/src/pages/HostsPage.jsx`
- Her host'un yanında ✎ (edit) butonu.
- Modal form alanları: hostname, IP, servis (dropdown), daemon listesi (tip, systemctl adı, log yolu).
- **Jump Host desteği:** Checkbox işaretlendiğinde jump host input'u açılır (Doris BE vb. için).
- Daemon ekleme/çıkarma: dinamik satır ekleme, her satırda silme butonu.

#### 6. Host Ekleme ve Silme
- **Backend:** `backend/api/hosts.py` — Yeni endpoint'ler:
  - `POST /api/hosts` — Yeni host + daemon'ları oluşturur.
  - `PUT /api/hosts/{host_id}` — Host bilgilerini ve daemon'larını günceller.
  - `DELETE /api/hosts/{host_id}` — Host ve bağlı daemon'ları siler.
- **Backend:** Pydantic modeller eklendi: `HostCreate`, `HostUpdate`, `DaemonInput`.
- **Frontend API:** `frontend/src/api.js` — `createHost`, `updateHost`, `deleteHost` fonksiyonları.
- **Frontend:** "+ Host Ekle" butonu toolbar'da. Silme işlemi onay modalı ile.
- **Not:** `GET /api/hosts` artık `full=True` ile döner (daemon bilgileri dahil), çünkü hosts sayfası rolleri gösteriyor.

#### 7. Log Viewer — "Log akışı kesildi" Sorunu Giderildi
- **Dosyalar:** `frontend/src/components/LogViewer.jsx`
- **Sorun:** `EventSource.onerror` tetiklendiğinde bağlantı kapatılıp bir daha açılmıyordu.
- **Çözüm:**
  - Bağlantı koptuğunda 3 saniye sonra **otomatik yeniden bağlanma** (`setTimeout` ile retry).
  - Hata mesajı "Yeniden bağlanılıyor..." olarak güncellendi.
  - Sağ üste **↻ Yenile** butonu eklendi (manuel yeniden bağlanma).
  - Hata banner'ında da **"Şimdi Bağlan"** butonu var.

#### 8. CLAUDE.md Güncellendi
- Deploy süreci dokümante edildi: air-gapped ortam, frontend-dist önceden build edilir.
- Repo'daki config dosyalarının dummy veri olduğu notu eklendi.

### Teknik Notlar
- Frontend build çıktısı `frontend-dist/` dizinine yazılır (`vite.config.js` → `outDir: '../frontend-dist'`).
- Deploy'da sunucuya taşınan dizinler: `backend/`, `frontend/`, `frontend-dist/`, `topology.yaml`, `cluster_manager.cfg`.
- Sunucuda Node.js/npm yok — frontend mutlaka önceden build edilmiş olmalı.
