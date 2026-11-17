package com.arffsnia.simap.data.repo

import com.arffsnia.simap.data.api.ApiClient
import com.arffsnia.simap.data.api.ApiService
import com.arffsnia.simap.data.dto.ApproveRequest
import com.arffsnia.simap.data.dto.DashboardResponse
import com.arffsnia.simap.data.dto.MessageResponse
import com.arffsnia.simap.data.dto.MissingAsset
import com.arffsnia.simap.data.dto.Penugasan
import com.arffsnia.simap.data.dto.PenugasanDetail
import com.arffsnia.simap.data.dto.RecipientsActionRequest
import com.arffsnia.simap.data.dto.RejectRequest
import com.arffsnia.simap.data.dto.ReminderLogItem
import com.arffsnia.simap.data.dto.ReminderRecipientsResponse
import com.arffsnia.simap.data.dto.SendReminderRequest
import com.arffsnia.simap.data.dto.SendReminderResponse
import com.arffsnia.simap.data.dto.ValidasiResponse

/** Repositori endpoint admin (dashboard, penugasan, validasi, pengingat). */
class AdminRepository(private val api: ApiService) {

    suspend fun dashboard(): AppResult<DashboardResponse> = call { api.dashboard() }

    suspend fun missing(): AppResult<List<MissingAsset>> = call { api.missing().items }

    suspend fun penugasan(): AppResult<List<Penugasan>> = call { api.penugasan().items }

    suspend fun penugasanDetail(id: Int): AppResult<PenugasanDetail> = call { api.penugasanDetail(id) }

    suspend fun validasi(): AppResult<ValidasiResponse> = call { api.validasi() }

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

    suspend fun reminderSendAll(): AppResult<SendReminderResponse> =
        call { api.reminderSend(SendReminderRequest("send_all", null)) }

    suspend fun reminderSendOne(logId: Int): AppResult<SendReminderResponse> =
        call { api.reminderSend(SendReminderRequest("send_one", logId)) }

    private inline fun <T> call(block: () -> T): AppResult<T> = try {
        AppResult.Success(block())
    } catch (e: Exception) {
        AppResult.Error(ApiClient.errorMessage(e))
    }
}