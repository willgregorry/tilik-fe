# 🎨 TILIK DESIGN SYSTEM & UI CLONE SPECIFICATION

Dokumen ini adalah **Spesifikasi Resmi Design System Tilik**, mengadopsi rancangan antarmuka dari clone aplikasi investasi modern (*fintech dark mode*) berbasis **Jetpack Compose (Material 3)**.

---

## 1. 🎯 Filosofi Desain & Core Principle

1. **High-Signal, Zero-Fluff (Fokus Informasi Penting):**
   - Hapus semua kalimat bertele-tele, paragraf basa-basi, atau penjelasan redundan.
   - Angka, metrik perubahan harga (+/-), nama emiten, dan label status harus langsung terbaca dalam lirikan sekilas (<1 detik).
2. **True Dark Fintech Aesthetic:**
   - Latar belakang murni pekat (AMOLED Black `#0A0A0A`), permukaan kartu bertingkat `#141414`, dan aksen oranye-merah menyala `#FF5C35`.
   - Menggunakan hairline border `Color(0x14FFFFFF)` (8% putih) untuk mendefinisikan batas kartu secara elegan tanpa efek neon murahan.
3. **Glassmorphism Translucency:**
   - Navigasi bawah (Bottom Navigation) dan header menggunakan latar belakang semi-transparan `Color(0xE60A0A0A)` (90% opacity) dengan batas atas 1dp `Color(0x14FFFFFF)` yang menciptakan kedalaman ruang visual (*frosted glass dock*).

---

## 2. 🎨 Token Warna (Color Tokens)

Semua token warna didefinisikan secara sentral di `id.tilik.app.ui.theme.Theme.kt`:

| Token Name | Hex / Value | Deskripsi Penggunaan |
| :--- | :--- | :--- |
| `AppBg` / `AppBackground` | `#0A0A0A` | Latar belakang utama seluruh aplikasi |
| `AppCard` / `AppSurface` | `#141414` | Kartu portofolio, item watchlist, dan dialog |
| `AppCardSubtle` / `AppSurfaceSubtle` | `#1A1A1A` | Kontainer sekunder, ikon latar squircle |
| `AppAccent` / `BrandPrimary` | `#FF5C35` | Aksen oranye-merah investasi, tombol utama, tab aktif |
| `AppGreen` / `StatusSuccess` | `#22C55E` | Metrik naik (bullish), status valid, return positif |
| `AppGreenBg` / `StatusSuccessBg` | `#0F2115` | Kontainer pill hijau gelap |
| `AppGreenBorder` | `Color(0x3322C55E)` | Border pill hijau (20% opacity) |
| `AppRed` / `StatusDanger` | `#EF4444` | Metrik turun (bearish), status tidak valid, FCA |
| `AppRedBg` / `StatusDangerBg` | `#241113` | Kontainer pill merah gelap |
| `AppRedBorder` | `Color(0x33EF4444)` | Border pill merah (20% opacity) |
| `AppGray` / `TextSecondary` | `#888888` | Teks sekunder, label pembantu, ikon tidak aktif |
| `AppGrayDark` / `TextMuted` | `#737373` | Judul seksi kartu ("My Portofolio", "My Watchlist") |
| `TextPrimary` | `#FFFFFF` | Teks utama, harga, ticker emiten, nama investor |
| `AppBorder` | `Color(0x14FFFFFF)` | Hairline border kartu (8% white) |
| `AppBorderLight` | `Color(0x0FFFFFFF)` | Divider garis halus pemisah baris (6% white) |

---

## 3. 🧩 Komponen Utama UI

### A. Top Bar (Header Investor)
- **Komponen:** `AppTopBar.kt`
- **Sisi Kiri:** Avatar lingkaran (44dp) foto profil akun Google (atau fallback favicon putih) + Teks "Hi, Good morning" (13sp gray) & Nama User/Investor dinamis (17sp white).
- **Sisi Kanan:** Tombol lingkaran riwayat notifikasi (44dp, `#141414`) membuka riwayat verifikasi fakta.

### B. Status & Pantauan Klaim Hero Section (Zero AI-Slop, High-Signal)
- **Komponen:** `BalanceSection.kt`
- Label: `Klaim Saham Ditilik` dengan ikon verifikasi `CheckCircle`.
- Angka: `${verifiedCount} Klaim` (angka riil dari histori analisis bursa, zero mock / zero fake bank balance).
- Tombol Aksi Kanan: Tombol bulat putih kontras tinggi (50dp, `Color.White`) dengan ikon hitam untuk memulai/menghentikan layanan Tilik secara instan.
- Status Pill Badge: Pill lengkung penuh (`percent = 50`) menampilkan status layanan Tilik Aktif / Standby dan jumlah klaim tervalidasi (`• X Valid`).

### C. My Portfolio Horizontal Cards
- **Komponen:** `PortfolioCarousel.kt`
- Judul seksi: "Portofolio Saham" + "Lihat Detail >".
- Slot dinamis / `-` ("Belum ada portofolio terhubung") jika belum ada data emiten riil.
- Kartu horizontal (172dp x 160dp, `RoundedCornerShape(24.dp)`, `#141414`):
  - Ticker & nama perusahaan di kiri atas, ikon lingkaran emiten di kanan atas.
  - Harga saham (18sp bold) & persentase naik/turun di kiri bawah.
  - **Sparkline Curve:** Grafik mini kurva di dasar kartu digambar dinamis via Compose `Canvas` dengan garis bergradien halus dan arsiran memudar ke bawah (*gradient fill area*).

### D. My Watchlist Section
- **Komponen:** `WatchlistSection.kt`
- Judul seksi: "My Watchlist" + "Add Watchlist +".
- Baris item:
  - Ikon squircle 44dp (`RoundedCornerShape(14.dp)`, background `#141414`, border `Color(0x14FFFFFF)`).
  - Ticker (15sp bold) dan nama emiten di sebelah kiri.
  - Harga saham dan persentase perubahan hijau/merah di sebelah kanan.

### E. Service & System Readiness Hub
- **Komponen:** `ServiceControlCard.kt` & `SystemReadinessCard.kt`
- Kartu sudut melengkung 20dp dengan latar `#141414` dan border `Color(0x14FFFFFF)`.
- Mengeliminasi teks deskripsi panjang; menyajikan status to-the-point:
  - "Floating Overlay · Izin melayang di atas aplikasi"
  - "Tangkapan Layar · MediaProjection pembaca klaim"
  - "Deteksi Teks · Aksesibilitas deteksi otomatis"
  - "Latar Belakang · Proteksi memori & baterai"

### F. Screen 2: Notifications & Riwayat
- **Komponen:** `HistoryScreen.kt` & `HistoryItemCard.kt`
- Header: Tombol kembali bulat 40dp + judul "Notifications".
- Filter Chips: Pill rounded penuh; chip aktif berlatar putih dengan teks hitam, chip tidak aktif berlatar `#141414`.
- Item Notifikasi: Ikon squircle 14dp di kiri, judul dan timestamp di kanan, diikuti teks klaim/fakta yang ringkas tanpa kata-kata pengisi.

### G. Bottom Navigation Glassmorphism
- **Komponen:** `AppBottomNav.kt`
- Latar belakang: `Color(0xE60A0A0A)` dengan garis atas `Color(0x14FFFFFF)`.
- 4 Tab: `Home`, `Markets`, `Portofolio`, `Profile`.
- Aksen aktif: `AppAccent` (`#FF5C35`), aksen tidak aktif: `AppGray` (`#888888`).

---

## 4. 🫧 Floating Overlay & Sheet Cohesion
- Komponen overlay melayang (`FloatingBubbleView.kt`, `ClaimBottomSheet.kt`, `VerdictCardView.kt`) menggunakan palet warna gelap yang sepenuhnya seragam dengan aplikasi utama.
- Menjaga aturan *Snap-to-Edge*, *Auto-Tuck* 3 detik, *Drag Threshold* 8px, dan *Bidirectional Drag* modal bottom sheet.
