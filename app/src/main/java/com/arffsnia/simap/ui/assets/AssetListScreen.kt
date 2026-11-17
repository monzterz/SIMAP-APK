package com.arffsnia.simap.ui.assets

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arffsnia.simap.data.dto.Asset
import com.arffsnia.simap.data.dto.Checks
import com.arffsnia.simap.ui.formatTanggal
import com.arffsnia.simap.ui.theme.Bad
import com.arffsnia.simap.ui.theme.Good
import com.arffsnia.simap.ui.theme.Teal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetListScreen(
    onBack: (() -> Unit)?,
    onDetail: (String) -> Unit,
    vm: AssetsViewModel = viewModel(),
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Daftar Aset") },
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
            OutlinedTextField(
                value = vm.search,
                onValueChange = vm::onSearchChange,
                label = { Text("Cari nama / kode / lokasi") },
                leadingIcon = { Icon(Icons.Rounded.Search, null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            CategoryDropdown(vm)
            Spacer(Modifier.height(8.dp))

            if (vm.loading && vm.items.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator()
                }
            } else if (vm.error != null && vm.items.isEmpty()) {
                Text(
                    vm.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp),
                )
            } else {
                Text(
                    "${vm.total} aset",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 4.dp),
                )
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(vm.items, key = { it.id }) { a -> AssetCard(a, onDetail) }
                }
            }
        }
    }
}

@Composable
private fun CategoryDropdown(vm: AssetsViewModel) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(
                vm.categories.firstOrNull { it.id == vm.selectedCategory }?.nama ?: "Semua kategori",
                modifier = Modifier.weight(1f),
            )
            Icon(Icons.Rounded.ExpandMore, "Buka")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Semua kategori") },
                onClick = { vm.onCategorySelect(null); expanded = false },
            )
            vm.categories.forEach { c ->
                DropdownMenuItem(
                    text = { Text(c.nama) },
                    onClick = { vm.onCategorySelect(c.id); expanded = false },
                )
            }
        }
    }
}

@Composable
private fun AssetCard(a: Asset, onDetail: (String) -> Unit) {
    val hasProblem = assetHasProblem(a.checks)
    Card(onClick = { onDetail(a.slug) }, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(a.nama, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    a.slug,
                    style = MaterialTheme.typography.labelMedium,
                    color = Teal,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                listOfNotNull(a.category, a.room).joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
            Text(
                "Lokasi: ${a.lokasi ?: "-"}  ·  ${a.berat ?: ""}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (hasProblem) "Ada masalah" else "Baik",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (hasProblem) Bad else Good,
                )
                Text(
                    "Update ${formatTanggal(a.updatedAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
            }
        }
    }
}

/** Nilai item dianggap baik kalau kosong/'-' atau diawali Baik/OK/Normal/dst. */
fun assetHasProblem(checks: Checks?): Boolean {
    if (checks == null) return false
    val vals = listOf(checks.pressure, checks.pin, checks.hose, checks.klam, checks.handle, checks.flak)
    return vals.any { v ->
        val s = (v ?: "").trim().lowercase()
        s.isNotEmpty() && s != "-" && !(s.startsWith("baik") || s == "ok" || s.startsWith("normal") || s.startsWith("aman") || s.startsWith("bagus") || s.startsWith("good"))
    }
}