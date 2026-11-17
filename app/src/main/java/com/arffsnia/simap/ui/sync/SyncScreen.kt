package com.arffsnia.simap.ui.sync

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.arffsnia.simap.SimapApp
import com.arffsnia.simap.ui.formatTanggal
import com.arffsnia.simap.ui.theme.Good
import com.arffsnia.simap.ui.theme.Warn
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncScreen(onBack: () -> Unit) {
    val app = LocalContext.current.applicationContext as SimapApp
    val scope = rememberCoroutineScope()
    var pending by remember { mutableStateOf(app.db.pendingChecks(onlyUnsynced = true)) }
    var syncing by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    fun refresh() { pending = app.db.pendingChecks(onlyUnsynced = true) }

    fun doSync() {
        if (syncing) return
        scope.launch {
            syncing = true
            message = null
            val sebelum = pending.size
            app.checkRepo.syncAll()
            refresh()
            val terkirim = sebelum - pending.size
            message = when {
                terkirim > 0 && pending.isEmpty() -> "Semua terkirim"
                terkirim > 0 -> "$terkirim terkirim, sisa ${pending.size} menunggu"
                pending.isNotEmpty() -> "Belum ada yang terkirim. Cek koneksi internet."
                else -> null
            }
            syncing = false
        }
    }

    // Cek & kirim otomatis saat layar dibuka
    LaunchedEffect(Unit) { if (pending.isNotEmpty()) doSync() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Sinkronisasi") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Kembali") }
                },
            )
        },
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (pending.isEmpty()) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.tertiaryContainer,
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (pending.isEmpty()) Good.copy(alpha = 0.15f) else Warn.copy(alpha = 0.15f),
                                CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = if (pending.isEmpty()) Icons.Rounded.CheckCircle else Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = if (pending.isEmpty()) Good else Warn,
                        )
                    }
                    Spacer(Modifier.padding(4.dp))
                    Column {
                        Text(
                            if (pending.isEmpty()) "Semua terkirim" else "${pending.size} belum terkirim",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            if (pending.isEmpty()) "Tidak ada data yang menunggu." else "Dikirim otomatis saat ada koneksi.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            if (pending.isNotEmpty()) {
                Button(
                    onClick = { doSync() },
                    enabled = !syncing,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (syncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Icon(Icons.Rounded.Autorenew, null)
                        Spacer(Modifier.padding(4.dp))
                        Text("Sinkron Sekarang")
                    }
                }
            }

            message?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
            }

            Spacer(Modifier.height(12.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(pending, key = { it.id }) { p ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Rounded.Schedule,
                                null,
                                tint = Warn,
                                modifier = Modifier.size(24.dp),
                            )
                            Spacer(Modifier.padding(4.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(p.nama ?: p.slug ?: "Aset ${p.assetId}", fontWeight = FontWeight.SemiBold)
                                Text(
                                    "${p.category ?: ""} · Dicek ${formatTanggal(p.checkedAt)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                )
                            }
                            Text("antre", style = MaterialTheme.typography.labelSmall, color = Warn)
                        }
                    }
                }
            }
        }
    }
}