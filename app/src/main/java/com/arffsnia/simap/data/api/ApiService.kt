package com.arffsnia.simap.data.api

import com.arffsnia.simap.data.dto.ApproveRequest
import com.arffsnia.simap.data.dto.AssetDetail
import com.arffsnia.simap.data.dto.AssetHistoryResponse
import com.arffsnia.simap.data.dto.AssetMapResponse
import com.arffsnia.simap.data.dto.AssetsResponse
import com.arffsnia.simap.data.dto.ReguAnggotaResponse
import com.arffsnia.simap.data.dto.ReguResponse
import com.arffsnia.simap.data.dto.UsersResponse
import com.arffsnia.simap.data.dto.ChecklistResponse
import com.arffsnia.simap.data.dto.DashboardMobile
import com.arffsnia.simap.data.dto.DashboardResponse
import com.arffsnia.simap.data.dto.LoginRequest
import com.arffsnia.simap.data.dto.LogItem
import com.arffsnia.simap.data.dto.LogResponse
import com.arffsnia.simap.data.dto.LoginResponse
import com.arffsnia.simap.data.dto.LaporanHarianLive
import com.arffsnia.simap.data.dto.MeResponse
import com.arffsnia.simap.data.dto.MessageResponse
import com.arffsnia.simap.data.dto.MissingResponse
import com.arffsnia.simap.data.dto.NotifResponse
import com.arffsnia.simap.data.dto.PenugasanDetail
import com.arffsnia.simap.data.dto.PenugasanAssetsResponse
import com.arffsnia.simap.data.dto.PenugasanIdRequest
import com.arffsnia.simap.data.dto.PenugasanListResponse
import com.arffsnia.simap.data.dto.PenugasanOptions
import com.arffsnia.simap.data.dto.PenugasanSaveRequest
import com.arffsnia.simap.data.dto.PhotoResponse
import com.arffsnia.simap.data.dto.RecipientsActionRequest
import com.arffsnia.simap.data.dto.ReguListResponse
import com.arffsnia.simap.data.dto.RejectRequest
import com.arffsnia.simap.data.dto.ReminderDeleteRequest
import com.arffsnia.simap.data.dto.ReminderLogResponse
import com.arffsnia.simap.data.dto.ReminderRecipientsResponse
import com.arffsnia.simap.data.dto.SaveCheckRequest
import com.arffsnia.simap.data.dto.SaveCheckResponse
import com.arffsnia.simap.data.dto.SendReminderRequest
import com.arffsnia.simap.data.dto.SendReminderResponse
import com.arffsnia.simap.data.dto.SimpleItem
import com.arffsnia.simap.data.dto.ValidasiDetail
import com.arffsnia.simap.data.dto.ValidasiResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
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
    @GET("api/assets/history.php")
    suspend fun assetHistory(
        @Query("slug") slug: String,
        @Query("tahun") tahun: Int?,
        @Query("limit") limit: Int = 50,
    ): AssetHistoryResponse

    @GET("api/assets/map.php")
    suspend fun assetsMap(@Query("status") status: String): AssetMapResponse

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

    @GET("api/penugasan/assets.php")
    suspend fun penugasanAssets(
        @Query("id") id: Int,
        @Query("status") status: String = "semua",
        @Query("category_id") categoryId: Int = 0,
        @Query("minggu") minggu: Int = 0,
        @Query("search") search: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
    ): PenugasanAssetsResponse

    @GET("api/penugasan/options.php")
    suspend fun penugasanOptions(): PenugasanOptions

    @POST("api/penugasan/create.php")
    suspend fun penugasanCreate(@Body body: PenugasanSaveRequest): MessageResponse

    @POST("api/penugasan/update.php")
    suspend fun penugasanUpdate(@Body body: PenugasanSaveRequest): MessageResponse

    @POST("api/penugasan/delete.php")
    suspend fun penugasanDelete(@Body body: PenugasanIdRequest): MessageResponse

    // ===== Notifikasi =====
    @GET("api/notifikasi/index.php")
    suspend fun notifikasi(
        @Query("tab") tab: String = "belum",
        @Query("regu_id") reguId: Int = 0,
        @Query("search") search: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
    ): NotifResponse

    // ===== Validasi =====
    @GET("api/validasi/index.php")
    suspend fun validasi(): ValidasiResponse

    @GET("api/validasi/show.php")
    suspend fun validasiDetail(@Query("id") id: Int): ValidasiDetail

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

    @POST("api/reminders/delete.php")
    suspend fun reminderDelete(@Body body: ReminderDeleteRequest): MessageResponse

    @POST("api/reminders/send.php")
    suspend fun reminderSend(@Body body: SendReminderRequest): SendReminderResponse

    // ===== Laporan Harian =====
    @GET("api/regu/index.php")
    suspend fun reguList(): ReguListResponse

    // ===== Manajemen Pengguna =====
    @GET("api/users/index.php")
    suspend fun users(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("search") search: String?,
        @Query("level") level: String?,
        @Query("regu_id") reguId: Int?,
    ): UsersResponse

    @POST("api/users/create.php")
    suspend fun userCreate(@Body body: Map<String, @JvmSuppressWildcards Any?>): MessageResponse

    @POST("api/users/update.php")
    suspend fun userUpdate(@Body body: Map<String, @JvmSuppressWildcards Any?>): MessageResponse

    @POST("api/users/toggle.php")
    suspend fun userToggle(@Body body: Map<String, @JvmSuppressWildcards Any?>): MessageResponse

    @POST("api/users/delete.php")
    suspend fun userDelete(@Body body: Map<String, @JvmSuppressWildcards Any?>): MessageResponse

    // ===== Kelola Regu =====
    @GET("api/regu/index.php")
    suspend fun reguFull(): ReguResponse

    @POST("api/regu/create.php")
    suspend fun reguCreate(@Body body: Map<String, @JvmSuppressWildcards Any?>): MessageResponse

    @POST("api/regu/update.php")
    suspend fun reguUpdate(@Body body: Map<String, @JvmSuppressWildcards Any?>): MessageResponse

    @POST("api/regu/delete.php")
    suspend fun reguDelete(@Body body: Map<String, @JvmSuppressWildcards Any?>): MessageResponse

    @GET("api/regu/anggota.php")
    suspend fun reguAnggota(@Query("id") id: Int): ReguAnggotaResponse

    @POST("api/regu/anggota.php")
    suspend fun reguSetAnggota(@Body body: Map<String, @JvmSuppressWildcards Any?>): MessageResponse

    @GET("api/laporan/harian_live.php")
    suspend fun laporanHarianLive(
        @Query("tanggal") tanggal: String,
        @Query("regu_id") reguId: Int,
        @Query("shift") shift: String?,
    ): LaporanHarianLive

    /** PDF laporan data aset (halaman web admin, otomatis pakai Bearer token). */
    @GET("admin/print_reportaset.php")
    suspend fun laporanAsetPdf(
        @Query("jenis") jenis: String?,
        @Query("lokasi") lokasi: String?,
        @Query("search") search: String?,
    ): ResponseBody

    // ===== Dashboard mobile =====
    @GET("api/dashboard/mobile.php")
    suspend fun dashboardMobile(): DashboardMobile

    @GET("api/logs/index.php")
    suspend fun logs(
        @Query("search") search: String?,
        @Query("page") page: Int,
        @Query("limit") limit: Int,
    ): LogResponse

    /** PDF Laporan Harian (Dompdf di server, template yang sama dengan web admin). */
    @Multipart
    @POST("admin/print_reportharian.php")
    suspend fun laporanHarianPdf(
        @Part("tanggal") tanggal: RequestBody,
        @Part("regu") regu: RequestBody,
        @Part("shift") shift: RequestBody,
        @Part("chief") chief: RequestBody,
        @Part("jabatan") jabatan: RequestBody,
        @Part foto: List<MultipartBody.Part>,
    ): ResponseBody
}