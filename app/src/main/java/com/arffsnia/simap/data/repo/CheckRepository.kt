package com.arffsnia.simap.data.repo

import com.arffsnia.simap.data.api.ApiService
import com.arffsnia.simap.data.db.OfflineDb
import com.arffsnia.simap.data.dto.SaveCheckRequest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class CheckRepository(
    private val api: ApiService,
    private val db: OfflineDb,
) {

    data class SaveResult(
        val synced: Boolean,
        val message: String,
        val rusak: Boolean = false,
    )

    /**
     * Simpan inspeksi (offline-first): tulis ke antrian lokal dulu,
     * lalu coba sinkron segera. Hasil memberitahu status sinkron.
     */
    suspend fun saveInspection(
        assetId: Int,
        slug: String?,
        nama: String?,
        categoryId: Int,
        category: String?,
        result: Map<String, String>,
        fotoPath: String?,
        checkedAt: String,
        latitude: String?,
        longitude: String?,
    ): AppResult<SaveResult> {
        val id = db.queueCheck(
            assetId = assetId,
            slug = slug,
            nama = nama,
            categoryId = categoryId,
            category = category,
            result = result,
            fotoPath = fotoPath,
            checkedAt = checkedAt,
            latitude = latitude,
            longitude = longitude,
        )

        val synced = syncOne(id)
        return if (synced != null) {
            val rusak = synced.rusak == true
            AppResult.Success(
                SaveResult(
                    synced = true,
                    rusak = rusak,
                    message = if (rusak) "Terkirim ke server — ada item tidak baik" else "Terkirim ke server",
                )
            )
        } else {
            AppResult.Success(
                SaveResult(
                    synced = false,
                    rusak = false,
                    message = "Tersimpan offline — kirim otomatis saat online",
                )
            )
        }
    }

    /** Sinkron semua antrian; return jumlah yang berhasil. */
    suspend fun syncAll(): Int {
        var ok = 0
        for (p in db.pendingChecks(onlyUnsynced = true)) {
            if (syncOne(p.id) != null) ok++
        }
        return ok
    }

    /** Sinkron satu antrian; return respons atau null bila gagal (tetap di antrian). */
    private suspend fun syncOne(pendingId: Long): com.arffsnia.simap.data.dto.SaveCheckResponse? {
        val p = db.pendingChecks(onlyUnsynced = false).firstOrNull { it.id == pendingId }
            ?: return null

        return try {
            // 1) Foto (kalau ada) -> upload dulu, ambil nama file remote.
            //    Kalau gagal, JANGAN batalkan simpan cek — lanjut tanpa foto.
            var fotoRemote: String? = null
            if (!p.fotoPath.isNullOrBlank()) {
                var file = File(p.fotoPath)
                if (file.exists()) {
                    try {
                        // Pastikan kecil (foto lama dari HP kamera besar belum terkompresi)
                        com.arffsnia.simap.util.compressImage(file.absolutePath)?.let { file = File(it) }
                        val mediaType = "image/jpeg".toMediaTypeOrNull()
                        val body = file.asRequestBody(mediaType)
                        val part = MultipartBody.Part.createFormData("foto", file.name, body)
                        val idBody = p.assetId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                        val pr = api.uploadPhoto(idBody, part)
                        fotoRemote = pr.fotoUrl?.substringAfterLast('/')
                    } catch (e: Exception) {
                        android.util.Log.e("SIMAP", "upload foto gagal (lanjut simpan cek)", e)
                        fotoRemote = null
                    }
                }
            }

            // 2) Simpan hasil cek
            val payload = SaveCheckRequest(
                assetId = p.assetId,
                result = parseResult(p.resultJson),
                checkedAt = p.checkedAt,
                latitude = p.latitude,
                longitude = p.longitude,
                foto = fotoRemote,
            )
            android.util.Log.e("SIMAP", "sync id=$pendingId resultJson=${p.resultJson}")
            val resp = api.saveCheck(payload)

            // 3) Tandai tersinkron & hapus foto lokal
            db.markSynced(id = pendingId)
            p.fotoPath?.let { runCatching { File(it).delete() } }
            resp
        } catch (e: Exception) {
            android.util.Log.e("SIMAP", "sync gagal id=$pendingId", e)
            if (e is retrofit2.HttpException) {
                val body = try { e.response()?.errorBody()?.string() } catch (_: Throwable) { null }
                android.util.Log.e("SIMAP", "HTTP ${e.code()} body=$body")
            }
            null
        }
    }

    private fun parseResult(json: String): Map<String, String> {
        val out = mutableMapOf<String, String>()
        if (json.isBlank()) return out
        return try {
            val obj = com.google.gson.JsonParser.parseString(json).asJsonObject
            obj.entrySet().forEach { (k, v) -> out[k] = if (v.isJsonNull) "" else v.asString }
            out
        } catch (e: Throwable) {
            android.util.Log.e("SIMAP", "parseResult gagal: json=$json", e)
            out
        }
    }
}