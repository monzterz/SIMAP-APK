package com.arffsnia.simap.ui.admin

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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.arffsnia.simap.SimapApp
import com.arffsnia.simap.data.dto.Asset
import com.arffsnia.simap.data.dto.AssetStats
import com.arffsnia.simap.data.dto.AssetsResponse
import com.arffsnia.simap.data.dto.SimpleItem
import com.arffsnia.simap.data.repo.AppResult
import com.arffsnia.simap.ui.formatTanggal
import com.arffsnia.simap.ui.mapError
import com.arffsnia.simap.ui.theme.PlusJakarta
import com.arffsnia.simap.ui.theme.PlusJakartaTypography
import com.arffsnia.simap.ui.theme.SimapTeal
import com.arffsnia.simap.ui.theme.Slate400
import com.arffsnia.simap.ui.theme.Slate900

private val DaBg = Color(0xFFF1F5F9)
private val DaBorder = Color(0xFFE2E8F0)
private val DaHair = Color(0xFFF1F5F9)
private val DaSlate700 = Color(0xFF334155)
private val DaSlate500 = Color(0xFF64748B)

/** Data Aset (super admin): statistik, filter lokasi + pencarian, kartu aset, paginasi. */
@Composable
fun DataAsetScreen(
    onOpen: (String) -> Unit,
    onNotif: (() -> Unit)? = null,
    onProfile: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
) {
    val app = LocalContext.current.applicationContext as SimapApp
    val user = remember { app.authRepo.currentUser() }

    var data by remember { mutableStateOf<AssetsResponse?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var page by remember { mutableStateOf(1) }
    var limit by remember { mutableStateOf(10) }
    var search by remember { mutableStateOf("") }
    var roomId by remember { mutableStateOf(0) }
    var rooms by remember { mutableStateOf<List<SimpleItem>>(emptyList()) }
    var refreshKey by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        (app.adminRepo.rooms() as? AppResult.Success)?.let { rooms = it.data }
    }

    LaunchedEffect(page, limit, search, roomId, refreshKey) {
        loading = true
        when (val r = app.adminRepo.assetsPage(page, limit, search, null, roomId.takeIf { it > 0 })) {
            is AppResult.Success -> { data = r.data; error = null }
            is AppResult.Error -> error = mapError(r.message)
        }
        loading = false
    }

    val stats = data?.stats ?: AssetStats()

    MaterialTheme(colorScheme = MaterialTheme.colorScheme, typography = PlusJakartaTypography) {
        CompositionLocalProvider(
            LocalTextStyle provides TextStyle(
                fontFamily = PlusJakarta, fontSize = 12.sp,
                platformStyle = PlatformTextStyle(includeFontPadding = false),
            ),
        ) {
            Column(Modifier.fillMaxSize().background(Color(0xFFF8FAFC)).statusBarsPadding()) {
                TopBar(user = user, onNotif = onNotif, onProfile = onProfile)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp)
                        .padding(top = 12.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    BannerDataAset(onBack)
                    GridStats(stats)

                    KartuFilterAset(
                        rooms = rooms,
                        roomId = roomId,
                        onRoom = { roomId = it; page = 1 },
                        search = search,
                        onSearch = { search = it; page = 1 },
                        limit = limit,
                        onLimit = { limit = it; page = 1 },
                    )

                    when {
                        loading -> Box(
                            Modifier.fillMaxWidth().height(140.dp),
                            contentAlignment = Alignment.Center,
                        ) { CircularProgressIndicator(color = SimapTeal) }

                        error != null -> Text(error!!, fontSize = 11.sp, color = Color(0xFFBE123C))

                        data != null && data!!.items.isEmpty() -> Text(
                            "Tidak ada aset yang cocok dengan filter ini.",
                            fontSize = 11.sp, color = Slate400,
                        )

                        data != null -> {
                            data!!.items.forEach { a -> KartuAsetData(a) { onOpen(a.slug) } }
                            PagerAset(data!!) { page = it }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BannerDataAset(onBack: (() -> Unit)?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF0D9488), Color(0xFF06B6D4))))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Home", fontSize = 9.sp, color = Color.White.copy(alpha = 0.8f),
                    modifier = if (onBack != null) Modifier.clickable { onBack() } else Modifier)
                Text(" / ", fontSize = 9.sp, color = Color.White.copy(alpha = 0.6f))
                Text("Data Aset", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Text("Data Aset", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Text(
                "Inventaris Alat Pemadam Api & Penyelamatan",
                fontSize = 9.sp, color = Color.White.copy(alpha = 0.88f),
            )
        }
        Box(
            Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(Color.White.copy(alpha = 0.20f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.Inventory2, null, Modifier.size(19.dp), tint = Color.White)
        }
    }
}

@Composable
private fun GridStats(s: AssetStats) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatKotak("Aset Aktif", s.aktif, Color(0xFF0D9488), Color(0xFFECFDF5), Modifier.weight(1f))
            StatKotak("Maintenance", s.maintenance, Color(0xFFF43F5E), Color(0xFFFFF1F2), Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatKotak("Aset Rusak", s.rusak, Color(0xFFF59E0B), Color(0xFFFFFBEB), Modifier.weight(1f))
            StatKotak("Total Aset", s.total, Color(0xFF6366F1), Color(0xFFEEF2FF), Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatKotak(label: String, nilai: Int, warna: Color, latar: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, warna.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("$nilai", fontSize = 19.sp, fontWeight = FontWeight.Black, color = Slate900)
            Spacer(Modifier.weight(1f))
            Box(
                Modifier.size(22.dp).clip(RoundedCornerShape(6.dp)).background(latar),
                contentAlignment = Alignment.Center,
            ) { Text("•", fontSize = 11.sp, color = warna) }
        }
        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Medium, color = DaSlate500)
        Box(Modifier.fillMaxWidth().height(2.dp).clip(RoundedCornerShape(2.dp)).background(warna))
    }
}

@Composable
private fun KartuFilterAset(
    rooms: List<SimpleItem>,
    roomId: Int,
    onRoom: (Int) -> Unit,
    search: String,
    onSearch: (String) -> Unit,
    limit: Int,
    onLimit: (Int) -> Unit,
) {
    var roomOpen by remember { mutableStateOf(false) }
    var limitOpen by remember { mutableStateOf(false) }
    val roomNama = rooms.firstOrNull { it.id == roomId }?.nama ?: "Semua Lokasi"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, DaBorder, RoundedCornerShape(14.dp))
            .padding(11.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("FILTER LOKASI", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = DaSlate500)
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, DaBorder, RoundedCornerShape(8.dp))
                    .clickable { roomOpen = true }
                    .padding(horizontal = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(roomNama, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = DaSlate700, maxLines = 1)
                Spacer(Modifier.weight(1f))
                Text("▾", fontSize = 10.sp, color = Slate400)
            }
            DropdownMenu(expanded = roomOpen, onDismissRequest = { roomOpen = false }) {
                DropdownMenuItem(
                    text = { Text("Semua Lokasi", fontSize = 11.sp) },
                    onClick = { onRoom(0); roomOpen = false },
                )
                rooms.forEach { r ->
                    DropdownMenuItem(
                        text = { Text(r.nama, fontSize = 11.sp) },
                        onClick = { onRoom(r.id); roomOpen = false },
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, DaBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.Search, null, Modifier.size(12.dp), tint = Slate400)
                Spacer(Modifier.width(5.dp))
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (search.isEmpty()) {
                        Text("Cari nama, kode...", fontSize = 10.sp, color = Slate400)
                    }
                    BasicTextField(
                        value = search,
                        onValueChange = onSearch,
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 10.sp, color = Slate900),
                        cursorBrush = SolidColor(SimapTeal),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            Box {
                Row(
                    modifier = Modifier
                        .height(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(1.dp, DaBorder, RoundedCornerShape(8.dp))
                        .clickable { limitOpen = true }
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("$limit / hal", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = DaSlate700)
                }
                DropdownMenu(expanded = limitOpen, onDismissRequest = { limitOpen = false }) {
                    listOf(10, 25, 50).forEach { n ->
                        DropdownMenuItem(
                            text = { Text("$n / hal", fontSize = 11.sp) },
                            onClick = { onLimit(n); limitOpen = false },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KartuAsetData(a: Asset, onDetail: () -> Unit) {
    val warnaStatus = when (a.status) {
        "maintenance" -> Color(0xFFF43F5E)
        "rusak" -> Color(0xFFF59E0B)
        else -> Color(0xFF0D9488)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, DaBorder, RoundedCornerShape(14.dp))
            .padding(11.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center,
            ) {
                if (!a.fotoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = a.fotoUrl,
                        contentDescription = a.nama,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Icon(Icons.Rounded.Inventory2, null, Modifier.size(22.dp), tint = Slate400)
                }
            }
            Spacer(Modifier.width(9.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (a.status == "maintenance") "Maintenance" else if (a.status == "rusak") "Rusak" else "Aktif",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = warnaStatus,
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(warnaStatus.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                    Spacer(Modifier.weight(1f))
                    Text("Thn ${a.tahun ?: "-"}", fontSize = 9.sp, color = Slate400, fontFamily = FontFamily.Monospace)
                }
                Text(
                    a.nama,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900,
                    maxLines = 1,
                    modifier = Modifier.padding(top = 3.dp),
                )
                Text(a.slug, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0D9488), fontFamily = FontFamily.Monospace)
                Text(
                    listOfNotNull(a.room, a.lokasi).joinToString(" • "),
                    fontSize = 9.sp, color = DaSlate500, maxLines = 1,
                )
            }
        }

        HorizontalDivider(color = DaHair)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF8FAFC))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SelData("Merek", a.merek, Modifier.weight(1f))
                SelData("Jenis Bahan", a.category, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SelData("Berat Tabung", a.berat, Modifier.weight(1f))
                SelData("Zonasi", null, Modifier.weight(1f))
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Pengecekan Terakhir:", fontSize = 8.sp, color = Slate400)
                Text(
                    a.lastChecked?.let { formatTanggal(it) } ?: "Belum pernah",
                    fontSize = 9.sp, fontWeight = FontWeight.Medium, color = DaSlate700,
                )
            }
            Box(
                Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF0FDFA))
                    .clickable { onDetail() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Visibility, "Detail", Modifier.size(15.dp), tint = Color(0xFF0D9488))
            }
        }
    }
}

@Composable
private fun SelData(label: String, nilai: String?, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label.uppercase(), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Slate400)
        Text(
            nilai?.ifBlank { "—" } ?: "—",
            fontSize = 10.sp, fontWeight = FontWeight.Medium,
            color = if (nilai.isNullOrBlank()) Color(0xFF94A3B8) else DaSlate700,
            maxLines = 1,
        )
    }
}

@Composable
private fun PagerAset(d: AssetsResponse, onPage: (Int) -> Unit) {
    val dari = if (d.items.isEmpty()) 0 else (d.page - 1) * d.limit + 1
    val sampai = (dari + d.items.size - 1).coerceAtMost(d.total)
    val pages = ((d.total + d.limit - 1) / d.limit).coerceAtLeast(1)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, DaBorder, RoundedCornerShape(12.dp))
            .padding(9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Menampilkan $dari–$sampai dari ${d.total} aset", fontSize = 9.sp, color = DaSlate500)
        Spacer(Modifier.weight(1f))
        TombolHalamanAset("Prev", enabled = d.page > 1) { onPage(d.page - 1) }
        Text(
            "${d.page}/$pages",
            fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D9488),
            modifier = Modifier.padding(horizontal = 7.dp),
        )
        TombolHalamanAset("Next", enabled = d.page < pages) { onPage(d.page + 1) }
    }
}

@Composable
private fun TombolHalamanAset(label: String, enabled: Boolean, onClick: () -> Unit) {
    Text(
        label,
        fontSize = 9.sp,
        fontWeight = FontWeight.SemiBold,
        color = if (enabled) DaSlate700 else Color(0xFFCBD5E1),
        modifier = Modifier
            .clip(RoundedCornerShape(7.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, DaBorder, RoundedCornerShape(7.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 9.dp, vertical = 4.dp),
    )
}
