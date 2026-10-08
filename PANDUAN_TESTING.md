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

---

### Skenario 15: Uji Snap-to-Edge (50% Layar), Anti-Overflow Layar, & Favicon Idle Tuck
Skenario ini memverifikasi fisika floating bubble sesuai permintaan terbaru:
1. **Uji Drag & Snap ke Tepi Terdekat (50% Layar):**
   - Sentuh dan geser floating bubble ke area kiri layar (< 50% lebar layar), lalu lepaskan jari.
     - **Hasil yang Diharapkan:** Bubble otomatis meluncur dan menempel (*snap*) ke **tepi kiri layar**.
   - Geser floating bubble ke area kanan layar (>= 50% lebar layar), lalu lepaskan jari.
     - **Hasil yang Diharapkan:** Bubble otomatis meluncur dan menempel (*snap*) ke **tepi kanan layar**.
2. **Uji Anti-Overflow Batas Layar:**
   - Geser bubble sejauh mungkin ke ujung kiri, kanan, atas, maupun bawah layar.
     - **Hasil yang Diharapkan:**
       - Bubble **TIDAK BISA** tembus atau keluar batas layar sama sekali (*zero overflow*).
       - Sisi kanan dan kiri selalu terkunci rapi di dalam batas layar.
       - Sisi atas tidak menabrak jam/status bar, dan sisi bawah tidak menabrak gesture navigation bar.
3. **Uji Favicon Idle Tuck (Tanpa Ikon Panah/Arrow):**
   - Lepaskan bubble dan biarkan menganggur (*idle*) selama 3 detik tanpa disentuh.
     - **Hasil yang Diharapkan:**
       - **TIDAK ADA** ikon panah (`>` / `<`). Ikon tetap menampilkan **Favicon Tilik** resmi.
       - Bubble mengecil halus (skala ~72%, ukuran favicon 30dp) dan meredup transparan (opacity ~38%).
       - Bubble merapat rapi ke samping (*tucked into side edge*).
4. **Uji Instan Wake-Up:**
   - Sentuh kembali bubble yang sedang tucked.
     - **Hasil yang Diharapkan:** Bubble seketika bangun (*wake up*) ke ukuran penuh 100% dan opacity 1.0f, siap digunakan atau digeser kembali.

---

### Skenario 16: Uji Skeleton Shimmer Loading & Fallback "-" (Zero-Mock Data)
Skenario ini memverifikasi bahwa semua data pura-pura/mock palsu (seperti "Tarik napas 5 detik!", fake PE/PBV, angka portofolio fiktif) telah dibersihkan secara total dan digantikan oleh animasi Skeleton/Shimmer serta placeholder `"-"` (yang nantinya siap disambungkan ke animasi Lottie):
1. **Uji Loading Skeleton / Shimmer:**
   - Pada saat membuka aplikasi atau ketika data portofolio, watchlist, dan riwayat sedang dimuat:
     - **Hasil yang Diharapkan:** Komponen menampilkan efek animasi berkilau (*shimmer animation*) bernuansa AMOLED (`AppCard` dengan gradient sweep lembut), bukan blank putih atau teks kosong patah-patah.
2. **Uji Penanganan Request Gagal / Offline (Placeholder "-"):**
   - Matikan koneksi internet atau jalankan verifikasi klaim ketika backend service tidak dapat dihubungi.
   - Buka sheet hasil vonis atau periksa response verifikasi.
   - **Hasil yang Diharapkan:**
     - **TIDAK ADA LAGI** teks buatan palsu seperti *"Tarik napas 5 detik! Yakin membeli karena analisa..."*.
     - **TIDAK ADA LAGI** data saham fiktif seperti PE/PBV palsu atau broker fiktif.
     - Kotak indikator menampilkan status `"-"` yang bersih dan minimalis (sebagai penanda slot yang nantinya akan disambungkan ke animasi Lottie).
     - Angka pertumbuhan laba dan valuasi menampilkan `"-"` jika data belum tersedia atau gagal diambil.



---

### Skenario 17: Uji Integrasi Backend API (Google Auth, Role Onboarding, History & Markets Detail)
Skenario ini memverifikasi integrasi penuh endpoint backend OpenAPI v2.0.0 (https://6rlfv87r-8000.asse.devtunnels.ms/):

1. **Uji Masuk dengan Google (Google Sign-In Single-Button):**
   - Buka tab **Profile** atau buka app saat sesi belum terautentikasi (atau logout dari Settings).
   - Tekan tombol **"Lanjutkan dengan Google"**.
   - Pilih akun Google Anda pada Google Account Picker native Android.
   - **Hasil yang Diharapkan:**
     - Aplikasi menukar ID Token Google ke backend via `POST /api/v1/auth/google`.
     - Access token JWT dan profil pengguna (nama, email, avatar foto profil) tersimpan aman di `SessionManager`.
     - Tidak ada form email/password lokal yang ditampilkan (sesuai spesifikasi zero local auth).

2. **Uji Pemilihan Profil & Role Onboarding (Pemula vs Expert):**
   - Setelah login pertama kali, layar akan membuka **RoleSelectionScreen** ("Pilih Profil Analisis Saham").
   - Terdapat 2 kartu interaktif bergaya dark fintech:
     - **Investor Pemula (Retail Beginner)**: Penjelasan sederhana, sinyal praktis & bahasa awam.
     - **Trader & Analis Expert**: Valuasi PE/PBV mendalam, broker summary & metriks keuangan detail.
   - Tekan salah satu kartu dan ketuk **"Simpan & Lanjutkan"**.
   - **Hasil yang Diharapkan:**
     - Role tersimpan ke backend via `PUT /api/v1/user/settings` (`{"user_role": "PEMULA"}` atau `{"user_role": "EXPERT"}`).
     - Dashboard utama terbuka seketika dengan badge status role yang sesuai.

3. **Uji Ganti Role Kapan Saja di Menu Profil:**
   - Masuk ke tab **Pengaturan** -> ketuk kartu profil akun Anda untuk membuka **ProfileDetailScreen**.
   - Anda dapat beralih antara role **Pemula** dan **Expert** secara langsung.
   - **Hasil yang Diharapkan:**
     - Role baru otomatis dikirim ke backend `PUT /api/v1/user/settings` dan tersinkronisasi secara instan.

4. **Uji Riwayat Verifikasi (History API):**
   - Buka tab **Riwayat** (ikon jam/history pada bottom navigation dock).
   - Sistem akan memanggil `GET /api/v1/history`.
   - **Hasil yang Diharapkan:**
     - Menampilkan daftar klaim yang pernah diverifikasi oleh akun Anda.
     - Jarak antar kartu lega dan rapi (14.dp) dengan badge status vonis: Sesuai Fakta (Hijau), Perlu Waspada (Kuning), atau Klaim Beresiko / Hoax (Merah).
     - Terdapat filter tab kategori di bagian atas ("Semua", "Sesuai Fakta", "Waspada", "Hoax").

5. **Uji Detail Forensik Saham (Markets Tab / History Detail):**
   - Pada tab **Riwayat**, ketuk salah satu kartu riwayat verifikasi.
   - Aplikasi akan mengarahkan ke tab **Detail Forensik (Markets)** dan memanggil `GET /api/v1/history/{history_id}`.
   - **Hasil yang Diharapkan:**
     - Header menampilkan emiten, nama perusahaan, score keyakinan, dan badge vonis.
     - Kartu teks klaim menampilkan teks asli klaim yang ditelusuri.
     - Rekomendasi analis cooling-off prompt tampil rapi.
     - Valuasi Fundamental Sectors (P/E Ratio & PBV Ratio) tampil dinamis.
     - Arus Modal Asing (Broker Flow) menampilkan net flow IDR, Top Foreign Buyers, dan Top Foreign Sellers tanpa data tiruan/mock fiktif.

6. **Uji Bersihkan Riwayat:**
   - Masuk ke tab **Pengaturan** -> ketuk **"Bersihkan Cache & Riwayat"**.
   - **Hasil yang Diharapkan:**
     - Aplikasi memanggil `DELETE /api/v1/history` dan mengosongkan riwayat lokal.

---

### Skenario 15: Uji Alur Wajib Login Google, Onboarding Role, dan Zero-Mock Data
1. **Uji Pengalihan Wajib Login (Authentication Gate):**
   - Buka aplikasi Tilik dalam kondisi belum login (atau setelah Logout).
   - **Hasil yang Diharapkan:**
     - Aplikasi langsung menampilkan **AuthScreen** (Lanjutkan dengan Google).
     - Tombol "Mode Tamu" telah dihapus sepenuhnya sehingga tidak ada bypass.
2. **Uji Login Sekali (Persistent Session):**
   - Ketuk **"Lanjutkan dengan Google"** dan pilih akun Anda.
   - **Hasil yang Diharapkan:**
     - Jika akun telah memiliki role (`Pemula` atau `Pakar/Expert`), aplikasi langsung membuka **Dashboard**.
     - Sesi tersimpan permanen di `SessionManager` sehingga saat aplikasi ditutup dan dibuka kembali, pengguna tetap berada di Dashboard tanpa perlu login berulang.
3. **Uji Redirect Pemilihan Role (Bagi Akun Baru):**
   - Jika akun belum memilih role:
     - Aplikasi otomatis mengalihkan ke **RoleSelectionScreen** (Pemilihan Mode Investor).
     - Pengguna memilih antara **Pemula** atau **Pakar (Expert)**.
     - Ketuk **"Simpan & Lanjutkan"**.
     - Aplikasi mengirim `PUT /api/v1/user/settings`, menyimpan role ke profil, lalu otomatis masuk ke **Beranda/Dashboard**.
4. **Uji Verifikasi Zero-Mock & Dynamic User Data:**
   - Nama default lama (seperti "Matthew" atau "Pengguna Tilik") sudah 100% diganti secara dinamis dengan nama akun Google pengguna atau fallback `"User"`.
   - Avatar menampilkan foto profil akun Google dengan fallback favicon putih resmi.
   - Tidak ada angka saldo palsu, tidak ada watchlist dummy, tidak ada kartu tren rumor fiktif, dan tidak ada hardcoded BBRI di tab Markets saat belum ada scan. Seluruh data kosong tampil sebagai `"-"` yang bersih dan siap dihubungkan ke API / animasi lottie.
5. **Uji Haptic Feedback & Copywriting Error Baru:**
   - Sentuh tombol **"Lanjutkan dengan Google"**.
   - **Hasil yang Diharapkan:**
     - Tombol menghasilkan getaran sentuhan taktil (*haptic feedback*) yang responsif.
     - Jika pemilihan akun dibatalkan oleh pengguna, muncul pesan yang ramah: *"Gagal menghubungkan ke Google. Coba lagi."* tanpa kode teknis membingungkan.

---

### Skenario 18: Uji Desain Modern Floating Overlay & Toolbar Icon-Only (ClaimBottomSheet)
Skenario ini memverifikasi refaktor antarmuka pengguna pada bottom sheet melayang (*floating overlay*) agar 100% konsisten dengan Dark Investment Design System di `DESIGN.md`:

1. **Uji Tampilan Persistent Editor Card (Zero Mock Data):**
   - Buka floating bottom sheet dengan mengetuk bubble Tilik saat tidak ada teks di clipboard.
   - **Hasil yang Diharapkan:**
     - Kotak editor klaim langsung tampil rapi secara permanen dengan placeholder *"Ketik atau salin klaim saham dari Threads/X di sini..."*.
     - **TIDAK ADA LAGI** sample chips palsu/mock (*"BBRI Akumulasi Asing"*, *"GOTO Rekor Laba"*).
     - Desain mengadopsi standar Fintech Dark Mode: Latar belakang `AppBg` (`#0A0A0A`), kartu `AppCard` (`#141414`), kontainer editor `AppCardSubtle` (`#1A1A1A`), dan hairline border `Color(0x14FFFFFF)`.

2. **Uji Posisi Klaim Diskusi & Toolbar Icon-Only:**
   - Perhatikan baris header di dalam kartu editor:
     - **Sebelah Kiri (Badge Identitas):**
       - Saat belum ada emiten terdeteksi: Menampilkan pill badge `💬 Klaim Diskusi` dengan ikon forum berwarna `AppAccent` (`#FF5C35`).
       - Saat teks mengandung ticker saham (misal: "BBRI"): Badge otomatis bertransformasi menjadi pill hijau `📈 $BBRI` (`AppGreen`).
     - **Sebelah Kanan (Aksi Toolbar Berupa Icon Saja):**
       - Teks link panjang (*"Perluas"*, *"Segarkan"*, *"Hapus"*) telah digantikan sepenuhnya menjadi tombol squircle 32dp modern dengan ikon saja:
         1. 🔄 **Segarkan (`Refresh`)**: Menarik teks clipboard terbaru seketika.
         2. ⤢ **Perluas / Perkecil (`OpenInFull` / `CloseFullscreen`)**: Mengubah ukuran tinggi sheet secara dinamis.
         3. 🗑️ **Hapus (`DeleteOutline`)**: Mengosongkan teks input, meredup jika kosong dan aktif berwarna merah (`AppRed`) jika ada teks.

3. **Uji Sentuhan Taktil (Haptic Feedback) & CTA Button:**
   - Sentuh masing-masing tombol ikon (Segarkan, Perluas, Hapus) dan tombol CTA **"Periksa Fakta di Sectors API"**.
   - **Hasil yang Diharapkan:**
     - Setiap interaksi tombol memicu getaran sentuhan taktil (*haptic feedback*) yang nyata dan responsif.
     - Tombol CTA menyala terang dengan warna brand `AppAccent` (`#FF5C35`) saat ada teks, dan meredup rapi saat kosong.

---

### Skenario 19: Uji Hold-to-Paste (Pilih Semua, Salin, Potong, Tempel), Bubble Spinner Murni Tanpa Text, dan CTA Kembali ke Input Saat Hasil Gagal

Skenario ini memverifikasi 3 fitur penting yang baru saja disempurnakan:
1. **Hold & Selection Menu di Floating Textbox (Pilih Semua, Salin, Potong, Tempel ala Native Android):**
   - Buka floating bottom sheet Tilik di atas aplikasi apa pun (WhatsApp/Threads/Chrome).
   - **Uji Tahan (Long Press) Saat Kotak Kosong:**
     - Pastikan ada teks yang sudah Anda salin di clipboard HP Anda.
     - Tekan dan tahan (*hold*) di dalam kotak textbox input klaim.
     - **Hasil yang Diharapkan:** Menu toolbar melayang ala native Android langsung muncul tepat di atas jari Anda menampilkan tombol **[ 📋 Tempel ]**. Ketuk "Tempel" dan teks akan langsung terisi dengan haptic feedback.
   - **Uji Tahan & Seleksi Teks Saat Ada Tulisan:**
     - Ketik atau isi teks beberapa kalimat di kotak input.
     - Tekan dan tahan (*hold*) pada salah satu kata atau blok teks.
     - **Hasil yang Diharapkan:** Menu toolbar melayang muncul menampilkan opsi lengkap:
       - **Pilih Semua**: Memblokir seluruh teks di dalam kotak.
       - **Salin**: Menyalin teks yang sedang diblok ke clipboard.
       - **Potong**: Memotong teks yang diblok dan menyalinnya ke clipboard.
       - **Tempel**: Menimpa atau menyisipkan teks dari clipboard di posisi kursor.

2. **Bubble Loading Murni Tanpa Text ("Zero-Fluff Spinner Only"):**
   - Masukkan klaim saham (misal: "BBRI akumulasi broker asing hari ini") lalu ketuk **"Periksa Fakta di Sectors API"**.
   - **Hasil yang Diharapkan:**
     - Bottom sheet tertutup dan bubble floating berubah ke mode loading verifikasi.
     - **TIDAK ADA LAGI TEKS** *"Menilik bursa..."* atau copywriting panjang yang mengganggu.
     - Bubble tampil berbentuk bulat minimalis 56dp dengan spinner `CircularProgressIndicator` oranye (`AppAccent`) yang berputar halus.

3. **Avatar Gambar Role & CTA "Kembali" Saat Hasil Gagal / Error / Kosong:**
   - Jika verifikasi gagal (misal koneksi terputus atau server mengembalikan status gagal):
   - **Hasil yang Diharapkan:**
     - **Gambar Role Pengguna:** Di bagian tengah kartu status gagal, bulatan minus (`-`) telah digantikan dengan **gambar avatar role pengguna**:
       - Secara default (atau sebelum login): Menampilkan gambar **`PEMULA.jpg`** dengan badge `"Mode Investor: Pemula"`.
       - Jika pengguna telah login dan memilih role **Pakar (Expert)**: Otomatis menampilkan gambar **`EXPERT.jpg`** dengan badge `"Mode Investor: Pakar"`.
     - **Copywriting CTA Ringkas ("Kembali"):**
       - Tulisan tombol CTA yang panjang (*"Kembali ke Input Klaim"*) telah disederhanakan menjadi **"Kembali"** lengkap dengan ikon panah kembali (`ArrowBack`).
       - Begitu juga pada tombol aksi di bagian bawah sheet, tertulis rapi **"Kembali"**.
     - Saat tombol **"Kembali"** ditekan, aplikasi floating langsung membuka kembali bottom sheet ke **halaman awal input klaim**.
     - Teks klaim yang sebelumnya telah diketik **TIDAK HILANG** sehingga pengguna tidak perlu mengetik ulang dari awal.
---

### Skenario 20: Uji Transisi Animasi Antar Halaman & Tab Navigasi Bawah (Smooth Transition & BackHandler)

Skenario ini memverifikasi bahwa perpindahan halaman di dalam aplikasi Tilik tidak lagi berganti secara tiba-tiba/abrupt, melainkan mengadopsi standar animasi transisi modern Android:

1. **Uji Transisi Antar Tab Bottom Navigation (Home ↔ Markets ↔ History ↔ Settings):**
   - Buka aplikasi **Tilik**.
   - Dari tab **Home**, ketuk tab **Markets** atau **History** (bergerak ke kanan).
   - **Hasil yang Diharapkan:**
     - Halaman baru meluncur masuk secara halus dari sisi kanan (`slideInHorizontally` + `fadeIn`), sementara halaman sebelumnya meluncur keluar ke arah kiri dengan redaman transparansi.
     - Ikon dan teks di Bottom Bar memiliki respon animasi warna (`animateColorAsState`) dan sedikit pantulan skala elastis (`animateFloatAsState`).
   - Dari tab **Settings**, ketuk tab **Home** (bergerak ke kiri).
   - **Hasil yang Diharapkan:** Halaman meluncur masuk secara halus dari sisi kiri ke kanan, memberikan sensasi spasial yang konsisten dan natural.

2. **Uji Transisi Halaman Penuh (Profile Detail & Notifikasi / History Fullscreen):**
   - Di tab Home / Bar atas, ketuk foto profil avatar akun Anda atau opsi **Profil** di tab Pengaturan.
   - **Hasil yang Diharapkan:**
     - Halaman **Profil Detail** meluncur masuk mulus dari kanan layar ke kiri dengan fade-in standar Android (*push transition*).
   - Tekan tombol panah kembali (`<`) atau tekan tombol / gestur **Back** di HP Anda.
   - **Hasil yang Diharapkan:**
     - Halaman Profil Detail meluncur keluar kembali ke arah kanan (*pop exit*), dan halaman beranda utama kembali tampil dari kiri tanpa ada kedipan kasar atau layar kosong tiba-tiba.

3. **Uji Tombol Back Native Android (`BackHandler` Cerdas):**
   - Buka halaman **Notifikasi** (ikon lonceng di atas) atau masuk ke tab **Markets** dengan memilih salah satu saham forensik.
   - Tekan tombol / gestur **Back** fisik HP:
   - **Hasil yang Diharapkan:**
     - Sistem secara pintar menutup halaman detail/notifikasi terlebih dahulu kembali ke tab utama.
     - Jika Anda berada di tab selain Home (misal tab Settings), menekan Back akan memindahkan Anda kembali ke tab **Home** secara animasi sebelum akhirnya keluar dari aplikasi.
---

### Skenario 21: Uji Sistem Caching (Anti-Hit API Berulang), Pilihan Role & Tombol Simpan, dan Desain Profil

Skenario ini memverifikasi optimasi performa backend & perbaikan UI di halaman Profil dan Riwayat:

1. **Uji Caching Halaman Riwayat & Forensik (Bebas Beban Server):**
   - Masuk ke tab **History**.
   - Perhatikan bahwa daftar riwayat tampil seketika (*instant* dari cache) tanpa perlu menampilkan loading skeleton lagi setiap kali berpindah tab.
   - Ketuk salah satu item riwayat untuk membuka tab **Markets** (Detail Forensik). Data langsung tampil dari cache tanpa loading spinner.
   - Kembali ke tab History lalu ke Markets lagi: **Tidak ada pemanggilan API berulang ke backend**.
   - Uji tombol Refresh manual: Ketuk ikon reload putar di kanan atas tab History. Ikon akan berputar dan barulah aplikasi melakukan penarikan data baru dari server (*force refresh*).

2. **Uji Pemilihan Role & Tombol Simpan di Profil (Bebas Bug):**
   - Buka halaman **Profil** (ketuk avatar di kanan atas atau tab Settings -> Profil).
   - Misalkan saat ini Anda berada di role **Pakar**.
   - Ketuk kartu **Pemula**:
     - **Hasil yang Diharapkan:** Ceklis langsung berpindah seketika ke kartu **Pemula** di layar HP Anda tanpa ada bug tertinggal.
     - Di pojok kanan atas, tombol **[ Simpan ]** otomatis menyala oranye (`AppAccent`), dan muncul panduan di bawah kartu: *"Ketuk Simpan di kanan atas untuk menerapkan."*
     - Server **BELUM dipanggil sama sekali** sehingga tidak ada beban jaringan sia-sia jika Anda berubah pikiran.
   - Ketuk kartu **Pakar** kembali: Tombol Simpan otomatis meredup/nonaktif karena tidak ada perubahan yang perlu disimpan.
   - Pilih **Pemula** lalu ketuk tombol **[ Simpan ]**:
     - Tombol menampilkan spinner kecil sejenak, data dikirimkan **hanya satu kali** ke API.
     - Muncul indikator hijau: *"Perubahan role berhasil disimpan."*

3. **Uji Estetika Profil Baru (Logo Google & Border Bersih):**
   - Pada foto profil bulat di atas: Border oranye tebal telah **dihapus**, digantikan dengan hairline border minimalis abu-abu gelap yang rapi.
   - Pada bagian informasi akun: Terdapat logo resmi **Google** (`google.png`) tepat di sebelah tulisan **"AKUN GOOGLE"**.
---

### Skenario 22: Uji Logo Google Transparan, UX Tombol Simpan Zero-Fluff, dan Dialog Perubahan Belum Disimpan (Unsaved Changes Dialog)

Skenario ini memverifikasi pembaruan visual aset Google dan proteksi data role pengguna:

1. **Uji Logo Google Transparan (No White Box Background):**
   - Buka halaman **Profil**.
   - Perhatikan bagian **AKUN GOOGLE**.
   - **Hasil yang Diharapkan:** Logo Google kini tampil dengan latar belakang **transparan sempurna** tanpa kotak putih di belakangnya, menyatu sangat elegan dengan latar AMOLED gelap (`#0A0A0A`).

2. **Uji Tombol Simpan Zero-Fluff (Hapus Teks Panduan):**
   - Pada halaman Profil, tidak ada lagi kalimat panduan bertele-tele di bawah kartu role.
   - Status tombol **[ Simpan ]** di kanan atas:
     - **Meredup (Disabled)**: Saat role sama dengan yang tersimpan saat ini.
     - **Menyala Oranye (Active)**: Begitu Anda mengetuk role yang berbeda (misal dari Pakar ke Pemula). Pengguna cukup melihat tombol Simpan aktif tanpa perlu membaca instruksi panjang.

3. **Uji Komponen Dialog Konfirmasi (UnsavedChangesDialog):**
   - Ubah role (misal ketuk **Pemula** saat akun Anda berada di Pakar) hingga tombol Simpan menyala.
   - **JANGAN** ketuk tombol Simpan.
   - Tekan tombol **Kembali (`<`)** di kiri atas Top Bar, **ATAU** lakukan gestur/tombol **Back** native Android di HP Anda:
   - **Hasil yang Diharapkan:**
     - Aplikasi tidak langsung menutup, melainkan memunculkan dialog konfirmasi elegan ala Fintech AMOLED:
       - Judul: *"Perubahan Belum Disimpan"*
       - Pesan: *"Perubahan role Anda belum disimpan. Yakin ingin keluar tanpa menyimpan?"*
       - Dua tombol: **[ Lanjut Edit ]** dan **[ Buang ]** (merah).
   - **Uji Tombol [ Lanjut Edit ]:**
     - Ketuk "Lanjut Edit". Dialog tertutup dan Anda tetap berada di halaman profil sehingga bisa menekan [ Simpan ].
   - **Uji Tombol [ Buang ]:**
     - Ketuk tombol Back lagi, lalu ketuk "Buang". Dialog tertutup, perubahan dibatalkan, dan Anda kembali ke halaman sebelumnya.

---

### Skenario 23: Uji Perbaikan Insets & Top Bar Halaman Notifikasi (Bebas Tabrakan Status Bar & Jam HP)

Skenario ini memverifikasi bahwa saat membuka halaman Notifikasi / Riwayat dari ikon lonceng, layout tidak lagi melompat ke atas (*naik ke atas*) dan tidak lagi tertabrak jam status bar:

1. **Uji Pembukaan Halaman Notifikasi (Status Bar Clearance):**
   - Dari halaman Beranda (Dashboard), ketuk ikon **Lonceng Notifikasi** di pojok kanan atas `AppTopBar`.
   - **Hasil yang Diharapkan:**
     - Halaman Notifikasi meluncur masuk secara halus (*slide transition*).
     - **TIDAK ADA LAGI KONTEN NAIK KE ATAS:** Judul *"Riwayat Verifikasi"*, tombol kembali (`<`), dan tombol muat ulang (`↻`) kini memiliki padding status bar (`statusBarsPadding()`) yang presisi.
     - Posisi tombol kembali dan judul berada tepat di bawah jam HP (misal: 14:38), ikon sinyal, dan persentase baterai tanpa saling bertumpukan atau terpotong bezel/punch hole kamera.
2. **Uji Proporsi Top Bar & Tombol Navigasi:**
   - Perhatikan tombol kembali (`<`) dan tombol reload (`↻`) di bagian atas:
     - Ukuran lingkaran konsisten 40dp dengan ikon 18dp ala Fintech Dark Mode, sejajar dan proporsional dengan halaman Profil.
   - Bagian bawah halaman juga terlindungi oleh `navigationBarsPadding()` sehingga tombol CTA *"Scan sekarang"* aman di atas gesture pill navigasi Android.
3. **Uji Kembali ke Halaman Utama:**
   - Ketuk tombol **`<`** di kiri atas atau lakukan gestur **Back** Android di HP Anda:
   - **Hasil yang Diharapkan:**
     - Halaman menutup kembali ke Beranda secara mulus tanpa ada kedipan layar.

---

### Skenario 24: Uji Login Ulang Tanpa Pemilihan Role Berulang (One-Time Role Onboarding Per Akun)

Skenario ini memverifikasi bahwa pemilihan mode investor (Role Onboarding) **hanya terjadi satu kali saat pertama kali akun didaftarkan**, dan saat pengguna logout lalu login kembali menggunakan akun yang sama, aplikasi langsung masuk ke Beranda tanpa meminta pemilihan role ulang:

1. **Uji Logout Akun yang Sudah Memiliki Role:**
   - Masuk ke menu **Profil** (ketuk foto profil avatar di pojok kanan atas `AppTopBar` atau via Pengaturan).
   - Geser ke bagian bawah, lalu ketuk tombol **Logout**.
   - **Hasil yang Diharapkan:** Sesi aktif dikeluarkan dengan aman dan aplikasi kembali ke **AuthScreen** (*Lanjutkan dengan Google*).

2. **Uji Login Ulang Menggunakan Akun Google yang Sama:**
   - Di **AuthScreen**, sentuh tombol **"Lanjutkan dengan Google"**.
   - Pilih akun Google Anda yang sebelumnya sudah pernah memilih role (misal: Pakar atau Pemula).
   - **Hasil yang Diharapkan:**
     - Aplikasi langsung menyelesaikan autentikasi dan **langsung membuka Beranda/Dashboard**.
     - **TIDAK MUNCUL LAGI LAYAR PILIH PERAN/ROLE (`RoleSelectionScreen`)**.
     - Role yang sebelumnya Anda pilih tetap aktif dan tersimpan rapi di profil Anda.

3. **Verifikasi Akun Baru vs Akun Lama:**
   - Pemilihan role hanya akan dimunculkan jika akun Google yang digunakan adalah akun baru yang belum pernah memilih role di server Tilik maupun di perangkat HP Anda.

---

### Skenario 25: Uji Kecepatan Login Google (Fast-Failover & Direct Wi-Fi LAN)

Skenario ini memverifikasi bahwa proses login ulang dengan Google kini berlangsung instan (< 1 detik) tanpa delay timeout 10 detik:

1. **Uji Kecepatan Login Ulang (Instant Redirect):**
   - Di **AuthScreen**, sentuh tombol **"Lanjutkan dengan Google"**.
   - Pilih akun Google Anda di jendela sistem.
   - **Hasil yang Diharapkan:**
     - Begitu akun Google dipilih, aplikasi langsung terhubung ke backend dalam **~300-500ms**.
     - Spinner loading hanya muncul sekejap dan langsung **ter-redirect seketika ke Beranda/Dashboard**.
     - Tidak ada lagi jeda macet/hang selama 10 detik yang sebelumnya disebabkan oleh timeout localhost.
2. **Uji Ketahanan Jaringan (Dynamic Host Memory):**
   - Aplikasi kini secara cerdas mengingat host backend yang berhasil (`activeHost`), sehingga setiap request berikutnya langsung menuju rute tercepat tanpa mencoba ulang host yang mati.

---

### Skenario 26: Uji Tampilan Chip Kode Broker (Bebas Kotak Putih) & Integritas 100% Real Data Sectors API

Skenario ini memverifikasi perbaikan kontras visual pada daftar broker (Top Buyers & Top Sellers) serta memastikan integritas data bursa murni dari backend:

1. **Penjelasan Mengapa Sebelumnya Muncul "Kotak Putih":**
   - Kotak putih tersebut sebenarnya adalah **badge chip kode broker** (misal: `AK`, `YU`, `DX`, `YP`).
   - Pada versi sebelumnya, background chip tersebut diset ke `Color.White`, sedangkan teks di dalamnya menggunakan `TextDark` yang bernilai `Color(0xFFFFFFFF)` (putih).
   - Akibatnya terjadi **tabrakan warna putih-di-atas-putih** (*white-on-white text collision*), membuat teks kode sekuritas/broker tertutup sempurna dan hanya tampak sebagai kotak putih polos.

2. **Uji Tampilan Visual Chip Broker Baru (Fintech Dark AMOLED):**
   - Lakukan pemindaian klaim bursa (misal teks terkait saham `BBRI` atau saham lainnya).
   - Buka kartu hasil verifikasi (**Verdict Card**):
   - Geser ke bagian **Top Buyers (Akumulasi)** dan **Top Sellers (Distribusi)**.
   - **Hasil yang Diharapkan:**
     - **TIDAK ADA LAGI KOTAK PUTIH POLOS**.
     - Kode sekuritas kini tampil sangat jelas dengan font monospace tebal (contoh: `AK`, `YU`, `DX`, `YP`) di dalam kapsul abu-abu gelap elegan (`#1E1E1E`) berbatas garis tipis (*hairline border*).
     - Label tipe broker asing (**Asing**) tampil dengan warna biru langit lembut (`#38BDF8`), sedangkan domestik (**Domestik**) berwarna abu-abu netral (`#888888`).
     - Angka nilai transaksi akumulasi tampil hijau menyala (`+Rp 15,2 Miliar`), dan nilai distribusi tampil merah tegas (`-Rp 24,1 Miliar`).
     - Semua elemen warna harmonis dan tidak ada lagi teks atau elemen yang bertabrakan.

3. **Verifikasi Integritas Data (100% Murni Live Data Sectors Financial API):**
   - Seluruh data angka, metrik valuasi, dan status bursa yang tampil di layar kartu hasil **100% bersumber langsung dari Sectors Financial API v2**:
     - **Arus Modal Asing:** Ditarik langsung dari endpoint `/v2/foreign-flow/{ticker}/` (contoh: `-Rp 106,8 Miliar`).
     - **Top Buyers & Sellers:** Ditarik langsung dari endpoint `/v2/broker-summary/{ticker}/top/?n_brokers=10` dengan rincian transaksi riil broker BEI.
     - **Valuasi PBV & PER vs Median Sektor:** Dihitung langsung dari data pasar harian `/v2/company/report/{ticker}/` perbandingan sektor perbankan/industri terkait.
     - **Kesehatan Finansial & Sanksi BEI:** Laba YoY dan Arus Kas dari `/v2/financials/quarterly/{ticker}/`, serta status Papan Khusus (FCA) dari `/v2/suspensions/`.
     - **TIDAK ADA data mock, dummy, atau data acak sama sekali**.

---

### Skenario 27: Uji Penyelarasan Beranda dengan OpenAPI Docs (Pembersihan Mock Saldo & Portofolio)

Skenario ini memverifikasi bahwa halaman Beranda (Dashboard) telah dibersihkan dari seluruh fitur mock/fiktif yang tidak didukung API backend, serta sepenuhnya menyajikan data riil yang sesuai dengan `openapi.json`:

1. **Verifikasi Penghapusan Fitur Fiktif:**
   - Buka tab **Beranda / Home**.
   - **Hasil yang Diharapkan:**
     - **TOTAL SALDO PORTOFOLIO DIHAPUS**: Tidak ada lagi kartu saldo fiktif ("Rp 128.450.000", tombol "Top Up" / "Tarik"). Tilik adalah asisten fact-checker bursa, bukan dompet/sekuritas.
     - **PORTOFOLIO SAHAM DUMMY DIHAPUS**: Carousel saham dummy dan grafik kurva portofolio telah dihapus total karena tidak memiliki endpoint di API.
     - **DAFTAR PANTAUAN (WATCHLIST) DUMMY DIHAPUS**: Fitur watchlist dummy telah dihapus.
     - **ISU & RUMOR TREN DUMMY DIHAPUS**: Bagian rumor trending yang tidak memiliki API pendukung telah dibersihkan.

2. **Verifikasi Fitur Asli yang Tersedia (Didukung OpenAPI):**
   - **1. Kontrol Layanan Tilik (`ServiceControlCard`):**
     - Kartu kontrol status widget mengambang di baris teratas:
       - Status: `Standby` (abu-abu) atau `Aktif` (hijau).
       - Tombol: `Mulai Layanan Tilik` (Oranye) atau `Hentikan Layanan` (Merah).
     - Mengetuk tombol ini langsung menyalakan/mematikan bubble melayang Tilik di atas layar HP Anda.
   - **2. Riwayat Pemindaian Terakhir (`RecentScansSection`):**
     - Mengambil data asli dari endpoint `GET /api/v1/history` (menggunakan sistem in-memory cache instan):
       - Menampilkan 3 hasil scan verifikasi bursa terakhir yang benar-benar pernah Anda lakukan (misal: BBRI).
       - Dilengkapi badge ticker monospace (misal: `BBRI`), platform asal (`X`), dan status vonis (`WASPADA • 92%`).
       - Mengetuk item riwayat akan **langsung membuka tab Markets (Detail Forensik)** untuk saham tersebut.
       - Mengetuk teks **"Lihat Semua >"** di kanan atas akan langsung memindahkan Anda ke tab **History**.
       - Jika akun baru belum memiliki riwayat, tampil kartu panduan bersih: *"Belum Ada Riwayat Pemindaian"*.
   - **3. Kesiapan Sistem Android (`SystemReadinessCard`):**
     - Menampilkan indikator izin native: Floating Overlay, Tangkapan Layar, Deteksi Teks, dan Latar Belakang.
   - **4. Integrasi Pasar BEI (`MarketOverviewCard`):**
     - Menampilkan ringkasan cakupan 900+ emiten BEI yang terhubung langsung dengan Sectors Financial API v2.




---

### Skenario 28: Uji Seleksi Teks Android Langsung ("Tilik AI" via `ACTION_PROCESS_TEXT`)

Skenario ini memverifikasi integrasi native Android text selection toolbar (`ACTION_PROCESS_TEXT`) yang memungkinkan Anda memverifikasi teks apa pun langsung dari menu seleksi teks aplikasi lain tanpa perlu menyentuh floating bubble terlebih dahulu:

1. **Konsep & Cara Kerja Fitur:**
   - Fitur ini memanfaatkan standar native Android `Intent.ACTION_PROCESS_TEXT` (sesuai panduan resmi Google / Ian Lake).
   - Ketika teks diblok/diseleksi di aplikasi manapun (WhatsApp, Twitter/X, Threads, Telegram, Chrome, Gmail, Catatan), Android akan menyisipkan aksi custom **"Tilik AI"** (lengkap dengan ikon Tilik) berdampingan dengan menu sistem seperti *Copy*, *Paste*, *Select all*, dan *Share*.
   - Saat opsi **"Tilik AI"** ditekan, Modal Bottom Sheet verifikasi klaim akan **langsung meluncur mulus dari bawah** (*slide up*) dengan teks yang diseleksi sudah terisi otomatis di kotak klaim.
   - Fungsi floating widget tetap bekerja normal dan tidak terpengaruh sedikit pun.

2. **Langkah Pengujian Langsung di HP:**
   - **Langkah 1:** Buka aplikasi perpesanan atau media sosial apa pun di HP Anda (contoh: WhatsApp, Twitter/X, Chrome, atau Gmail).
   - **Langkah 2:** Seleksi / blok kalimat atau isu saham, contohnya:
     > *"BBRI laba bersih tembus 60 triliun kuartal ini"* atau *"GOTO mau diakuisisi konglomerat"*
   - **Langkah 3:** Perhatikan toolbar seleksi teks yang muncul mengambang di atas teks (berisi *Salin / Copy*, *Tempel / Paste*, dsb).
   - **Langkah 4:** Cari dan pilih menu **"Tilik AI"** (jika tertutup tombol panah/titik tiga overflow, geser toolbar ke samping).
   - **Langkah 5 (Hasil yang Diharapkan):**
     - Menu teks menutup seketika tanpa ada kedipan layar hitam (*zero flicker*).
     - Modal Bottom Sheet Tilik **langsung muncul dari bawah layar**.
     - Teks yang Anda seleksi sudah **otomatis terisi di kolom klaim**, tanpa perlu menyalin (*copy-paste*) manual.
     - Kode emiten (misal `$BBRI` atau `$GOTO`) otomatis terdeteksi dengan badge hijau menyala di atas kolom input.
     - Tombol oranye **"Periksa Fakta di Sectors API"** langsung siap ditekan.
     - Keyboard tidak menutupi atau meremukkan (*squish*) modal sheet.

3. **Verifikasi Integritas Floating Bubble:**
   - Tekan tanda silang **(X)** atau ketuk di luar area sheet untuk menutupnya.
   - Ketuk langsung floating bubble Tilik di tepi layar.
   - **Hasil yang Diharapkan:** Floating bubble tetap responsif membuka modal sheet secara normal, auto-tuck 3 detik tetap aktif, dan fisika snap-to-edge tetap bekerja sempurna.
