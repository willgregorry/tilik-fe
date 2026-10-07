# 📱 PANDUAN PENGUJIAN (TESTING GUIDE) - TILIK OVERLAY & CLIPBOARD

Dokumen ini memandu Anda untuk menguji **fitur dan desain baru** yang baru saja diperbaiki dan dipasang di HP Anda:
1. **Floating Bubble Snap to Edge (Mepet ke Ujung Terdekat)**: Saat digeser dan dilepaskan, bubble otomatis mepet/menempel halus (*snap*) ke tepi kiri atau kanan layar yang paling dekat.
2. **Desain Modal Bottom Sheet ala Grammarly** (Latar gelap `#181A1D`, ilustrasi easel kanvas & kuas lukis, *"No pressure. Suggestion will appear here."*, logo teal `T`, sparkle icon, dan pill button).
3. **Auto-Detect Clipboard Teks Baru** (Saat salin teks ke-2, otomatis muncul teks ke-2 tanpa perlu tekan "Segarkan").
4. **Alur Izin & Share Screen** (Otomatis aktif saat buka app, dan berhenti saat layanan dimatikan).

---

## 🛠️ Ringkasan Perbaikan yang Diterapkan di Kode

| Bagian | Masalah Sebelumnya | Solusi yang Diterapkan |
|---|---|---|
| **Floating Bubble (Snap to Edge)** | Kadang tidak bisa digeser atau berhenti sembarangan di tengah layar. | Ambang drag diturunkan ke **8px** agar responsif seketika. Saat dilepaskan, sistem menghitung posisi tengah bubble: jika berada di sisi kiri layar maka mepet ke tepi kiri, jika di sisi kanan maka mepet ke tepi kanan menggunakan animasi halus (`ValueAnimator` + `DecelerateInterpolator`). Klik tetap diproteksi (tap < 280ms & geser < 12px). |
| **Clipboard Teks Ke-2** | Teks lama masih nyangkut karena `remember(initialText)` mempertahankan state lama dan aplikasi sumber (WhatsApp/Threads) butuh beberapa milidetik untuk menulis ke clipboard sistem. | Ditambahkan **Live Listener** (`OnPrimaryClipChangedListener`), **Polling Sequence** (0ms, 80ms, 200ms, 450ms, 800ms, 1400ms), serta `inputSessionId` baru setiap kali sheet terbuka. |
| **Share Screen & Setelan** | Harus verifikasi manual berulang kali dan bingung opsi 10 detik. | Aplikasi otomatis mengaktifkan service saat dibuka. Disediakan tombol langsung ke **Setelan Dibatasi** (hitung mundur 10 detik) dan saat tombol merah ditekan, share screen langsung berhenti. |

---

## 🧪 Langkah-Langkah Pengujian Mandiri di HP

### Skenario 1: Uji Floating Bubble (Snap to Edge / Mepet ke Tepi Layar)
1. Buka aplikasi **Tilik** di HP Anda.
2. Pastikan bubble bertuliskan **TILIK** muncul melayang di layar.
3. **Uji Geser ke Kiri:**
   - Geser bubble ke arah tengah agak ke kiri, lalu **lepaskan jari Anda**.
   - **Hasil yang Diharapkan:** Bubble otomatis meluncur dan mepet rapi ke tepi layar sebelah **kiri**.
4. **Uji Geser ke Kanan:**
   - Geser bubble ke arah tengah agak ke kanan, lalu **lepaskan jari Anda**.
   - **Hasil yang Diharapkan:** Bubble otomatis meluncur dan mepet rapi ke tepi layar sebelah **kanan**.
5. **Uji Ketuk (Buka Sheet):**
   - Ketuk sekali (*tap*) pada bubble tanpa menggesernya.
   - **Hasil yang Diharapkan:** Modal Bottom Sheet terbuka dari bawah layar.

---

### Skenario 2: Uji Auto-Detect Clipboard (Teks Pertama & Teks Kedua)
1. **Langkah 1 (Copy Teks Pertama):**
   - Buka aplikasi **WhatsApp**, **Threads**, atau **Chrome**.
   - Salin (Copy) satu kalimat atau pesan, misalnya:
     > *"BBRI laba kuartal ini tembus rekor dan asing akumulasi masif"*
   - Jika tombol "Salin" ditekan, Bottom Sheet Tilik akan otomatis meluncur (atau ketuk floating bubble jika sheet tertutup).
   - **Hasil yang Diharapkan:** Teks pertama langsung terisi di dalam kotak input dengan banner *"Tersalin Otomatis dari Threads/Clipboard"*.
2. **Langkah 2 (Tutup Sheet):**
   - Ketuk area gelap di luar sheet (scrim) atau tombol silang (X) di kanan atas sheet untuk me-minimize kembali ke bubble.
3. **Langkah 3 (Copy Teks Kedua yang Berbeda):**
   - Pilih pesan atau kalimat lain yang sama sekali berbeda, misalnya:
     > *"GOTO catat efisiensi operasional dan rencana buyback saham"*
   - Salin (Copy) kalimat kedua tersebut.
   - Buka kembali Bottom Sheet (bisa otomatis atau ketuk bubble Tilik).
   - **Hasil yang Diharapkan:** 
     - Kotak input **langsung menampilkan teks kedua** (*"GOTO catat efisiensi..."*).
     - Anda **TIDAK PERLU** menekan tombol *"Segarkan"* lagi! Teks langsung tersinkronisasi otomatis.

---

### Skenario 3: Uji Pengaturan Izin 10 Detik & Share Screen
Khusus di HP Android dengan sistem keamanan seperti **OriginOS (Vivo)** atau **HyperOS/MIUI (Xiaomi)**:
1. Buka aplikasi **Tilik**.
2. Pada kartu **STATUS PERIZINAN & SHARE SCREEN**:
   - Jika ingin mengaktifkan izin permanen: Ketuk **Buka Info** di baris *"Setelan Dibatasi (Tunggu 10 Detik)"*.
   - Pada halaman info aplikasi yang terbuka, ketuk **titik tiga di pojok kanan atas** -> pilih **Izinkan setelan yang dibatasi (Allow restricted settings)**.
   - Masukkan PIN HP Anda / tunggu hitung mundur 10 detik lalu pilih **Izinkan**.
   - *(Setelah langkah ini selesai sekali, izin latar belakang dan aksesibilitas aktif permanen tanpa perlu diminta ulang).*
3. **Share Screen Otomatis:**
   - Saat aplikasi Tilik dibuka, service otomatis berjalan.
   - Jika dialog *"Mulai merekam atau mentransmisikan?"* muncul, pilih *"Seluruh layar (Entire screen)"* -> *"Mulai sekarang"*.
4. **Mematikan Layanan:**
   - Ketuk tombol merah **"Hentikan Layanan Tilik"**.
   - **Hasil yang Diharapkan:** Floating bubble langsung hilang dari layar, dan rekaman/tangkapan layar otomatis dihentikan (*stop projection*).

---

### Skenario 4: Uji Desain Modal Bottom Sheet ala Grammarly
1. Pastikan floating bubble Tilik ada di layar.
2. **Uji Tampilan Kosong (Empty State ala Grammarly):**
   - Ketuk floating bubble Tilik saat tidak ada teks di clipboard (atau setelah menekan tombol "Hapus").
   - **Hasil yang Diharapkan:**
     - Header atas: Bar abu-abu bulat di tengah (drag handle), logo lingkaran hijau toska **T** di kiri dengan teks *"TILIK AI"* & *"Sectors Stock Fact-Checker"*, serta ikon **Sparkle AI** dan tombol silang (X) di kanan.
     - Di tengah: Ilustrasi kanvas easel lukis kayu dengan kuas berujung hijau/teal.
     - Teks tebal putih: **"No pressure."**
     - Subjudul abu-abu: **"Suggestion will appear here."**
     - Dua tombol chip sampel cepat di bawah: `📈 $BBRI Akumulasi Asing` dan `🚀 $GOTO Rekor Profit`.
3. **Uji Transisi ke Suggestion Card:**
   - Ketuk salah satu chip sampel (misal: `📈 $BBRI Akumulasi Asing`) atau lakukan Salin (Copy) teks saham dari WhatsApp/Threads.
   - **Hasil yang Diharapkan:**
     - Tampilan kosong otomatis berganti menjadi kartu saran gelap berbingkai halus (`#202227`).
     - Muncul badge emiten hijau toska: `🎯 EMITEN: $BBRI`.
     - Tombol aksi utama di bawah berupa pill button penuh warna hijau toska khas Grammarly: **"Periksa Fakta di Sectors API"**.

---

### Skenario 5: Uji Animasi Ease In-Out & Auto-Close (Swipe Up / Keluar App)
1. **Animasi Ease In-Out Slide Up:**
   - Ketuk floating bubble Tilik untuk membuka sheet.
   - **Hasil yang Diharapkan:** Bottom Sheet meluncur naik dari bawah layar dengan kurva *ease in-out* yang mulus (`CubicBezier`), dan latar belakang hitam meredup secara halus (*fade in*).
2. **Auto-Close saat Swipe Up ke Home:**
   - Saat Bottom Sheet sedang terbuka di layar, lakukan gestur navigasi **Swipe Up dari tepi bawah layar** untuk kembali ke layar Home HP Anda.
   - **Hasil yang Diharapkan:** Bottom sheet **otomatis tertutup dan kembali ke bubble** (*auto-close* seketika). Layar Home Anda bersih tanpa sheet yang menghalangi.
3. **Auto-Close saat Beralih Aplikasi (Recent Apps):**
   - Buka kembali Bottom Sheet, lalu buka menu Recent Apps atau beralih ke aplikasi lain.
   - **Hasil yang Diharapkan:** Karena interaksi dilakukan di luar floating app, sheet langsung otomatis menutup ke bubble.
4. **Auto-Close saat Tekan Tombol Back (Kembali):**
   - Buka Bottom Sheet, lalu lakukan gestur Back (swipe dari tepi layar) atau tekan tombol Back fisik/navigasi.
   - **Hasil yang Diharapkan:** Sheet langsung tertutup dengan animasi turun ke bawah.
5. **Auto-Close saat Sentuh Luar Sheet (Scrim):**
   - Ketuk area gelap di luar sheet.
   - **Hasil yang Diharapkan:** Sheet meluncur turun ke bawah dan latar belakang kembali terang secara halus (*fade out*).
6. **Drag Down untuk Menutup (Swipe Down to Dismiss):**
   - Sentuh area atas sheet (handle bar / header bar atau area ilustrasi kosong), lalu geser (*drag*) jari Anda ke arah bawah.
   - **Hasil yang Diharapkan:**
     - Seluruh modal sheet meluncur ke bawah mengikuti jari Anda secara *real-time*.
     - Latar belakang gelap (*scrim*) otomatis memudar mengikuti jarak tarikan jari.
     - Jika ditarik ke bawah lebih dari ~90dp atau disentak (*flick*) ke bawah, sheet langsung meluncur mulus keluar layar dan otomatis tertutup (*dismiss*).
     - Jika dilepas sebelum batas tarikan, sheet akan membal (*spring bounce*) kembali ke posisi atas dengan animasi fisika yang halus.

---

### Skenario 6: Uji Idle Auto-Tuck & Dimming (Mengecil, Redup, & Ngumpet di Samping)
1. **Uji Auto-Tuck saat Diam (Inactivity):**
   - Biarkan bubble berada di tepi layar (kiri atau kanan) tanpa disentuh selama **3 detik** (baik saat awal buka, setelah geser bubble, ataupun setelah menutup Bottom Sheet).
   - **Hasil yang Diharapkan:**
     - Bubble otomatis **mengecil** (skala 86%).
     - Opacity / transparansi **meredup** (alpha ~42%) agar tidak mengganggu pandangan konten aplikasi lain.
     - Bubble **meluncur masuk sebagian** (*tuck-in*) ke tepi/bezel layar.
     - **Tampilan Premium Edge Tab:** Tulisan "TILIK" tidak terpotong jelek oleh bezel, melainkan bertransisi halus menjadi **indikator panah (*chevron tab*) yang mengarah ke dalam layar** (`>` jika di sisi kiri, `<` jika di sisi kanan) yang menandakan tab siap ditarik/diketuk.
2. **Uji Wake Up Seketika (Sentuh/Ketuk):**
   - Sentuh atau ketuk tab bubble yang sedang ngumpet di pinggir layar tersebut.
   - **Hasil yang Diharapkan:**
     - Bubble **langsung bangun seketika** (*wake up*): kembali ke ukuran penuh (100%), opacity terang penuh (1.0), ikon panah berganti kembali ke teks brand "TILIK", dan meluncur keluar dari tepi layar.
     - Jika diketuk langsung, Modal Bottom Sheet langsung terbuka mulus ke atas.
3. **Uji Drag saat Tucked:**
   - Sentuh dan langsung geser tab bubble yang sedang ngumpet -> bubble langsung bangun dan mengikuti jari Anda dengan mulus ke posisi baru tanpa patah-patah.
   - Saat dilepas, bubble mepet ke tepi terdekat dan setelah 3 detik akan otomatis ngumpet kembali.

---

### Skenario 7: Uji Anti-Overflow (Kapsul Proses "Menilik bursa..." & Ticker Tidak Terpotong)
1. **Latar Masalah Sebelumnya (Screenshot):**
   - Sebelumnya, saat bubble berada di sisi kanan layar lalu membesar menjadi kapsul proses (*"Menilik bursa..."* atau badge emiten), sisi kanannya menabrak batas layar dan terpotong (*overflow* / kepotong teksnya).
2. **Uji Coba Saat Proses Berjalan:**
   - Geser bubble ke **tepi kanan layar**.
   - Buka Bottom Sheet, pilih salah satu chip sampel (misal `$BBRI`), lalu tekan tombol **"Periksa Fakta di Sectors API"**.
   - Perhatikan kapsul proses *"Menilik bursa..."* yang muncul di layar.
   - **Hasil yang Diharapkan:**
     - Kapsul proses otomatis bergeser ke kiri (*auto-clamp*) menyesuaikan panjang teksnya.
     - Seluruh teks *"Menilik bursa..."* dan ikon loading berputar **100% utuh berada di dalam layar** dengan jarak aman (*margin*) dari tepi kanan, **tidak ada teks yang terpotong sama sekali**.
3. **Uji Coba Badge Emiten Saham:**
   - Salin teks saham dari Threads / WhatsApp saat bubble di kanan layar.
   - **Hasil yang Diharapkan:** Kapsul badge emiten (misal `📈 BBRI DARI CLIPBOARD`) tampil penuh dan rapi di dalam layar tanpa keluar atau terpotong ke kanan.
4. **Anti-Tuck Saat Proses Berlangsung:**
   - Selama proses analisis / verifikasi API sedang berjalan, bubble TIDAK AKAN ngumpet atau mengecil, melainkan tetap tampil utuh di layar sampai proses selesai.

---

### Skenario 8: Uji Drag-Up Expand (Tarik ke Atas untuk Memperluas Sheet / Teks Panjang)
1. **Fitur Drag-Up (Tarik ke Atas):**
   - Buka Modal Bottom Sheet Tilik (bisa ketuk floating bubble).
   - Sentuh area bar atas (*drag handle*) atau bagian atas kartu, lalu geser jari Anda **ke arah atas** (*drag up*).
   - **Hasil yang Diharapkan:**
     - Modal sheet otomatis meluncur dan **tampil lebih tinggi (expanded mode)** dengan animasi per membal halus (*spring animation*).
     - Kotak teks input otomatis membesar (menampung hingga 14 baris teks sekaligus) sehingga teks panjang dari Threads atau WhatsApp bisa dibaca dan diedit dengan sangat leluasa.
     - Handle bar atas berubah warna toska lembut sebagai indikator mode expanded.
2. **Uji Auto-Expand saat Salin Teks Panjang:**
   - Salin sebuah paragraf panjang (misal analisis saham dari Threads atau WhatsApp yang terdiri dari 4+ baris atau 200+ karakter).
   - Buka sheet (atau biarkan auto-detect mendeteksinya).
   - **Hasil yang Diharapkan:** Sheet **otomatis memperluas diri lebih tinggi** tanpa perlu Anda tarik manual!
3. **Uji Tombol Toggle "Perluas ⤢" / "Perkecil ⤡" & Handle Tap:**
   - Di kartu input di samping tombol "Segarkan", terdapat tombol cepat **"Perluas ⤢"** / **"Perkecil ⤡"**.
   - Anda juga bisa mengetuk (*tap*) **drag handle bar di tengah atas** sheet.
   - **Hasil yang Diharapkan:** Sheet bertransisi dengan mulus antara mode ringkas (compact) dan mode tinggi (expanded).
4. **Uji Drag-Down saat Mode Expanded:**
   - Saat sheet sedang dalam mode tinggi (*expanded*):
     - Tarik sedikit ke bawah (~60dp) -> sheet kembali ke ukuran standar (*collapsed*).
     - Tarik jauh ke bawah atau sentak cepat ke bawah -> sheet langsung tertutup (*dismiss*).

---

### Skenario 10: Uji Desain Modern 21st Dev Mobile (Neutral Light, Logo Indigo, TopBar & Menubar)
1. **Buka Aplikasi Tilik:**
   - Buka aplikasi **Tilik** di HP Anda.
   - **Hasil yang Diharapkan:**
     - Latar belakang kini bersih netral (**Clean Off-White** `#F8FAFC`) dengan kartu putih bersih (`#FFFFFF`) dan hairline border halus (`#E2E8F0`).
     - **TIDAK ADA LAGI** teks "Her 75", tidak ada AI slop, dan tidak ada emoji sama sekali. Semua menggunakan **Vector Icons** resmi.
2. **Uji Clean TopBar (Header Aplikasi):**
   - Di sisi **kiri**: Logo Tilik resmi dengan teks judul *"Tilik"* dan *"Pemeriksa Fakta Saham"*.
   - Di sisi **kanan**: Tombol avatar profil lingkaran bersih dengan aksen Indigo.
3. **Uji Menubar Bawah (Bottom Navigation Bar):**
   - Di bagian bawah layar terdapat **Menubar**:
     - Tab **"Beranda"** (Ikon Home): Menampilkan kontrol widget melayang, checklist kesiapan sistem, dan ringkasan data pasar BEI.
     - Tab **"Riwayat"** (Ikon History): Menampilkan daftar riwayat pemeriksaan klaim saham tersimpan dengan filter pill (`Semua`, `Valid`, `Meragukan`, `Tidak Valid`).
   - Ketuk tab **"Riwayat"**: Layar berganti menampilkan kartu verifikasi saham ($BBRI, $GOTO, $BBCA, $AMMN) beserta data net foreign dan valuasi PE/PBV resmi Sectors API.
4. **Uji Status Badge Netral & Warna Sesuai Logo:**
   - Aksen utama menggunakan warna **Deep Indigo** (`#4F46E5`) yang selaras persis dengan logo Tilik.
   - Badge status (`Aktif`, `Standby`, `Valid`, `Tidak Valid`) berpenampilan netral (background abu-abu lembut `#F1F5F9` dengan teks warna status solid, tanpa border mencolok atau opacity 15%).

---

### Skenario 11: Uji Tipografi DM Sans di Seluruh Aplikasi
1. **Periksa Teks di Seluruh Halaman:**
   - Perhatikan jenis huruf (*font*) pada TopBar (*"Tilik"*, *"Pemeriksa Fakta Saham"*), judul kartu, tombol, hingga Menubar bawah.
   - **Hasil yang Diharapkan:**
     - Seluruh tipografi aplikasi kini menggunakan font **DM Sans** (bersih, geometris, modern, dan sangat nyaman dibaca).
     - Font dibundle langsung secara offline di dalam APK (`res/font/dm_sans.ttf`) dengan dukungan bobot lengkap (*Regular*, *Medium*, *SemiBold*, *Bold*, hingga *Black*).

---

### Skenario 12: Uji Card & Icon Refactoring (No-Box Icons, Grouped Settings & Tabular Metrics)
1. **Periksa Kartu Kesiapan Sistem:**
   - Perhatikan baris-baris perizinan di kartu *"KESIAPAN SISTEM"*.
   - **Hasil yang Diharapkan:**
     - **TIDAK ADA LAGI** kotak/box rounded dengan background opacity rendah di sekeliling setiap ikon.
     - Ikon tampil langsung (*direct inline icon*) berukuran 20dp yang rapi dan terhubung dengan teks judul serta subtitle.
     - Setiap baris dipisahkan oleh garis tipis (*divider*) 0.8dp ala grouped table/settings di iOS & Linear.
     - Status izin menampilkan teks *"✓ Aktif"* bersih warna hijau solid atau tombol *"Izinkan"* / *"Bagikan"* berwarna Indigo solid.
2. **Periksa Kartu Integrasi Pasar:**
   - Bagian metrik kini menggunakan format 2-kolom bersih (*tabular column*) tanpa rounded box buatan di dalam kartu:
     - `900+ Emiten` (Cakupan Saham BEI) | `Real-time` (Arus Broker Asing).
     - Footer jaminan verifikasi menampilkan ikon verifikasi inline yang elegan.
3. **Periksa Kartu Riwayat Pemeriksaan:**
   - Kode emiten ($BBRI, $GOTO, dll.) tampil bersih menyatu dengan nama perusahaan tanpa kotak warna buatan.

---

---

### Skenario 13: Uji Hasil Pemeriksaan Fakta Bursa (Verdict Bottom Sheet ala OVO & GoPay)
Skenario ini menguji penyajian hasil pemeriksaan fakta saham bergaya modern fintech (seperti OVO & GoPay) yang bersih, lega, bebas dari "AI slop", dan nyaman dibaca (scrollable):
1. **Langkah Persiapan:**
   - Backend FastAPI berjalan di laptop pada port 8000.
   - Dual-host fallback aktif (koneksi otomatis melalui Wi-Fi LAN IP http://10.110.120.199:8000/ atau adb reverse tcp:8000 tcp:8000).
2. **Uji Periksa Klaim Saham:**
   - Di aplikasi Threads, WhatsApp, atau ketik langsung di Claim Bottom Sheet Tilik, salin/ketik teks:
     > *"BBRI asing kabur jualan terus ratusan miliar, harga saham overvalued dan mau anjlok!"*
     atau
     > *"Si ijo mulai diserok bandar YP, valuasi salah harga to the moon!"*
   - Ketuk tombol ungu **"Periksa Fakta di Sectors API"**.
   - **Hasil yang Diharapkan:**
     - Bubble melayang berubah menjadi kapsul putih bersih berborder ungu dengan tulisan *"Menilik bursa..."* yang kontras dan jelas (tidak ada teks gelap di background gelap).
     - Begitu data bursa diterima, muncul **Bottom Sheet Hasil Pemeriksaan Fakta** setinggi ~86% layar dengan background putih bersih (`#FFFFFF`):
       - **Header Atas**: Kode ticker bersih (misal `BBRI` atau `GOTO` dalam badge ungu lembut tanpa emoji), nama emiten (`Bank Rakyat Indonesia Tbk`), dan tombol bulat 'X' di kanan.
       - **Hero Status Banner (ala GoPay / OVO)**: Kartu status berlatar pastel elegan dengan ikon status bersih (`CheckCircle`, `WarningAmber`, atau `Warning`), judul vonis bursa (*"Klaim Terverifikasi Sesuai Data"* / *"Klaim Perlu Diwaspadai"* / *"Klaim Berpotensi Menyesatkan"*), chip akurasi analisis (`Akurasi 92%`), dan catatan analisis berbahasa Indonesia yang jernih dan nyaman dibaca.
       - **Klaim yang Dianalisis**: Kutipan teks yang diuji dalam kartu slate bersih.
       - **Pemeriksaan Fakta**: Poin-poin verifikasi terpisah rapi dengan ikon indikator jelas (hijau/merah) dan teks penjelasan yang lega (13.5sp, tidak berdesakan).
       - **Data Pasar & Finansial**:
         - *Arus Modal Asing*: Nilai Foreign Net IDR tebal (e.g. `+Rp 9,8 Miliar` hijau / `-Rp 15,2 Miliar` merah), ringkasan flow, dan perbandingan broker Top Buyers vs Top Sellers.
         - *Valuasi Saham*: Nilai PBV & PER vs Median Industri beserta tag status valuasi.
         - *Kesehatan Finansial & Notasi*: Pertumbuhan laba YoY, arus kas operasi, status papan FCA, dan notasi khusus BEI.
       - **Footer & Tombol Aksi**: Di bagian paling bawah sheet terdapat tombol utama berwarna ungu **"Tutup"** yang selalu *pinned* dan mudah dijangkau satu jempol.
3. **Uji Kemudahan Membaca (*Scrollable*):**
   - Geser layar naik-turun pada area konten.
   - **Hasil yang Diharapkan:** Halaman dapat di-*scroll* dengan sangat halus dan lega. Teks tidak dipadatkan ke dalam kotak kecil berukuran mini, melainkan berukuran 13–16sp yang sangat nyaman dibaca.
4. **Uji Gestur Menutup Sheet:**
   - Ketuk tombol **"Tutup"** di bawah, ATAU geser (*swipe down*) drag handle di bagian atas sheet, ATAU ketuk area gelap (scrim) di luar sheet.
   - **Hasil yang Diharapkan:** Sheet menutup seketika dan kembali ke floating bubble melayang.

---

### Skenario 14: Uji Tampilan Layout Lega Bebas Desak-Desakan (Un-Cramped Fintech Layout)
Skenario ini memverifikasi bahwa seluruh data bursa di Bottom Sheet hasil tampil lega, rapi, dan tidak ada teks yang terpotong/terdesak:
1. **Periksa Kartu Arus Modal Asing (Top Buyers & Top Sellers):**
   - Perhatikan bagian daftar broker *Top Buyers* dan *Top Sellers*.
   - **Hasil yang Diharapkan:**
     - **TIDAK ADA LAGI** teks nilai rupiah yang terpotong menjadi 2 baris (misal `"Rp 24,1"` di baris atas dan `"Miliar"` di baris bawah).
     - Setiap broker ditampilkan dalam **satu baris penuh (full width)** yang terpisah:
       - Di kiri: Badge kode broker (misal `BK`, `YP`, `MG`) dengan teks tipe broker (`Asing` warna biru / `Domestik` warna abu-abu).
       - Di kanan: Nilai transaksi lengkap dalam satu baris monospaced tebal (misal `+Rp 15,2 Miliar` hijau atau `-Rp 24,1 Miliar` merah).
2. **Periksa Kartu Valuasi Saham:**
   - Perhatikan judul *"Valuasi Saham"* dan kotak rasio metrik.
   - **Hasil yang Diharapkan:**
     - Judul *"Valuasi Saham"* berdiri sendiri dengan bersih di kiri atas kartu.
     - **TIDAK ADA LAGI** badge kuning panjang yang dipaksa sempit di sebelah kanan judul.
     - Dua kotak rasio metrik (`PBV Ratio` dan `PER Ratio`) tampil lega bersanding dengan angka tebal besar (`2,4x`) dan keterangan perbandingan (`Median Sektor: 1,6x`).
     - Keterangan status valuasi (misal *"Harga Premium (45.5% Lebih Tinggi dari Rata-Rata Industri)"*) ditampilkan sebagai **banner callout lebar penuh** di bawah kotak metrik sehingga kalimat panjang dapat dibaca secara alami dan santai.
3. **Periksa Jarak Bawah (*Bottom Clearance*):**
   - Gulir (*scroll*) konten sheet hingga ke bagian paling bawah.
   - **Hasil yang Diharapkan:**
     - Teks disclaimer (*"Data bersumber dari Sectors Financial API..."*) terlihat seutuhnya di atas tombol *"Tutup"* tanpa tertutup atau terpotong sebagian.
