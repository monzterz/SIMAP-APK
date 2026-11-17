package com.arffsnia.simap.ui.inspection

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

@OptIn(ExperimentalMaterial3Api::class)
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

    // Lokasi: ambil diam-diam kalau izin sudah ada; tombol akan minta izin bila belum.
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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Cek Aset") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Kembali") }
                },
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                if (vm.saved != null) {
                    Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Rounded.CheckCircle, null)
                        Spacer(Modifier.padding(4.dp))
                        Text("Selesai")
                    }
                } else {
                    Button(
                        onClick = { vm.save() },
                        enabled = !vm.saving && vm.detail != null,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (vm.saving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(Icons.Rounded.Save, null)
                            Spacer(Modifier.padding(4.dp))
                            Text("Simpan Pengecekan")
                        }
                    }
                }
            }
        },
    ) { pad ->
        when {
            vm.loading -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(pad),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) { CircularProgressIndicator() }
            }
            vm.detail == null -> {
                Text(
                    vm.error ?: "Gagal memuat",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(pad).padding(16.dp),
                )
            }
            else -> {
                val d = vm.detail!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(pad)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                ) {
                    Text(d.nama, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(d.slug, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(12.dp))

                    // Foto aset (paling atas, sesuai web check.php)
                    d.fotoUrl?.let { url ->
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            AsyncImage(
                                model = url,
                                contentDescription = "Foto aset",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .width(150.dp)
                                    .height(190.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    LocationSection(
                        locationText = vm.locationText,
                        onCapture = {
                            if (LocationHelper.hasPermission(context)) {
                                vm.setLocation(LocationHelper.lastKnown(context))
                            } else {
                                locPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        },
                    )

                    Spacer(Modifier.height(4.dp))
                    d.template.orEmpty().forEach { item ->
                        ChecklistItemCard(
                            item = item,
                            status = vm.values[item.kodeItem] ?: "",
                            note = vm.notes[item.kodeItem] ?: "",
                            onStatus = { vm.setStatus(item.kodeItem, it) },
                            onNote = { vm.setNote(item.kodeItem, it) },
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                    PhotoSection(
                        photoPath = vm.photoPath,
                        onPhoto = { vm.setPhoto(it) },
                    )

                    vm.error?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    vm.saved?.let { s ->
                        Spacer(Modifier.height(12.dp))
                        SaveResultCard(s)
                    }

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun LocationSection(locationText: String, onCapture: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.MyLocation, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.padding(2.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Lokasi Pengecekan", style = MaterialTheme.typography.labelMedium)
                Text(locationText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            }
            OutlinedButton(onClick = onCapture) {
                Text(if (locationText == "GPS belum terdeteksi" || locationText == "-") "Ambil GPS" else "Muat Ulang")
            }
        }
    }
}

@Composable
private fun SaveResultCard(r: com.arffsnia.simap.data.repo.CheckRepository.SaveResult) {
    val ok = r.synced
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (ok) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.tertiaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (ok) Icons.Rounded.CheckCircle else Icons.Rounded.Schedule,
                contentDescription = null,
                tint = if (ok) Good else Warn,
            )
            Spacer(Modifier.padding(4.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (ok) "Tersimpan & terkirim" else "Tersimpan offline",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(r.message, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun ChecklistItemCard(
    item: ChecklistItem,
    status: String,
    note: String,
    onStatus: (String) -> Unit,
    onNote: (String) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "${item.label}${if (item.wajib == 1) " *" else ""}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                STATUS_OPTIONS.forEach { opt ->
                    FilterChip(
                        selected = status == opt,
                        onClick = { onStatus(opt) },
                        label = { Text(opt) },
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = note,
                onValueChange = onNote,
                label = { Text("Catatan (opsional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
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

    Column {
        Text("Foto Dokumentasi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        if (!photoPath.isNullOrBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = File(photoPath),
                    contentDescription = "Foto",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(120.dp),
                )
                Spacer(Modifier.padding(8.dp))
                IconButton(onClick = { onPhoto(null); File(photoPath).delete() }) {
                    Icon(Icons.Rounded.Close, "Hapus foto")
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        Button(onClick = { takePhoto() }) {
            Icon(Icons.Rounded.CameraAlt, null)
            Spacer(Modifier.padding(4.dp))
            Text(if (photoPath.isNullOrBlank()) "Ambil Foto" else "Foto Ulang")
        }
    }
}