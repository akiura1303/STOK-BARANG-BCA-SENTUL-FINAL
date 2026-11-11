# STOK BARANG BCA SENTUL

Aplikasi Android pencatatan stok barang berbasis Java dan SQLite.

## Build APK otomatis melalui GitHub Actions

1. Upload seluruh isi folder proyek ini ke root repository GitHub.
2. Pertahankan struktur folder (jangan meratakan isi folder `app`).
3. Buka tab **Actions**.
4. Pilih workflow **Android CI**. Build berjalan otomatis saat commit ke branch `main`, atau jalankan lewat **Run workflow**.
5. Setelah build hijau, buka hasil workflow dan unduh artifact **STOK-BARANG-BCA-SENTUL-APK**.
6. Ekstrak ZIP artifact, lalu instal `app-debug.apk` di Android.

Workflow menyiapkan Gradle 8.9 di runner GitHub. File `gradlew` tidak diperlukan.

## Struktur proyek

```text
STOK-BARANG-BCA-SENTUL/
├── .github/workflows/android.yml
├── app/
│   ├── build.gradle
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/bca/stokbarang/
│       │   ├── MainActivity.java
│       │   └── DatabaseHelper.java
│       └── res/values/styles.xml
├── build.gradle
├── gradle.properties
└── settings.gradle
```

## Teknologi
- Java
- Android SDK 35
- Minimum Android 6.0 (API 23)
- SQLite lokal (data tersimpan di perangkat)
