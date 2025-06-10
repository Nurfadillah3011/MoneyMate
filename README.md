💰 FinFlex - Personal Finance Manager

*FinFlex* adalah aplikasi Android modern untuk pengelolaan keuangan pribadi yang membantu Anda melacak pemasukan dan pengeluaran dengan mudah dan efisien.

## ✨ Fitur Utama

### 📊 *Manajemen Transaksi*
- ✅ Tambah transaksi pemasukan dan pengeluaran
- ✅ Kategorisasi transaksi otomatis
- ✅ Riwayat transaksi lengkap dengan detail
- ✅ Edit dan hapus transaksi

### 💱 *Multi-Currency Support*
- ✅ Support 15+ mata uang internasional
- ✅ Konverter mata uang real-time
- ✅ Tampilan saldo dalam berbagai mata uang
- ✅ Update kurs otomatis via API

### 📈 *Analisis & Laporan*
- ✅ Grafik pie pengeluaran per kategori
- ✅ Laporan bulanan detail
- ✅ Analisis tren keuangan
- ✅ Export data laporan

### 🎨 *User Experience*
- ✅ Material Design 3
- ✅ Dark mode & Light mode
- ✅ Responsive design
- ✅ Animasi smooth
- ✅ Navigation yang intuitif

### 🔧 *Fitur Teknis*
- ✅ Offline-first architecture
- ✅ SQLite local database
- ✅ REST API integration
- ✅ Error handling yang robust

## 🛠 Tech Stack

### *Frontend*
- *Language:* Java
- *UI Framework:* Android Native
- *Design System:* Material Design 3
- *Navigation:* Navigation Component
- *Charts:* MPAndroidChart

### *Backend & Data*
- *Local Database:* SQLite with custom DatabaseHelper
- *API Client:* Retrofit 2
- *JSON Parsing:* Gson
- *HTTP Logging:* OkHttp Interceptor

### *Architecture*
- *Pattern:* Fragment-based Navigation
- *Currency API:* Frankfurter API (free exchange rates)
- *Database:* Room-like SQLite implementation
- *Threading:* AsyncTask & Callbacks

### *Dependencies*
gradle
dependencies {
    // UI Components
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.9.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    
    // Navigation
    implementation "androidx.navigation:navigation-fragment:2.5.3"
    implementation "androidx.navigation:navigation-ui:2.5.3"
    
    // Charts
    implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'
    
    // Networking
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    implementation 'com.squareup.okhttp3:logging-interceptor:4.9.1'
}


## 🚀 Installation & Setup

### *Prerequisites*
- Android Studio Arctic Fox atau lebih baru
- JDK 11+
- Android SDK API level 24+
- Device/Emulator dengan Android 7.0+

### *Clone Repository*
bash
git clone https://github.com/yourusername/moneymate.git
cd moneymate


### *Setup Project*
1. Buka Android Studio
2. File → Open → Pilih folder project
3. Sync project dengan Gradle files
4. Run aplikasi di emulator/device

### *API Configuration*
Aplikasi menggunakan [Frankfurter API](https://frankfurter.app/) untuk kurs mata uang:
- ✅ Free API (tidak perlu API key)
- ✅ Real-time exchange rates
- ✅ Support 30+ currencies

## 📖 Usage Guide

### *1. Menambah Transaksi*

1. Tap tombol "Tambah" di bottom navigation
2. Pilih tipe transaksi (Income/Expense)
3. Masukkan jumlah dan deskripsi
4. Pilih kategori dan tanggal
5. Tap "Simpan"


### *2. Melihat Laporan*

1. Tap "Laporan" di bottom navigation
2. Gunakan arrow untuk navigasi bulan
3. Lihat summary dan daftar transaksi
4. Tap transaksi untuk detail lengkap


### *3. Konversi Mata Uang*

1. Tap icon "Konverter" di home screen
2. Pilih mata uang asal dan tujuan
3. Masukkan nominal
4. Lihat hasil konversi real-time


### *4. Mengubah Tema*

1. Tap icon tema di header
2. Pilih Light/Dark/System Default
3. Tema akan berubah secara otomatis


## 🏗 Project Structure


app/src/main/java/com/example/moneymate/
├── 📁 activities/
│   ├── MainActivity.java
│   ├── ChartActivity.java
│   ├── CurrencyActivity.java
│   └── TransactionDetailActivity.java
├── 📁 fragments/
│   ├── HomeFragment.java
│   ├── AddTransactionFragment.java
│   └── MonthlyReportFragment.java
├── 📁 models/
│   ├── Transaction.java
│   └── ExchangeRateResponse.java
├── 📁 database/
│   └── DatabaseHelper.java
├── 📁 services/
│   ├── CurrencyService.java
│   ├── ApiService.java
│   └── RetrofitClient.java
├── 📁 adapters/
│   └── TransactionAdapter.java
└── 📁 utils/
    └── NetworkUtils.java



## 👨‍💻 Developer

*Nur Fadillah*
- GitHub: [@Nurfadillah3011](https://github.com/Nurfadillah3011)
- Email: fadillan945@gmail.com

## 🙏 Acknowledgments

- [Material Design](https://material.io/) - Design system
- [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart) - Chart library
- [Frankfurter API](https://frankfurter.app/) - Currency exchange rates
- [Retrofit](https://square.github.io/retrofit/) - HTTP client
- Android development community

---

<div align="center">
  <p>⭐ Star this repository if you find it helpful!</p>
  <p>Made with ❤ for personal finance management</p>
</div>
