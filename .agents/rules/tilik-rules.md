---
name: tilik-rules
description: Aturan baku pengembangan proyek Tilik Android (arsitektur overlay, Dark Investment UI System, gesture physics, clipboard auto-detect, kredensial, dan tata cara testing)
always_on: true
---

# 📜 TILIK PROJECT RULES & ARCHITECTURAL GUIDELINES

Dokumen rules ini adalah **pedoman wajib (Single Source of Truth)** bagi Antigravity Agent, Gemini CLI, dan AI developer lainnya dalam menganalisis, mengembangkan, merefaktor, maupun melakukan debug pada proyek **Tilik Android App**. Agent **WAJIB membaca dan mematuhi** aturan di bawah ini sebelum membuat atau memodifikasi kode.

---

## 1. 🏛️ Filosofi Proyek & Arsitektur Utama

1. **Identitas Aplikasi:**
   - **Tilik** adalah aplikasi Android Native berbasis **Kotlin** dan **Jetpack Compose (Material 3)**.
   - Berfungsi sebagai pemeriksa fakta (*fact-checker*) dan *financial insight companion* bursa saham (BEI / IDX) secara instan dan melayang (*overlay floating widget*).
   - Terintegrasi dengan **Sectors API** untuk data fundamental, kepemilikan investor/broker asing, valuasi PE/PBV, dan kinerja emiten.
2. **Kenyamanan Pengguna Tanpa Hambatan (Non-Intrusive UX):**
   - Aplikasi berjalan di atas aplikasi lain (Threads, Twitter/X, WhatsApp, Chrome).
   - Floating widget **tidak boleh menghalangi pandangan** konten aplikasi induk saat tidak digunakan.
   - Semua dismiss dan minimize harus otomatis saat pengguna berinteraksi di luar aplikasi melayang.
3. **Estetika Dark Investment App & Zero-Fluff (WAJIB):**
   - Desain UI mengadopsi standar **Fintech Dark Mode**:
     - Latar belakang AMOLED Black: `AppBg` (`#0A0A0A`), kartu bertingkat `AppCard` (`#141414`), kontainer sekunder `AppCardSubtle` (`#1A1A1A`).
     - Aksen oranye-merah investasi: `AppAccent` (`#FF5C35`), hairline border elegan `AppBorder` (`Color(0x14FFFFFF)`).
     - Pill badges (`percent = 50`), squircle cards (`RoundedCornerShape(14.dp)`), dan tombol bulat kontras tinggi (`Color.White`).
     - Floating widget mempertahankan fisika Grammarly (*spring bounce*, *auto-tuck* 3 detik, *snap-to-edge*).
   - **High-Signal, Zero-Fluff (Hapus Basa-Basi):**
     - DILARANG menampilkan kalimat tambahan/paragraf bertele-tele yang tidak penting di layar.
     - Setiap deskripsi harus singkat, padat, dan langsung pada inti informasi (< 12 kata).

---

## 2. 📁 Struktur Direktori & Tanggung Jawab Modul

Semua kode sumber utama berada di dalam `app/src/main/java/id/tilik/app/`:

```text
id/tilik/app/
├── MainActivity.kt               # Entry point dashboard & navigasi bottom dock
├── TilikApplication.kt           # Inisialisasi Timber logger & Global App Context
├── capture/
│   ├── ScreenCaptureManager.kt   # Pengelola MediaProjection, VirtualDisplay, & ImageReader
│   └── AudioBufferRecorder.kt    # Perekam audio internal (opsional)
├── detection/
│   └── StockKeywordDetector.kt   # Kamus emiten BEI (BBRI, BBCA, GOTO, AMMN, dll.) & regex klaim
├── network/
│   ├── ApiClientProvider.kt      # Retrofit & OkHttp client (auth interceptor, timeout, logging)
│   └── TilikApiService.kt        # Interface API multipart (/api/v1/verify)
├── repository/
│   ├── VerificationRepository.kt # Abstraksi repositori verifikasi
│   └── VerificationRepositoryImpl# Implementasi repositori dengan intelligent fallback
├── service/
│   ├── OverlayService.kt         # Foreground Service sentral pengelola floating window & token izin
│   └── TilikAccessibilityService # Service aksesibilitas pendeteksi teks layar & perpindahan window
└── ui/
    ├── theme/                    # Color tokens, Typography, Shape, dan Theme Compose (Theme.kt)
    ├── components/               # Komponen global (AppTopBar, AppBottomNav, StatusBadge, dll.)
    ├── dashboard/                # Layar utama (DashboardScreen, BalanceSection, PortfolioCarousel, WatchlistSection)
    ├── history/                  # Layar riwayat & notifikasi (HistoryScreen, HistoryItemCard)
    └── overlay/
        ├── OverlayWindowManager.kt # WindowManager native Android, gesture physics, drag clamp, auto-tuck
        ├── FloatingBubbleView.kt   # Compose UI bubble melayang, chevron dock tabs, status capsules
        ├── ClaimBottomSheet.kt    # Modal bottom sheet, bidirectional drag-up/down, clipboard sync
        └── VerdictCardView.kt      # Kartu hasil vonis bursa Sectors API & swipe-down to dismiss
```

---

## 3. 🎨 Dark Investment App Design System & UI Specifications

Untuk spesifikasi visual lengkap dan panduan token warna, selalu merujuk pada [DESIGN.md](DESIGN.md).

1. **Token Warna Utama:**
   - Background: `AppBg` (`#0A0A0A`)
   - Surface / Card: `AppCard` (`#141414`), `AppCardSubtle` (`#1A1A1A`)
   - Border: `AppBorder` (`Color(0x14FFFFFF)` / hairline border 8% white)
   - Accent: `AppAccent` (`#FF5C35`)
   - Positive/Bullish: `AppGreen` (`#22C55E`), `AppGreenBg` (`#0F2115`), `AppGreenBorder` (`Color(0x3322C55E)`)
   - Negative/Bearish: `AppRed` (`#EF4444`), `AppRedBg` (`#241113`), `AppRedBorder` (`Color(0x33EF4444)`)
   - Muted/Subtle Text: `AppGray` (`#888888`), `AppGrayDark` (`#737373`)
2. **Glassmorphism Translucency:**
   - Navigasi bawah (`AppBottomNav.kt`) dan top header menggunakan latar transparan `Color(0xE60A0A0A)` dengan batas 1dp `Color(0x14FFFFFF)`.
3. **Charts & Visual Sparklines:**
   - Kartu portofolio horizontal dilengkapi sparkline curve dinamis via Compose `Canvas` dengan kurva bezier dan gradient fill yang memudar ke bawah.
4. **Prinsip Copywriting Ringkas (Zero-Fluff):**
   - Hapus semua kata sambutan panjang atau penjelasan fitur yang redundan.
   - Fokus pada metrik angka, label status, ticker saham, dan tombol aksi langsung.

---

## 4. 🫧 Aturan Gestur & State Machine Floating Bubble

1. **Snap-to-Edge (Wajib Mepet ke Ujung Terdekat):**
   - Bubble **TIDAK BOLEH** berhenti melayang di tengah layar sembarangan setelah digeser pengguna.
   - Begitu jari dilepaskan (`ACTION_UP` atau `ACTION_CANCEL`), hitung titik tengah bubble terhadap lebar layar:
     - Jika berada di separuh kiri layar (`bubbleCenterX < screenWidth / 2`) -> otomatis meluncur dan menempel ke **tepi kiri layar** (`x = 0`).
     - Jika berada di separuh kanan layar (`bubbleCenterX >= screenWidth / 2`) -> otomatis meluncur dan menempel ke **tepi kanan layar** (`x = screenWidth - bubbleWidth`).
   - Gunakan animasi fisika `ValueAnimator` dengan `DecelerateInterpolator` durasi ~280ms.
2. **Ambang Sentuh (Drag Threshold) Sangat Rendah (8px):**
   - Gunakan ambang **8px** agar bubble responsif seketika.
   - Bedakan *tap* (ketukan) dan *drag* (geser): Jika durasi sentuhan < 280ms dan jarak tempuh < 12px, perlakukan sebagai klik untuk membuka sheet.
3. **Idle Auto-Tuck & Dimming (Setelah 3 Detik Diam):**
   - Jika bubble berada dalam status `IDLE` dan tidak disentuh selama **3 detik**, bubble wajib:
     - **Mengecil**: Skala ~86%.
     - **Meredup**: Alpha ~42% (`0.42f`).
     - **Ngumpet (Tuck-in)**: Bergeser masuk ke bezel samping layar.
     - **Chevron Edge Tab**: Tulisan teks digantikan dengan ikon panah indikator (*chevron tab*) yang mengarah ke dalam layar (`>` di tepi kiri, `<` di tepi kanan).
   - **Instan Wake Up**: Begitu tab disentuh atau digeser, bubble langsung bangun seketika ke skala 100%, alpha 1.0.
4. **Anti-Overflow Clamping (Proteksi Batas Layar):**
   - Koordinat `x` bubble wajib di-clamp:
     ```kotlin
     params.x = params.x.coerceIn(marginHorizontalPx, screenWidth - viewWidth - marginHorizontalPx)
     ```
   - Tidak boleh ada teks atau ikon yang meluber keluar layar atau terpotong bezel.
5. **Anti-Tuck Saat Proses Berlangsung:**
   - Selama verifikasi Sectors API berjalan (`ANALYZING` atau `RESULT`), idle timer wajib dihentikan agar bubble tetap tampil penuh.

---

## 5. 📄 Aturan Modal Bottom Sheet (Bidirectional Drag)

1. **Animasi Slide-Up Ease In-Out:**
   - Masuk dari bawah menggunakan `slideInVertically` dengan `CubicBezierEasing(0.18f, 0.9f, 0.2f, 1.0f)`.
   - Scrim semi-transparan hitam (`dynamicScrimAlpha`) memudar mengikuti tarikan jari.
2. **Auto-Close Cerdas:**
   - Gestur **Swipe Up ke Home**, tombol **Back**, **Recent Apps**, atau sentuhan di luar sheet **WAJIB langsung menutup sheet seketika** kembali ke bubble.
3. **Gestur Bidirectional Drag (Tarik Atas & Bawah):**
   - **Mode Standar (`!isExpanded`):**
     - Ditarik ke atas $\ge$ 45dp atau flick ke atas -> **Beralih ke mode Expanded**.
     - Ditarik ke bawah $\ge$ 90dp atau flick ke bawah -> **Menutup sheet (Dismiss)**.
   - **Mode Expanded (`isExpanded == true`):**
     - Ditarik ke bawah sedang (~60dp) -> **Kembali ke mode ukuran standar**.
     - Ditarik jauh ke bawah ($\ge$ 160dp) atau flick keras ke bawah -> **Menutup sheet (Dismiss)**.
4. **Auto-Expand pada Teks Panjang:**
   - Jika teks klaim memiliki **$\ge$ 4 baris** atau **$\ge$ 200 karakter**, sheet otomatis beralih ke `isExpanded = true`.
5. **Dynamic Height TextField:**
   - Kotak input klaim menggunakan:
     ```kotlin
     minLines = if (isExpanded) 7 else 2,
     maxLines = if (isExpanded) 14 else 5,
     modifier = Modifier.fillMaxWidth().animateContentSize()
     ```

---

## 6. 📋 Aturan Real-Time Clipboard Auto-Detection

1. **Live Synchronization:**
   - Gunakan `ClipboardManager.OnPrimaryClipChangedListener` di level Composable `ClaimBottomSheet`.
   - Jalankan urutan polling otomatis (0ms, 80ms, 200ms, 450ms, 800ms) saat sheet dibuka untuk mengantisipasi keterlambatan clipboard dari aplikasi induk.
2. **Proteksi Input Manual Pengguna:**
   - Gunakan flag `hasUserManuallyEdited`. Jika pengguna telah mengetik manual, auto-clipboard tidak boleh menimpa tulisan secara sepihak.

---

## 7. 🔒 Aturan Keamanan Kredensial & Secrets (.env)

1. **LARANGAN KERAS Hardcoding Secrets:**
   - Jangan pernah menuliskan API key asli, token produksi, password, atau IP statis di dalam file Kotlin (`.kt`) atau XML (`.xml`).
2. **Pemuatan Konfigurasi Dinamis di `build.gradle.kts`:**
   - Baca file `.env` di folder proyek, root, atau `local.properties`:
     - `TILIK_BASE_URL` (default: `http://10.0.2.2:8000/`)
     - `SECTORS_API_KEY` (Sectors Financial API key)
     - `GEMINI_API_KEY` (Gemini API key)
     - `ENVIRONMENT` (`development` / `production`)
3. **Proteksi `.gitignore`:**
   - Semua variasi `.env` (`.env`, `.env.*`) dan `local.properties` **WAJIB diabaikan oleh Git**. Hanya `!.env.example` yang diizinkan untuk di-commit.
4. **Auth Interceptor Otomatis:**
   - Di `ApiClientProvider.kt`, sertakan `authInterceptor` yang otomatis menyuntikkan header `Authorization` dan `X-API-KEY`.

---

## 8. 🐙 Aturan Repository Git & Struktur Proyek

1. **Root Git Berada di Folder Tilik:**
   - Repository Git **WAJIB diinisialisasi langsung di dalam folder root `tilik`**.
   - GitHub repo harus menampilkan langsung `app/`, `gradle/`, `README.md`, `DESIGN.md`, `PANDUAN_TESTING.md`, `.env.example`, `.gitignore`.
2. **Filtrasi File Cache & Intermediate:**
   - Jangan pernah meng-commit folder `.gradle/`, `build/`, `app/build/`, `.idea/`, `.kotlin/`, atau file `*.log`.

---

## 9. 🧪 Aturan Pengujian & Kolaborasi dengan User

1. **JANGAN Menguji di HP Terlalu Lama Sendirian:**
   - Pastikan kompilasi Gradle berhasil (`BUILD SUCCESSFUL`), deploy APK ke HP user, lalu **berikan petunjuk jelas agar user menguji langsung di HP fisiknya**.
2. **Wajib Memelihara `PANDUAN_TESTING.md`:**
   - Setiap fitur atau perbaikan gestur/UI baru harus dicatat dalam `PANDUAN_TESTING.md` dengan langkah yang runut dan "Hasil yang Diharapkan".
