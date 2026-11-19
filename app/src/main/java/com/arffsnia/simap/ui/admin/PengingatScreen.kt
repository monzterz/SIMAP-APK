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
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arffsnia.simap.SimapApp
import com.arffsnia.simap.data.dto.ReminderAvailableUser
import com.arffsnia.simap.data.dto.ReminderLogItem
import com.arffsnia.simap.data.dto.ReminderRecipient
import com.arffsnia.simap.data.repo.AppResult
import com.arffsnia.simap.ui.mapError
import com.arffsnia.simap.ui.theme.PlusJakarta
import com.arffsnia.simap.ui.theme.PlusJakartaTypography
import com.arffsnia.simap.ui.theme.SimapTeal
import com.arffsnia.simap.ui.theme.Slate400
import com.arffsnia.simap.ui.theme.Slate600
import com.arffsnia.simap.ui.theme.Slate900
import kotlinx.coroutines.launch

// ===== Palet lokal layar Pengingat =====
private val PgBg = Color(0xFFF1F5F9)
private val PgBorder = Color(0xFFE2E8F0)
private val PgHair = Color(0xFFF1F5F9)
private val PgSubtle = Color(0xFFF8FAFC)
private val PgSlate700 = Color(0xFF334155)
private val PgSlate500 = Color(0xFF64748B)
private val PgTeal = Color(0xFF0D9488)
private val PgTealDark = Color(0xFF0F766E)
private val PgTealSoft = Color(0xFFF0FDFA)
private val PgTealBorder = Color(0xFFBCE8EA)
private val PgAmberSoft = Color(0xFFFFFBEB)
private val PgAmberBorder = Color(0xFFFDE68A)
private val PgAmberFg = Color(0xFF92400E)
private val PgRoseSoft = Color(0xFFFFF1F2)
private val PgRoseBorder = Color(0xFFFECDD3)
private val PgRoseFg = Color(0xFFBE123C)
private val PgEmeraldSoft = Color(0xFFECFDF5)
private val PgEmeraldBorder = Color(0xFFA7F3D0)
private val PgEmeraldFg = Color(0xFF047857)

private fun waktuIndo(iso: String?): String {
    if (iso.isNullOrBlank() || iso.length < 19) return iso ?: "—"
    val bulan = arrayOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember",
    )
    val d = iso.substring(8, 10)
    val m = iso.substring(5, 7).toIntOrNull() ?: return iso
    val y = iso.substring(0, 4)
    val jam = iso.substring(11, 19)
    return "$d ${bulan.getOrNull(m - 1) ?: ""} $y - $jam"
}

@Composable
fun PengingatScreen(onNotif: (() -> Unit)? = null, onProfile: (() -> Unit)? = null) {
    val app = LocalContext.current.applicationContext as SimapApp
    val scope = rememberCoroutineScope()
    val user = remember { app.authRepo.currentUser() }

    var recipients by remember { mutableStateOf<List<ReminderRecipient>>(emptyList()) }
    var available by remember { mutableStateOf<List<ReminderAvailableUser>>(emptyList()) }
    var logs by remember { mutableStateOf<List<ReminderLogItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableStateOf(0) }
    var busy by remember { mutableStateOf(false) }
    var toast by remember { mutableStateOf<String?>(null) }

    var tab by remember { mutableStateOf("penerima") }
    var query by remember { mutableStateOf("") }
    var limit by remember { mutableStateOf(10) }
    var pagePenerima by remember { mutableStateOf(1) }
    var pageLog by remember { mutableStateOf(1) }

    var tambahId by remember { mutableStateOf(0) }
    var hapusTarget by remember { mutableStateOf<ReminderRecipient?>(null) }
    var hapusLogTarget by remember { mutableStateOf<ReminderLogItem?>(null) }
    var massalStatus by remember { mutableStateOf<String?>(null) }
    var lihatLog by remember { mutableStateOf<ReminderLogItem?>(null) }

    fun reload() {
        scope.launch {
            loading = true
            val r1 = app.adminRepo.reminderRecipients()
            val r2 = app.adminRepo.reminderLog()
            when (r1) {
                is AppResult.Success -> {
                    recipients = r1.data.recipients
                    available = r1.data.available
                    error = null
                }
                is AppResult.Error -> error = mapError(r1.message)
            }
            when (r2) {
                is AppResult.Success -> logs = r2.data
                is AppResult.Error -> if (error == null) error = mapError(r2.message)
            }
            loading = false
        }
    }

    LaunchedEffect(refreshKey) { reload() }

    val q = query.trim().lowercase()

    fun cocokPenerima(r: ReminderRecipient): Boolean =
        q.isEmpty() || listOfNotNull(r.fullName, r.email, r.level).any { it.lowercase().contains(q) }

    fun cocokLog(l: ReminderLogItem): Boolean =
        q.isEmpty() || listOfNotNull(l.penerima, l.penerimaEmail, l.status, l.channel)
            .any { it.lowercase().contains(q) }

    val penerima = recipients.filter(::cocokPenerima)
    val log = logs.filter(::cocokLog)

    val pagesPenerima = if (penerima.isEmpty()) 1 else (penerima.size + limit - 1) / limit
    val pagesLog = if (log.isEmpty()) 1 else (log.size + limit - 1) / limit
    LaunchedEffect(pagesPenerima) { if (pagePenerima > pagesPenerima) pagePenerima = pagesPenerima }
    LaunchedEffect(pagesLog) { if (pageLog > pagesLog) pageLog = pagesLog }

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
            Column(Modifier.fillMaxSize().background(PgBg).statusBarsPadding()) {
                TopBar(user = user, onNotif = onNotif, onProfile = onProfile)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp)
                        .padding(top = 12.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    BannerPengingat()
                    AlertPratinjau()

                    TabPengingat(
                        tab = tab,
                        nPenerima = recipients.size,
                        nLog = logs.size,
                        onTab = { tab = it },
                    )

                    toast?.let {
                        Text(it, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = PgTealDark)
                    }
                    error?.let { Text(it, fontSize = 11.sp, color = PgRoseFg) }

                    if (loading) {
                        Box(Modifier.fillMaxWidth().height(170.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = SimapTeal)
                        }
                    } else if (tab == "penerima") {
                        PanePenerima(
                            daftar = penerima,
                            semuaTersedia = available,
                            tambahId = tambahId,
                            onTambahId = { tambahId = it },
                            busy = busy,
                            query = query,
                            onQuery = { query = it; pagePenerima = 1 },
                            limit = limit,
                            onLimit = { limit = it; pagePenerima = 1 },
                            page = pagePenerima,
                            pages = pagesPenerima,
                            onPage = { pagePenerima = it },
                            onTambah = { id ->
                                busy = true
                                scope.launch {
                                    when (val r = app.adminRepo.reminderAdd(id)) {
                                        is AppResult.Success -> {
                                            toast = "Petugas ditambahkan sebagai penerima"
                                            tambahId = 0
                                            refreshKey++
                                        }
                                        is AppResult.Error -> toast = mapError(r.message)
                                    }
                                    busy = false
                                }
                            },
                            onHapus = { hapusTarget = it },
                        )
                    } else {
                        PaneLog(
                            daftar = log,
                            busy = busy,
                            query = query,
                            onQuery = { query = it; pageLog = 1 },
                            limit = limit,
                            onLimit = { limit = it; pageLog = 1 },
                            page = pageLog,
                            pages = pagesLog,
                            onPage = { pageLog = it },
                            onHitungUlang = {
                                busy = true
                                scope.launch {
                                    when (val r = app.adminRepo.reminderRecompute()) {
                                        is AppResult.Success -> {
                                            toast = "Pengingat dihitung ulang"
                                            refreshKey++
                                        }
                                        is AppResult.Error -> toast = mapError(r.message)
                                    }
                                    busy = false
                                }
                            },
                            onKirimSemua = {
                                busy = true
                                scope.launch {
                                    when (val r = app.adminRepo.reminderSendAll()) {
                                        is AppResult.Success -> {
                                            val d = r.data
                                            toast = "Kirim: ${d.sent ?: 0} terkirim, ${d.failed ?: 0} gagal, " +
                                                "${d.skipped ?: 0} dilewati" +
                                                if (d.message == "smtp_not_configured") " (SMTP belum diset)" else ""
                                            refreshKey++
                                        }
                                        is AppResult.Error -> toast = mapError(r.message)
                                    }
                                    busy = false
                                }
                            },
                            onHapusMassal = { massalStatus = it },
                            onLihat = { lihatLog = it },
                            onKirimUlang = { l ->
                                busy = true
                                scope.launch {
                                    when (val r = app.adminRepo.reminderSendOne(l.id)) {
                                        is AppResult.Success -> {
                                            toast = "Kirim ulang: ${r.data.sent ?: 0} terkirim, ${r.data.failed ?: 0} gagal"
                                            refreshKey++
                                        }
                                        is AppResult.Error -> toast = mapError(r.message)
                                    }
                                    busy = false
                                }
                            },
                            onHapus = { hapusLogTarget = it },
                        )
                    }
                }
            }

            // ===== Dialog hapus penerima =====
            hapusTarget?.let { r ->
                KonfirmasiDialog(
                    judul = "Hapus penerima?",
                    isi = "${r.fullName ?: "-"} tidak akan menerima email pengingat lagi.",
                    label = "Hapus",
                    warna = PgRoseFg,
                    onDismiss = { hapusTarget = null },
                    onConfirm = {
                        hapusTarget = null
                        busy = true
                        scope.launch {
                            when (val res = app.adminRepo.reminderRemove(r.userId)) {
                                is AppResult.Success -> { toast = "Penerima dihapus"; refreshKey++ }
                                is AppResult.Error -> toast = mapError(res.message)
                            }
                            busy = false
                        }
                    },
                )
            }

            // ===== Dialog hapus log satuan =====
            hapusLogTarget?.let { l ->
                KonfirmasiDialog(
                    judul = "Hapus entri log?",
                    isi = "Log untuk ${l.penerima ?: "-"} (${l.createdAt ?: "-"}) akan dihapus permanen.",
                    label = "Hapus",
                    warna = PgRoseFg,
                    onDismiss = { hapusLogTarget = null },
                    onConfirm = {
                        hapusLogTarget = null
                        busy = true
                        scope.launch {
                            when (val res = app.adminRepo.reminderDelete(id = l.id)) {
                                is AppResult.Success -> { toast = "Log dihapus"; refreshKey++ }
                                is AppResult.Error -> toast = mapError(res.message)
                            }
                            busy = false
                        }
                    },
                )
            }

            // ===== Dialog hapus massal =====
            massalStatus?.let { st ->
                KonfirmasiDialog(
                    judul = "Hapus log ${if (st == "sent") "terkirim" else "pending"}?",
                    isi = "Semua entri berstatus ${if (st == "sent") "sent" else "pending"} akan dihapus. " +
                        "Entri bisa dibuat lagi lewat Hitung Ulang.",
                    label = "Hapus semua",
                    warna = PgRoseFg,
                    onDismiss = { massalStatus = null },
                    onConfirm = {
                        massalStatus = null
                        busy = true
                        scope.launch {
                            when (val res = app.adminRepo.reminderDelete(status = st)) {
                                is AppResult.Success -> { toast = "Log dihapus"; refreshKey++ }
                                is AppResult.Error -> toast = mapError(res.message)
                            }
                            busy = false
                        }
                    },
                )
            }

            lihatLog?.let { l -> SheetLog(l, onDismiss = { lihatLog = null }) }
        }
    }
}

@Composable
private fun BannerPengingat() {
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
                Text(" / ", fontSize = 11.sp, color = Color(0xFFCCFBF1).copy(alpha = 0.7f))
                Text("Pratinjau Pengingat", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                Spacer(Modifier.weight(1f))
                Box(
                    Modifier.clip(RoundedCornerShape(999.dp))
                        .background(Color.White.copy(alpha = 0.20f))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    Text("Tahap Pratinjau", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(34.dp).clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.Notifications, null, Modifier.size(18.dp), tint = Color.White)
                }
                Spacer(Modifier.width(9.dp))
                Column {
                    Text("Pratinjau Pengingat", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(
                        "Daftar pengingat yang akan dikirim ke petugas. Tahap ini hanya pratinjau, " +
                            "belum ada pesan yang benar-benar dikirim.",
                        fontSize = 10.sp,
                        color = Color(0xFFF0FDFA),
                    )
                }
            }
        }
    }
}

@Composable
private fun AlertPratinjau() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PgAmberSoft)
            .border(1.dp, PgAmberBorder, RoundedCornerShape(12.dp))
            .padding(12.dp),
    ) {
        Column {
            Text("Mode pratinjau (Tahap 1).", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF78350F))
            Spacer(Modifier.height(2.dp))
            Text(
                "Pengiriman hanya berjalan bila SMTP sudah dikonfigurasi di web. Di luar itu, halaman ini " +
                    "menampilkan ke siapa pengingat ditujukan dan isi pesannya.",
                fontSize = 10.sp,
                color = PgAmberFg,
            )
        }
    }
}

@Composable
private fun TabPengingat(tab: String, nPenerima: Int, nLog: Int, onTab: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFE2E8F0))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        TabPgItem("Penerima", nPenerima, tab == "penerima", Modifier.weight(1f)) { onTab("penerima") }
        TabPgItem("Entri Log", nLog, tab == "log", Modifier.weight(1f)) { onTab("log") }
    }
}

@Composable
private fun TabPgItem(label: String, jumlah: Int, aktif: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(if (aktif) PgTealDark else Color.Transparent)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (aktif) Color.White else PgSlate700,
        )
        Spacer(Modifier.width(5.dp))
        Box(
            Modifier.clip(RoundedCornerShape(999.dp))
                .background(if (aktif) Color.White.copy(alpha = 0.25f) else Color(0xFFCBD5E1))
                .padding(horizontal = 5.dp, vertical = 1.dp),
        ) {
            Text(
                "$jumlah",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = if (aktif) Color.White else PgSlate700,
            )
        }
    }
}

@Composable
private fun PanePenerima(
    daftar: List<ReminderRecipient>,
    semuaTersedia: List<ReminderAvailableUser>,
    tambahId: Int,
    onTambahId: (Int) -> Unit,
    busy: Boolean,
    query: String,
    onQuery: (String) -> Unit,
    limit: Int,
    onLimit: (Int) -> Unit,
    page: Int,
    pages: Int,
    onPage: (Int) -> Unit,
    onTambah: (Int) -> Unit,
    onHapus: (ReminderRecipient) -> Unit,
) {
    var bukaPilih by remember { mutableStateOf(false) }
    val terpilih = semuaTersedia.firstOrNull { it.id == tambahId }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, PgBorder, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Kelola Penerima Pengingat", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900,
                modifier = Modifier.weight(1f))
            Box(
                Modifier.clip(RoundedCornerShape(999.dp)).background(Color(0xFFF1F5F9))
                    .padding(horizontal = 7.dp, vertical = 1.dp),
            ) {
                Text("${daftar.size} Petugas", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = PgSlate500)
            }
        }

        Column {
            Text("TAMBAH PETUGAS PENERIMA", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Slate400)
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PgSubtle)
                            .border(1.dp, PgBorder, RoundedCornerShape(10.dp))
                            .clickable { bukaPilih = true }
                            .padding(horizontal = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            terpilih?.let { "${it.fullName} (${it.level ?: "-"})" }
                                ?: "Pilih user untuk ditambahkan —",
                            fontSize = 10.sp,
                            color = if (terpilih == null) Slate400 else Slate900,
                            maxLines = 1,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    DropdownMenu(expanded = bukaPilih, onDismissRequest = { bukaPilih = false }) {
                        semuaTersedia.take(40).forEach { u ->
                            DropdownMenuItem(
                                text = { Text("${u.fullName} (${u.level ?: "-"})", fontSize = 12.sp) },
                                onClick = { onTambahId(u.id); bukaPilih = false },
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .height(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (tambahId == 0 || busy) Color(0xFF94A3B8) else PgTealDark)
                        .clickable(enabled = tambahId != 0 && !busy) { onTambah(tambahId) }
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Tambah", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        HorizontalDivider(color = PgHair)
        BarCariPengingat(query, onQuery, limit, onLimit, "Cari penerima...")

        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, PgBorder, RoundedCornerShape(12.dp)),
        ) {
            val potong = daftar.drop((page - 1) * limit).take(limit)
            if (potong.isEmpty()) {
                Text("Tidak ada penerima.", fontSize = 11.sp, color = Slate400,
                    modifier = Modifier.padding(12.dp))
            }
            potong.forEachIndexed { i, r ->
                if (i > 0) HorizontalDivider(color = PgHair)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(r.fullName ?: "-", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                color = Slate900, maxLines = 1)
                            Spacer(Modifier.width(5.dp))
                            val superAdmin = r.level?.contains("Super", true) == true
                            Box(
                                Modifier.clip(RoundedCornerShape(4.dp))
                                    .background(if (superAdmin) Color(0xFFEEF2FF) else Color(0xFFF1F5F9))
                                    .border(1.dp, if (superAdmin) Color(0xFFC7D2FE) else PgBorder, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 5.dp, vertical = 1.dp),
                            ) {
                                Text(
                                    r.level ?: "-",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (superAdmin) Color(0xFF3730A3) else PgSlate700,
                                )
                            }
                        }
                        Text(r.email ?: "—", fontSize = 9.sp, color = PgSlate500, maxLines = 1)
                    }
                    IconButton(
                        onClick = { onHapus(r) },
                        enabled = !busy,
                        modifier = Modifier.size(30.dp),
                    ) {
                        Icon(Icons.Rounded.Delete, "Hapus", Modifier.size(15.dp), tint = PgRoseFg)
                    }
                }
            }
        }

        PagerPengingat(
            dari = if (daftar.isEmpty()) 0 else (page - 1) * limit + 1,
            sampai = minOf(page * limit, daftar.size),
            total = daftar.size,
            page = page,
            pages = pages,
            onPage = onPage,
        )
    }
}

@Composable
private fun PaneLog(
    daftar: List<ReminderLogItem>,
    busy: Boolean,
    query: String,
    onQuery: (String) -> Unit,
    limit: Int,
    onLimit: (Int) -> Unit,
    page: Int,
    pages: Int,
    onPage: (Int) -> Unit,
    onHitungUlang: () -> Unit,
    onKirimSemua: () -> Unit,
    onHapusMassal: (String) -> Unit,
    onLihat: (ReminderLogItem) -> Unit,
    onKirimUlang: (ReminderLogItem) -> Unit,
    onHapus: (ReminderLogItem) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(1.dp, PgBorder, RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("${daftar.size} Entri Pengingat Terbaru", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
            Text("Status antrean dan log pengiriman terkini", fontSize = 10.sp, color = Slate400)

            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                AksiChip("Hitung Ulang", PgTeal, Color.White, busy) { onHitungUlang() }
                AksiChip("Kirim ke Semua Pending", PgTealDark, Color.White, busy) { onKirimSemua() }
                AksiChip("Hapus Terkirim", PgRoseSoft, PgRoseFg, busy) { onHapusMassal("sent") }
                AksiChip("Hapus Pending", PgRoseSoft, PgRoseFg, busy) { onHapusMassal("pending") }
            }

            BarCariPengingat(query, onQuery, limit, onLimit, "Cari entri pengingat...")
        }

        val potong = daftar.drop((page - 1) * limit).take(limit)
        if (potong.isEmpty()) {
            Text("Belum ada entri log.", fontSize = 11.sp, color = Slate400,
                modifier = Modifier.padding(vertical = 12.dp))
        }
        potong.forEach { l ->
            KartuLog(l, busy, onLihat = { onLihat(l) }, onKirimUlang = { onKirimUlang(l) }, onHapus = { onHapus(l) })
        }

        PagerPengingat(
            dari = if (daftar.isEmpty()) 0 else (page - 1) * limit + 1,
            sampai = minOf(page * limit, daftar.size),
            total = daftar.size,
            page = page,
            pages = pages,
            onPage = onPage,
        )
    }
}

@Composable
private fun AksiChip(label: String, bg: Color, fg: Color, busy: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .height(32.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(bg)
            .border(1.dp, if (bg == PgRoseSoft) PgRoseBorder else bg, RoundedCornerShape(9.dp))
            .clickable(enabled = !busy, onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = fg)
    }
}

@Composable
private fun KartuLog(
    l: ReminderLogItem,
    busy: Boolean,
    onLihat: () -> Unit,
    onKirimUlang: () -> Unit,
    onHapus: () -> Unit,
) {
    val status = l.status?.lowercase() ?: "-"
    val (bg, fg, border) = when (status) {
        "sent" -> Triple(PgEmeraldSoft, PgEmeraldFg, PgEmeraldBorder)
        "pending" -> Triple(PgAmberSoft, PgAmberFg, PgAmberBorder)
        else -> Triple(PgRoseSoft, PgRoseFg, PgRoseBorder)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(PgSubtle)
            .border(1.dp, PgBorder, RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("WAKTU DIBUAT", fontSize = 8.sp, fontWeight = FontWeight.Medium, color = Slate400)
                Text(waktuIndo(l.createdAt), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = PgSlate700)
            }
            Box(
                Modifier.clip(RoundedCornerShape(5.dp)).background(bg)
                    .border(1.dp, border, RoundedCornerShape(5.dp))
                    .padding(horizontal = 7.dp, vertical = 2.dp),
            ) {
                Text(status.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = fg)
            }
        }

        HorizontalDivider(color = PgBorder)

        BarisLog("Penerima:", l.penerima ?: "—", bold = true)
        BarisLog("Email:", l.penerimaEmail ?: "—")
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Overdue:", fontSize = 10.sp, color = Slate400)
            Spacer(Modifier.width(5.dp))
            Box(
                Modifier.clip(RoundedCornerShape(5.dp)).background(PgAmberSoft)
                    .border(1.dp, PgAmberBorder, RoundedCornerShape(5.dp))
                    .padding(horizontal = 6.dp, vertical = 1.dp),
            ) {
                Text("${l.overdueCount ?: 0}", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = PgAmberFg)
            }
            Spacer(Modifier.weight(1f))
            Text("via ${l.channel ?: "email"}", fontSize = 9.sp, color = PgSlate500)
        }
        l.error?.takeIf { it.isNotBlank() }?.let {
            Text("Error: $it", fontSize = 9.sp, color = PgRoseFg)
        }

        HorizontalDivider(color = PgBorder)

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier
                    .height(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .border(1.dp, PgBorder, RoundedCornerShape(8.dp))
                    .clickable(onClick = onLihat)
                    .padding(horizontal = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.Visibility, null, Modifier.size(12.dp), tint = PgSlate500)
                Spacer(Modifier.width(4.dp))
                Text("Lihat", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = PgSlate700)
            }
            Spacer(Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .height(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(PgTealDark)
                    .clickable(enabled = !busy, onClick = onKirimUlang)
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Kirim Ulang", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(PgRoseSoft)
                    .border(1.dp, PgRoseBorder, RoundedCornerShape(8.dp))
                    .clickable(enabled = !busy, onClick = onHapus),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Delete, "Hapus", Modifier.size(13.dp), tint = PgRoseFg)
            }
        }
    }
}

@Composable
private fun BarisLog(label: String, value: String, bold: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 10.sp, color = Slate400, modifier = Modifier.weight(1f))
        Text(
            value,
            fontSize = 10.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
            color = if (bold) Slate900 else PgSlate700,
            maxLines = 1,
        )
    }
}

@Composable
private fun BarCariPengingat(
    query: String,
    onQuery: (String) -> Unit,
    limit: Int,
    onLimit: (Int) -> Unit,
    placeholder: String,
) {
    var buka by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(PgSubtle)
                .border(1.dp, PgBorder, RoundedCornerShape(10.dp))
                .padding(horizontal = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.Search, null, Modifier.size(13.dp), tint = Slate400)
            Spacer(Modifier.width(6.dp))
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (query.isEmpty()) Text(placeholder, fontSize = 10.sp, color = Slate400)
                BasicTextField(
                    value = query,
                    onValueChange = onQuery,
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
                    .height(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PgSubtle)
                    .border(1.dp, PgBorder, RoundedCornerShape(10.dp))
                    .clickable { buka = true }
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("$limit baris", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = PgSlate700)
            }
            DropdownMenu(expanded = buka, onDismissRequest = { buka = false }) {
                listOf(10, 25, 50).forEach { n ->
                    DropdownMenuItem(
                        text = { Text("$n baris", fontSize = 12.sp) },
                        onClick = { onLimit(n); buka = false },
                    )
                }
            }
        }
    }
}

@Composable
private fun PagerPengingat(
    dari: Int,
    sampai: Int,
    total: Int,
    page: Int,
    pages: Int,
    onPage: (Int) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(
            if (total == 0) "Tidak ada data" else "Menampilkan $dari–$sampai dari $total",
            fontSize = 10.sp,
            color = PgSlate500,
            modifier = Modifier.weight(1f),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            TombolPg("<", page > 1) { onPage(page - 1) }
            Box(
                Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(PgTealDark),
                contentAlignment = Alignment.Center,
            ) {
                Text("$page", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Text("/ $pages", fontSize = 9.sp, color = Slate400)
            TombolPg(">", page < pages) { onPage(page + 1) }
        }
    }
}

@Composable
private fun TombolPg(label: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .border(1.dp, PgBorder, RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (enabled) PgSlate700 else Slate400)
    }
}

@Composable
private fun KonfirmasiDialog(
    judul: String,
    isi: String,
    label: String,
    warna: Color,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(judul, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = { Text(isi, fontSize = 13.sp, color = PgSlate500) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(label, color = warna, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal", color = PgSlate500) }
        },
    )
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun SheetLog(l: ReminderLogItem, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = Color.White) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Log Pengingat #${l.id}", fontSize = 15.sp, fontWeight = FontWeight.Bold,
                    color = Slate900, modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, "Tutup", Modifier.size(18.dp), tint = Slate400)
                }
            }
            BarisLog("Penerima:", l.penerima ?: "—", bold = true)
            BarisLog("Email:", l.penerimaEmail ?: "—")
            BarisLog("Status:", (l.status ?: "-").uppercase(), bold = true)
            BarisLog("Saluran:", l.channel ?: "email")
            BarisLog("Dibuat:", l.createdAt ?: "—")
            BarisLog("Terkirim:", l.sentAt ?: "—")
            BarisLog("Overdue:", "${l.overdueCount ?: 0}")
            l.error?.takeIf { it.isNotBlank() }?.let {
                BarisLog("Error:", it)
            }
            Spacer(Modifier.height(4.dp))
            Text("Isi Pesan", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate900)
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(PgSubtle)
                    .border(1.dp, PgBorder, RoundedCornerShape(10.dp))
                    .padding(10.dp)
                    .heightIn(min = 80.dp),
            ) {
                Text(
                    (l.payload ?: "—").replace(Regex("<[^>]+>"), " ").replace(Regex("\\s+"), " ").trim(),
                    fontSize = 10.sp,
                    color = PgSlate700,
                )
            }
        }
    }
}
