# 🔍 TILIK - Android Stock Fact-Checker Overlay

**Tilik** adalah aplikasi Android berbasis overlay interaktif (*floating widget*) yang berfungsi sebagai pemeriksa fakta (*fact-checker*) klaim saham Bursa Efek Indonesia (BEI / IDX) secara *real-time*. Terintegrasi dengan **Sectors API**, Tilik mendeteksi klaim saham dari media sosial (seperti Threads, Twitter/X, WhatsApp) dan langsung memverifikasi kebenarannya terhadap data fundamental dan transaksi bursa.

---

## ✨ Fitur Unggulan

- **Floating Bubble Pintar (Grammarly / AssistiveTouch Style):**
  - **Snap-to-Edge:** Bubble meluncur menempel ke tepi layar terdekat (kiri/kanan) saat digeser dan dilepaskan.
  - **Auto-Tuck & Dimming:** Mengecil dan ngumpet halus ke samping bezel layar setelah 3 detik tidak aktif (*idle*) dengan indikator panah (*chevron tab*).
  - **Anti-Overflow:** Kapsul status dan badge emiten otomatis menjaga batas layar agar tidak terpotong ke samping.
- **Modal Bottom Sheet ala Grammarly:**
  - Animasi kurva *ease in-out* meluncur dari bawah.
  - **Drag-Up Expand:** Tarik sheet ke atas untuk memperbesar area baca hingga 14 baris untuk teks klaim yang panjang.
  - **Auto-Expand:** Otomatis memperluas diri jika mendeteksi teks salinan panjang ($\ge$ 4 baris atau $\ge$ 200 karakter).
  - **Drag-Down Dismiss / Collapse:** Geser ke bawah untuk mengembalikan ukuran atau menutup sheet.
  - **Auto-Close Cerdas:** Otomatis menutup saat pengguna menekan tombol Home, Back, berganti aplikasi, atau menyentuh latar luar (*scrim*).
- **Auto-Detect Clipboard:**
  - Mendeteksi teks baru di clipboard sistem seketika berkat *Live Clip Listener* tanpa perlu menekan tombol "Segarkan".
- **Verifikasi Real-Time Sectors API:**
  - Menilik laporan keuangan, aksi korporasi, pergerakan harga, dan konsensus saham IDX.

---

## 📋 Prasyarat Sistem (Prerequisites)

Sebelum menjalankan project, pastikan perangkat komputer Anda telah terpasang:

1. **Java Development Kit (JDK):** Versi **17** (disarankan OpenJDK / Temurin 17 / Oracle JDK 17).
2. **Android Studio:** Android Studio Hedgehog / Iguana / Jellyfish / Ladybug atau versi lebih baru.
3. **Android SDK:**
   - Compile SDK: `34` (Android 14)
   - Target SDK: `34`
   - Minimum SDK: `29` (Android 10)
4. **Perangkat Uji:**
   - Smartphone fisik Android (disarankan) dengan fitur **USB Debugging** aktif, atau
   - Android Emulator (API level 29+ dengan Google APIs).

---

## 🚀 Langkah-Langkah Menjalankan Project

### Opsi 1: Menjalankan via Android Studio (Disarankan)

1. **Buka Project:**
   - Buka Android Studio.
   - Pilih menu **File > Open**, lalu arahkan ke folder:
     ```text
     d:/UB/Sectors/tilik
     ```
2. **Sinkronisasi Gradle:**
   - Tunggu proses sinkronisasi Gradle (*Gradle Sync*) selesai mengunduh seluruh *dependencies*.
3. **Hubungkan HP Android:**
   - Sambungkan HP Anda ke laptop via kabel data USB.
   - Aktifkan **USB Debugging** di *Developer Options* HP Anda.
   - Pastikan perangkat Anda terdeteksi di dropdown *Target Device* pada toolbar atas Android Studio.
4. **Jalankan Aplikasi:**
   - Tekan tombol hijau **Run 'app'** (atau shortcut `Shift + F10`).
   - Aplikasi akan otomatis dikompilasi, dipasang (*install*), dan dijalankan di HP Anda.

---

### Opsi 2: Menjalankan via Terminal / CLI

Jika Anda lebih menyukai perintah konsol (Command Prompt / PowerShell):

1. **Masuk ke Direktori Project:**
   ```powershell
   cd d:\UB\Sectors\tilik
   ```

2. **Kompilasi APK Debug:**
   ```powershell
   .\gradlew.bat assembleDebug
   ```

3. **Pasang (Install) APK ke HP:**
   ```powershell
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

4. **Jalankan Aplikasi Tilik:**
   ```powershell
   adb shell am start -n id.tilik.app/.MainActivity
   ```

5. **(Opsional) Pantau Log Real-Time:**
   ```powershell
   adb logcat -s TILIK_MONITOR,TILIK_TERMINAL
   ```

---

## ⚙️ Pengaturan Izin Awal di HP (Penting)

Untuk mengaktifkan fitur overlay melayang dan deteksi layar, berikan izin berikut pada saat pertama kali membuka aplikasi:

1. **Izin Tampilkan di Atas Aplikasi Lain (*Draw over other apps*):**
   - Diperlukan agar floating bubble Tilik bisa muncul melayang di atas aplikasi lain (Threads/WhatsApp).
2. **Izin Layanan Aksesibilitas (*Accessibility Service*):**
   - Diperlukan untuk membaca teks dan mendeteksi interaksi sistem.
   - *Khusus pengguna Vivo (OriginOS) / Xiaomi (HyperOS/MIUI):*
     Jika muncul notifikasi *"Setelan Dibatasi"*, buka **Info Aplikasi Tilik** > ketuk **titik tiga di pojok kanan atas** > pilih **Izinkan setelan yang dibatasi** > masukkan PIN HP / tunggu 10 detik.
3. **Izin Berbagi Layar (*Screen Capture / MediaProjection*):**
   - Saat dialog konfirmasi muncul, pilih **"Seluruh layar" (Entire screen)** lalu ketuk **"Mulai sekarang"**.

---

## 📁 Struktur Direktori Penting

```text
tilik/
├── app/
│   ├── src/main/java/id/tilik/app/
│   │   ├── MainActivity.kt               # Entry point UI dashboard & pengaturan izin
│   │   ├── capture/
│   │   │   └── ScreenCaptureManager.kt   # Pengelola MediaProjection & OCR layar
│   │   ├── detection/
│   │   │   └── StockKeywordDetector.kt   # Regex & kamus deteksi ticker saham IDX
│   │   ├── network/
│   │   │   └── ApiClientProvider.kt      # Integrasi klien Sectors API
│   │   ├── service/
│   │   │   ├── OverlayService.kt         # Foreground Service floating window manager
│   │   │   └── TilikAccessibilityService # Service aksesibilitas teks layar
│   │   └── ui/overlay/
│   │       ├── FloatingBubbleView.kt     # Compose view bubble melayang (snap, tuck, bounce)
│   │       ├── ClaimBottomSheet.kt      # Grammarly modal bottom sheet & drag gestur
│   │       ├── VerdictCardView.kt        # Kartu hasil fact-check Sectors API
│   │       └── OverlayWindowManager.kt   # WindowManager Android native bindings
│   └── build.gradle.kts                  # Konfigurasi dependensi Compose, Material3, Timber
├── PANDUAN_TESTING.md                    # 8 skenario pengujian mandiri di HP fisik
├── .gitignore                            # Aturan filter file Git (exclude .gradle, build, .kotlin)
└── README.md                             # Dokumentasi proyek ini
```

---

## 🧪 Panduan Pengujian

Untuk panduan langkah demi langkah pengujian seluruh 8 skenario (Floating bubble, clipboard auto-detect, Grammarly modal sheet, idle auto-tuck, anti-overflow, dan drag-up expand), silakan baca:
👉 **[PANDUAN_TESTING.md](PANDUAN_TESTING.md)**
