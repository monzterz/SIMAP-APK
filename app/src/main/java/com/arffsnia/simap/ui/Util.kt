package com.arffsnia.simap.ui

/** Terjemahkan kode error API ke pesan bahasa Indonesia. */
fun mapError(code: String): String = when (code) {
    "invalid_credentials" -> "Username atau password salah"
    "missing_credentials" -> "Username dan password wajib diisi"
    "too_many_attempts" -> "Terlalu banyak percobaan. Coba lagi 15 menit lagi"
    "unauthorized" -> "Sesi berakhir, silakan login ulang"
    "MAINTENANCE" -> "Server sedang maintenance. Coba lagi nanti"
    "network_error" -> "Tidak ada koneksi internet"
    "not_found" -> "Data tidak ditemukan"
    "missing_slug" -> "Kode aset tidak terbaca"
    "forbidden" -> "Akses ditolak untuk level akun ini"
    "missing_id" -> "Data tidak lengkap"
    "bad_action" -> "Aksi tidak dikenali"
    "server_error" -> "Kesalahan server"
    "smtp_not_configured" -> "SMTP belum dikonfigurasi. Set dulu di web."
    else -> "Terjadi kesalahan ($code)"
}

fun formatTanggal(iso: String?): String {
    if (iso.isNullOrBlank()) return "-"
    if (iso.length < 10) return iso
    val bulan = mapOf(
        "01" to "Jan", "02" to "Feb", "03" to "Mar", "04" to "Apr",
        "05" to "Mei", "06" to "Jun", "07" to "Jul", "08" to "Agu",
        "09" to "Sep", "10" to "Okt", "11" to "Nov", "12" to "Des",
    )
    val d = iso.substring(8, 10)
    val m = bulan[iso.substring(5, 7)] ?: iso.substring(5, 7)
    val y = iso.substring(0, 4)
    val jam = if (iso.length >= 16) " ${iso.substring(11, 16)}" else ""
    return "$d $m $y$jam"
}