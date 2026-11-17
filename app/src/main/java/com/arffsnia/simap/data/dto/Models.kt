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
    @SerializedName("updated_at") val updatedAt: String?,
)

data class AssetsResponse(
    val page: Int,
    val limit: Int,
    val total: Int,
    val items: List<Asset>,
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
    @SerializedName("updated_at") val updatedAt: String?,
    val template: List<ChecklistItem>?,
    @SerializedName("latest_check") val latestCheck: LatestCheck?,
)

data class LatestCheck(
    val result: Map<String, String?>?,
    @SerializedName("result_json") val resultJson: String?,
    @SerializedName("checked_at") val checkedAt: String?,
    @SerializedName("is_legacy") val isLegacy: Int?,
    val coords: Coords?,
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