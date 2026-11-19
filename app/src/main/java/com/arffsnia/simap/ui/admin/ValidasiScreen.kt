package com.arffsnia.simap.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.rounded.Assignment
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.arffsnia.simap.data.dto.ValidasiDetail
import com.arffsnia.simap.data.dto.ValidasiItem
import com.arffsnia.simap.data.repo.AppResult
import com.arffsnia.simap.ui.mapError
import com.arffsnia.simap.ui.theme.PlusJakarta
import com.arffsnia.simap.ui.theme.PlusJakartaTypography
import com.arffsnia.simap.ui.theme.SimapTeal
import com.arffsnia.simap.ui.theme.Slate400
import com.arffsnia.simap.ui.theme.Slate600
import com.arffsnia.simap.ui.theme.Slate900
import kotlinx.coroutines.launch

// ===== Palet lokal layar Validasi =====
private val ValBg = Color(0xFFF4F7F6)
private val ValCardBorder = Color(0xFFE2E8F0)
private val ValHair = Color(0xFFF1F5F9)
private val ValSubtle = Color(0xFFF8FAFC)
private val ValSlate700 = Color(0xFF334155)
private val ValSlate500 = Color(0xFF64748B)
private val ValTeal = Color(0xFF00A896)
private val ValTealDark = Color(0xFF0F766E)
private val ValTealSoft = Color(0xFFE6F7F5)
private val ValTealBorder = Color(0xFFB8E6E1)
private val AmberBg = Color(0xFFF59E0B)
private val EmeraldBtn = Color(0xFF059669)
private val RoseSoft = Color(0xFFFFF1F2)
private val RoseBorder = Color(0xFFFECDD3)
private val RoseFg = Color(0xFFBE123C)
private val CyanPill = Color(0xFF06B6D4)

private fun mapTipe(t: String?): String = when (t?.lowercase()) {
    "assets", "asset" -> "Data Aset"
    "maintenances" -> "Maintenance"
    "damagedassets" -> "Aset Rusak"
    else -> t?.replaceFirstChar { it.uppercase() } ?: "Data"
}

private fun mapAksi(a: String?): String = when (a?.lowercase()) {
    "create" -> "Tambah"
    "update" -> "Ubah"
    "delete" -> "Hapus"
    else -> a ?: ""
}

private fun aksiWarna(a: String?): Color = when (a?.lowercase()) {
    "delete" -> RoseFg
    "update" -> Color(0xFFB45309)
    else -> Slate900
}

private fun statusLabel(s: String?): String = when (s?.lowercase()) {
    "approved" -> "Disetujui"
    "rejected" -> "Ditolak"
    "pending" -> "Menunggu"
    else -> s ?: "-"
}

private fun perubahanTeks(v: ValidasiItem): String = when (v.aksi?.lowercase()) {
    "create" -> "Registrasi Inventaris Baru"
    "delete" -> "Permohonan penghapusan unit dari data operasional"
    "update" -> "Pembaruan data aset"
    else -> mapAksi(v.aksi)
}

@Composable
fun ValidasiScreen(
    onOpen: (ValidasiItem) -> Unit,
    onNotif: (() -> Unit)? = null,
    onProfile: (() -> Unit)? = null,
) {
    val app = LocalContext.current.applicationContext as SimapApp
    val scope = rememberCoroutineScope()
    val user = remember { app.authRepo.currentUser() }

    var pending by remember { mutableStateOf<List<ValidasiItem>>(emptyList()) }
    var riwayat by remember { mutableStateOf<List<ValidasiItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableStateOf(0) }
    var busyId by remember { mutableStateOf<Int?>(null) }
    var toast by remember { mutableStateOf<String?>(null) }

    var tab by remember { mutableStateOf("pending") }
    var query by remember { mutableStateOf("") }
    var limit by remember { mutableStateOf(10) }
    var page by remember { mutableStateOf(1) }

    var approveTarget by remember { mutableStateOf<ValidasiItem?>(null) }
    var rejectTarget by remember { mutableStateOf<ValidasiItem?>(null) }
    var rejectNote by remember { mutableStateOf("") }

    fun reload() {
        scope.launch {
            loading = true
            when (val r = app.adminRepo.validasi()) {
                is AppResult.Success -> {
                    pending = r.data.pending
                    riwayat = r.data.riwayat
                    error = null
                }
                is AppResult.Error -> error = mapError(r.message)
            }
            loading = false
        }
    }

    LaunchedEffect(refreshKey) { reload() }
    LaunchedEffect(tab, query, limit) { page = 1 }

    val q = query.trim().lowercase()
    fun cocok(v: ValidasiItem): Boolean {
        if (q.isEmpty()) return true
        return listOfNotNull(v.nama, v.pemohon, v.pemohonUsername, v.regu, v.tipe, v.aksi, v.catatan)
            .any { it.lowercase().contains(q) }
    }

    val daftar = if (tab == "pending") pending.filter(::cocok) else riwayat.filter(::cocok)
    val pages = if (daftar.isEmpty()) 1 else (daftar.size + limit - 1) / limit
    LaunchedEffect(pages) { if (page > pages) page = pages }
    val halaman = daftar.drop((page - 1) * limit).take(limit)

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
            Column(Modifier.fillMaxSize().background(ValBg).statusBarsPadding()) {
                TopBar(user = user, onNotif = onNotif, onProfile = onProfile)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp)
                        .padding(top = 12.dp, bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    BannerValidasi()
                    TabValidasi(
                        tab = tab,
                        pending = pending.size,
                        riwayat = riwayat.size,
                        onTab = { tab = it },
                    )
                    BarCari(
                        query = query,
                        onQuery = { query = it },
                        limit = limit,
                        onLimit = { limit = it },
                    )

                    toast?.let {
                        Text(it, fontSize = 11.sp, color = ValTeal, fontWeight = FontWeight.SemiBold)
                    }

                    when {
                        loading -> Box(
                            Modifier.fillMaxWidth().height(160.dp),
                            contentAlignment = Alignment.Center,
                        ) { CircularProgressIndicator(color = SimapTeal) }

                        error != null && daftar.isEmpty() -> Column {
                            Text(error!!, fontSize = 12.sp, color = RoseFg)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Coba lagi",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ValTeal,
                                modifier = Modifier.clickable { refreshKey++ }.padding(4.dp),
                            )
                        }

                        else -> {
                            HeaderSeksi(
                                tab = tab,
                                jumlah = daftar.size,
                            )
                            if (daftar.isEmpty()) {
                                Text(
                                    if (tab == "pending") "Tidak ada pengajuan menunggu."
                                    else "Belum ada riwayat validasi.",
                                    fontSize = 12.sp,
                                    color = Slate400,
                                    modifier = Modifier.padding(vertical = 14.dp),
                                )
                            }
                            halaman.forEach { v ->
                                if (tab == "pending") {
                                    KartuPending(
                                        v = v,
                                        busy = busyId == v.id,
                                        onDetail = { onOpen(v) },
                                        onApprove = { approveTarget = v },
                                        onReject = { rejectTarget = v; rejectNote = "" },
                                    )
                                } else {
                                    KartuRiwayat(v, onDetail = { onOpen(v) })
                                }
                            }
                            PagerValidasi(
                                dari = if (daftar.isEmpty()) 0 else (page - 1) * limit + 1,
                                sampai = minOf(page * limit, daftar.size),
                                total = daftar.size,
                                page = page,
                                pages = pages,
                                onPage = { page = it },
                            )
                        }
                    }
                }
            }

            // ===== Dialog setujui =====
            approveTarget?.let { v ->
                AlertDialog(
                    onDismissRequest = { if (busyId == null) approveTarget = null },
                    title = { Text("Setujui pengajuan?", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                    text = {
                        Text(
                            "${mapTipe(v.tipe)} · ${mapAksi(v.aksi)} — ${v.nama ?: "-"}\n" +
                                "Perubahan akan langsung diterapkan ke data operasional.",
                            fontSize = 13.sp,
                            color = ValSlate500,
                        )
                    },
                    confirmButton = {
                        TextButton(
                            enabled = busyId == null,
                            onClick = {
                                val target = v
                                approveTarget = null
                                busyId = target.id
                                scope.launch {
                                    when (val r = app.adminRepo.approve(target.id)) {
                                        is AppResult.Success -> { toast = "Pengajuan disetujui"; refreshKey++ }
                                        is AppResult.Error -> toast = mapError(r.message)
                                    }
                                    busyId = null
                                }
                            },
                        ) { Text(if (busyId == v.id) "Memproses..." else "Setujui", color = EmeraldBtn, fontWeight = FontWeight.Bold) }
                    },
                    dismissButton = {
                        TextButton(onClick = { approveTarget = null }) { Text("Batal", color = ValSlate500) }
                    },
                )
            }

            // ===== Dialog tolak =====
            rejectTarget?.let { v ->
                AlertDialog(
                    onDismissRequest = { if (busyId == null) { rejectTarget = null; rejectNote = "" } },
                    title = { Text("Tolak pengajuan", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                    text = {
                        Column {
                            Text(
                                "${mapTipe(v.tipe)} · ${mapAksi(v.aksi)} — ${v.nama ?: "-"}",
                                fontSize = 12.sp,
                                color = ValSlate500,
                            )
                            Spacer(Modifier.height(8.dp))
                            KotakTeks(
                                value = rejectNote,
                                onChange = { rejectNote = it },
                                placeholder = "Catatan penolakan (opsional)",
                                singleLine = false,
                                minHeight = 64,
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            enabled = busyId == null,
                            onClick = {
                                val target = v
                                val note = rejectNote
                                rejectTarget = null
                                rejectNote = ""
                                busyId = target.id
                                scope.launch {
                                    when (val r = app.adminRepo.reject(target.id, note.ifBlank { null })) {
                                        is AppResult.Success -> { toast = "Pengajuan ditolak"; refreshKey++ }
                                        is AppResult.Error -> toast = mapError(r.message)
                                    }
                                    busyId = null
                                }
                            },
                        ) { Text("Tolak", color = RoseFg, fontWeight = FontWeight.Bold) }
                    },
                    dismissButton = {
                        TextButton(onClick = { rejectTarget = null; rejectNote = "" }) {
                            Text("Batal", color = ValSlate500)
                        }
                    },
                )
            }

        }
    }
}

// ===== Bagian tampilan =====

@Composable
private fun BannerValidasi() {
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
                Text("Validasi Pengajuan", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Validasi Pengajuan", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(
                        "Persetujuan & riwayat mutasi data ARFF",
                        fontSize = 11.sp,
                        color = Color(0xFFF0FDFA),
                    )
                }
                Box(
                    Modifier.size(38.dp).clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.20f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.CheckCircle, null, Modifier.size(19.dp), tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun TabValidasi(tab: String, pending: Int, riwayat: Int, onTab: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, ValCardBorder, RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        TabItem("Menunggu", pending, tab == "pending", Modifier.weight(1f)) { onTab("pending") }
        TabItem("Riwayat", riwayat, tab == "riwayat", Modifier.weight(1f)) { onTab("riwayat") }
    }
}

@Composable
private fun TabItem(label: String, jumlah: Int, aktif: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(if (aktif) ValTealDark else Color.Transparent)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (aktif) Color.White else Slate600,
        )
        Spacer(Modifier.width(6.dp))
        Box(
            Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(if (aktif) Color.White.copy(alpha = 0.25f) else Color(0xFFE2E8F0))
                .padding(horizontal = 6.dp, vertical = 1.dp),
        ) {
            Text(
                "$jumlah",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (aktif) Color.White else Slate900,
            )
        }
    }
}

@Composable
private fun BarCari(query: String, onQuery: (String) -> Unit, limit: Int, onLimit: (Int) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(1.dp, ValCardBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.Search, null, Modifier.size(16.dp), tint = Slate400)
            Spacer(Modifier.width(8.dp))
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (query.isEmpty()) {
                    Text("Cari pengajuan, nama aset, regu...", fontSize = 12.sp, color = Slate400)
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQuery,
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 12.sp, color = Slate900),
                    cursorBrush = SolidColor(SimapTeal),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Tampil", fontSize = 11.sp, color = ValSlate500)
            Spacer(Modifier.width(6.dp))
            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .border(1.dp, ValCardBorder, RoundedCornerShape(8.dp))
                        .clickable { open = true }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("$limit", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = ValSlate700)
                }
                DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                    listOf(10, 25, 50).forEach { n ->
                        DropdownMenuItem(
                            text = { Text("$n", fontSize = 12.sp) },
                            onClick = { onLimit(n); open = false },
                        )
                    }
                }
            }
            Spacer(Modifier.width(6.dp))
            Text("baris", fontSize = 11.sp, color = ValSlate500)
        }
    }
}

@Composable
private fun HeaderSeksi(tab: String, jumlah: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(9.dp).clip(CircleShape)
                .background(if (tab == "pending") Color(0xFFF59E0B) else ValTeal)
                .border(3.dp, (if (tab == "pending") Color(0xFFFEF3C7) else Color(0xFFCCFBF1)), CircleShape),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            if (tab == "pending") "Pengajuan Menunggu Persetujuan" else "Riwayat Validasi",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Slate900,
            modifier = Modifier.weight(1f),
        )
        Box(
            Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(if (tab == "pending") Color(0xFFF43F5E) else Color(0xFFCCFBF1))
                .padding(horizontal = 8.dp, vertical = 2.dp),
        ) {
            Text(
                "$jumlah",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (tab == "pending") Color.White else ValTealDark,
            )
        }
    }
}

@Composable
private fun PilTeks(text: String, bg: Color, fg: Color = Color.White, border: Color? = null) {
    Box(
        Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(bg)
            .let { m -> if (border != null) m.border(1.dp, border, RoundedCornerShape(5.dp)) else m }
            .padding(horizontal = 7.dp, vertical = 2.dp),
    ) {
        Text(text, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = fg)
    }
}

@Composable
private fun KartuPending(
    v: ValidasiItem,
    busy: Boolean,
    onDetail: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, ValCardBorder, RoundedCornerShape(16.dp))
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.DateRange, null, Modifier.size(12.dp), tint = Slate400)
            Spacer(Modifier.width(5.dp))
            Text(
                v.createdAt ?: "-",
                fontSize = 10.sp,
                color = ValSlate500,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1f),
            )
            Box(
                Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(AmberBg)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            ) {
                Text("Menunggu", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = ValHair)
        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            PilTeks(mapTipe(v.tipe), CyanPill)
            PilTeks(mapAksi(v.aksi), aksiWarna(v.aksi))
        }
        Spacer(Modifier.height(6.dp))
        Text(v.nama ?: "-", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
        Spacer(Modifier.height(4.dp))
        Text(
            "Perubahan: ${perubahanTeks(v)}",
            fontSize = 11.sp,
            color = ValSlate500,
        )

        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ValSubtle)
                .border(1.dp, ValHair, RoundedCornerShape(12.dp))
                .padding(10.dp),
        ) {
            Column(Modifier.weight(1f)) {
                Text("PEMOHON", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Slate400)
                Text(v.pemohon ?: "-", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = ValSlate700)
                v.pemohonUsername?.let {
                    Text("($it)", fontSize = 9.sp, color = Slate400)
                }
            }
            Column(Modifier.weight(1f)) {
                Text("REGU JAGA", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Slate400)
                Text(v.regu ?: "-", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = ValTealDark)
            }
        }

        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(ValTealSoft)
                    .border(1.dp, ValTealBorder, RoundedCornerShape(10.dp))
                    .clickable(onClick = onDetail),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.Visibility, null, Modifier.size(13.dp), tint = ValTealDark)
                Spacer(Modifier.width(4.dp))
                Text("Detail", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = ValTealDark)
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (busy) Color(0xFF94A3B8) else EmeraldBtn)
                    .clickable(enabled = !busy, onClick = onApprove),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.CheckCircle, null, Modifier.size(13.dp), tint = Color.White)
                Spacer(Modifier.width(4.dp))
                Text(if (busy) "..." else "Setuju", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(RoseSoft)
                    .border(1.dp, RoseBorder, RoundedCornerShape(10.dp))
                    .clickable(enabled = !busy, onClick = onReject),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.Close, null, Modifier.size(13.dp), tint = RoseFg)
                Spacer(Modifier.width(4.dp))
                Text("Tolak", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = RoseFg)
            }
        }
    }
}

@Composable
private fun KartuRiwayat(v: ValidasiItem, onDetail: () -> Unit) {
    val disetujui = v.status?.lowercase() == "approved"
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, ValCardBorder, RoundedCornerShape(16.dp))
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.DateRange, null, Modifier.size(12.dp), tint = Slate400)
            Spacer(Modifier.width(5.dp))
            Text(
                v.reviewedAt ?: v.createdAt ?: "-",
                fontSize = 10.sp,
                color = ValSlate500,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1f),
            )
            Box(
                Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (disetujui) EmeraldBtn else Color(0xFFE11D48))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            ) {
                Text(statusLabel(v.status), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = ValHair)
        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            PilTeks(mapTipe(v.tipe), CyanPill)
            PilTeks(mapAksi(v.aksi), aksiWarna(v.aksi))
        }
        Spacer(Modifier.height(6.dp))
        Text(v.nama ?: "-", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)

        Spacer(Modifier.height(10.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ValSubtle)
                .border(1.dp, ValHair, RoundedCornerShape(12.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            BarisInfo("Pemohon:", "${v.pemohon ?: "-"}${v.pemohonUsername?.let { " ($it)" } ?: ""}")
            BarisInfo("Regu:", v.regu ?: "-", ValTealDark)
            BarisInfo("Peninjau:", v.peninjau ?: "-", bold = true)
            BarisInfo("Catatan:", v.catatan?.ifBlank { null } ?: "—")
        }

        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(ValTealSoft)
                .border(1.dp, ValTealBorder, RoundedCornerShape(10.dp))
                .clickable(onClick = onDetail),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.Visibility, null, Modifier.size(13.dp), tint = ValTealDark)
            Spacer(Modifier.width(5.dp))
            Text("Lihat Detail Audit Log", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = ValTealDark)
        }
    }
}

@Composable
private fun BarisInfo(label: String, value: String, tint: Color = ValSlate700, bold: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 11.sp, color = ValSlate500, modifier = Modifier.weight(1f))
        Text(
            value,
            fontSize = 11.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
            color = tint,
        )
    }
}

@Composable
private fun PagerValidasi(dari: Int, sampai: Int, total: Int, page: Int, pages: Int, onPage: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(
            "Menampilkan $dari–$sampai dari $total data",
            fontSize = 11.sp,
            color = ValSlate500,
            modifier = Modifier.weight(1f),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
            TombolHalaman(Icons.Rounded.ChevronLeft, page > 1) { onPage(page - 1) }
            Box(
                Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(ValTealDark),
                contentAlignment = Alignment.Center,
            ) {
                Text("$page", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Text("/ $pages", fontSize = 11.sp, color = Slate400)
            TombolHalaman(Icons.Rounded.ChevronRight, page < pages) { onPage(page + 1) }
        }
    }
}

@Composable
private fun TombolHalaman(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .border(1.dp, ValCardBorder, RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, Modifier.size(13.dp), tint = if (enabled) Slate600 else Slate400)
    }
}

// ===== Layar detail pengajuan =====

private fun labelField(key: String): String = when (key) {
    "nama" -> "Nama"
    "slug" -> "Kode"
    "merek" -> "Merek"
    "category_id" -> "Jenis ID"
    "berat" -> "Berat"
    "room_id" -> "Lokasi ID"
    "lokasi" -> "Lokasi"
    "latitude" -> "Latitude"
    "longitude" -> "Longitude"
    "tahun" -> "Tahun"
    "periode_id" -> "Periode ID"
    "periode" -> "Periode"
    "jenis" -> "Jenis"
    "ruangan" -> "Ruangan"
    "status" -> "Status Aset"
    "checklist" -> "Checklist"
    "pengecekan" -> "Pengecekan"
    "is_pengecekan" -> "Pengecekan Otomatis"
    else -> key.replace('_', ' ').replaceFirstChar { it.uppercase() }
}

private fun nilaiTeks(v: Any?): String = when (v) {
    null -> "-"
    is Map<*, *> -> v.entries
        .filter { it.value.toString().isNotBlank() }
        .joinToString(" · ") { "${it.key}: ${it.value}" }
    is Boolean -> if (v) "Ya" else "Tidak"
    is Double -> if (v == v.toLong().toDouble()) v.toLong().toString() else v.toString()
    else -> v.toString()
}

private fun urlFoto(nama: String?): String? {
    if (nama.isNullOrBlank()) return null
    if (nama.startsWith("http")) return nama
    return "https://simap.arffsnia.com/assets/images/items/$nama"
}

@Composable
fun PengajuanDetailScreen(
    item: ValidasiItem,
    onBack: () -> Unit,
    onChanged: () -> Unit,
    onNotif: (() -> Unit)? = null,
    onProfile: (() -> Unit)? = null,
) {
    val app = LocalContext.current.applicationContext as SimapApp
    val scope = rememberCoroutineScope()
    val user = remember { app.authRepo.currentUser() }

    var detail by remember { mutableStateOf<ValidasiDetail?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var approveOpen by remember { mutableStateOf(false) }
    var rejectOpen by remember { mutableStateOf(false) }
    var rejectNote by remember { mutableStateOf("") }
    var aksiError by remember { mutableStateOf<String?>(null) }

    val masihPending = item.status?.lowercase() == "pending"

    LaunchedEffect(item.id) {
        when (val r = app.adminRepo.validasiDetail(item.id)) {
            is AppResult.Success -> detail = r.data
            is AppResult.Error -> error = mapError(r.message)
        }
    }

    fun setujui() {
        busy = true
        aksiError = null
        scope.launch {
            when (val r = app.adminRepo.approve(item.id)) {
                is AppResult.Success -> { busy = false; onChanged() }
                is AppResult.Error -> { busy = false; aksiError = mapError(r.message) }
            }
        }
    }

    fun tolak(note: String) {
        busy = true
        aksiError = null
        scope.launch {
            when (val r = app.adminRepo.reject(item.id, note.ifBlank { null })) {
                is AppResult.Success -> { busy = false; onChanged() }
                is AppResult.Error -> { busy = false; aksiError = mapError(r.message) }
            }
        }
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
            Box(Modifier.fillMaxSize().background(ValBg)) {
                Column(Modifier.fillMaxSize().statusBarsPadding()) {
                    TopBar(user = user, onNotif = onNotif, onProfile = onProfile)

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 14.dp)
                            .padding(top = 12.dp, bottom = if (masihPending) 96.dp else 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        BannerDetailPengajuan()
                        KartuIdentitas(item, onBack)
                        KartuInfoPengajuan(item)
                        KartuPerubahan(item, detail, error, aksiError)
                    }
                }

                if (masihPending) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.97f))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(RoseSoft)
                                .border(1.dp, RoseBorder, RoundedCornerShape(12.dp))
                                .clickable(enabled = !busy) { rejectOpen = true; rejectNote = "" },
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Rounded.Close, null, Modifier.size(14.dp), tint = RoseFg)
                            Spacer(Modifier.width(5.dp))
                            Text("Tolak", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RoseFg)
                        }
                        Row(
                            modifier = Modifier
                                .weight(1.4f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.horizontalGradient(listOf(ValTealDark, EmeraldBtn)))
                                .clickable(enabled = !busy) { approveOpen = true },
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Rounded.CheckCircle, null, Modifier.size(14.dp), tint = Color.White)
                            Spacer(Modifier.width(5.dp))
                            Text(
                                if (busy) "Memproses..." else "Setujui Validasi",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                        }
                    }
                }
            }

            if (approveOpen) {
                AlertDialog(
                    onDismissRequest = { if (!busy) approveOpen = false },
                    title = { Text("Setujui pengajuan?", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                    text = {
                        Text(
                            "${mapTipe(item.tipe)} · ${mapAksi(item.aksi)} — ${item.nama ?: "-"}\n" +
                                "Perubahan akan langsung diterapkan ke data operasional.",
                            fontSize = 13.sp,
                            color = ValSlate500,
                        )
                    },
                    confirmButton = {
                        TextButton(enabled = !busy, onClick = { approveOpen = false; setujui() }) {
                            Text("Setujui", color = EmeraldBtn, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { approveOpen = false }) { Text("Batal", color = ValSlate500) }
                    },
                )
            }

            if (rejectOpen) {
                AlertDialog(
                    onDismissRequest = { if (!busy) { rejectOpen = false; rejectNote = "" } },
                    title = { Text("Tolak pengajuan", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                    text = {
                        Column {
                            Text(
                                "${mapTipe(item.tipe)} · ${mapAksi(item.aksi)} — ${item.nama ?: "-"}",
                                fontSize = 12.sp,
                                color = ValSlate500,
                            )
                            Spacer(Modifier.height(8.dp))
                            KotakTeks(
                                value = rejectNote,
                                onChange = { rejectNote = it },
                                placeholder = "Catatan penolakan (opsional)",
                                singleLine = false,
                                minHeight = 64,
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(enabled = !busy, onClick = { rejectOpen = false; tolak(rejectNote) }) {
                            Text("Tolak", color = RoseFg, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { rejectOpen = false; rejectNote = "" }) {
                            Text("Batal", color = ValSlate500)
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun BannerDetailPengajuan() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF14B8A6), Color(0xFF06B6D4)))),
    ) {
        OrnamenHeader(Modifier.align(Alignment.BottomEnd), ukuran = 112.dp, alpha = 0.16f)
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Home", fontSize = 11.sp, color = Color(0xFFCCFBF1))
                Text(" › ", fontSize = 11.sp, color = Color(0xFFCCFBF1).copy(alpha = 0.7f))
                Text("Detail Pengajuan", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                Spacer(Modifier.weight(1f))
                Box(
                    Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.White.copy(alpha = 0.22f))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    Text("Validasi", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            Spacer(Modifier.height(6.dp))
            Text("Detail Pengajuan", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(
                "Pengajuan validasi dan pembaruan data sistem",
                fontSize = 11.sp,
                color = Color(0xFFF0FDFA),
            )
        }
    }
}

@Composable
private fun KartuIdentitas(v: ValidasiItem, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, ValCardBorder, RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(ValTealSoft),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.Assignment, null, Modifier.size(17.dp), tint = ValTealDark)
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text("Detail Pengajuan #${v.id}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
            Text("ID Pengajuan: REQ-ARFF-${"%03d".format(v.id)}", fontSize = 10.sp, color = Slate400)
        }
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(ValTealDark)
                .clickable(onClick = onBack)
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.ChevronLeft, null, Modifier.size(13.dp), tint = Color.White)
            Spacer(Modifier.width(3.dp))
            Text("Kembali", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
    }
}

@Composable
private fun KartuInfoPengajuan(v: ValidasiItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, ValCardBorder, RoundedCornerShape(14.dp)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFFFFBEB))
                    .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text("Info Pengajuan", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
            }
            Spacer(Modifier.weight(1f))
            Text("Metadata", fontSize = 10.sp, color = Slate400)
        }
        HorizontalDivider(color = ValHair)

        BarisMeta("Tipe") { PilTeks(mapTipe(v.tipe), Color(0xFFF0FDFA), ValTealDark, ValTealBorder) }
        BarisMeta("Aksi") { PilTeks(mapAksi(v.aksi), aksiWarna(v.aksi)) }
        BarisMeta("Pemohon") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(v.pemohon ?: "-", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate900)
                v.pemohonUsername?.let {
                    Spacer(Modifier.width(4.dp))
                    Text("($it)", fontSize = 10.sp, color = Slate400, fontFamily = FontFamily.Monospace)
                }
            }
        }
        BarisMeta("Regu") {
            Text(v.regu ?: "-", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
        }
        BarisMeta("Waktu Pengajuan") {
            Text(
                v.createdAt ?: "-",
                fontSize = 10.sp,
                color = ValSlate700,
                fontFamily = FontFamily.Monospace,
            )
        }
        BarisMeta("Status") {
            Box(
                Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        when (v.status?.lowercase()) {
                            "approved" -> EmeraldBtn
                            "rejected" -> Color(0xFFE11D48)
                            else -> AmberBg
                        },
                    )
                    .padding(horizontal = 10.dp, vertical = 3.dp),
            ) {
                Text(statusLabel(v.status), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
        BarisMeta("Peninjau") {
            Text(
                v.peninjau ?: "—",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (v.peninjau == null) Slate400 else Slate900,
            )
        }
        BarisMeta("Diproses Pada") {
            Text(
                v.reviewedAt ?: "—",
                fontSize = 10.sp,
                color = if (v.reviewedAt == null) Slate400 else ValSlate700,
                fontFamily = FontFamily.Monospace,
            )
        }
        BarisMeta("Catatan") {
            Text(
                v.catatan?.ifBlank { null } ?: "—",
                fontSize = 11.sp,
                color = if (v.catatan.isNullOrBlank()) Slate400 else ValSlate700,
            )
        }
    }
}

@Composable
private fun BarisMeta(label: String, value: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(4.dp).clip(CircleShape).background(ValTeal.copy(alpha = 0.5f)))
        Spacer(Modifier.width(8.dp))
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Slate600,
            modifier = Modifier.weight(1f),
        )
        value()
    }
}

@Composable
private fun KartuPerubahan(
    v: ValidasiItem,
    detail: ValidasiDetail?,
    error: String?,
    aksiError: String?,
) {
    val data = detail?.data ?: emptyMap()
    val foto = urlFoto(data["_foto"] as? String)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, ValCardBorder, RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFFFFBEB))
                    .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text("Detail Perubahan", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(ValSubtle)
                .border(1.dp, ValCardBorder, RoundedCornerShape(10.dp))
                .padding(10.dp),
        ) {
            Column {
                Text("DESKRIPSI PERMOHONAN", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Slate400)
                Spacer(Modifier.height(2.dp))
                Text(v.nama ?: "-", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate900)
            }
        }

        if (foto != null) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFFFFBEB))
                        .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(6.dp))
                        .padding(horizontal = 7.dp, vertical = 2.dp),
                ) {
                    Text("Foto", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                }
                AsyncImage(
                    model = foto,
                    contentDescription = "Foto lampiran pengajuan",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp, max = 260.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A)),
                )
            }
        }

        error?.let { Text(it, fontSize = 11.sp, color = RoseFg) }

        if (detail == null && error == null) {
            Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(Modifier.size(20.dp), color = SimapTeal, strokeWidth = 2.dp)
            }
        }

        val entri = data.entries.filter { (k, nilai) ->
            !k.startsWith("_") && nilai != null && nilaiTeks(nilai).isNotBlank()
        }
        if (entri.isNotEmpty()) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, ValCardBorder, RoundedCornerShape(10.dp)),
            ) {
                entri.forEachIndexed { idx, (k, nilai) ->
                    if (idx > 0) HorizontalDivider(color = ValHair)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (idx % 2 == 0) Color.White else ValSubtle)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            labelField(k),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Slate600,
                            modifier = Modifier.weight(1f),
                        )
                        val teks = nilaiTeks(nilai)
                        when {
                            k == "slug" -> Box(
                                Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFF0FDFA))
                                    .border(1.dp, ValTealBorder, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 7.dp, vertical = 1.dp),
                            ) {
                                Text(
                                    teks,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ValTealDark,
                                    fontFamily = FontFamily.Monospace,
                                )
                            }
                            k == "pengecekan" || k == "is_pengecekan" -> PilTeks(
                                "Otomatis",
                                Color(0xFFECFDF5),
                                Color(0xFF047857),
                                Color(0xFFA7F3D0),
                            )
                            k == "latitude" || k == "longitude" -> Text(
                                teks,
                                fontSize = 11.sp,
                                color = ValSlate700,
                                fontFamily = FontFamily.Monospace,
                            )
                            k == "nama" || k == "tahun" -> Text(
                                teks,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900,
                            )
                            else -> Text(
                                teks,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate900,
                            )
                        }
                    }
                }
            }
        }

        aksiError?.let { Text(it, fontSize = 11.sp, color = RoseFg) }
    }
}

// Kotak teks sederhana (form penolakan)
@Composable
private fun KotakTeks(
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean = true,
    minHeight: Int = 44,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .heightIn(min = minHeight.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(ValSubtle)
            .border(1.dp, ValCardBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 10.dp),
    ) {
        if (value.isEmpty()) Text(placeholder, fontSize = 12.sp, color = Slate400)
        BasicTextField(
            value = value,
            onValueChange = onChange,
            singleLine = singleLine,
            textStyle = TextStyle(fontSize = 12.sp, color = Slate900),
            cursorBrush = SolidColor(SimapTeal),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
