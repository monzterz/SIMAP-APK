package com.arffsnia.simap.ui.assets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.arffsnia.simap.SimapApp
import com.arffsnia.simap.data.dto.AssetDetail
import com.arffsnia.simap.data.dto.ChecklistItem
import com.arffsnia.simap.data.repo.AppResult
import com.arffsnia.simap.ui.formatTanggal
import com.arffsnia.simap.ui.theme.Bad
import com.arffsnia.simap.ui.theme.Good
import com.arffsnia.simap.ui.theme.Slate400
import kotlinx.coroutines.launch

class DetailViewModel : ViewModel() {
    private val repo = SimapApp.instance.assetRepo
    var detail by mutableStateOf<AssetDetail?>(null)
    var loading by mutableStateOf(true)
    var error by mutableStateOf<String?>(null)

    fun load(slug: String) {
        viewModelScope.launch {
            loading = true
            when (val r = repo.assetBySlug(slug)) {
                is AppResult.Success -> { detail = r.data; error = null }
                is AppResult.Error -> error = com.arffsnia.simap.ui.mapError(r.message)
            }
            loading = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetDetailScreen(
    slug: String,
    onBack: () -> Unit,
    onInspect: (AssetDetail) -> Unit,
    vm: DetailViewModel = viewModel(),
) {
    LaunchedEffect(slug) { vm.load(slug) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Detail Aset") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "Kembali") }
                },
            )
        },
        bottomBar = {
            if (vm.detail != null) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Button(
                        onClick = { vm.detail?.let(onInspect) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Rounded.PlayArrow, null)
                        Spacer(Modifier.padding(4.dp))
                        Text("Cek Sekarang")
                    }
                }
            }
        },
    ) { pad ->
        when {
            vm.loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(pad),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) { CircularProgressIndicator() }
            }
            vm.error != null -> {
                Text(
                    vm.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .padding(pad)
                        .padding(16.dp),
                )
            }
            else -> {
                val d = vm.detail!!
                DetailBody(d, Modifier.padding(pad))
            }
        }
    }
}

@Composable
private fun DetailBody(d: AssetDetail, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        d.fotoUrl?.let {
            AsyncImage(
                model = it,
                contentDescription = "Foto aset",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
            )
            Spacer(Modifier.height(12.dp))
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(d.nama, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(d.slug, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                InfoRow("Jenis", d.category ?: "-")
                InfoRow("Lokasi", listOfNotNull(d.room, d.lokasi).joinToString(" · "))
                InfoRow("Berat", d.berat ?: "-")
                InfoRow("Periode", d.periode ?: "-")
                InfoRow("Tahun", d.tahun?.toString() ?: "-")
                InfoRow("Status", d.status ?: "-")
            }
        }

        Spacer(Modifier.height(12.dp))
        Text("Kondisi Terakhir", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text(
            "Dicek ${formatTanggal(d.latestCheck?.checkedAt)}",
            style = MaterialTheme.typography.bodySmall,
            color = Slate400,
        )
        Spacer(Modifier.height(8.dp))

        val items = d.template.orEmpty()
        if (items.isEmpty()) {
            Text("Belum ada template checklist.", color = Slate400)
        } else {
            items.forEach { item ->
                ChecklistRow(item, d.latestCheck?.result)
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ChecklistRow(item: ChecklistItem, result: Map<String, String?>?) {
    val value = result?.get(item.kodeItem)?.trim().orEmpty()
    val good = value.isEmpty() || value == "-" || isGoodValue(value)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (good) Icons.Rounded.CheckCircle else Icons.Rounded.Error,
            contentDescription = null,
            tint = if (good) Good else Bad,
        )
        Spacer(Modifier.padding(4.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.label, style = MaterialTheme.typography.bodyMedium)
            if (value.isNotEmpty()) {
                Text(value, style = MaterialTheme.typography.bodySmall, color = if (good) Good else Bad)
            }
        }
    }
}

fun isGoodValue(v: String): Boolean {
    val s = v.lowercase()
    return s.startsWith("baik") || s == "ok" || s.startsWith("normal") ||
        s.startsWith("aman") || s.startsWith("bagus") || s.startsWith("good")
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 3.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth(0.4f),
        )
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}