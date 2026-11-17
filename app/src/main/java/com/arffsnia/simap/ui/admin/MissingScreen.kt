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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Notifications
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
import com.arffsnia.simap.data.dto.MissingAsset
import com.arffsnia.simap.data.repo.AppResult
import com.arffsnia.simap.ui.mapError
import com.arffsnia.simap.ui.theme.Slate400
import com.arffsnia.simap.ui.theme.Slate50
import com.arffsnia.simap.ui.theme.Warn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissingScreen(onBack: (() -> Unit)?, onOpen: (String) -> Unit) {
    val app = LocalContext.current.applicationContext as SimapApp
    var items by remember { mutableStateOf<List<MissingAsset>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableStateOf(0) }

    LaunchedEffect(refreshKey) {
        loading = true
        when (val r = app.adminRepo.missing()) {
            is AppResult.Success -> { items = r.data; error = null }
            is AppResult.Error -> error = mapError(r.message)
        }
        loading = false
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Aset Belum Dicek") },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Kembali") }
                    }
                },
            )
        },
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(horizontal = 16.dp),
        ) {
            when {
                loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                error != null && items.isEmpty() -> {
                    Spacer(Modifier.height(20.dp))
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(onClick = { refreshKey++ }) { Text("Coba lagi") }
                }
                items.isEmpty() -> {
                    Spacer(Modifier.height(20.dp))
                    Text("Semua aset sudah dicek periode ini.", color = Slate400)
                }
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(items, key = { it.id }) { a -> MissingCard(a, onOpen) }
                }
            }
        }
    }
}

@Composable
private fun MissingCard(a: MissingAsset, onOpen: (String) -> Unit) {
    Card(
        onClick = { onOpen(a.slug) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .background(Warn.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                    .padding(8.dp),
            ) {
                Icon(Icons.Rounded.Notifications, null, tint = Warn)
            }
            Spacer(Modifier.padding(5.dp))
            Column(Modifier.weight(1f)) {
                Text(a.nama, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                Text(
                    listOfNotNull(a.slug, a.lokasi).joinToString(" · "),
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate400,
                )
            }
            StatusChip(
                text = if (a.urgency == "danger") "Lewat jadwal" else "Perlu dicek",
                tone = if (a.urgency == "danger") ChipTone.BAD else ChipTone.WARN,
            )
        }
    }
}