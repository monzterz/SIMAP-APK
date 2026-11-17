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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.arffsnia.simap.data.dto.ReminderAvailableUser
import com.arffsnia.simap.data.dto.ReminderLogItem
import com.arffsnia.simap.data.dto.ReminderRecipient
import com.arffsnia.simap.data.repo.AppResult
import com.arffsnia.simap.ui.mapError
import com.arffsnia.simap.ui.theme.Slate400
import com.arffsnia.simap.ui.theme.Slate50
import com.arffsnia.simap.ui.theme.Teal
import kotlinx.coroutines.launch

@Composable
fun PengingatScreen() {
    val app = LocalContext.current.applicationContext as SimapApp
    val scope = rememberCoroutineScope()

    var recipients by remember { mutableStateOf<List<ReminderRecipient>>(emptyList()) }
    var available by remember { mutableStateOf<List<ReminderAvailableUser>>(emptyList()) }
    var log by remember { mutableStateOf<List<ReminderLogItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableStateOf(0) }
    var busy by remember { mutableStateOf(false) }
    var toast by remember { mutableStateOf<String?>(null) }
    var addMenu by remember { mutableStateOf(false) }

    fun reload() {
        scope.launch {
            loading = true
            when (val r = app.adminRepo.reminderRecipients()) {
                is AppResult.Success -> { recipients = r.data.recipients; available = r.data.available; error = null }
                is AppResult.Error -> error = mapError(r.message)
            }
            when (val r = app.adminRepo.reminderLog()) {
                is AppResult.Success -> log = r.data
                is AppResult.Error -> if (error == null) error = mapError(r.message)
            }
            loading = false
        }
    }

    LaunchedEffect(refreshKey) { reload() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate50)
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
    ) {
        Text(
            "Pengingat",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 18.dp, bottom = 10.dp),
        )

        toast?.let { Text(it, color = Teal, modifier = Modifier.padding(bottom = 6.dp)) }

        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            error != null && recipients.isEmpty() -> {
                Text(error!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { refreshKey++ }) { Text("Coba lagi") }
            }
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item { SectionLabel("Kelola penerima") }
                if (recipients.isEmpty()) {
                    item { Text("Belum ada penerima.", style = MaterialTheme.typography.bodySmall, color = Slate400) }
                }
                items(recipients, key = { it.userId }) { r ->
                    RecipientRow(r, onRemove = {
                        busy = true
                        scope.launch {
                            when (val res = app.adminRepo.reminderRemove(r.userId)) {
                                is AppResult.Success -> { toast = "Dihapus"; refreshKey++ }
                                is AppResult.Error -> toast = mapError(res.message)
                            }
                            busy = false
                        }
                    })
                }

                item {
                    Box(Modifier.fillMaxWidth().padding(top = 4.dp)) {
                        OutlinedButton(onClick = { addMenu = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("Pilih user untuk ditambahkan")
                        }
                        DropdownMenu(expanded = addMenu, onDismissRequest = { addMenu = false }) {
                            if (available.isEmpty()) {
                                DropdownMenuItem(text = { Text("Tidak ada user tersedia") }, onClick = { addMenu = false })
                            }
                            available.forEach { u ->
                                DropdownMenuItem(
                                    text = { Text("${u.fullName ?: "-"} · ${u.level ?: ""}") },
                                    onClick = {
                                        addMenu = false
                                        busy = true
                                        scope.launch {
                                            when (val res = app.adminRepo.reminderAdd(u.id)) {
                                                is AppResult.Success -> { toast = "Ditambahkan"; refreshKey++ }
                                                is AppResult.Error -> toast = mapError(res.message)
                                            }
                                            busy = false
                                        }
                                    },
                                )
                            }
                        }
                    }
                }

                item { SectionLabel("Pengingat") }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = {
                            busy = true
                            scope.launch {
                                when (val r = app.adminRepo.reminderRecompute()) {
                                    is AppResult.Success -> { toast = "Dihitung ulang"; refreshKey++ }
                                    is AppResult.Error -> toast = mapError(r.message)
                                }
                                busy = false
                            }
                        }, enabled = !busy, modifier = Modifier.weight(1f)) { Text("Hitung ulang") }
                        Button(onClick = {
                            busy = true
                            scope.launch {
                                when (val r = app.adminRepo.reminderSendAll()) {
                                    is AppResult.Success -> {
                                        toast = if (r.data.sent == null && r.data.message != null) r.data.message
                                        else "Terkirim ${r.data.sent ?: 0}, gagal ${r.data.failed ?: 0}"
                                        refreshKey++
                                    }
                                    is AppResult.Error -> toast = mapError(r.message)
                                }
                                busy = false
                            }
                        }, enabled = !busy, modifier = Modifier.weight(1f)) { Text("Kirim ke semua") }
                    }
                }

                if (log.isEmpty()) {
                    item { Text("Belum ada log pengingat.", style = MaterialTheme.typography.bodySmall, color = Slate400) }
                }
                items(log, key = { it.id }) { l -> LogRow(l, busy = busy, onSend = {
                    busy = true
                    scope.launch {
                        when (val r = app.adminRepo.reminderSendOne(l.id)) {
                            is AppResult.Success -> {
                                toast = r.data.message ?: (if (r.data.ok == true) "Terkirim" else "Gagal")
                                refreshKey++
                            }
                            is AppResult.Error -> toast = mapError(r.message)
                        }
                        busy = false
                    }
                }) }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun RecipientRow(r: ReminderRecipient, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(r.fullName ?: "-", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
            Text("${r.level ?: ""} · ${r.email ?: "-"}", style = MaterialTheme.typography.labelSmall, color = Slate400)
        }
        TextButton(onClick = onRemove) { Text("Hapus") }
    }
}

@Composable
private fun LogRow(l: ReminderLogItem, busy: Boolean, onSend: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(l.penerima ?: "-", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "${l.penerimaEmail ?: "-"} · ${l.overdueCount ?: 0} overdue",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate400,
                    )
                }
                StatusChip(
                    when (l.status?.lowercase()) {
                        "sent" -> "Terkirim"
                        "failed" -> "Gagal"
                        else -> "Pending"
                    },
                    when (l.status?.lowercase()) {
                        "sent" -> ChipTone.GOOD
                        "failed" -> ChipTone.BAD
                        else -> ChipTone.WARN
                    },
                )
            }
            if (l.status?.lowercase() == "pending") {
                Spacer(Modifier.height(6.dp))
                Button(onClick = onSend, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                    Text("Kirim")
                }
            }
        }
    }
}