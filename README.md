FinFlex - Android Personal Finance Application
FinFlex adalah aplikasi keuangan pribadi berbasis Android yang dirancang untuk membantu pengguna mengelola transaksi, melacak pengeluaran, dan melakukan konversi mata uang dengan antarmuka yang intuitif dan fitur-fitur praktis. Aplikasi ini dikembangkan untuk mempermudah pengelolaan keuangan sehari-hari.

1. Fitur Utama

- Dual Activity & Fragment Architecture: Menggunakan MainActivity sebagai launcher dan berbagai fragment (HomeFragment, AddTransactionFragment, MonthlyReportFragment) untuk navigasi.
- Intent Communication: Navigasi antar activity seperti TransactionDetailActivity dan ChartActivity menggunakan Intent.
- RecyclerView: Menampilkan daftar transaksi di MonthlyReportFragment dengan scrolling yang mulus.
- Fragment Navigation: Menggunakan Navigation Component dengan BottomNavigationView untuk navigasi antar fragment.
- Background Threading: Operasi database dijalankan secara asinkronus menggunakan Executor di DatabaseHelper.
- API Integration: Menggunakan Frankfurter API dengan Retrofit untuk data kurs mata uang.
- Local Data Persistence: SQLite database (MoneyMate.db) untuk menyimpan transaksi.
- Dark/Light Theme: Toggle tema (light, dark, system) dengan SharedPreferences di MainActivity.
- Refresh on Error: Tombol refresh di ChartActivity dan CurrencyActivity untuk menangani kegagalan koneksi.

2. Fitur Tambahan

- Real-time Currency Conversion: Konversi mata uang instan di CurrencyActivity dengan input dinamis.
- Offline Support: Menggunakan data kurs yang di-cache jika tidak ada koneksi internet.
- Transaction Management: Tambah, lihat detail, dan hapus transaksi dengan konfirmasi.
- Visual Reporting: Pie chart di ChartActivity untuk visualisasi pengeluaran per kategori.
- Balance Checking: Validasi saldo sebelum menambahkan pengeluaran di AddTransactionFragment.
- Error Handling: Pesan error yang informatif untuk kegagalan database, jaringan, dan konversi.
- Monthly Reports: Ringkasan pemasukan, pengeluaran, dan saldo per bulan di MonthlyReportFragment.

3. Arsitektur Aplikasi
   
- Technical Stack
Language: Java
Min SDK: Android API 24 (Android 7.0)
Target SDK: Android API 34
Architecture: MVC (Model-View-Controller) dengan pendekatan modular
Database: SQLite via SQLiteOpenHelper
Network: Retrofit2 + OkHttp3
UI: Material Design components
Navigation: Android Navigation Component
Charting: MPAndroidChart untuk visualisasi data

4. Setup & Installation

- Prerequisites
Android Studio Arctic Fox atau versi lebih baru
Android SDK API 24+
Java Development Kit 8+
Koneksi internet untuk build dan API call

- Installation Steps
1. Clone Repository
git clone https://github.com/yourusername/moneymate.git
cd moneymate

2. Open in Android Studio
Buka Android Studio
Pilih "Open an existing project"
Arahkan ke folder moneymate

3. Sync Project
Tunggu Gradle sync selesai
Install missing SDK components jika diminta

4. API Key Configuration (SECURE SETUP)
IMPORTANT: Jangan commit API key ke repository!

Buat file local.properties di root project:
# local.properties
FRANKFURTER_API_KEY=(API KEY HERE)

Pastikan local.properties ada di .gitignore:
# .gitignore
local.properties

Untuk Tim: Bagikan API key secara terpisah (email, chat, dll)

Verifikasi: Build gagal jika API key tidak dikonfigurasi

5. Build & Run
./gradlew assembleDebug
# atau run langsung dari Android Studio

📖 Cara Penggunaan
1. Navigation
- Home Tab: Tampilan ringkasan saldo dan transaksi terbaru (HomeFragment)
- Add Transaction: Form untuk menambah transaksi baru (AddTransactionFragment)
- Monthly Report: Laporan bulanan dan daftar transaksi (MonthlyReportFragment)
- Chart: Visualisasi pengeluaran per kategori (ChartActivity)
- Currency: Konversi mata uang real-time (CurrencyActivity)

2. Fitur Interaksi
- Tambah Transaksi: Isi jumlah, deskripsi, tipe, kategori, dan tanggal, lalu simpan
- Lihat Detail: Tap transaksi di daftar untuk melihat detail
- Hapus Transaksi: Long-press transaksi atau gunakan tombol hapus di detail
- Navigasi Bulan: Tombol prev/next untuk lihat data bulan lain
- Konversi Mata Uang: Pilih mata uang asal/tujuan, masukkan jumlah
- Swap Currency: Tombol swap untuk tukar mata uang
- Theme Toggle: Pilih tema (light/dark/system) di MainActivity

3. Offline Mode
- Transaksi disimpan di database lokal
- Data kurs di-cache untuk konversi offline
- Laporan dan chart tetap dapat diakses tanpa internet

🔌 API Integration
- Frankfurter API Implementation
  
- Base URL: https://api.frankfurter.app/

- Endpoints Used:
/latest - Dapatkan kurs mata uang terbaru

- Request Example:
GET /latest?from=EUR

Error Handling:
- Network timeout dengan retry
- Fallback ke data cache saat gagal
- Pesan error ramah pengguna
  

 Database Schema
Transactions Table
CREATE TABLE transactions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    amount REAL NOT NULL,
    description TEXT,
    type TEXT NOT NULL CHECK (type IN ('income', 'expense')),
    category TEXT NOT NULL,
    date TEXT NOT NULL
);

Database Operations
- Insert: Simpan transaksi baru
- Query: Ambil transaksi per bulan, hitung saldo, laporan kategori
- Update: Edit transaksi (opsional, tergantung implementasi)
- Delete: Hapus transaksi dengan konfirmasi

Testing
Manual Testing Checklist
- Aplikasi launch dengan sukses
- Penambahan transaksi berfungsi
- Laporan bulanan akurat
- Pie chart menampilkan data benar
- Konversi mata uang real-time
- Hapus transaksi berfungsi
- Offline mode berjalan
- Toggle tema berfungsi
- Error handling untuk jaringan dan database
- Navigasi antar fragment/activity

Performance Testing
- Scrolling halus di RecyclerView
- Operasi database asinkronus
- Optimisasi penggunaan memori
- Responsivitas UI

Build & Release
Debug Build
./gradlew assembleDebug

Release Build
./gradlew assembleRelease

APK Location
Debug: app/build/outputs/apk/debug/app-debug.apk
Release: app/build/outputs/apk/release/app-release.apk

🔐 Security & Privacy
🛡️ API Key Security (BEST PRACTICES)

✅ API key disimpan di local.properties (tidak di-commit)
✅ Validasi otomatis untuk API key
✅ Komunikasi API via HTTPS


Nama: Nur Fadillaah
NIM: H071231080
Praktikum: Lab Pemrograman Mobile 2025
Tema: Pengelolaan Keuangan Pribadi (Keuangan)

