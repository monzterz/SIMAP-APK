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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.arffsnia.simap.SimapApp
import com.arffsnia.simap.data.dto.Penugasan
import com.arffsnia.simap.data.dto.PenugasanDetail
import com.arffsnia.simap.data.repo.AppResult
import com.arffsnia.simap.ui.Role
import com.arffsnia.simap.ui.mapError
import com.arffsnia.simap.ui.theme.Good
import com.arffsnia.simap.ui.theme.Sky
import com.arffsnia.simap.ui.theme.Slate400
import com.arffsnia.simap.ui.theme.Slate50
import com.arffsnia.simap.ui.theme.Slate600
import com.arffsnia.simap.ui.theme.Teal
import com.arffsnia.simap.ui.theme.Warn

private fun statusLabel(s: String?): String = when (s?.trim()?.lowercase()) {
    "aktif" -> "Berjalan"
    "selesai" -> "Selesai"
    else -> s ?: "-"
}

private fun statusTone(s: String?): ChipTone = when (s?.trim()?.lowercase()) {
    "selesai" -> ChipTone.SLATE
    "aktif" -> ChipTone.GOOD
    else -> ChipTone.SLATE
}

private fun shortDate(s: String?): String? {
    if (s.isNullOrBlank() || s.length < 10) return s
    return "${s.substring(8, 10)}/${s.substring(5, 7)}"
}

private fun rangeDate(mulai: String?, selesai: String?): String {
    val a = shortDate(mulai) ?: "-"
    val b = shortDate(selesai) ?: "-"
    return "$a – $b"
}

@Composable
fun PenugasanScreen(onOpen: (Int) -> Unit) {
    val app = LocalContext.current.applicationContext as SimapApp
    val role = remember { Role.fromLevel(app.authRepo.currentUser()?.level) }

    var items by remember { mutableStateOf<List<Penugasan>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableStateOf(0) }

    LaunchedEffect(refreshKey) {
        loading = true
        when (val r = app.adminRepo.penugasan()) {
            is AppResult.Success -> { items = r.data; error = null }
            is AppResult.Error -> error = mapError(r.message)
        }
        loading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate50)
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
    ) {
        Text(
            "Penugasan",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 18.dp, bottom = 10.dp),
        )

        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            error != null && items.isEmpty() -> {
                Text(error!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { refreshKey++ }) { Text("Coba lagi") }
            }
            else -> {
                val selesai = items.count { it.status?.lowercase() == "selesai" }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    KpiStat("Total", items.size.toString(), "Surat Tugas", Teal, Modifier.weight(1f))
                    KpiStat("Berjalan", (items.size - selesai).toString(), "Shift Aktif", Good, Modifier.weight(1f))
                    KpiStat("Selesai", selesai.toString(), "Terverifikasi", Slate600, Modifier.weight(1f))
                }

                Spacer(Modifier.height(10.dp))
                ScopeBanner(role)

                if (items.isEmpty()) {
                    Spacer(Modifier.height(20.dp))
                    Text("Belum ada penugasan.", color = Slate400)
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(top = 4.dp),
                    ) {
                        items(items, key = { it.id }) { p -> PenugasanCard(p, onOpen) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScopeBanner(role: Role) {
    val (text, color, edge) = when {
        role.isSuper -> Triple("Menampilkan semua penugasan (semua regu).", Color(0xFF075985), Color(0xFFBAE6FD))
        role.isAdminRegu -> Triple("Anda hanya melihat penugasan regu Anda.", Color(0xFF075985), Color(0xFFBAE6FD))
        else -> return
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE0F2FE), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun KpiStat(label: String, value: String, caption: String?, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Slate400)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = color)
            caption?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = Slate400) }
        }
    }
}

@Composable
private fun PenugasanCard(p: Penugasan, onOpen: (Int) -> Unit) {
    Card(
        onClick = { onOpen(p.id) },
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
                    Text(p.nama, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "${p.regu ?: "-"} · ${rangeDate(p.tanggalMulai, p.tanggalSelesai)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate400,
                    )
                }
                StatusChip(statusLabel(p.status), statusTone(p.status))
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProgressBar(p.persen, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                Text("${p.checked}/${p.total} · ${p.persen}%", style = MaterialTheme.typography.labelSmall, color = Slate400)
            }
        }
    }
}

// ===== Detail =====

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PenugasanDetailScreen(id: Int, onBack: () -> Unit) {
    val app = LocalContext.current.applicationContext as SimapApp
    var detail by remember { mutableStateOf<PenugasanDetail?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(id) {
        loading = true
        when (val r = app.adminRepo.penugasanDetail(id)) {
            is AppResult.Success -> { detail = r.data; error = null }
            is AppResult.Error -> error = mapError(r.message)
        }
        loading = false
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Detail Penugasan") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Kembali") } },
            )
        },
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            when {
                loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                error != null -> Text(error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 16.dp))
                detail != null -> {
                    val d = detail!!
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
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
                                    Text(d.nama, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        "${d.regu ?: "-"} · ${rangeDate(d.tanggalMulai, d.tanggalSelesai)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Slate400,
                                    )
                                }
                                StatusChip(statusLabel(d.status), statusTone(d.status))
                            }
                            Spacer(Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                ProgressBar(d.persen, modifier = Modifier.weight(1f))
                                Spacer(Modifier.width(8.dp))
                                Text("${d.checked}/${d.total} · ${d.persen}%", style = MaterialTheme.typography.labelSmall, color = Slate400)
                            }
                        }
                    }

                    SectionLabel("Progres per zona / lokasi")
                    if (d.breakdown.isEmpty()) {
                        Text("Belum ada progres detail.", style = MaterialTheme.typography.bodySmall, color = Slate400)
                    }
                    d.breakdown.forEach { b ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(b.nama, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                                Text("${b.done} dari ${b.n} aset", style = MaterialTheme.typography.labelSmall, color = Slate400)
                            }
                            Box(Modifier.width(120.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    ProgressBar(b.persen, modifier = Modifier.weight(1f))
                                    Spacer(Modifier.width(6.dp))
                                    Text("${b.persen}%", style = MaterialTheme.typography.labelSmall, color = Slate400)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}