# SIMAP-APK

Aplikasi Android **native** (Kotlin + Jetpack Compose) untuk inspeksi Alat Pemadam (APAR)
SIMAP ARFF — Bandara Syamsudin Noor (PT Angkasa Pura Indonesia).

Terhubung ke backend `https://simap.arffsnia.com/` lewat API JSON (auth JWT).
Source backend (PHP) ada di repo terpisah.

## Fitur

- Login (username/email + password), token JWT tersimpan, auto-relogin bila expired.
- Dashboard: sapaan user + KPI coverage, tombol Scan QR, daftar aset, sinkron.
- Daftar aset: cari nama/kode/lokasi, filter kategori, indikator Baik / Ada masalah.
- Scan QR tabung APAR (kamera + ML Kit) — QR lama berisi URL
  `https://simap.arffsnia.com/check?slug=<kode>`, APK ambil slug-nya otomatis.
  Ada fallback masukkan kode manual.
- Detail aset: info + foto + kondisi checklist terakhir.
- Form inspeksi: checklist dinamis per kategori (Baik / Tidak Baik / N/A),
  catatan opsional per item, foto dokumentasi.
- Offline-first: hasil cek disimpan lokal lalu disinkron saat online (layar Sinkron).
- Layar admin (per role): Dashboard (KPI + chart coverage + aset belum dicek),
  Penugasan (progres per regu/zona), Validasi (setujui/tolak pengajuan aset),
  Pengingat (penerima + hitung/kirim), Lainnya.

## Role (3 level, dari `user.level` di backend)

- `Super Admin` — akses penuh.
- `Admin Regu` — ter-scope ke regunya (`user.regu_id`).
- `User` — petugas lapangan (cek APAR).

Bottom nav + menu dibangun dari role map terpusat di `ui/Nav.kt`
(`Role` + `RoleNav`), bukan `if level == X` yang tersebar.

## Build

Prasyarat: JDK 17+ (disarankan 21) dan Android SDK (compileSdk 37, targetSdk 36, minSdk 24).

```bash
cd simap-android
./gradlew assembleDebug     # cepat, untuk development
./gradlew assembleRelease   # ±6 menit (R8 minify) → app/build/outputs/apk/release/app-release.apk
```

CATATAN: `assembleRelease` lama (R8); jalankan di background kalau pakai terminal
dengan timeout pendek.

### Signing

Keystore **TIDAK** ada di repo ini (sengaja di-gitignore):
`app/release.keystore` dan `app/keystore.properties` — minta ke pemilik proyek.

- Tanpa keystore, `assembleRelease` menghasilkan APK unsigned (tidak bisa install/update).
- APK debug tidak bisa menimpa APK release terpasang (signature beda).

## Struktur

```
app/src/main/java/com/arffsnia/simap/
  data/       → api (Retrofit), dto (Models.kt), repo, SessionManager
  ui/         → login, home, scan, assets, inspection, admin/, Nav.kt, AppRoot.kt
  ui/theme/   → Color.kt (palet final: teal #0d9488 + sky #0ea5e9)
```

## Backend / API

Endpoint API (admin v1) sudah **ter-deploy ke produksi** (`simap.arffsnia.com`).
Verifikasi live: login OK, endpoint baru balas 401 tanpa token (koneksi DB OK).

Status runtime: data riil tiap layar admin **belum** dicek visual dengan akun admin
asli (perlu login NIP + password valid di HP).

Detail deploy/endpoint: lihat repo backend / `DEPLOY_APK_BACKEND.md` di sana.

## Belum masuk APK (tetap di web)

Riwayat cek, peta sebaran (ditunda). Edit aset / filter regu dari APK: endpoint
`assets/submit` dan `regu/index` sudah siap di backend, UI-nya belum dibuat.
