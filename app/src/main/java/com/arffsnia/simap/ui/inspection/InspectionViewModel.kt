package com.arffsnia.simap.ui.inspection

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arffsnia.simap.SimapApp
import com.arffsnia.simap.data.dto.AssetDetail
import com.arffsnia.simap.data.dto.ChecklistItem
import com.arffsnia.simap.data.repo.AppResult
import com.arffsnia.simap.data.repo.CheckRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InspectionViewModel : ViewModel() {
    private val assetRepo = SimapApp.instance.assetRepo
    private val checkRepo = SimapApp.instance.checkRepo

    var detail by mutableStateOf<AssetDetail?>(null)
    var loading by mutableStateOf(true)
    var error by mutableStateOf<String?>(null)
    var saving by mutableStateOf(false)
    var saved by mutableStateOf<CheckRepository.SaveResult?>(null)

    var values by mutableStateOf<Map<String, String>>(emptyMap())
    var notes by mutableStateOf<Map<String, String>>(emptyMap())
    var photoPath by mutableStateOf<String?>(null)

    var latitude by mutableStateOf<String?>(null)
    var longitude by mutableStateOf<String?>(null)
    var locationText by mutableStateOf("-")

    fun load(slug: String) {
        viewModelScope.launch {
            loading = true
            // Reset state cek sebelumnya — ViewModel dishare antar layar, jadi
            // tanpa reset, hasil "Selesai" cek pertama kebawa ke cek kedua.
            saved = null
            photoPath = null
            error = null
            latitude = null
            longitude = null
            locationText = "-"
            when (val r = assetRepo.assetBySlug(slug)) {
                is AppResult.Success -> {
                    detail = r.data
                    error = null
                    // prefill dari hasil cek terakhir
                    val prev = r.data.latestCheck?.result.orEmpty()
                    val v = mutableMapOf<String, String>()
                    val n = mutableMapOf<String, String>()
                    prev.forEach { (k, valv) ->
                        if (k.startsWith("_desc_")) n[k.removePrefix("_desc_")] = valv ?: ""
                        else v[k] = valv ?: ""
                    }
                    values = v
                    notes = n
                }
                is AppResult.Error -> error = com.arffsnia.simap.ui.mapError(r.message)
            }
            loading = false
        }
    }

    fun setStatus(kode: String, value: String) {
        values = values + (kode to value)
    }

    fun setNote(kode: String, text: String) {
        notes = notes + (kode to text)
    }

    fun setPhoto(path: String?) {
        photoPath = path
    }

    /** Set lokasi hasil GPS. Balik true kalau dapat, false kalau null. */
    fun setLocation(pair: Pair<Double, Double>?) {
        if (pair == null) {
            latitude = null
            longitude = null
            locationText = "GPS belum terdeteksi"
            return
        }
        latitude = pair.first.toString()
        longitude = pair.second.toString()
        locationText = com.arffsnia.simap.util.LocationHelper.format(pair)
    }

    /** Reset state hasil simpan (buat cek ulang). */
    fun resetSave() {
        saved = null
    }

    fun save() {
        val d = detail ?: return
        if (saving) return

        // Validasi item wajib
        val missing = d.template.orEmpty().filter { it.wajib == 1 && values[it.kodeItem].isNullOrBlank() }
        if (missing.isNotEmpty()) {
            error = "Lengkapi item: " + missing.joinToString(", ") { it.label }
            return
        }

        val resultMap = mutableMapOf<String, String>()
        values.forEach { (k, v) -> if (v.isNotBlank()) resultMap[k] = v }
        notes.forEach { (k, v) -> if (v.isNotBlank()) resultMap["_desc_$k"] = v }

        val checkedAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())

        viewModelScope.launch {
            saving = true
            error = null
            when (val r = checkRepo.saveInspection(
                assetId = d.id,
                slug = d.slug,
                nama = d.nama,
                categoryId = d.categoryId,
                category = d.category,
                result = resultMap,
                fotoPath = photoPath,
                checkedAt = checkedAt,
                latitude = latitude,
                longitude = longitude,
            )) {
                is AppResult.Success -> saved = r.data
                is AppResult.Error -> error = com.arffsnia.simap.ui.mapError(r.message)
            }
            saving = false
        }
    }
}