package com.arffsnia.simap.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arffsnia.simap.SimapApp
import com.arffsnia.simap.data.dto.AssetStats
import com.arffsnia.simap.data.dto.AssetsResponse
import com.arffsnia.simap.data.dto.SimpleItem
import com.arffsnia.simap.data.repo.AppResult
import com.arffsnia.simap.ui.mapError
import com.arffsnia.simap.ui.theme.PlusJakarta
import com.arffsnia.simap.ui.theme.PlusJakartaTypography
import com.arffsnia.simap.ui.theme.SimapTeal
import com.arffsnia.simap.ui.theme.Slate400
import com.arffsnia.simap.ui.theme.Slate900
import kotlinx.coroutines.launch

/** Laporan Data Aset: ringkasan, ekspor (copy/CSV/PDF), filter, kartu aset, paginasi. */
@Composable
fun LaporanAsetScreen(
    onOpen: (String) -> Unit,
    onBack: (() -> Unit)? = null,
    onNotif: (() -> Unit)? = null,
    onProfile: (() -> Unit)? = null,
) {
    val app = LocalContext.current.applicationContext as SimapApp
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val user = remember { app.authRepo.currentUser() }

    var data by remember { mutableStateOf<AssetsResponse?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var page by remember { mutableStateOf(1) }
    var limit by remember { mutableStateOf(10) }
    var search by remember { mutableStateOf("") }
    var jenisNama by remember { mutableStateOf("") }
    var lokasiNama by remember { mutableStateOf("") }
    var jenisId by remember { mutableStateOf<Int?>(null) }
    var lokasiId by remember { mutableStateOf<Int?>(null) }
    var kategori by remember { mutableStateOf<List<SimpleItem>>(emptyList()) }
    var rooms by remember { mutableStateOf<List<SimpleItem>>(emptyList()) }
    var tampilKondisi by remember { mutableStateOf(true) }
    var sibuk by remember { mutableStateOf(false) }
    var pesan by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        (app.adminRepo.categories() as? AppResult.Success)?.let { kategori = it.data }
        (app.adminRepo.rooms() as? AppResult.Success)?.let { rooms = it.data }
    }

    LaunchedEffect(page, limit, search, jenisId, lokasiId, refreshKey) {
        loading = true
        when (val r = app.adminRepo.assetsPage(page, limit, search, null, lokasiId, jenisId)) {
            is AppResult.Success -> { data = r.data; error = null }
            is AppResult.Error -> error = mapError(r.message)
        }
        loading = false
    }

    val stats = data?.stats ?: AssetStats()
    val items = data?.items.orEmpty()

    MaterialTheme(colorScheme = MaterialTheme.colorScheme, typography = PlusJakartaTypography) {
        CompositionLocalProvider(
            LocalTextStyle provides TextStyle(
                fontFamily = PlusJakarta, fontSize = 12.sp,
                platformStyle = PlatformTextStyle(includeFontPadding = false),
            ),
        ) {
            Column(Modifier.fillMaxSize().background(Color(0xFFF4F7F9)).statusBarsPadding()) {
                TopBar(user = user, onNotif = onNotif, onProfile = onProfile)

                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp)
                        .padding(top = 10.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    BannerLaporanAset(onBack)
                    RingkasanAset(stats)

                    // ===== Ekspor =====
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(13.dp))
                            .background(Color.White)
                            .border(1.dp, LapBorder, RoundedCornerShape(13.dp))
                            .padding(9.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("EKSPOR LAPORAN", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = LapSlate700)
                            Spacer(Modifier.weight(1f))
                            Text(
                                if (sibuk) "Memproses..." else "Format siap cetak",
                                fontSize = 8.sp, color = Slate400,
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            TombolEkspor("Copy", Color(0xFFE2E8F0), Color(0xFF334155)) {
                                if (items.isEmpty()) pesan = "Belum ada data untuk disalin."
                                else { salinCsv(context, csvAset(items)); pesan = "${items.size} baris disalin ke clipboard." }
                            }
                            TombolEkspor("Excel", Color(0xFFDCFCE7), Color(0xFF166534)) {
                                if (items.isEmpty()) pesan = "Belum ada data untuk diekspor."
                                else bagikanCsv(context, csvAset(items), "laporan_aset")
                            }
                            TombolEkspor(
                                if (tampilKondisi) "Kolom ✓" else "Kolom",
                                Color(0xFFE0F2FE), Color(0xFF075985),
                            ) { tampilKondisi = !tampilKondisi }
                            TombolEkspor("PDF", Color(0xFFFFE4E6), Color(0xFF9F1239)) {
                                scope.launch {
                                    sibuk = true
                                    pesan = null
                                    when (val r = app.adminRepo.laporanAsetPdf(jenisNama, lokasiNama, search)) {
                                        is AppResult.Success -> {
                                            val err = bukaPdfAset(context, r.data, "laporan_aset")
                                            if (err != null) pesan = err
                                        }
                                        is AppResult.Error -> pesan = mapError(r.message)
                                    }
                                    sibuk = false
                                }
                            }
                        }
                        pesan?.let { Text(it, fontSize = 8.sp, color = LapSlate500) }
                    }

                    // ===== Filter =====
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(13.dp))
                            .background(Color.White)
                            .border(1.dp, LapBorder, RoundedCornerShape(13.dp))
                            .padding(9.dp),
                        verticalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF8FAFC))
                                .border(1.dp, LapBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Rounded.Search, null, Modifier.size(12.dp), tint = Slate400)
                            Spacer(Modifier.width(5.dp))
                            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                                if (search.isEmpty()) {
                                    Text("Cari kode, nama, lokasi, jenis...", fontSize = 9.sp, color = Slate400)
                                }
                                BasicTextField(
                                    value = search,
                                    onValueChange = { search = it; page = 1 },
                                    singleLine = true,
                                    textStyle = TextStyle(fontSize = 9.sp, color = Slate900),
                                    cursorBrush = SolidColor(SimapTeal),
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            DropdownLaporan(
                                label = "Jenis",
                                nilai = jenisNama.ifBlank { "Semua Jenis" },
                                opsi = listOf("" to "Semua Jenis") + kategori.map { it.nama to it.nama },
                                onPilih = { nama ->
                                    jenisNama = nama
                                    jenisId = kategori.firstOrNull { it.nama == nama }?.id
                                    page = 1
                                },
                                modifier = Modifier.weight(1f),
                            )
                            DropdownLaporan(
                                label = "Lokasi",
                                nilai = lokasiNama.ifBlank { "Semua Lokasi" },
                                opsi = listOf("" to "Semua Lokasi") + rooms.map { it.nama to it.nama },
                                onPilih = { nama ->
                                    lokasiNama = nama
                                    lokasiId = rooms.firstOrNull { it.nama == nama }?.id
                                    page = 1
                                },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Tampil:", fontSize = 8.sp, color = LapSlate500)
                            Spacer(Modifier.width(4.dp))
                            listOf(10, 25, 50).forEach { n ->
                                Text(
                                    "$n",
                                    fontSize = 8.sp,
                                    fontWeight = if (limit == n) FontWeight.Bold else FontWeight.Medium,
                                    color = if (limit == n) Color.White else LapSlate700,
                                    modifier = Modifier
                                        .padding(end = 3.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (limit == n) LapTeal else Color(0xFFF1F5F9))
                                        .clickable { limit = n; page = 1 }
                                        .padding(horizontal = 5.dp, vertical = 1.dp),
                                )
                            }
                            Spacer(Modifier.weight(1f))
                            Text(
                                "Menampilkan ${items.size} dari ${data?.total ?: 0}",
                                fontSize = 8.sp, color = LapSlate500,
                            )
                        }
                    }

                    when {
                        loading -> Box(
                            Modifier.fillMaxWidth().height(140.dp),
                            contentAlignment = Alignment.Center,
                        ) { CircularProgressIndicator(color = SimapTeal) }

                        error != null -> Text(error!!, fontSize = 10.sp, color = Color(0xFFBE123C))

                        items.isEmpty() -> Text(
                            "Tidak ada aset yang cocok dengan filter ini.",
                            fontSize = 10.sp, color = Slate400,
                        )

                        else -> {
                            items.forEachIndexed { i, a ->
                                KartuAsetLaporan(
                                    a = a,
                                    nomor = (page - 1) * limit + i + 1,
                                    onOpen = { onOpen(a.slug) },
                                    tampilKondisi = tampilKondisi,
                                )
                            }
                            PagerLaporan(data!!) { page = it }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BannerLaporanAset(onBack: (() -> Unit)?) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF0D9488), Color(0xFF06B6D4)))),
    ) {
    OrnamenHeader(Modifier.align(Alignment.BottomEnd), ukuran = 104.dp, alpha = 0.16f)
    Column(
        Modifier
            .padding(13.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Home",
                fontSize = 9.sp,
                color = Color.White.copy(alpha = 0.8f),
                modifier = if (onBack != null) Modifier.clickable { onBack() } else Modifier,
            )
            Text(" / ", fontSize = 9.sp, color = Color.White.copy(alpha = 0.6f))
            Text("Laporan Data Aset", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
        Text("Laporan Data Aset", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        Text(
            "SIMAP Airport Rescue & Fire Fighting BDJ",
            fontSize = 9.sp, color = Color.White.copy(alpha = 0.88f),
        )
    }
    }
}

@Composable
private fun RingkasanAset(s: AssetStats) {
    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        KotakRingkas("Total Aset", s.total, "Teregistrasi", Color(0xFFE0F7FA), Color(0xFF00696B), Modifier.weight(1f))
        KotakRingkas("Lokasi Aktif", s.lokasi, "Area Terdata", Color(0xFFDCFCE7), Color(0xFF166534), Modifier.weight(1f))
        KotakRingkas("Jenis Aset", s.jenis, "Kategori", Color(0xFFFEF3C7), Color(0xFF92400E), Modifier.weight(1f))
    }
}

@Composable
private fun KotakRingkas(label: String, nilai: Int, sub: String, latar: Color, teks: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(11.dp))
            .background(latar)
            .border(1.dp, teks.copy(alpha = 0.25f), RoundedCornerShape(11.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        Text(label.uppercase(), fontSize = 7.sp, fontWeight = FontWeight.Bold, color = teks, maxLines = 1)
        Text("$nilai", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = teks)
        Text(sub, fontSize = 7.sp, color = teks.copy(alpha = 0.8f), maxLines = 1)
    }
}

@Composable
private fun RowScope.TombolEkspor(label: String, latar: Color, teks: Color, onClick: () -> Unit) {
    Text(
        label,
        fontSize = 9.sp,
        fontWeight = FontWeight.SemiBold,
        color = teks,
        maxLines = 1,
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(latar)
            .border(1.dp, teks.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 7.dp),
    )
}

@Composable
private fun DropdownLaporan(
    label: String,
    nilai: String,
    opsi: List<Pair<String, String>>,
    onPilih: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var buka by remember { mutableStateOf(false) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label.uppercase(), fontSize = 7.sp, fontWeight = FontWeight.Bold, color = LapSlate500)
        Box {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, LapBorder, RoundedCornerShape(8.dp))
                    .clickable { buka = true }
                    .padding(horizontal = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(nilai, fontSize = 9.sp, fontWeight = FontWeight.Medium, color = LapSlate700, maxLines = 1)
                Spacer(Modifier.weight(1f))
                Text("▾", fontSize = 9.sp, color = Slate400)
            }
            DropdownMenu(expanded = buka, onDismissRequest = { buka = false }) {
                opsi.forEach { (v, l) ->
                    DropdownMenuItem(
                        text = { Text(l, fontSize = 11.sp) },
                        onClick = { onPilih(v); buka = false },
                    )
                }
            }
        }
    }
}

@Composable
private fun PagerLaporan(d: AssetsResponse, onPage: (Int) -> Unit) {
    val pages = ((d.total + d.limit - 1) / d.limit).coerceAtLeast(1)
    val dari = if (d.items.isEmpty()) 0 else (d.page - 1) * d.limit + 1
    val sampai = (dari + d.items.size - 1).coerceAtMost(d.total)

    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .background(Color.White)
            .border(1.dp, LapBorder, RoundedCornerShape(11.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Menampilkan $dari–$sampai dari ${d.total} data", fontSize = 8.sp, color = LapSlate500)
        Spacer(Modifier.weight(1f))
        TombolHalamanLaporan("‹", d.page > 1) { onPage(d.page - 1) }
        Text(
            "${d.page}/$pages",
            fontSize = 9.sp, fontWeight = FontWeight.Bold, color = LapTeal,
            modifier = Modifier.padding(horizontal = 6.dp),
        )
        TombolHalamanLaporan("›", d.page < pages) { onPage(d.page + 1) }
    }
}

@Composable
private fun TombolHalamanLaporan(label: String, aktif: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(24.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, LapBorder, RoundedCornerShape(6.dp))
            .clickable(enabled = aktif) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontSize = 11.sp, color = if (aktif) LapSlate700 else Color(0xFFCBD5E1))
    }
}
