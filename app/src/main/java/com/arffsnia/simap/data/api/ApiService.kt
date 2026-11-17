package com.arffsnia.simap.data.api

import com.arffsnia.simap.data.dto.ApproveRequest
import com.arffsnia.simap.data.dto.AssetDetail
import com.arffsnia.simap.data.dto.AssetsResponse
import com.arffsnia.simap.data.dto.ChecklistResponse
import com.arffsnia.simap.data.dto.DashboardResponse
import com.arffsnia.simap.data.dto.LoginRequest
import com.arffsnia.simap.data.dto.LoginResponse
import com.arffsnia.simap.data.dto.MeResponse
import com.arffsnia.simap.data.dto.MessageResponse
import com.arffsnia.simap.data.dto.MissingResponse
import com.arffsnia.simap.data.dto.PenugasanDetail
import com.arffsnia.simap.data.dto.PenugasanListResponse
import com.arffsnia.simap.data.dto.PhotoResponse
import com.arffsnia.simap.data.dto.RecipientsActionRequest
import com.arffsnia.simap.data.dto.RejectRequest
import com.arffsnia.simap.data.dto.ReminderLogResponse
import com.arffsnia.simap.data.dto.ReminderRecipientsResponse
import com.arffsnia.simap.data.dto.SaveCheckRequest
import com.arffsnia.simap.data.dto.SaveCheckResponse
import com.arffsnia.simap.data.dto.SendReminderRequest
import com.arffsnia.simap.data.dto.SendReminderResponse
import com.arffsnia.simap.data.dto.SimpleItem
import com.arffsnia.simap.data.dto.ValidasiResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {

    // ===== Auth =====
    @POST("api/auth/login.php")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @GET("api/auth/me.php")
    suspend fun me(): MeResponse

    // ===== Master data =====
    @GET("api/categories/index.php")
    suspend fun categories(): List<SimpleItem>

    @GET("api/rooms/index.php")
    suspend fun rooms(): List<SimpleItem>

    @GET("api/periode/index.php")
    suspend fun periode(): List<SimpleItem>

    // ===== Aset =====
    @GET("api/assets/index.php")
    suspend fun assets(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("search") search: String? = null,
        @Query("status") status: String? = null,
        @Query("category_id") categoryId: Int? = null,
        @Query("room_id") roomId: Int? = null,
        @Query("periode_id") periodeId: Int? = null,
    ): AssetsResponse

    @GET("api/assets/show_by_slug.php")
    suspend fun assetBySlug(@Query("slug") slug: String): AssetDetail

    @GET("api/checklist/index.php")
    suspend fun checklist(@Query("category_id") categoryId: Int): ChecklistResponse

    @POST("api/checks/save.php")
    suspend fun saveCheck(@Body body: SaveCheckRequest): SaveCheckResponse

    @Multipart
    @POST("api/assets/upload_photo.php")
    suspend fun uploadPhoto(
        @Part("id") id: RequestBody,
        @Part foto: MultipartBody.Part,
    ): PhotoResponse

    // ===== Dashboard =====
    @GET("api/dashboard/index.php")
    suspend fun dashboard(): DashboardResponse

    @GET("api/dashboard/missing.php")
    suspend fun missing(): MissingResponse

    // ===== Penugasan =====
    @GET("api/penugasan/index.php")
    suspend fun penugasan(): PenugasanListResponse

    @GET("api/penugasan/show.php")
    suspend fun penugasanDetail(@Query("id") id: Int): PenugasanDetail

    // ===== Validasi =====
    @GET("api/validasi/index.php")
    suspend fun validasi(): ValidasiResponse

    @POST("api/validasi/approve.php")
    suspend fun approveValidasi(@Body body: ApproveRequest): MessageResponse

    @POST("api/validasi/reject.php")
    suspend fun rejectValidasi(@Body body: RejectRequest): MessageResponse

    // ===== Reminders =====
    @GET("api/reminders/recipients.php")
    suspend fun reminderRecipients(): ReminderRecipientsResponse

    @POST("api/reminders/recipients.php")
    suspend fun reminderRecipientsAction(@Body body: RecipientsActionRequest): MessageResponse

    @GET("api/reminders/index.php")
    suspend fun reminderLog(): ReminderLogResponse

    @POST("api/reminders/recompute.php")
    suspend fun reminderRecompute(): MessageResponse

    @POST("api/reminders/send.php")
    suspend fun reminderSend(@Body body: SendReminderRequest): SendReminderResponse
}