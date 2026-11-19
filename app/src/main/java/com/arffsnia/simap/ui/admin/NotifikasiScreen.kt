package com.arffsnia.simap.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Notifications
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arffsnia.simap.SimapApp
import com.arffsnia.simap.data.dto.NotifItem
import com.arffsnia.simap.data.dto.NotifResponse
import com.arffsnia.simap.data.repo.AppResult
import com.arffsnia.simap.ui.mapError
import com.arffsnia.simap.ui.theme.PlusJakarta
import com.arffsnia.simap.ui.theme.PlusJakartaTypography
import com.arffsnia.simap.ui.theme.SimapTeal
import com.arffsnia.simap.ui.theme.Slate400
import com.arffsnia.simap.ui.theme.Slate600
import com.arffsnia.simap.ui.theme.Slate900
import java.util.Locale

private val NfBg = Color(0xFFF1F5F9)
private val NfBorder = Color(0xFFE2E8F0)
private val NfHair = Color(0xFFF1F5F9)
private val NfSubtle = Color(0xFFF8FAFC)
private val NfSlate700 = Color(0xFF334155)
private val NfSlate500 = Color(0xFF64748B)
private val NfTeal = Color(0xFF0D9488)
private val NfTealDark = Color(0xFF0F766E)
private val NfTealSoft = Color(0xFFF0FDFA)
private val NfTealBorder = Color(0xFFBCE8EA)
private val NfAmber = Color(0xFFF59E0B)
private val NfAmberSoft = Color(0xFFFFFBEB)
private val NfAmberBorder = Color(0xFFFDE68A)
private val NfAmberFg = Color(0xFF92400E)
private val NfIndigoSoft = Color(0xFFEEF2FF)
private val NfIndigoBorder = Color(0xFFC7D2FE)
private val NfIndigoFg = Color(0xFF3730A3)
private val NfRoseSoft = Color(0xFFFFF1F2)
private val NfRoseBorder = Color(0xFFFECDD3)
private val NfRoseFg = Color(0xFFBE123C)

private val BULAN_PANJANG = arrayOf(
    "Januari", "Februari", "Maret", "April", "Mei", "Juni",
    "Juli", "Agustus", "September", "Oktober", "November", "Desember",
)

/** "2026-06-02 20:38:31" -> "02 Juni 2026 • 20:38:31" */
private fun waktuPanjang(iso: String?): String {
    if (iso.isNullOrBlank() || iso.length < 16) return iso ?: "—"
    val d = iso.substring(8, 10)
    val m = iso.substring(5, 7).toIntOrNull() ?: return iso
    val y = iso.substring(0, 4)
    val jam = iso.substring(11, 19)
    val nama = BULAN_PANJANG.getOrNull(m - 1) ?: ""
    return "$d $nama $y • $jam"
}

private data class NotifTab(val key: String, val label: String, val jumlah: Int)

@Composable
fun NotifikasiScreen(onOpen: (String) -> Unit, onProfile: (() -> Unit)? = null) {
    val app = LocalContext.current.applicationContext as SimapApp
    val user = remember { app.authRepo.currentUser() }

    var tab by remember { mutableStateOf("belum") }
    var reguId by remember { mutableStateOf(0) }
    var query by remember { mutableStateOf("") }
    var limit by remember { mutableStateOf(10) }
    var page by remember { mutableStateOf(1) }
    var data by remember { mutableStateOf<NotifResponse?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableStateOf(0) }

    LaunchedEffect(tab, reguId, query, limit, page, refreshKey) {
        loading = true
        when (val r = app.adminRepo.notifikasi(tab, reguId, query, page, limit)) {
            is AppResult.Success -> { data = r.data; error = null }
            is AppResult.Error -> error = mapError(r.message)
        }
        loading = false
    }

    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme,
        typography = PlusJakartaTypography,
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides TextStyle(
                fontFamily = PlusJakarta,
                fontSize = 12.sp,
                platformStyle = PlatformTextStyle(includeFontPadding = false),
            ),
        ) {
            Column(Modifier.fillMaxSize().background(NfBg).statusBarsPadding()) {
                TopBar(
                    user = user,
                    onNotif = { refreshKey++ },
                    onProfile = onProfile,
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp)
                        .padding(top = 12.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    BannerNotifikasi(data?.periode)

                    val d = data
                    if (d != null) {
                        RingkasanNotif(d)

                        BarTabNotif(
                            tabs = listOf(
                                NotifTab("belum", "Belum Dicek", d.ringkasan.belum),
                                NotifTab("foto", "Tanpa Foto", d.ringkasan.tanpaFoto),
                                NotifTab("rusak", "Aset Rusak", d.ringkasan.rusak),
                                NotifTab("maintenance", "Maintenance", d.ringkasan.maintenance),
                            ),
                            aktif = tab,
                            onTab = { tab = it; page = 1 },
                        )

                        KartuFilterNotif(
                            regu = d.regu.map { it.id to it.nama },
                            reguId = reguId,
                            onRegu = { reguId = it; page = 1 },
                            query = query,
                            onQuery = { query = it; page = 1 },
                            limit = limit,
                            onLimit = { limit = it; page = 1 },
                            page = d.page,
                            pages = d.pages,
                            periode = d.periode,
                        )
                    }

                    when {
                        loading -> Box(
                            Modifier.fillMaxWidth().height(170.dp),
                            contentAlignment = Alignment.Center,
                        ) { CircularProgressIndicator(color = SimapTeal) }

                        error != null -> Text(error!!, fontSize = 12.sp, color = NfRoseFg)

                        d != null -> {
                            if (d.items.isEmpty()) {
                                Text(
                                    "Tidak ada data pada kategori ini.",
                                    fontSize = 12.sp,
                                    color = Slate400,
                                    modifier = Modifier.padding(vertical = 18.dp),
                                )
                            }
                            d.items.forEachIndexed { i, aset ->
                                KartuNotif(
                                    nomor = (d.page - 1) * d.limit + i + 1,
                                    aset = aset,
                                    tab = tab,
                                    onDetail = { onOpen(aset.slug) },
                                )
                            }
                            if (d.items.isNotEmpty()) {
                                PagerNotif(d) { page = it }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BannerNotifikasi(periode: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF0D9488), Color(0xFF06B6D4)))),
    ) {
        OrnamenHeader(Modifier.align(Alignment.BottomEnd), ukuran = 112.dp, alpha = 0.16f)
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Home", fontSize = 11.sp, color = Color(0xFFCCFBF1))
                Text(" › ", fontSize = 11.sp, color = Color(0xFFCCFBF1).copy(alpha = 0.7f))
                Text("Pusat Notifikasi", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                Spacer(Modifier.weight(1f))
                Box(
                    Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.White.copy(alpha = 0.22f))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    Text("Alert Hub", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(30.dp).clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.20f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.Notifications, null, Modifier.size(16.dp), tint = Color.White)
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("Pusat Notifikasi", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(
                        "Pantau semua alert dan status aset yang memerlukan perhatian.",
                        fontSize = 11.sp,
                        color = Color(0xFFF0FDFA),
                        modifier = Modifier.fillMaxWidth(0.92f),
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.22f))
            Spacer(Modifier.height(8.dp))
            Text(
                "Periode aktif: ${periode?.ifBlank { null } ?: "—"}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.95f),
            )
        }
    }
}

@Composable
private fun RingkasanNotif(d: NotifResponse) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "RINGKASAN NOTIFIKASI",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = NfSlate500,
                letterSpacing = 0.5.sp,
                modifier = Modifier.weight(1f),
            )
            Box(
                Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(NfTealSoft)
                    .border(1.dp, NfTealBorder, RoundedCornerShape(999.dp))
                    .padding(horizontal = 7.dp, vertical = 1.dp),
            ) {
                Text("5 Kategori", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = NfTealDark)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatNotif(d.ringkasan.total.toString(), "Total Aset", "Total",
                NfTealSoft, NfTealBorder, NfTealDark, Modifier.weight(1f))
            StatNotif(d.ringkasan.belum.toString(), "Belum Dicek", "Perlu Cek",
                NfAmberSoft, NfAmberBorder, NfAmberFg, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatNotif(d.ringkasan.tanpaFoto.toString(), "Aset Tanpa Foto", "Foto",
                NfIndigoSoft, NfIndigoBorder, NfIndigoFg, Modifier.weight(1f))
            StatNotif(d.ringkasan.rusak.toString(), "Laporan Rusak", "30 Hari",
                NfRoseSoft, NfRoseBorder, NfRoseFg, Modifier.weight(1f))
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(NfTealSoft)
                .border(1.dp, NfTealBorder, RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Maintenance", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = NfTealDark)
                Text("30 hari terakhir", fontSize = 10.sp, color = NfTeal.copy(alpha = 0.8f))
            }
            Text(d.ringkasan.maintenance.toString(), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = NfTealDark)
        }
    }
}

@Composable
private fun StatNotif(
    nilai: String,
    label: String,
    tag: String,
    bg: Color,
    border: Color,
    fg: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(12.dp))
            .padding(11.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(6.dp).background(fg.copy(alpha = 0.55f), CircleShape))
            Spacer(Modifier.weight(1f))
            Box(
                Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .background(fg.copy(alpha = 0.12f))
                    .padding(horizontal = 5.dp, vertical = 1.dp),
            ) {
                Text(tag, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = fg)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(nilai, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = fg)
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = fg.copy(alpha = 0.9f), maxLines = 1)
    }
}

@Composable
private fun BarTabNotif(tabs: List<NotifTab>, aktif: String, onTab: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        tabs.forEach { t ->
            val on = t.key == aktif
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (on) NfTealDark else Color.White)
                    .border(1.dp, if (on) NfTealDark else NfBorder, RoundedCornerShape(12.dp))
                    .clickable { onTab(t.key) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    t.label,
                    fontSize = 11.sp,
                    fontWeight = if (on) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (on) Color.White else NfSlate700,
                )
                Spacer(Modifier.width(6.dp))
                Box(
                    Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (on) Color.White.copy(alpha = 0.25f) else Color(0xFFF1F5F9))
                        .padding(horizontal = 5.dp, vertical = 1.dp),
                ) {
                    Text(
                        "${t.jumlah}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (on) Color.White else NfSlate500,
                    )
                }
            }
        }
    }
}

@Composable
private fun KartuFilterNotif(
    regu: List<Pair<Int, String>>,
    reguId: Int,
    onRegu: (Int) -> Unit,
    query: String,
    onQuery: (String) -> Unit,
    limit: Int,
    onLimit: (Int) -> Unit,
    page: Int,
    pages: Int,
    periode: String?,
) {
    var reguOpen by remember { mutableStateOf(false) }
    var limitOpen by remember { mutableStateOf(false) }
    val reguLabel = if (reguId == 0) "Regu: Semua"
    else "Regu: ${regu.firstOrNull { it.first == reguId }?.second ?: "-"}"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, NfBorder, RoundedCornerShape(14.dp))
            .padding(11.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.weight(1f))
            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NfSubtle)
                        .border(1.dp, NfBorder, RoundedCornerShape(8.dp))
                        .clickable { reguOpen = true }
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(reguLabel, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = NfSlate700)
                    Icon(Icons.Rounded.ChevronRight, null, Modifier.size(11.dp), tint = Slate400)
                }
                DropdownMenu(expanded = reguOpen, onDismissRequest = { reguOpen = false }) {
                    DropdownMenuItem(
                        text = { Text("Regu: Semua", fontSize = 12.sp) },
                        onClick = { onRegu(0); reguOpen = false },
                    )
                    regu.forEach { (id, nama) ->
                        DropdownMenuItem(
                            text = { Text(nama, fontSize = 12.sp) },
                            onClick = { onRegu(id); reguOpen = false },
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(NfSubtle)
                .border(1.dp, NfBorder, RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.Search, null, Modifier.size(14.dp), tint = Slate400)
            Spacer(Modifier.width(7.dp))
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (query.isEmpty()) {
                    Text("Cari aset, kode, lokasi...", fontSize = 11.sp, color = Slate400)
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQuery,
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 11.sp, color = Slate900),
                    cursorBrush = SolidColor(SimapTeal),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        HorizontalDivider(color = NfHair)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Tampilkan", fontSize = 10.sp, color = NfSlate500)
            Spacer(Modifier.width(5.dp))
            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFF1F5F9))
                        .clickable { limitOpen = true }
                        .padding(horizontal = 7.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("$limit", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NfSlate700)
                }
                DropdownMenu(expanded = limitOpen, onDismissRequest = { limitOpen = false }) {
                    listOf(10, 25, 50).forEach { n ->
                        DropdownMenuItem(
                            text = { Text("$n", fontSize = 12.sp) },
                            onClick = { onLimit(n); limitOpen = false },
                        )
                    }
                }
            }
            Spacer(Modifier.width(5.dp))
            Text("entri", fontSize = 10.sp, color = NfSlate500)
            Spacer(Modifier.weight(1f))
            Text("Hal $page dari $pages", fontSize = 9.sp, fontWeight = FontWeight.Medium, color = Slate400)
        }

        if (!periode.isNullOrBlank()) {
            Text("Periode aktif: $periode", fontSize = 9.sp, color = Slate400)
        }
    }
}

@Composable
private fun KartuNotif(nomor: Int, aset: NotifItem, tab: String, onDetail: () -> Unit) {
    val aksen = when (tab) {
        "foto" -> Color(0xFF6366F1)
        "rusak" -> Color(0xFFE11D48)
        "maintenance" -> NfTeal
        else -> NfAmber
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, NfBorder, RoundedCornerShape(14.dp)),
    ) {
        Box(Modifier.width(4.dp).fillMaxHeight().background(aksen))
        Column(Modifier.weight(1f).padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(20.dp).clip(CircleShape).background(Color(0xFFF1F5F9)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("$nomor", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = NfSlate500)
                }
                Spacer(Modifier.width(7.dp))
                Box(
                    Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .background(NfTealSoft)
                        .border(1.dp, NfTealBorder, RoundedCornerShape(5.dp))
                        .padding(horizontal = 6.dp, vertical = 1.dp),
                ) {
                    Text(
                        aset.slug,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NfTealDark,
                        fontFamily = FontFamily.Monospace,
                    )
                }
                Spacer(Modifier.weight(1f))
                val aktifPeriode = aset.badge == "Periode Aktif"
                Box(
                    Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (aktifPeriode) NfTealSoft else Color(0xFFFFF7ED))
                        .border(1.dp, if (aktifPeriode) NfTealBorder else NfAmberBorder, RoundedCornerShape(999.dp))
                        .padding(horizontal = 7.dp, vertical = 2.dp),
                ) {
                    Text(
                        aset.badge ?: "—",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (aktifPeriode) NfTealDark else NfAmberFg,
                    )
                }
            }

            Spacer(Modifier.height(7.dp))
            Text(aset.nama, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate900)

            Spacer(Modifier.height(7.dp))
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(NfSubtle)
                    .border(1.dp, NfHair, RoundedCornerShape(10.dp))
                    .padding(9.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row {
                    MetaNotif("Jenis:", aset.jenis ?: "—", Modifier.weight(1f))
                    MetaNotif("Lokasi:", aset.lokasi ?: "—", Modifier.weight(1f))
                }
                Row {
                    MetaNotif("Zonasi:", aset.zonasi ?: "—", Modifier.weight(1f))
                    MetaNotif("Periode:", aset.periode ?: "—", Modifier.weight(1f))
                }
                aset.keterangan?.takeIf { it.isNotBlank() }?.let {
                    HorizontalDivider(color = NfHair)
                    Text(it, fontSize = 9.sp, color = NfSlate500)
                }
            }

            Spacer(Modifier.height(9.dp))
            HorizontalDivider(color = NfHair)
            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.DateRange, null, Modifier.size(11.dp), tint = Slate400)
                Spacer(Modifier.width(4.dp))
                Text(
                    waktuPanjang(aset.updatedAt),
                    fontSize = 9.sp,
                    color = Slate400,
                    modifier = Modifier.weight(1f),
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9.dp))
                        .background(NfTealDark)
                        .clickable(onClick = onDetail)
                        .padding(horizontal = 11.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.Visibility, null, Modifier.size(12.dp), tint = Color.White)
                    Spacer(Modifier.width(4.dp))
                    Text("Detail", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun MetaNotif(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, fontSize = 9.sp, color = Slate400)
        Text(value, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = NfSlate700, maxLines = 1)
    }
}

@Composable
private fun PagerNotif(d: NotifResponse, onPage: (Int) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(top = 2.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "Menampilkan ${(d.page - 1) * d.limit + 1}–${minOf(d.page * d.limit, d.hits)} dari ${d.hits} entri",
            fontSize = 10.sp,
            color = NfSlate500,
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
            HalamanNotif(Icons.Rounded.ChevronLeft, d.page > 1) { onPage(d.page - 1) }
            val nomor = mutableListOf<Int?>()
            nomor += 1
            if (d.page > 3) nomor += null
            for (n in maxOf(2, d.page - 1)..minOf(d.pages - 1, d.page + 1)) nomor += n
            if (d.page < d.pages - 2) nomor += null
            if (d.pages > 1) nomor += d.pages
            nomor.distinct().forEach { n ->
                if (n == null) {
                    Text("...", fontSize = 10.sp, color = Slate400)
                } else {
                    Box(
                        Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (n == d.page) NfTealDark else Color.White)
                            .border(1.dp, if (n == d.page) NfTealDark else NfBorder, RoundedCornerShape(8.dp))
                            .clickable(enabled = n != d.page) { onPage(n) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "$n",
                            fontSize = 10.sp,
                            fontWeight = if (n == d.page) FontWeight.Bold else FontWeight.Medium,
                            color = if (n == d.page) Color.White else NfSlate700,
                        )
                    }
                }
            }
            HalamanNotif(Icons.Rounded.ChevronRight, d.page < d.pages) { onPage(d.page + 1) }
        }
    }
}

@Composable
private fun HalamanNotif(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .border(1.dp, NfBorder, RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, Modifier.size(12.dp), tint = if (enabled) Slate600 else Slate400)
    }
}
