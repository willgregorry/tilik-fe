---
name: tilik-rules
description: Aturan baku pengembangan proyek Tilik Android (arsitektur overlay, Grammarly UI, gesture physics, clipboard auto-detect, kredensial, dan tata cara testing)
always_on: true
---

# 📜 TILIK PROJECT RULES & ARCHITECTURAL GUIDELINES

Dokumen rules ini adalah **pedoman wajib (Single Source of Truth)** bagi Antigravity Agent dalam menganalisis, mengembangkan, merefaktor, maupun melakukan debug pada proyek **Tilik Android App**. Agent **WAJIB membaca dan mematuhi** aturan di bawah ini sebelum membuat atau memodifikasi kode.

---

## 1. 🏛️ Filosofi Proyek & Arsitektur Utama

1. **Identitas Aplikasi:**
   - **Tilik** adalah aplikasi Android Native berbasis **Kotlin** dan **Jetpack Compose (Material 3)**.
   - Berfungsi sebagai pemeriksa fakta (*fact-checker*) bursa saham (BEI / IDX) secara instan dan melayang (*overlay floating widget*).
   - Terintegrasi dengan **Sectors API** untuk data fundamental, kepemilikan investor/broker asing, valuasi PE/PBV, dan kinerja emiten.
2. **Kenyamanan Pengguna Tanpa Hambatan (Non-Intrusive UX):**
   - Aplikasi berjalan di atas aplikasi lain (Threads, Twitter/X, WhatsApp, Chrome).
   - Floating widget **tidak boleh menghalangi pandangan** konten aplikasi induk saat tidak digunakan.
   - Semua dismiss dan minimize harus otomatis saat pengguna berinteraksi di luar aplikasi melayang.
3. **Estetika Grammarly-Class (WAJIB):**
   - Desain UI terinspirasi penuh dari **Grammarly Android**:
     - Latar belakang gelap berkelas: `GrammarlySurfaceDark` (`#181A1D`), `GrammarlyCardBg` (`#1E2024`).
     - Aksen hijau toska: `GrammarlyTeal` (`#00A389`), garis border tipis `#202227`.
     - Tipografi modern monospaced pada badge emiten (`🎯 EMITEN: $TICKER`).
     - Animasi fisika halus (*spring bounce*, kurva *cubic-bezier* ease in-out), **BUKAN UI MVP polos**.

---

## 2. 📁 Struktur Direktori & Tanggung Jawab Modul

Semua kode sumber utama berada di dalam `app/src/main/java/id/tilik/app/`:

```text
id/tilik/app/
├── MainActivity.kt               # Dashboard konfigurasi izin (Overlay, Aksesibilitas, Share Screen)
├── TilikApplication.kt           # Inisialisasi Timber logger & Global App Context
├── capture/
│   ├── ScreenCaptureManager.kt   # Pengelola MediaProjection, VirtualDisplay, & ImageReader
│   └── AudioBufferRecorder.kt    # Perekam audio internal (opsional)
├── detection/
│   └── StockKeywordDetector.kt   # Kamus emiten BEI (BBRI, BBCA, GOTO, AMMN, dll.) & regex deteksi klaim
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
    ├── theme/                    # Color tokens, Typography, dan Theme Compose
    └── overlay/
        ├── OverlayWindowManager.kt # WindowManager native Android, gesture physics, drag clamp, auto-tuck
        ├── FloatingBubbleView.kt   # Compose UI bubble melayang, chevron dock tabs, status capsules
        ├── ClaimBottomSheet.kt    # Grammarly modal bottom sheet, bidirectional drag-up/down, clipboard sync
        └── VerdictCardView.kt      # Kartu hasil vonis bursa Sectors API & swipe-down to dismiss
```

---

## 3. 🫧 Aturan Gestur & State Machine Floating Bubble

1. **Snap-to-Edge (Wajib Mepet ke Ujung Terdekat):**
   - Bubble **TIDAK BOLEH** berhenti melayang di tengah layar sembarangan setelah digeser pengguna.
   - Begitu jari dilepaskan (`ACTION_UP` atau `ACTION_CANCEL`), hitung titik tengah bubble terhadap lebar layar:
     - Jika berada di separuh kiri layar (`bubbleCenterX < screenWidth / 2`) -> otomatis meluncur dan menempel ke **tepi kiri layar** (`x = 0`).
     - Jika berada di separuh kanan layar (`bubbleCenterX >= screenWidth / 2`) -> otomatis meluncur dan menempel ke **tepi kanan layar** (`x = screenWidth - bubbleWidth`).
   - Gunakan animasi fisika `ValueAnimator` dengan `DecelerateInterpolator` durasi ~280ms.
2. **Ambang Sentuh (Drag Threshold) Sangat Rendah (8px):**
   - Jangan gunakan ambang drag yang terlalu besar (misal 24px) yang menyebabkan bubble terasa "stuck/keras". Gunakan **8px** agar responsif seketika.
   - Bedakan *tap* (ketukan) dan *drag* (geser): Jika durasi sentuhan < 280ms dan jarak tempuh < 12px, perlakukan sebagai klik untuk membuka sheet.
3. **Idle Auto-Tuck & Dimming (Setelah 3 Detik Diam):**
   - Jika bubble berada dalam status `IDLE` (tidak sedang menganalisis) dan tidak disentuh selama **3 detik**, bubble wajib:
     - **Mengecil**: Skala ~86%.
     - **Meredup**: Alpha ~42% (`0.42f`).
     - **Ngumpet (Tuck-in)**: Bergeser masuk ke bezel samping layar.
     - **Chevron Edge Tab**: Tulisan teks "TILIK" **TIDAK BOLEH terpotong jelek oleh bezel**. Saat tucked, gantikan teks dengan ikon panah indikator (*chevron tab*) yang mengarah ke dalam layar (`>` di tepi kiri, `<` di tepi kanan).
   - **Instan Wake Up**: Begitu tab disentuh atau digeser, bubble langsung bangun seketika ke skala 100%, alpha 1.0, dan teks brand "TILIK" tampil utuh.
4. **Anti-Overflow Clamping (Proteksi Batas Layar):**
   - Saat bubble berada di sisi kanan layar dan membesar menjadi kapsul proses (*"Menilik bursa..."*) atau badge emiten, koordinat `x` **wajib di-clamp**:
     ```kotlin
     params.x = params.x.coerceIn(marginHorizontalPx, screenWidth - viewWidth - marginHorizontalPx)
     ```
   - Tidak boleh ada teks atau ikon yang meluber keluar layar atau terpotong oleh bezel kanan.
5. **Anti-Tuck Saat Proses Berlangsung:**
   - Selama proses verifikasi Sectors API sedang berjalan (`ANALYZING` atau `RESULT`), idle timer wajib dihentikan agar bubble tetap tampil penuh dan tidak ngumpet.

---

## 4. 📄 Aturan Modal Bottom Sheet (Grammarly UI & Bidirectional Drag)

1. **Animasi Slide-Up Ease In-Out:**
   - Masuk dari bawah menggunakan `slideInVertically` dengan `CubicBezierEasing(0.18f, 0.9f, 0.2f, 1.0f)`.
   - Latar belakang scrim semi-transparan hitam (`dynamicScrimAlpha`) memudar (*fade in/out*) mengikuti tarikan jari.
2. **Auto-Close Cerdas (Interaksi Luar Otomatis Menutup Sheet):**
   - Jika pengguna melakukan gestur **Swipe Up ke Home**, menekan tombol **Back**, membuka **Recent Apps**, atau mengetuk **area luar (Scrim)**, bottom sheet **WAJIB langsung tertutup seketika** kembali ke bubble.
   - Diproteksi oleh `homeKeyReceiver` (`ACTION_CLOSE_SYSTEM_DIALOGS`), `ViewTreeObserver.addOnWindowFocusChangeListener`, dan `onBackKeyListener`.
3. **Gestur Bidirectional Drag (Tarik Atas & Bawah):**
   - **Mode Standar (`!isExpanded`):**
     - Ditarik ke atas $\ge$ 45dp atau flick ke atas -> **Beralih ke mode lebih tinggi (Expanded)**.
     - Ditarik ke bawah $\ge$ 90dp atau flick ke bawah -> **Menutup sheet (Dismiss)**.
     - Ditarik sedikit -> Membal kembali ke posisi semula via animasi `spring`.
   - **Mode Expanded (`isExpanded == true`):**
     - Ditarik ke bawah sedang (~60dp) -> **Kembali ke mode ukuran standar**.
     - Ditarik jauh ke bawah ($\ge$ 160dp) atau flick keras ke bawah -> **Menutup sheet (Dismiss)**.
4. **Auto-Expand pada Teks Panjang:**
   - Jika teks yang disalin atau diketik memiliki **$\ge$ 4 baris** atau **$\ge$ 200 karakter**, sheet **secara otomatis memperluas diri lebih tinggi** (`isExpanded = true`).
5. **Dynamic Height TextField:**
   - Kotak input klaim menggunakan:
     ```kotlin
     minLines = if (isExpanded) 7 else 2,
     maxLines = if (isExpanded) 14 else 5,
     modifier = Modifier.fillMaxWidth().animateContentSize()
     ```
   - Sediakan tombol pintas **"Perluas ⤢"** / **"Perkecil ⤡"** di samping tombol "Segarkan" dan jadikan drag handle bar di atas bisa diketuk langsung untuk beralih mode.

---

## 5. 📋 Aturan Real-Time Clipboard Auto-Detection

1. **Live Synchronization (Tanpa Perlu Tekan "Segarkan"):**
   - Gunakan `ClipboardManager.OnPrimaryClipChangedListener` di level Composable `ClaimBottomSheet`.
   - Jalankan urutan polling otomatis (0ms, 80ms, 200ms, 450ms, 800ms) saat sheet dibuka untuk mengantisipasi keterlambatan penulisan clipboard oleh aplikasi pihak ketiga (Threads / WhatsApp).
2. **Proteksi Input Manual Pengguna:**
   - Gunakan flag `hasUserManuallyEdited`. Jika pengguna telah mengetik manual di dalam kotak teks, auto-clipboard tidak boleh menimpa tulisan pengguna secara sepihak.

---

## 6. 🔒 Aturan Keamanan Kredensial & Secrets (.env)

1. **LARANGAN KERAS Hardcoding Secrets:**
   - Jangan pernah menuliskan API key asli, token produksi, password, atau IP statis di dalam file Kotlin (`.kt`) atau file resource (`.xml`).
2. **Pemuatan Konfigurasi Dinamis di `build.gradle.kts`:**
   - Gunakan fungsi `loadConfigValue` di `app/build.gradle.kts` yang membaca file `.env` di folder proyek, root, atau `local.properties`:
     - `TILIK_BASE_URL` (default: `http://10.0.2.2:8000/`)
     - `SECTORS_API_KEY` (Sectors Financial API key)
     - `GEMINI_API_KEY` (Gemini API key)
     - `ENVIRONMENT` (`development` / `production`)
3. **Proteksi `.gitignore`:**
   - Semua variasi file `.env` (`.env`, `.env.*`) dan `local.properties` **WAJIB diabaikan oleh Git**.
   - Hanya template `!.env.example` yang diizinkan untuk di-commit.
4. **Auth Interceptor Otomatis:**
   - Di `ApiClientProvider.kt`, sertakan `authInterceptor` yang otomatis menyuntikkan header `Authorization` dan `X-API-KEY` jika `BuildConfig.SECTORS_API_KEY` tersedia.

---

## 7. 🐙 Aturan Repository Git & Struktur Proyek

1. **Root Git Berada di Folder Tilik:**
   - Repository Git **WAJIB diinisialisasi langsung di dalam folder `tilik`** (`d:\UB\Sectors\tilik`), BUKAN di parent folder `d:\UB\Sectors`.
   - GitHub repo `willgregorry/tilik` harus menampilkan langsung `app/`, `gradle/`, `README.md`, `PANDUAN_TESTING.md`, `.env.example`, `.gitignore`.
2. **Filtrasi File Cache & Intermediate:**
   - Jangan pernah meng-commit folder `.gradle/`, `build/`, `app/build/`, `.idea/`, `.kotlin/`, atau file `*.log`.
   - Jumlah file yang di-commit harus selalu ringkas dan murni file proyek (belasan hingga puluhan file, BUKAN ribuan file).

---

## 8. 🧪 Aturan Pengujian & Kolaborasi dengan User

1. **JANGAN Menguji di HP Terlalu Lama Sendirian:**
   - Jangan menghabiskan turn agent untuk terus-menerus melakukan pengujian via adb shell/screencap yang rawan gagal dan memakan waktu lama.
   - Cukup pastikan kompilasi Gradle berhasil (`BUILD SUCCESSFUL`), deploy APK ke HP user, lalu **berikan petunjuk jelas agar user yang menguji langsung di HP fisiknya**.
2. **Wajib Memelihara `PANDUAN_TESTING.md`:**
   - Setiap kali menambahkan fitur baru atau memperbaiki bug gestur/UI, selalu tambahkan skenario baru ke dalam [PANDUAN_TESTING.md](file:///d:/UB/Sectors/tilik/PANDUAN_TESTING.md) dengan bahasa Indonesia yang jelas, runut, dan dilengkapi "Hasil yang Diharapkan".
