package com.arffsnia.simap.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.arffsnia.simap.SimapApp
import com.arffsnia.simap.data.dto.ValidasiItem
import com.arffsnia.simap.data.repo.AppResult
import com.arffsnia.simap.ui.formatTanggal
import com.arffsnia.simap.ui.mapError
import com.arffsnia.simap.ui.theme.Bad
import com.arffsnia.simap.ui.theme.Slate400
import com.arffsnia.simap.ui.theme.Slate50
import com.arffsnia.simap.ui.theme.Teal
import kotlinx.coroutines.launch

private fun mapTipe(t: String?): String = when (t?.lowercase()) {
    "assets" -> "Data Aset"
    "maintenances" -> "Maintenance"
    "damagedassets" -> "Aset Rusak"
    else -> t ?: "Data"
}

private fun mapAksi(a: String?): String = when (a?.lowercase()) {
    "create" -> "Tambah"
    "update" -> "Ubah"
    "delete" -> "Hapus"
    else -> a ?: ""
}

@Composable
fun ValidasiScreen() {
    val app = LocalContext.current.applicationContext as SimapApp
    val scope = rememberCoroutineScope()

    var pending by remember { mutableStateOf<List<ValidasiItem>>(emptyList()) }
    var riwayat by remember { mutableStateOf<List<ValidasiItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableStateOf(0) }
    var busyId by remember { mutableStateOf<Int?>(null) }
    var toast by remember { mutableStateOf<String?>(null) }
    var rejectTarget by remember { mutableStateOf<ValidasiItem?>(null) }
    var rejectNote by remember { mutableStateOf("") }

    fun reload() {
        scope.launch {
            loading = true
            when (val r = app.adminRepo.validasi()) {
                is AppResult.Success -> { pending = r.data.pending; riwayat = r.data.riwayat; error = null }
                is AppResult.Error -> error = mapError(r.message)
            }
            loading = false
        }
    }

    LaunchedEffect(refreshKey) { reload() }

    rejectTarget?.let { t ->
        AlertDialog(
            onDismissRequest = { rejectTarget = null; rejectNote = "" },
            title = { Text("Tolak pengajuan") },
            text = {
                Column {
                    Text("${mapTipe(t.tipe)} · ${mapAksi(t.aksi)} — ${t.nama ?: "-"}", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = rejectNote,
                        onValueChange = { rejectNote = it },
                        label = { Text("Catatan (opsional)") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val note = rejectNote
                    rejectTarget = null
                    rejectNote = ""
                    busyId = t.id
                    scope.launch {
                        when (val r = app.adminRepo.reject(t.id, note.ifBlank { null })) {
                            is AppResult.Success -> { toast = "Ditolak"; refreshKey++ }
                            is AppResult.Error -> toast = mapError(r.message)
                        }
                        busyId = null
                    }
                }) { Text("Tolak", color = Bad) }
            },
            dismissButton = {
                TextButton(onClick = { rejectTarget = null; rejectNote = "" }) { Text("Batal") }
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate50)
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
    ) {
        Text(
            "Validasi Aset",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 18.dp, bottom = 10.dp),
        )

        toast?.let {
            Text(it, color = Teal, modifier = Modifier.padding(bottom = 6.dp))
        }

        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            error != null && pending.isEmpty() -> {
                Text(error!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { refreshKey++ }) { Text("Coba lagi") }
            }
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item { SectionLabel("Pengajuan menunggu persetujuan") }
                if (pending.isEmpty()) {
                    item { Text("Tidak ada pengajuan menunggu.", style = MaterialTheme.typography.bodySmall, color = Slate400) }
                }
                items(pending, key = { it.id }) { p ->
                    PendingCard(p, busy = busyId == p.id, onApprove = {
                        busyId = p.id
                        scope.launch {
                            when (val r = app.adminRepo.approve(p.id)) {
                                is AppResult.Success -> { toast = "Disetujui"; refreshKey++ }
                                is AppResult.Error -> toast = mapError(r.message)
                            }
                            busyId = null
                        }
                    }, onReject = { rejectTarget = p })
                }

                item { SectionLabel("Riwayat validasi") }
                if (riwayat.isEmpty()) {
                    item { Text("Belum ada riwayat.", style = MaterialTheme.typography.bodySmall, color = Slate400) }
                }
                items(riwayat, key = { it.id }) { h -> RiwayatRow(h) }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun PendingCard(p: ValidasiItem, busy: Boolean, onApprove: () -> Unit, onReject: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        "${mapTipe(p.tipe)} · ${mapAksi(p.aksi)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Teal,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(p.nama ?: "-", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "Diajukan ${p.pemohon ?: "-"}${p.regu?.let { " ($it)" } ?: ""}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate400,
                    )
                }
                StatusChip("Menunggu", ChipTone.WARN)
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onApprove,
                    enabled = !busy,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Teal),
                ) { Text("Setujui") }
                OutlinedButton(
                    onClick = onReject,
                    enabled = !busy,
                    modifier = Modifier.weight(1f),
                ) { Text("Tolak", color = Bad) }
            }
        }
    }
}

@Composable
private fun RiwayatRow(h: ValidasiItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                "${mapTipe(h.tipe)} · ${mapAksi(h.aksi)} — ${h.nama ?: "-"}",
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodySmall,
            )
            val meta = buildList {
                add(h.pemohon ?: "-")
                if (h.catatan != null) add("catatan: ${h.catatan}")
                add(formatTanggal(h.reviewedAt ?: h.createdAt))
            }.joinToString(" · ")
            Text(meta, style = MaterialTheme.typography.labelSmall, color = Slate400)
        }
        StatusChip(
            when (h.status?.lowercase()) {
                "approved" -> "Disetujui"
                "rejected" -> "Ditolak"
                else -> h.status ?: "-"
            },
            when (h.status?.lowercase()) {
                "approved" -> ChipTone.GOOD
                "rejected" -> ChipTone.BAD
                else -> ChipTone.SLATE
            },
        )
    }
}