package com.arffsnia.simap.data.dto

import com.google.gson.annotations.SerializedName

// ===== Auth =====
data class LoginRequest(
    val username: String,
    val password: String,
)

data class User(
    val id: Int,
    @SerializedName("full_name") val fullName: String?,
    val email: String?,
    val username: String?,
    val level: String?,
    @SerializedName("regu_id") val reguId: Int?,
    @SerializedName("profile_url") val profileUrl: String?,
)

data class LoginResponse(
    val token: String,
    val user: User,
)

data class MeResponse(
    val user: User,
    val token: TokenInfo,
)

data class TokenInfo(
    val exp: Long,
    @SerializedName("ttl_seconds") val ttlSeconds: Long,
)

// ===== Master data =====
data class SimpleItem(
    val id: Int,
    val nama: String,
)

// ===== Asset =====
data class Coords(
    val lat: Double?,
    val lng: Double?,
)

data class Checks(
    val pressure: String?,
    val pin: String?,
    val hose: String?,
    val klam: String?,   // klem (api menulis 'klam')
    val handle: String?,
    val flak: String?,   // fisik (api menulis 'flak')
)

data class Asset(
    val id: Int,
    val nama: String,
    val slug: String,
    val category: String?,
    val room: String?,
    val periode: String?,
    val berat: String?,
    val lokasi: String?,
    val tahun: Int?,
    val coords: Coords?,
    val checks: Checks?,
    @SerializedName("foto_url") val fotoUrl: String?,
    val qrcode: String?,
    val status: String? = null,
    val merek: String? = null,
    @SerializedName("last_checked") val lastChecked: String? = null,
    @SerializedName("updated_at") val updatedAt: String?,
)

data class AssetsResponse(
    val page: Int,
    val limit: Int,
    val total: Int,
    val stats: AssetStats = AssetStats(),
    val items: List<Asset>,
)

data class AssetStats(
    val aktif: Int = 0,
    val maintenance: Int = 0,
    val rusak: Int = 0,
    val total: Int = 0,
    val lokasi: Int = 0,
    val jenis: Int = 0,
)

// ===== Checklist =====
data class ChecklistItem(
    @SerializedName("kode_item") val kodeItem: String,
    val label: String,
    val urutan: Int,
    val wajib: Int,
)

data class ChecklistResponse(
    @SerializedName("category_id") val categoryId: Int,
    val items: List<ChecklistItem>,
)

// ===== Asset detail (show_by_slug) =====
data class AssetDetail(
    val id: Int,
    val nama: String,
    val slug: String,
    val merek: String?,
    val status: String?,
    @SerializedName("category_id") val categoryId: Int,
    val category: String?,
    @SerializedName("periode_id") val periodeId: Int,
    val periode: String?,
    @SerializedName("room_id") val roomId: Int,
    val room: String?,
    val berat: String?,
    val lokasi: String?,
    val tahun: Int?,
    val coords: Coords?,
    @SerializedName("foto_url") val fotoUrl: String?,
    val qrcode: String?,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String?,
    val template: List<ChecklistItem>?,
    @SerializedName("latest_check") val latestCheck: LatestCheck?,
)

data class LatestCheck(
    val result: Map<String, String?>?,
    @SerializedName("result_json") val resultJson: String?,
    @SerializedName("checked_at") val checkedAt: String?,
    val user: String? = null,
    val periode: String? = null,
    @SerializedName("is_legacy") val isLegacy: Int?,
    val coords: Coords?,
)

// ===== Riwayat pengecekan aset (api/assets/history.php) =====
data class AssetHistoryResponse(
    val slug: String = "",
    val nama: String = "",
    val tahun: List<Int> = emptyList(),
    val total: Int = 0,
    val items: List<AssetHistoryItem> = emptyList(),
)

data class AssetHistoryItem(
    val id: Int = 0,
    @SerializedName("checked_at") val checkedAt: String? = null,
    val user: String = "",
    val level: String = "",
    val periode: String = "",
    val checks: Map<String, String?> = emptyMap(),
    @SerializedName("foto_url") val fotoUrl: String? = null,
    val coords: Coords? = null,
)

// ===== Save check =====
data class SaveCheckRequest(
    @SerializedName("asset_id") val assetId: Int,
    val result: Map<String, String>,
    @SerializedName("checked_at") val checkedAt: String?,
    val latitude: String?,
    val longitude: String?,
    val foto: String?,
)

data class SaveCheckResponse(
    val message: String?,
    @SerializedName("asset_id") val assetId: Int?,
    @SerializedName("periode_id") val periodeId: Int?,
    @SerializedName("checked_at") val checkedAt: String?,
    val rusak: Boolean?,
    val problems: Map<String, String>?,
)

data class PhotoResponse(
    val message: String?,
    val id: Int?,
    @SerializedName("foto_url") val fotoUrl: String?,
)

data class ApiError(
    val error: String?,
)

// ===== Dashboard (beranda Super Admin) =====
data class DashboardSummary(
    @SerializedName("total_aset") val totalAset: Int,
    val terperiksa: Int,
    @SerializedName("belum_dicek") val belumDicek: Int,
    @SerializedName("jumlah_regu") val jumlahRegu: Int,
)

data class CoveragePoint(
    val label: String,
    val checked: Int,
    val total: Int,
    val pct: Int,
)

data class DashboardResponse(
    val summary: DashboardSummary,
    val coverage: List<CoveragePoint>,
    @SerializedName("missing_total") val missingTotal: Int,
)

// ===== Aset belum dicek =====
data class MissingAsset(
    val id: Int,
    val slug: String,
    val nama: String,
    val lokasi: String?,
    val jenis: String?,
    val urgency: String?,
    @SerializedName("last_check") val lastCheck: String?,
    @SerializedName("foto_url") val fotoUrl: String?,
)

data class MissingResponse(val items: List<MissingAsset>)

// ===== Penugasan =====
data class Penugasan(
    val id: Int,
    val nama: String,
    val regu: String?,
    @SerializedName("regu_id") val reguId: Int?,
    @SerializedName("tanggal_mulai") val tanggalMulai: String?,
    @SerializedName("tanggal_selesai") val tanggalSelesai: String?,
    val keterangan: String?,
    val status: String?,
    @SerializedName("n_zonasi") val nZonasi: Int?,
    val total: Int,
    val checked: Int,
    val persen: Int,
    @SerializedName("is_active") val isActive: Boolean?,
)

data class PenugasanListResponse(val items: List<Penugasan>)

// ===== Penugasan: form tambah/edit =====

/** Data pendukung form: regu penanggung jawab, zonasi & lokasi tambahan. */
data class ZonasiOpt(
    val id: Int,
    val nama: String,
    val warna: String? = null,
    val lantai: String? = null,
    val poly: List<List<Double>> = emptyList(),
)

data class PeriodeOpt(
    val id: Int,
    val nama: String,
    @SerializedName("bulan_mulai") val bulanMulai: Int = 0,
    @SerializedName("bulan_selesai") val bulanSelesai: Int = 0,
)

data class PenugasanOptions(
    val regu: List<SimpleItem> = emptyList(),
    val zonasi: List<ZonasiOpt> = emptyList(),
    val rooms: List<SimpleItem> = emptyList(),
    val periode: List<PeriodeOpt> = emptyList(),
)

/** Body POST penugasan/create.php (tanpa id) dan penugasan/update.php (dengan id). */
data class PenugasanSaveRequest(
    val id: Int? = null,
    val nama: String,
    @SerializedName("regu_id") val reguId: Int,
    @SerializedName("tanggal_mulai") val tanggalMulai: String,
    @SerializedName("tanggal_selesai") val tanggalSelesai: String,
    val keterangan: String? = null,
    val status: String? = null,
    @SerializedName("zonasi_ids") val zonasiIds: List<Int>,
    @SerializedName("room_ids") val roomIds: List<Int> = emptyList(),
)

data class PenugasanIdRequest(val id: Int)

// ===== Detail penugasan: daftar aset + filter =====

data class PenugasanAset(
    val id: Int,
    val slug: String,
    val nama: String,
    val merek: String? = null,
    val lokasi: String? = null,
    @SerializedName("category_id") val categoryId: Int = 0,
    val kategori: String? = null,
    val checked: Boolean = false,
    @SerializedName("checked_at") val checkedAt: String? = null,
    @SerializedName("checked_by") val checkedBy: String? = null,
    @SerializedName("checked_regu") val checkedRegu: String? = null,
)

data class PenugasanKategori(val id: Int, val nama: String, val n: Int = 0)

data class PenugasanMinggu(val n: Int, val mulai: String?, val selesai: String?)

data class PenugasanFilterCount(val total: Int = 0, val sudah: Int = 0, val belum: Int = 0)

data class PenugasanAssetsResponse(
    val id: Int,
    val nama: String,
    val regu: String? = null,
    @SerializedName("tanggal_mulai") val tanggalMulai: String? = null,
    @SerializedName("tanggal_selesai") val tanggalSelesai: String? = null,
    val status: String? = null,
    val total: Int = 0,
    val checked: Int = 0,
    val persen: Int = 0,
    @SerializedName("dari_filter") val dariFilter: PenugasanFilterCount = PenugasanFilterCount(),
    val kategori: List<PenugasanKategori> = emptyList(),
    val minggu: List<PenugasanMinggu> = emptyList(),
    val zonasi: List<String> = emptyList(),
    val page: Int = 1,
    val limit: Int = 10,
    val pages: Int = 1,
    val hits: Int = 0,
    val items: List<PenugasanAset> = emptyList(),
)

data class PenugasanBreakdown(
    val nama: String,
    val n: Int,
    val done: Int,
    val persen: Int,
)

data class PenugasanDetail(
    val id: Int,
    val nama: String,
    val regu: String?,
    @SerializedName("regu_id") val reguId: Int?,
    @SerializedName("tanggal_mulai") val tanggalMulai: String?,
    @SerializedName("tanggal_selesai") val tanggalSelesai: String?,
    val status: String?,
    val keterangan: String?,
    val total: Int,
    val checked: Int,
    val persen: Int,
    val breakdown: List<PenugasanBreakdown>,
    @SerializedName("zonasi_ids") val zonasiIds: List<Int> = emptyList(),
    @SerializedName("room_ids") val roomIds: List<Int> = emptyList(),
)

// ===== Validasi (pengajuan) =====
data class ValidasiItem(
    val id: Int,
    val tipe: String?,
    val aksi: String?,
    @SerializedName("target_id") val targetId: Int?,
    val nama: String?,
    val status: String?,
    val pemohon: String?,
    @SerializedName("pemohon_username") val pemohonUsername: String?,
    val regu: String?,
    val peninjau: String?,
    val catatan: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("reviewed_at") val reviewedAt: String?,
)

data class ValidasiResponse(
    val pending: List<ValidasiItem>,
    val riwayat: List<ValidasiItem>,
)

/** Detail satu pengajuan (api/validasi/show.php) termasuk isi data_json. */
data class ValidasiDetail(
    val id: Int,
    val tipe: String? = null,
    val aksi: String? = null,
    @SerializedName("target_id") val targetId: Int? = null,
    val status: String? = null,
    @SerializedName("requested_by") val requestedBy: Int? = null,
    @SerializedName("reviewed_by") val reviewedBy: Int? = null,
    val catatan: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("reviewed_at") val reviewedAt: String? = null,
    val data: Map<String, Any>? = null,
)

// ===== Pusat Notifikasi =====
data class NotifRingkasan(
    val total: Int = 0,
    val belum: Int = 0,
    @SerializedName("tanpa_foto") val tanpaFoto: Int = 0,
    val rusak: Int = 0,
    val maintenance: Int = 0,
)

data class NotifItem(
    val id: Int,
    val slug: String,
    val nama: String,
    val jenis: String? = null,
    val lokasi: String? = null,
    val zonasi: String? = null,
    val periode: String? = null,
    @SerializedName("periode_id") val periodeId: Int = 0,
    val badge: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null,
    val keterangan: String? = null,
)

data class NotifResponse(
    val periode: String? = null,
    val tab: String? = null,
    val ringkasan: NotifRingkasan = NotifRingkasan(),
    val regu: List<SimpleItem> = emptyList(),
    val page: Int = 1,
    val limit: Int = 10,
    val pages: Int = 1,
    val hits: Int = 0,
    val items: List<NotifItem> = emptyList(),
)

// ===== Pengingat (reminders) =====
data class ReminderRecipient(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("full_name") val fullName: String?,
    val level: String?,
    val email: String?,
)

data class ReminderAvailableUser(
    val id: Int,
    @SerializedName("full_name") val fullName: String?,
    val level: String?,
)

data class ReminderRecipientsResponse(
    val recipients: List<ReminderRecipient>,
    val available: List<ReminderAvailableUser>,
)

data class ReminderLogItem(
    val id: Int,
    @SerializedName("user_id") val userId: Int?,
    @SerializedName("periode_id") val periodeId: Int?,
    val tahun: Int?,
    @SerializedName("overdue_count") val overdueCount: Int?,
    val payload: String?,
    val channel: String?,
    val status: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("sent_at") val sentAt: String?,
    val error: String?,
    val penerima: String?,
    @SerializedName("penerima_email") val penerimaEmail: String?,
)

data class ReminderLogResponse(val items: List<ReminderLogItem>)

// ===== Request & response generik =====
data class ApproveRequest(val id: Int)
data class RejectRequest(val id: Int, val catatan: String?)
data class RecipientsActionRequest(val action: String, @SerializedName("user_id") val userId: Int)
data class ReminderDeleteRequest(val id: Int? = null, val status: String? = null)

data class SendReminderRequest(val action: String, @SerializedName("log_id") val logId: Int?)

data class MessageResponse(
    val message: String?,
    val id: Int?,
    @SerializedName("user_id") val userId: Int?,
    @SerializedName("pengajuan_id") val pengajuanId: Int?,
)

data class SendReminderResponse(
    val message: String?,
    val ok: Boolean?,
    val sent: Int?,
    val failed: Int?,
    val skipped: Int?,
    val total: Int?,
)

// ===== Laporan Harian (live) =====
data class ReguListResponse(val items: List<SimpleItem> = emptyList())

// ===== Kelola Regu =====
data class ReguAnggota(
    val id: Int,
    val nama: String,
    val level: String? = null,
)

data class ReguFull(
    val id: Int,
    val nama: String,
    val keterangan: String? = null,
    @SerializedName("jml_anggota") val jmlAnggota: Int = 0,
    val anggota: List<ReguAnggota> = emptyList(),
)

data class ReguStats(
    @SerializedName("total_regu") val totalRegu: Int = 0,
    @SerializedName("total_anggota") val totalAnggota: Int = 0,
    @SerializedName("regu_aktif") val reguAktif: Int = 0,
)

data class ReguResponse(
    val items: List<ReguFull> = emptyList(),
    val stats: ReguStats = ReguStats(),
)

data class ReguUser(
    val id: Int,
    val nama: String,
    val username: String? = null,
    val level: String? = null,
    val terpilih: Boolean = false,
    @SerializedName("regu_id") val reguId: Int? = null,
)

data class ReguAnggotaResponse(
    val regu: SimpleItem? = null,
    val items: List<ReguUser> = emptyList(),
)

data class LaporanHarianLive(
    val tanggal: String = "",
    @SerializedName("regu_id") val reguId: Int = 0,
    val shift: String? = null,
    @SerializedName("total_aset") val totalAset: Int = 0,
    val checked: Int = 0,
    val persen: Int = 0,
    @SerializedName("log_table") val logTable: Boolean = true,
    val summary: List<LaporanPenugasanRingkas> = emptyList(),
    val items: List<LaporanBaris> = emptyList(),
    // Pilihan form Cetak PDF.
    val chiefs: List<String> = emptyList(),
    val jabatan: List<String> = emptyList(),
)

data class LaporanPenugasanRingkas(
    @SerializedName("penugasan_id") val penugasanId: Int = 0,
    @SerializedName("penugasan_nama") val penugasanNama: String? = null,
    @SerializedName("regu_nama") val reguNama: String? = null,
    val total: Int = 0,
    @SerializedName("checked_today") val checkedToday: Int = 0,
)

data class LaporanBaris(
    @SerializedName("log_id") val logId: Int = 0,
    val slug: String = "",
    val nama: String = "",
    val merek: String? = null,
    val jenis: String? = null,
    val lokasi: String? = null,
    val pemeriksa: String? = null,
    @SerializedName("checked_at") val checkedAt: String? = null,
    @SerializedName("regu_nama") val reguNama: String? = null,
    @SerializedName("penugasan_nama") val penugasanNama: String? = null,
)

// ===== Dashboard mobile (api/dashboard/mobile.php) =====
data class DashboardMobile(
    val summary: DashboardMobileSummary = DashboardMobileSummary(),
    val coverage: CoverageBlock = CoverageBlock(),
    val kategori: List<KategoriStat> = emptyList(),
    val lokasi: List<LokasiStat> = emptyList(),
    val maintenance: List<BulanStat> = emptyList(),
    val inspeksi: InspeksiStat = InspeksiStat(),
)

data class DashboardMobileSummary(
    @SerializedName("total_aset") val totalAset: Int = 0,
    @SerializedName("total_lokasi") val totalLokasi: Int = 0,
    @SerializedName("total_jenis") val totalJenis: Int = 0,
    val terperiksa: Int = 0,
    @SerializedName("belum_dicek") val belumDicek: Int = 0,
)

data class CoverageBlock(
    val points: List<CoverageIns> = emptyList(),
    val active: CoverageIns? = null,
    /** Semua periode yang punya data inspeksi (multi-tahun) — untuk tab Pertahun. */
    val all: List<CoverageIns> = emptyList(),
    /** Daftar tahun (terbaru dulu) yang tersedia di [all]. */
    val years: List<Int> = emptyList(),
)

data class CoverageIns(
    val label: String = "",
    val checked: Int = 0,
    val total: Int = 0,
    val pct: Int = 0,
)

data class KategoriStat(val nama: String = "", val total: Int = 0)
data class LokasiStat(val nama: String = "", val total: Int = 0)
data class BulanStat(val label: String = "", val total: Int = 0)

data class InspeksiStat(
    val lengkap: Int = 0,
    val sebagian: Int = 0,
    val belum: Int = 0,
    val total: Int = 0,
)

// ===== Log aktivitas (api/logs/index.php) =====
data class LogResponse(
    val page: Int = 1,
    val limit: Int = 10,
    val total: Int = 0,
    val pages: Int = 1,
    val items: List<LogItem> = emptyList(),
)

data class LogItem(
    val id: Int = 0,
    val user: String = "",
    val level: String = "",
    val activity: String = "",
    val description: String = "",
    @SerializedName("created_at") val createdAt: String = "",
)

// ===== Peta sebaran aset (api/assets/map.php) =====
data class AssetMapResponse(
    val total: Int = 0,
    @SerializedName("tanpa_koordinat") val tanpaKoordinat: Int = 0,
    val items: List<AssetMapItem> = emptyList(),
)

data class AssetMapItem(
    val id: Int = 0,
    val nama: String = "",
    val slug: String = "",
    val status: String = "",
    val category: String? = null,
    val room: String? = null,
    val periode: String? = null,
    val lat: Double = 0.0,
    val lng: Double = 0.0,
)


// ===== Manajemen Pengguna =====
data class UserItem(
    val id: Int,
    @SerializedName("full_name") val fullName: String? = null,
    val username: String? = null,
    val email: String? = null,
    val level: String? = null,
    @SerializedName("regu_id") val reguId: Int? = null,
    @SerializedName("regu_nama") val reguNama: String? = null,
    @SerializedName("profile_url") val profileUrl: String? = null,
    @SerializedName("is_active") val isActive: Int = 1,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null,
)

data class UserStats(
    val total: Int = 0,
    val admin: Int = 0,
    val user: Int = 0,
    val baru: Int = 0,
    val nonaktif: Int = 0,
)

data class UsersResponse(
    val page: Int = 1,
    val limit: Int = 10,
    val total: Int = 0,
    val items: List<UserItem> = emptyList(),
    val stats: UserStats = UserStats(),
)
