package com.arffsnia.simap.data.repo

import com.arffsnia.simap.data.api.ApiClient
import com.arffsnia.simap.data.api.ApiService
import com.arffsnia.simap.data.dto.ApproveRequest
import com.arffsnia.simap.data.dto.AssetHistoryResponse
import com.arffsnia.simap.data.dto.AssetMapResponse
import com.arffsnia.simap.data.dto.AssetsResponse
import com.arffsnia.simap.data.dto.ReguAnggotaResponse
import com.arffsnia.simap.data.dto.ReguResponse
import com.arffsnia.simap.data.dto.UsersResponse
import com.arffsnia.simap.data.dto.DashboardMobile
import com.arffsnia.simap.data.dto.DashboardResponse
import com.arffsnia.simap.data.dto.LaporanHarianLive
import com.arffsnia.simap.data.dto.LogResponse
import com.arffsnia.simap.data.dto.MessageResponse
import com.arffsnia.simap.data.dto.MissingAsset
import com.arffsnia.simap.data.dto.NotifResponse
import com.arffsnia.simap.data.dto.Penugasan
import com.arffsnia.simap.data.dto.PenugasanDetail
import com.arffsnia.simap.data.dto.PenugasanAssetsResponse
import com.arffsnia.simap.data.dto.PenugasanIdRequest
import com.arffsnia.simap.data.dto.PenugasanOptions
import com.arffsnia.simap.data.dto.PenugasanSaveRequest
import com.arffsnia.simap.data.dto.RecipientsActionRequest
import com.arffsnia.simap.data.dto.RejectRequest
import com.arffsnia.simap.data.dto.ReminderDeleteRequest
import com.arffsnia.simap.data.dto.ReminderLogItem
import com.arffsnia.simap.data.dto.ReminderRecipientsResponse
import com.arffsnia.simap.data.dto.SendReminderRequest
import com.arffsnia.simap.data.dto.SendReminderResponse
import com.arffsnia.simap.data.dto.SimpleItem
import com.arffsnia.simap.data.dto.ValidasiDetail
import com.arffsnia.simap.data.dto.ValidasiResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

/** Repositori endpoint admin (dashboard, penugasan, validasi, pengingat). */
class AdminRepository(private val api: ApiService) {

    suspend fun dashboard(): AppResult<DashboardResponse> = call { api.dashboard() }

    suspend fun dashboardMobile(): AppResult<DashboardMobile> = call { api.dashboardMobile() }

    suspend fun assetsMap(status: String): AppResult<AssetMapResponse> = call { api.assetsMap(status) }

    suspend fun assetHistory(slug: String, tahun: Int?): AppResult<AssetHistoryResponse> =
        call { api.assetHistory(slug, tahun) }

    suspend fun rooms(): AppResult<List<SimpleItem>> = call { api.rooms() }

    suspend fun categories(): AppResult<List<SimpleItem>> = call { api.categories() }

    suspend fun laporanAsetPdf(jenis: String, lokasi: String, search: String): AppResult<ByteArray> =
        call {
            api.laporanAsetPdf(
                jenis = jenis.ifBlank { null },
                lokasi = lokasi.ifBlank { null },
                search = search.ifBlank { null },
            ).bytes()
        }

    suspend fun assetsPage(
        page: Int,
        limit: Int,
        search: String,
        status: String?,
        roomId: Int?,
        categoryId: Int? = null,
    ): AppResult<AssetsResponse> = call {
        api.assets(
            page = page,
            limit = limit,
            search = search.ifBlank { null },
            status = status,
            categoryId = categoryId,
            roomId = roomId,
            periodeId = null,
        )
    }

    suspend fun logs(search: String, page: Int, limit: Int): AppResult<LogResponse> =
        call { api.logs(search.ifBlank { null }, page, limit) }

    suspend fun missing(): AppResult<List<MissingAsset>> = call { api.missing().items }

    suspend fun notifikasi(
        tab: String,
        reguId: Int,
        search: String,
        page: Int,
        limit: Int,
    ): AppResult<NotifResponse> = call {
        api.notifikasi(tab, reguId, search.ifBlank { null }, page, limit)
    }

    suspend fun penugasan(): AppResult<List<Penugasan>> = call { api.penugasan().items }

    suspend fun penugasanDetail(id: Int): AppResult<PenugasanDetail> = call { api.penugasanDetail(id) }

    suspend fun penugasanAssets(
        id: Int,
        status: String,
        categoryId: Int,
        minggu: Int,
        search: String,
        page: Int,
        limit: Int,
    ): AppResult<PenugasanAssetsResponse> = call {
        api.penugasanAssets(id, status, categoryId, minggu, search.ifBlank { null }, page, limit)
    }

    suspend fun penugasanOptions(): AppResult<PenugasanOptions> = call { api.penugasanOptions() }

    suspend fun penugasanCreate(body: PenugasanSaveRequest): AppResult<MessageResponse> =
        call { api.penugasanCreate(body) }

    suspend fun penugasanUpdate(body: PenugasanSaveRequest): AppResult<MessageResponse> =
        call { api.penugasanUpdate(body) }

    suspend fun penugasanDelete(id: Int): AppResult<MessageResponse> =
        call { api.penugasanDelete(PenugasanIdRequest(id)) }

    suspend fun validasi(): AppResult<ValidasiResponse> = call { api.validasi() }

    suspend fun validasiDetail(id: Int): AppResult<ValidasiDetail> = call { api.validasiDetail(id) }

    suspend fun approve(id: Int): AppResult<MessageResponse> =
        call { api.approveValidasi(ApproveRequest(id)) }

    suspend fun reject(id: Int, catatan: String?): AppResult<MessageResponse> =
        call { api.rejectValidasi(RejectRequest(id, catatan)) }

    suspend fun reminderRecipients(): AppResult<ReminderRecipientsResponse> =
        call { api.reminderRecipients() }

    suspend fun reminderAdd(userId: Int): AppResult<MessageResponse> =
        call { api.reminderRecipientsAction(RecipientsActionRequest("add", userId)) }

    suspend fun reminderRemove(userId: Int): AppResult<MessageResponse> =
        call { api.reminderRecipientsAction(RecipientsActionRequest("remove", userId)) }

    suspend fun reminderLog(): AppResult<List<ReminderLogItem>> = call { api.reminderLog().items }

    suspend fun reminderRecompute(): AppResult<MessageResponse> = call { api.reminderRecompute() }

    suspend fun reminderDelete(id: Int? = null, status: String? = null): AppResult<MessageResponse> =
        call { api.reminderDelete(ReminderDeleteRequest(id, status)) }

    suspend fun reminderSendAll(): AppResult<SendReminderResponse> =
        call { api.reminderSend(SendReminderRequest("send_all", null)) }

    suspend fun reminderSendOne(logId: Int): AppResult<SendReminderResponse> =
        call { api.reminderSend(SendReminderRequest("send_one", logId)) }

    suspend fun reguList(): AppResult<List<SimpleItem>> = call { api.reguList().items }

    suspend fun users(
        page: Int,
        limit: Int,
        search: String,
        level: String?,
        reguId: Int?,
    ): AppResult<UsersResponse> = call {
        api.users(page, limit, search.ifBlank { null }, level?.ifBlank { null }, reguId)
    }

    suspend fun userCreate(body: Map<String, Any?>): AppResult<MessageResponse> = call { api.userCreate(body) }

    suspend fun userUpdate(body: Map<String, Any?>): AppResult<MessageResponse> = call { api.userUpdate(body) }

    suspend fun userToggle(id: Int): AppResult<MessageResponse> = call { api.userToggle(mapOf("id" to id)) }

    suspend fun userDelete(id: Int): AppResult<MessageResponse> = call { api.userDelete(mapOf("id" to id)) }

    suspend fun reguAll(): AppResult<ReguResponse> = call { api.reguFull() }

    suspend fun reguCreate(nama: String, keterangan: String): AppResult<MessageResponse> =
        call { api.reguCreate(mapOf("nama" to nama, "keterangan" to keterangan)) }

    suspend fun reguUpdate(id: Int, nama: String, keterangan: String): AppResult<MessageResponse> =
        call { api.reguUpdate(mapOf("id" to id, "nama" to nama, "keterangan" to keterangan)) }

    suspend fun reguDelete(id: Int): AppResult<MessageResponse> =
        call { api.reguDelete(mapOf("id" to id)) }

    suspend fun reguAnggota(id: Int): AppResult<ReguAnggotaResponse> = call { api.reguAnggota(id) }

    suspend fun reguSetAnggota(id: Int, userIds: List<Int>): AppResult<MessageResponse> =
        call { api.reguSetAnggota(mapOf("id" to id, "user_ids" to userIds)) }

    suspend fun laporanHarian(tanggal: String, reguId: Int, shift: String?): AppResult<LaporanHarianLive> =
        call { api.laporanHarianLive(tanggal, reguId, shift?.ifBlank { null }) }

    /** Minta PDF laporan harian; server memakai template web + TTD digital. */
    suspend fun laporanHarianPdf(
        tanggal: String,
        reguId: Int,
        shift: String,
        chief: String,
        jabatan: String,
        foto: List<Pair<String, ByteArray>>,
    ): AppResult<ByteArray> = call {
        fun txt(s: String) = s.toRequestBody("text/plain".toMediaType())
        api.laporanHarianPdf(
            tanggal = txt(tanggal),
            regu = txt(reguId.toString()),
            shift = txt(shift),
            chief = txt(chief),
            jabatan = txt(jabatan),
            foto = foto.map { (nama, bytes) ->
                MultipartBody.Part.createFormData(
                    "foto[]", nama, bytes.toRequestBody("image/jpeg".toMediaType()),
                )
            },
        ).bytes()
    }

    private inline fun <T> call(block: () -> T): AppResult<T> = try {
        AppResult.Success(block())
    } catch (e: Exception) {
        AppResult.Error(ApiClient.errorMessage(e))
    }
}