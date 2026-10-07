# 🤖 GEMINI SYSTEM RULES & PROJECT CONTEXT

Dokumen ini adalah **pedoman resmi bagi Gemini CLI / AI Assistant** saat beroperasi pada repositori **Tilik Android App** (`E:\tilik-fe`). AI **WAJIB membaca dan mematuhi** aturan di bawah ini sebelum membuat atau memodifikasi kode.

---

## 1. 🏛️ Ringkasan Proyek & Filosofi

- **Nama Proyek:** Tilik (Android Native)
- **Teknologi:** Kotlin 2.0, Jetpack Compose (Material 3), Gradle 8.11.1 (Kotlin DSL), Android SDK 35 (Min SDK 26).
- **Fungsi Utama:** Pemeriksa fakta bursa saham (BEI / IDX) instan berbasis *floating overlay widget*, terintegrasi dengan Sectors API.
- **Arsitektur:** Single Activity (`MainActivity.kt`), Foreground Service (`OverlayService.kt`), WindowManager overlay (`OverlayWindowManager.kt`), Accessibility Service (`TilikAccessibilityService.kt`), dan Retrofit client (`ApiClientProvider.kt`).

---

## 2. 🎨 Dark Investment App Design System & Zero-Fluff

Semua UI mematuhi spesifikasi desain di [DESIGN.md](DESIGN.md):

1. **Prinsip Utama (Zero-Fluff, High-Signal):**
   - Hapus semua kalimat basa-basi, pengantar panjang, atau paragraf bertele-tele di layar.
   - Fokus hanya pada metrik, angka, badge status, ticker saham, dan tombol aksi langsung.
   - Panjang deskripsi per baris tidak boleh lebih dari 12 kata.
2. **Palet Warna Sentral (`id.tilik.app.ui.theme.Theme.kt`):**
   - Background Utama: `AppBg` (`#0A0A0A` AMOLED Black).
   - Permukaan Kartu: `AppCard` (`#141414`), Kontainer Sekunder: `AppCardSubtle` (`#1A1A1A`).
   - Hairline Border: `AppBorder` (`Color(0x14FFFFFF)` / 8% putih).
   - Brand Accent: `AppAccent` (`#FF5C35` oranye-merah).
   - Hijau Bullish/Valid: `AppGreen` (`#22C55E`), kontainer `AppGreenBg` (`#0F2115`), border `AppGreenBorder` (`Color(0x3322C55E)`).
   - Merah Bearish/FCA: `AppRed` (`#EF4444`), kontainer `AppRedBg` (`#241113`), border `AppRedBorder` (`Color(0x33EF4444)`).
   - Teks Muted: `AppGray` (`#888888`), `AppGrayDark` (`#737373`).
3. **Glassmorphism Translucency:**
   - Navigasi bawah (`AppBottomNav.kt`) dan top header menggunakan `Color(0xE60A0A0A)` dengan garis pemisah atas 1dp `Color(0x14FFFFFF)`.
4. **Bentuk & Elemen:**
   - Pill badge: `RoundedCornerShape(percent = 50)` (Catatan: Compose tidak mendukung parameter `full`).
   - Tombol aksi saldo: Lingkaran putih kontras tinggi 50dp (`Color.White`) dengan ikon hitam.
   - Kartu portofolio: 24dp squircle dengan kurva sparkline dinamis (`Canvas` bezier + vertical gradient).
   - Item watchlist / notifikasi: 14dp squircle (`RoundedCornerShape(14.dp)`).

---

## 3. 🫧 Aturan Floating Window & Fisika Gestur

1. **Snap-to-Edge:** Bubble melayang wajib otomatis menempel ke tepi kiri (`x = 0`) atau kanan layar (`x = screenWidth - bubbleWidth`) begitu jari dilepas.
2. **Idle Auto-Tuck (3 Detik):** Bubble mengecil (86%), meredup (alpha 0.42), dan bergeser masuk ke bezel samping menampilkan ikon chevron (`>` / `<`). Bangun seketika (100%, alpha 1.0) saat disentuh.
3. **Ambang Drag Rendah (8px):** Ambang sentuh 8px untuk memastikan responsivitas instan tanpa jeda.
4. **Modal Bottom Sheet:**
   - Drag ke atas $\ge$ 45dp -> Masuk mode Expanded.
   - Drag ke bawah $\ge$ 90dp -> Dismiss / tutup sheet.
   - Auto-expand jika teks clipboard $\ge$ 4 baris atau $\ge$ 200 karakter.
   - Auto-close jika pengguna menekan Home, Back, Recent Apps, atau tap di luar sheet.

---

## 4. 🔒 Keamanan Kredensial (.env)

- DILARANG KERAS menuliskan hardcoded API key atau URL produksi di source code.
- Semua konfigurasi dimuat melalui `BuildConfig` yang membaca `.env` atau `local.properties`.
- Pastikan file `.env` dan `local.properties` selalu terabaikan di `.gitignore`.

---

## 5. 🛠️ Tata Cara Kompilasi & Verifikasi

- Perintah verifikasi build: `.\gradlew.bat compileDebugKotlin`
- Perintah assemble APK: `.\gradlew.bat assembleDebug`
- Jangan membuang waktu menguji mandiri secara berulang via screencap/adb shell; setelah build berhasil, arahkan user menguji langsung pada perangkat fisik sesuai skenario di `PANDUAN_TESTING.md`.
