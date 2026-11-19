package com.arffsnia.simap.ui.inspection

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.arffsnia.simap.data.dto.ChecklistItem
import com.arffsnia.simap.ui.theme.Good
import com.arffsnia.simap.ui.theme.Warn
import com.arffsnia.simap.util.LocationHelper
import com.arffsnia.simap.util.compressImage
import java.io.File

private val STATUS_OPTIONS = listOf("Baik", "Tidak Baik", "Tidak Ada")

private val InsBg = Color(0xFFF1F5F9)
private val InsBorder = Color(0xFFE2E8F0)
private val InsHair = Color(0xFFF1F5F9)
private val InsSlate700 = Color(0xFF334155)
private val InsSlate500 = Color(0xFF64748B)
private val Teal600 = Color(0xFF0D9488)

@Composable
fun InspectionScreen(
    assetId: Int,
    slug: String,
    nama: String,
    categoryId: Int,
    onBack: () -> Unit,
    onDone: () -> Unit,
    vm: InspectionViewModel = viewModel(),
) {
    val context = LocalContext.current

    LaunchedEffect(slug) { vm.load(slug) }

    LaunchedEffect(Unit) {
        if (LocationHelper.hasPermission(context)) {
            vm.setLocation(LocationHelper.lastKnown(context))
        }
    }
    val locPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) vm.setLocation(LocationHelper.lastKnown(context))
    }

    val template = vm.detail?.template.orEmpty()
    val terisi = template.count { !vm.values[it.kodeItem].isNullOrBlank() }

    Column(Modifier.fillMaxSize().background(InsBg).statusBarsPadding()) {
        // ===== Hero =====
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(Color(0xFF0D9488), Color(0xFF06B6D4))))
                .padding(horizontal = 12.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Rounded.ArrowBack, "Kembali", Modifier.size(16.dp), tint = Color.White)
            }
            Spacer(Modifier.width(6.dp))
            Column(Modifier.weight(1f)) {
                Text("Periksa Aset", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                Text(
                    "Home / ${nama}",
                    fontSize = 9.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 1,
                )
            }
            Text(
                "$terisi/${template.size}",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.White.copy(alpha = 0.22f))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }

        when {
            vm.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Teal600)
            }

            vm.detail == null -> Text(
                vm.error ?: "Gagal memuat aset",
                color = Color(0xFFBE123C),
                fontSize = 11.sp,
                modifier = Modifier.padding(18.dp),
            )

            else -> {
                val d = vm.detail!!
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp)
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(11.dp),
                ) {
                    // ===== Identitas aset =====
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .border(1.dp, InsBorder, RoundedCornerShape(14.dp)),
                    ) {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .background(Brush.linearGradient(listOf(Color(0xFF0D9488), Color(0xFF06B6D4))))
                                .padding(11.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text("Pengecekan Aset :", fontSize = 9.sp, color = Color(0xFFA7F3D0))
                            Text(d.nama, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(
                                d.slug,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFD1FAE5),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF0F766E))
                                    .padding(horizontal = 5.dp, vertical = 1.dp),
                            )
                        }
                        d.fotoUrl?.let { url ->
                            Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = "Foto aset",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                )
                                Spacer(Modifier.width(9.dp))
                                Text(
                                    listOfNotNull(d.category, d.room).joinToString(" • "),
                                    fontSize = 9.sp,
                                    color = InsSlate500,
                                )
                            }
                        }
                    }

                    // ===== Lokasi =====
                    KartuIns("Lokasi Pengecekan", "GPS") {
                        Row(
                            Modifier.fillMaxWidth().padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Rounded.MyLocation, null, Modifier.size(15.dp), tint = Color(0xFF4F46E5))
                            Spacer(Modifier.width(7.dp))
                            Text(
                                vm.locationText,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = InsSlate700,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                if (vm.locationText == "GPS belum terdeteksi" || vm.locationText == "-") "Ambil GPS" else "Muat Ulang",
                                fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(7.dp))
                                    .background(Teal600)
                                    .clickable {
                                        if (LocationHelper.hasPermission(context)) {
                                            vm.setLocation(LocationHelper.lastKnown(context))
                                        } else {
                                            locPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                        }
                                    }
                                    .padding(horizontal = 9.dp, vertical = 4.dp),
                            )
                        }
                    }

                    // ===== Checklist =====
                    KartuIns("Checklist Pengecekan", "$terisi/${template.size}") {
                        Column(
                            Modifier.padding(9.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            template.forEach { item ->
                                ItemCek(
                                    item = item,
                                    status = vm.values[item.kodeItem] ?: "",
                                    note = vm.notes[item.kodeItem] ?: "",
                                    onStatus = { vm.setStatus(item.kodeItem, it) },
                                    onNote = { vm.setNote(item.kodeItem, it) },
                                )
                            }
                        }
                    }

                    // ===== Foto =====
                    KartuIns("Foto Dokumentasi", "Bukti") {
                        PhotoSection(photoPath = vm.photoPath, onPhoto = { vm.setPhoto(it) })
                    }

                    vm.error?.let {
                        Text(it, fontSize = 10.sp, color = Color(0xFFBE123C))
                    }

                    vm.saved?.let { s -> SaveResultCard(s) }

                    Spacer(Modifier.height(6.dp))
                }

                // ===== Aksi bawah =====
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                ) {
                    if (vm.saved != null) {
                        TombolAksi("Selesai", aktif = true, gelap = false, onClick = onDone)
                    } else {
                        TombolAksi(
                            label = if (vm.saving) "Menyimpan..." else "Simpan Pengecekan",
                            aktif = !vm.saving && vm.detail != null,
                            sibuk = vm.saving,
                            onClick = { vm.save() },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TombolAksi(
    label: String,
    aktif: Boolean,
    gelap: Boolean = false,
    sibuk: Boolean = false,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (aktif) (if (gelap) Color(0xFF0F172A) else Teal600) else Color(0xFF94A3B8))
            .clickable(enabled = aktif) { onClick() },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (sibuk) {
            CircularProgressIndicator(Modifier.size(15.dp), strokeWidth = 2.dp, color = Color.White)
        } else {
            Icon(
                if (gelap) Icons.Rounded.CheckCircle else Icons.Rounded.CheckCircle,
                null, Modifier.size(14.dp), tint = Color.White,
            )
            Spacer(Modifier.width(5.dp))
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
private fun KartuIns(judul: String, badge: String, isi: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, InsBorder, RoundedCornerShape(14.dp)),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(Color(0xFF0D9488), Color(0xFF06B6D4))))
                .padding(horizontal = 11.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(judul, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.weight(1f))
            Text(
                badge.uppercase(),
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(alpha = 0.22f))
                    .padding(horizontal = 5.dp, vertical = 2.dp),
            )
        }
        isi()
    }
}

@Composable
private fun ItemCek(
    item: ChecklistItem,
    status: String,
    note: String,
    onStatus: (String) -> Unit,
    onNote: (String) -> Unit,
) {
    val warna = when (status) {
        "Baik" -> Color(0xFF059669)
        "Tidak Baik" -> Color(0xFFDC2626)
        "Tidak Ada" -> Color(0xFFB45309)
        else -> Color(0xFF94A3B8)
    }
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, warna.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
            .padding(9.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                item.label + if (item.wajib == 1) " *" else "",
                fontSize = 10.sp, fontWeight = FontWeight.Bold, color = InsSlate700,
                modifier = Modifier.weight(1f),
            )
            Text(
                status.ifBlank { "belum" },
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = warna,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(warna.copy(alpha = 0.12f))
                    .padding(horizontal = 5.dp, vertical = 1.dp),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            STATUS_OPTIONS.forEach { opt ->
                val aktif = status == opt
                val c = when (opt) {
                    "Baik" -> Color(0xFF059669)
                    "Tidak Baik" -> Color(0xFFDC2626)
                    else -> Color(0xFFB45309)
                }
                Text(
                    opt,
                    fontSize = 9.sp,
                    fontWeight = if (aktif) FontWeight.Bold else FontWeight.Medium,
                    color = if (aktif) Color.White else c,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (aktif) c else Color.White)
                        .border(1.dp, c.copy(alpha = if (aktif) 1f else 0.45f), RoundedCornerShape(999.dp))
                        .clickable { onStatus(opt) }
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
        }
        OutlinedTextField(
            value = note,
            onValueChange = onNote,
            placeholder = { Text("Catatan (opsional)", fontSize = 9.sp) },
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.sp),
            modifier = Modifier.fillMaxWidth().height(48.dp),
        )
    }
}

@Composable
private fun SaveResultCard(r: com.arffsnia.simap.data.repo.CheckRepository.SaveResult) {
    val ok = r.synced
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (ok) Color(0xFFECFDF5) else Color(0xFFFFFBEB))
            .border(1.dp, if (ok) Color(0xFFA7F3D0) else Color(0xFFFDE68A), RoundedCornerShape(12.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            if (ok) Icons.Rounded.CheckCircle else Icons.Rounded.Schedule,
            null, Modifier.size(16.dp),
            tint = if (ok) Good else Warn,
        )
        Spacer(Modifier.width(7.dp))
        Column(Modifier.weight(1f)) {
            Text(
                if (ok) "Tersimpan & terkirim" else "Tersimpan offline",
                fontSize = 10.sp, fontWeight = FontWeight.Bold, color = InsSlate700,
            )
            Text(r.message, fontSize = 9.sp, color = InsSlate500)
        }
    }
}

@Composable
private fun PhotoSection(photoPath: String?, onPhoto: (String?) -> Unit) {
    val context = LocalContext.current
    var pendingFile by remember { mutableStateOf<File?>(null) }

    val takePicture = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { ok ->
        val f = pendingFile ?: return@rememberLauncherForActivityResult
        if (ok) {
            val small = compressImage(f.absolutePath) ?: f.absolutePath
            onPhoto(small)
        } else {
            f.delete()
        }
        pendingFile = null
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val f = pendingFile ?: return@rememberLauncherForActivityResult
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", f)
            takePicture.launch(uri)
        }
    }

    fun takePhoto() {
        val f = File(context.cacheDir, "simap_${System.currentTimeMillis()}.jpg")
        pendingFile = f
        if (androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", f)
            takePicture.launch(uri)
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Column(Modifier.padding(9.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
        if (!photoPath.isNullOrBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = File(photoPath),
                    contentDescription = "Foto dokumentasi",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .border(1.dp, InsBorder, RoundedCornerShape(9.dp)),
                )
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = { onPhoto(null); File(photoPath).delete() }) {
                    Icon(Icons.Rounded.Close, "Hapus foto", Modifier.size(15.dp), tint = Color(0xFFDC2626))
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF8FAFC))
                .border(1.dp, InsBorder, RoundedCornerShape(10.dp))
                .clickable { takePhoto() }
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(Icons.Rounded.CameraAlt, null, Modifier.size(15.dp), tint = Teal600)
            Spacer(Modifier.width(6.dp))
            Text(
                if (photoPath.isNullOrBlank()) "Ambil Foto Dokumentasi" else "Foto Ulang",
                fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Teal600,
            )
        }
    }
}
