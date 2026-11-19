package com.arffsnia.simap.ui.admin

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Assignment
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.arffsnia.simap.BuildConfig
import com.arffsnia.simap.R
import com.arffsnia.simap.SimapApp
import com.arffsnia.simap.data.dto.Penugasan
import com.arffsnia.simap.data.dto.PenugasanAset
import com.arffsnia.simap.data.dto.PenugasanAssetsResponse
import com.arffsnia.simap.data.dto.PenugasanDetail
import com.arffsnia.simap.data.dto.PenugasanKategori
import com.arffsnia.simap.data.dto.PenugasanMinggu
import com.arffsnia.simap.data.dto.User
import com.arffsnia.simap.data.repo.AppResult
import com.arffsnia.simap.ui.Role
import com.arffsnia.simap.ui.mapError
import com.arffsnia.simap.ui.theme.AmberBg
import com.arffsnia.simap.ui.theme.AmberBorder
import com.arffsnia.simap.ui.theme.AmberFg
import com.arffsnia.simap.ui.theme.EmeraldBg
import com.arffsnia.simap.ui.theme.EmeraldFg
import com.arffsnia.simap.ui.theme.PlusJakarta
import com.arffsnia.simap.ui.theme.PlusJakartaTypography
import com.arffsnia.simap.ui.theme.RoseBg
import com.arffsnia.simap.ui.theme.RoseBorder
import com.arffsnia.simap.ui.theme.RoseFg
import com.arffsnia.simap.ui.theme.SimapBg
import com.arffsnia.simap.ui.theme.SimapBrandEnd
import com.arffsnia.simap.ui.theme.SimapPillStart
import com.arffsnia.simap.ui.theme.SimapTeal
import com.arffsnia.simap.ui.theme.SimapTealChipBg
import com.arffsnia.simap.ui.theme.SimapTealChipFg
import com.arffsnia.simap.ui.theme.SimapTealDark
import com.arffsnia.simap.ui.theme.Slate200
import com.arffsnia.simap.ui.theme.Slate400
import com.arffsnia.simap.ui.theme.Slate50
import com.arffsnia.simap.ui.theme.Slate600
import com.arffsnia.simap.ui.theme.Slate900
import com.arffsnia.simap.data.dto.PenugasanOptions
import com.arffsnia.simap.data.dto.SimpleItem
import com.arffsnia.simap.data.dto.ZonasiOpt
import com.arffsnia.simap.data.dto.PenugasanSaveRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

// ===== Palet lokal (turunan Tailwind yang dipakai desain) =====
private val CardBorder = Color(0xFFE2E8F0)
private val HairLine = Color(0xFFF1F5F9)
private val SubtleBg = Color(0xFFF8FAFC)
private val Slate700 = Color(0xFF334155)
private val Slate500 = Color(0xFF64748B)
private val TealBarStart = Color(0xFF2DD4BF)
private val TealBarEnd = Color(0xFF50B2B6)
private val HeroStart = Color(0xFF0D9488)
private val HeroEnd = Color(0xFF06B6D4)
private val PillInner = Color(0x26000000)
private val StatTeal = Color(0xFF56B8BD)     // bg kartu statistik
private val SectionBar = Color(0xFF52B7BC)   // bar judul "Daftar Penugasan"

// ===== Util format =====
private val NAMA_HARI = arrayOf("Minggu", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu")
private val NAMA_BULAN = arrayOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des")
private val ZONA_WITA: TimeZone = TimeZone.getTimeZone("Asia/Makassar")

private fun calendarku(): Calendar = Calendar.getInstance(ZONA_WITA)

private fun labelTanggal(c: Calendar): String =
    "${NAMA_HARI[c.get(Calendar.DAY_OF_WEEK) - 1]}, ${c.get(Calendar.DAY_OF_MONTH)} " +
        "${NAMA_BULAN[c.get(Calendar.MONTH)]} ${c.get(Calendar.YEAR)}"

private fun labelJam(c: Calendar): String = String.format(
    Locale.US, "%02d:%02d:%02d",
    c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND),
)

/** "2026-11-18" -> "18/11/2026" (gaya desain: dd/MM/yyyy). */
private fun tanggalPenuh(iso: String?): String {
    if (iso.isNullOrBlank() || iso.length < 10) return "-"
    return "${iso.substring(8, 10)}/${iso.substring(5, 7)}/${iso.substring(0, 4)}"
}

private fun isAktif(status: String?, isActive: Boolean?): Boolean =
    isActive == true || status?.trim()?.equals("aktif", ignoreCase = true) == true

private fun isSelesai(status: String?): Boolean =
    status?.trim()?.equals("selesai", ignoreCase = true) == true

private fun labelStatus(status: String?): String = when {
    isSelesai(status) -> "Selesai"
    status?.trim()?.equals("aktif", ignoreCase = true) == true -> "Berjalan"
    status.isNullOrBlank() -> "-"
    else -> status
}

private fun inisial(nama: String?): String =
    nama?.trim()?.split(" ")?.filter { it.isNotBlank() }?.take(2)
        ?.map { it.first().uppercaseChar() }?.joinToString("")
        ?.ifBlank { "AR" } ?: "AR"

// ===== Layar =====

@Composable
fun PenugasanScreen(
    onOpen: (Int) -> Unit,
    onNotif: (() -> Unit)? = null,
    onProfile: (() -> Unit)? = null,
) {
    val ctx = LocalContext.current
    val app = ctx.applicationContext as SimapApp
    val user = remember { app.authRepo.currentUser() }

    var items by remember { mutableStateOf<List<Penugasan>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableStateOf(0) }
    var query by remember { mutableStateOf("") }
    var pageSize by remember { mutableStateOf(10) }
    var page by remember { mutableStateOf(1) }
    var notice by remember { mutableStateOf<Notice?>(null) }
    var loadedAt by remember { mutableStateOf<String?>(null) }
    var form by remember { mutableStateOf<FormSeed?>(null) }
    var hapus by remember { mutableStateOf<Penugasan?>(null) }

    LaunchedEffect(refreshKey) {
        loading = true
        when (val r = app.adminRepo.penugasan()) {
            is AppResult.Success -> {
                items = r.data
                error = null
                loadedAt = labelJam(calendarku())
            }
            is AppResult.Error -> error = mapError(r.message)
        }
        loading = false
    }

    val filtered = remember(items, query) {
        val q = query.trim().lowercase(Locale.getDefault())
        if (q.isEmpty()) items
        else items.filter {
            it.nama.lowercase(Locale.getDefault()).contains(q) ||
                (it.regu ?: "").lowercase(Locale.getDefault()).contains(q)
        }
    }
    val totalPages = if (filtered.isEmpty()) 1 else (filtered.size + pageSize - 1) / pageSize
    LaunchedEffect(totalPages) { if (page > totalPages) page = totalPages }
    val pageItems = filtered.drop((page - 1) * pageSize).take(pageSize)
    val dari = if (filtered.isEmpty()) 0 else (page - 1) * pageSize + 1
    val sampai = minOf(page * pageSize, filtered.size)

    val selesai = items.count { isSelesai(it.status) }
    val berjalan = items.count { isAktif(it.status, it.isActive) }

    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme,
        typography = PlusJakartaTypography,
    ) {
        // Web memakai line-height rapat; matikan font padding agar tinggi teks
        // di aplikasi setara dengan desain (pill/tanggal tidak terlihat gemuk).
        CompositionLocalProvider(
            LocalTextStyle provides TextStyle(
                fontFamily = PlusJakarta,
                fontSize = 12.sp,
                platformStyle = PlatformTextStyle(includeFontPadding = false),
            ),
        ) {
        Box(Modifier.fillMaxSize().background(SimapBg)) {
            Column(Modifier.fillMaxSize().statusBarsPadding()) {
                TopBar(user = user, onNotif = onNotif, onProfile = onProfile)

                if (loading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = SimapTeal)
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                            .padding(top = 12.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        HeroBanner()

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatKartu(
                                Icons.Rounded.Assignment, "Total Tugas",
                                items.size.toString(), "Penugasan", Modifier.weight(1f),
                            )
                            StatKartu(
                                Icons.Rounded.PlayCircle, "Berjalan",
                                berjalan.toString(), "Aktif sekarang", Modifier.weight(1f),
                            )
                            StatKartu(
                                Icons.Rounded.CheckCircle, "Selesai",
                                selesai.toString(), "Tuntas / Arsip", Modifier.weight(1f),
                            )
                        }

                        DaftarPenugasanKartu(
                            query = query,
                            onQuery = { query = it; page = 1 },
                            pageSize = pageSize,
                            onPageSize = { pageSize = it; page = 1 },
                            items = pageItems,
                            error = error,
                            onRetry = { refreshKey++ },
                            onOpen = onOpen,
                            onEdit = { p -> form = FormSeed(p) },
                            onHapus = { p -> hapus = p },
                            onTambah = { form = FormSeed(null) },
                            dari = dari,
                            sampai = sampai,
                            total = filtered.size,
                            page = page,
                            totalPages = totalPages,
                            onPrev = { if (page > 1) page-- },
                            onNext = { if (page < totalPages) page++ },
                        )

                        FooterKartu(
                            total = filtered.size,
                            terakhirMuat = loadedAt,
                        )
                    }
                }
            }
        }

        notice?.let { n ->
            AlertDialog(
                onDismissRequest = { notice = null },
                title = { Text(n.judul, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                text = { Text(n.isi, fontSize = 13.sp, color = Slate600) },
                confirmButton = {
                    TextButton(onClick = { notice = null }) {
                        Text("Mengerti", color = SimapTeal, fontWeight = FontWeight.Bold)
                    }
                },
            )
        }

        form?.let { seed ->
            PenugasanFormSheet(
                seed = seed,
                onDismiss = { form = null },
                onSaved = { form = null; refreshKey++ },
            )
        }

        hapus?.let { p ->
            HapusPenugasanDialog(
                penugasan = p,
                onDismiss = { hapus = null },
                onDeleted = { hapus = null; refreshKey++ },
            )
        }
        }
    }
}

private data class Notice(val judul: String, val isi: String)

// ===== Top bar + pill jam =====

@Composable
internal fun TopBar(
    user: User?,
    onNotif: (() -> Unit)? = null,
    onProfile: (() -> Unit)? = null,
) {
    var infoGelap by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth().background(Color.White)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 10.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            androidx.compose.foundation.Image(
                painter = painterResource(R.drawable.ic_logo_pinwheel),
                contentDescription = "Logo SIMAP",
                modifier = Modifier.size(26.dp),
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Row {
                    Text("SIM", fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = Slate900)
                    Text("AP", fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = SimapBrandEnd)
                }
                Text(
                    "INJOURNEY AIRPORTS",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate400,
                    letterSpacing = 0.8.sp,
                )
            }

            Spacer(Modifier.weight(1f))

            IconButton(onClick = { infoGelap = true }, modifier = Modifier.size(34.dp)) {
                Icon(Icons.Rounded.DarkMode, "Mode gelap", Modifier.size(17.dp), tint = Slate500)
            }
            Box {
                IconButton(
                    onClick = { onNotif?.invoke() },
                    modifier = Modifier.size(34.dp),
                    enabled = onNotif != null,
                ) {
                    Icon(Icons.Rounded.Notifications, "Notifikasi", Modifier.size(17.dp), tint = Slate600)
                }
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 7.dp, end = 7.dp)
                        .size(7.dp)
                        .background(Color(0xFFEF4444), CircleShape)
                        .border(1.5.dp, Color.White, CircleShape),
                )
            }
            Spacer(Modifier.width(4.dp))
            Box(Modifier.clip(CircleShape).clickable(enabled = onProfile != null) { onProfile?.invoke() }) {
                Avatar(user)
            }
        }

        LiveClockPill()
        HorizontalDivider(color = HairLine)
    }

    if (infoGelap) {
        AlertDialog(
            onDismissRequest = { infoGelap = false },
            title = { Text("Mode gelap belum tersedia", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Text(
                    "Tampilan SIMAP untuk saat ini dikunci ke mode terang. " +
                        "Tombol ini hanya tampil mengikuti desain.",
                    fontSize = 13.sp,
                    color = Slate600,
                )
            },
            confirmButton = {
                TextButton(onClick = { infoGelap = false }) {
                    Text("Mengerti", color = SimapTeal, fontWeight = FontWeight.Bold)
                }
            },
        )
    }
}

@Composable
private fun Avatar(user: User?) {
    val url = user?.profileUrl
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(Slate200)
            .border(2.dp, SimapTeal.copy(alpha = 0.45f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (!url.isNullOrBlank()) {
            AsyncImage(model = url, contentDescription = "Foto pengguna", modifier = Modifier.fillMaxSize())
        } else {
            Text(
                inisial(user?.fullName ?: user?.username),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SimapTealChipFg,
            )
        }
    }
}

@Composable
private fun LiveClockPill() {
    var now by remember { mutableStateOf(calendarku()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = calendarku()
            delay(1000L)
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 10.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(listOf(SimapPillStart, SimapPillStart)),
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Rounded.DateRange,
            null,
            Modifier.size(13.dp),
            tint = Color(0xFFCCFBF1),
        )
        Spacer(Modifier.width(6.dp))
        Text(labelTanggal(now), fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color.White)
        Spacer(Modifier.weight(1f))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(PillInner)
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                labelJam(now),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            )
            Spacer(Modifier.width(4.dp))
            Box(
                Modifier
                    .background(Color.White, RoundedCornerShape(2.dp))
                    .padding(horizontal = 3.dp),
            ) {
                Text("WITA", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = SimapPillStart)
            }
        }
    }
}

// ===== Hero + statistik =====

@Composable
private fun HeroBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(HeroStart, HeroEnd))),
    ) {
        androidx.compose.foundation.Image(
            painter = painterResource(R.drawable.grf),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(128.dp)
                .alpha(0.2f),
        )
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Home", fontSize = 11.sp, color = Color(0xFFCCFBF1))
                Text(" / ", fontSize = 11.sp, color = Color(0xFFCCFBF1).copy(alpha = 0.6f))
                Text(
                    "Penugasan Pengecekan",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "Penugasan Pengecekan",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "Monitoring penugasan checklist inspeksi fasilitas ARFF bandara",
                fontSize = 11.sp,
                color = Color(0xFFF0FDFA),
                modifier = Modifier.fillMaxWidth(0.78f),
            )
        }
    }
}

@Composable
private fun StatKartu(icon: ImageVector, label: String, nilai: String, caption: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(StatTeal)
            .padding(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, Modifier.size(12.dp), tint = Color(0xFFF0FDFA))
            Spacer(Modifier.width(4.dp))
            Text(
                label,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFF0FDFA),
                maxLines = 1,
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(nilai, fontSize = 23.sp, fontWeight = FontWeight.Black, color = Color.White)
        Text(caption, fontSize = 9.sp, fontWeight = FontWeight.Medium, color = Color(0xFFCCFBF1))
    }
}

// ===== Kartu daftar =====

@Composable
private fun DaftarPenugasanKartu(
    query: String,
    onQuery: (String) -> Unit,
    pageSize: Int,
    onPageSize: (Int) -> Unit,
    items: List<Penugasan>,
    error: String?,
    onRetry: () -> Unit,
    onOpen: (Int) -> Unit,
    onEdit: (Penugasan) -> Unit,
    onHapus: (Penugasan) -> Unit,
    onTambah: () -> Unit,
    dari: Int,
    sampai: Int,
    total: Int,
    page: Int,
    totalPages: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, HairLine, RoundedCornerShape(16.dp)),
    ) {
        // Bar judul + tombol tambah
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SectionBar)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.Assignment, null, Modifier.size(18.dp), tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text("Daftar Penugasan", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(SimapPillStart)
                    .border(1.dp, Color(0xFF14B8A6), RoundedCornerShape(8.dp))
                    .clickable(onClick = onTambah)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.Add, null, Modifier.size(14.dp), tint = Color.White)
                Spacer(Modifier.width(4.dp))
                Text("Tambah", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FilterRow(
                query = query,
                onQuery = onQuery,
                pageSize = pageSize,
                onPageSize = onPageSize,
            )

            when {
                error != null && items.isEmpty() -> Box(
                    Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(error, fontSize = 12.sp, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Coba lagi",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SimapTeal,
                            modifier = Modifier.clickable(onClick = onRetry).padding(6.dp),
                        )
                    }
                }

                items.isEmpty() -> Box(
                    Modifier.fillMaxWidth().padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (query.isBlank()) "Belum ada penugasan." else "Tidak ada penugasan yang cocok.",
                        fontSize = 12.sp,
                        color = Slate400,
                    )
                }

                else -> items.forEach { p -> TaskCard(p, onOpen, onEdit, onHapus) }
            }

            PaginationRow(dari, sampai, total, page, totalPages, onPrev, onNext)
        }
    }
}

@Composable
private fun FilterRow(query: String, onQuery: (String) -> Unit, pageSize: Int, onPageSize: (Int) -> Unit) {
    var open by remember { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Tampil:", fontSize = 11.sp, color = Slate500)
        Spacer(Modifier.width(6.dp))
        Box {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(SubtleBg)
                    .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                    .clickable { open = true }
                    .padding(start = 8.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("$pageSize", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                Icon(Icons.Rounded.ChevronRight, null, Modifier.size(12.dp).alpha(0.5f), tint = Slate500)
            }
            DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                listOf(10, 25, 50).forEach { n ->
                    DropdownMenuItem(
                        text = { Text("$n", fontSize = 12.sp) },
                        onClick = { onPageSize(n); open = false },
                    )
                }
            }
        }

        Spacer(Modifier.width(8.dp))

        Row(
            modifier = Modifier
                .weight(1f)
                .height(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SubtleBg)
                .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.Search, null, Modifier.size(14.dp), tint = Slate400)
            Spacer(Modifier.width(6.dp))
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (query.isEmpty()) {
                    Text("Cari penugasan...", fontSize = 11.sp, color = Slate400)
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
    }
}

@Composable
private fun TaskCard(
    p: Penugasan,
    onOpen: (Int) -> Unit,
    onEdit: (Penugasan) -> Unit,
    onHapus: (Penugasan) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                p.nama,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900,
                maxLines = 2,
                modifier = Modifier.weight(1f, fill = false),
            )
            Spacer(Modifier.width(6.dp))
            Box(
                Modifier
                    .background(Color(0xFFF1F5F9), RoundedCornerShape(4.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(4.dp))
                    .padding(horizontal = 5.dp, vertical = 1.dp),
            ) {
                Text("#${p.id}", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Slate600)
            }
        }

        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            ReguBadge(p.regu)
            if (!p.regu.isNullOrBlank()) Unit
            StatusPill(p.status, p.isActive)
        }

        Spacer(Modifier.height(10.dp))
        HorizontalDivider(color = HairLine)
        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(SubtleBg)
                .border(1.dp, HairLine, RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.DateRange, null, Modifier.size(14.dp), tint = Slate400)
            Spacer(Modifier.width(6.dp))
            Text(
                "${tanggalPenuh(p.tanggalMulai)} s.d ${tanggalPenuh(p.tanggalSelesai)}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Slate700,
            )
        }

        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Place, null, Modifier.size(14.dp), tint = Slate400)
                Spacer(Modifier.width(5.dp))
                Text("Zonasi: ", fontSize = 11.sp, color = Slate500)
                Text("${p.nZonasi ?: 0} zona", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
            }
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Widgets, null, Modifier.size(14.dp), tint = Slate400)
                Spacer(Modifier.width(5.dp))
                Text("Total Aset: ", fontSize = 11.sp, color = Slate500)
                Text("${p.total}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate900)
            }
        }

        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.ShowChart, null, Modifier.size(12.dp), tint = SimapTealDark)
                Spacer(Modifier.width(4.dp))
                Text("Progres Pengecekan", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Slate500)
            }
            Text("${p.checked}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SimapTealDark)
            Text(" / ${p.total}", fontSize = 12.sp, color = Slate400)
            Spacer(Modifier.width(4.dp))
            Text("(${p.persen}%)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SimapTealChipFg)
        }
        Spacer(Modifier.height(6.dp))
        ProgressTrack(p.persen)

        Spacer(Modifier.height(10.dp))
        HorizontalDivider(color = HairLine)
        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SimapTealChipBg)
                    .clickable { onOpen(p.id) },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.Visibility, null, Modifier.size(14.dp), tint = SimapTealChipFg)
                Spacer(Modifier.width(6.dp))
                Text("Detail", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SimapTealChipFg)
            }
            AksiKecil(
                icon = Icons.Rounded.Edit,
                label = "Edit",
                bg = AmberBg,
                fg = AmberFg,
                border = AmberBorder,
                onClick = { onEdit(p) },
            )
            AksiKecil(
                icon = Icons.Rounded.Delete,
                label = "Hapus",
                bg = RoseBg,
                fg = RoseFg,
                border = RoseBorder,
                onClick = { onHapus(p) },
            )
        }
    }
}

@Composable
private fun AksiKecil(
    icon: ImageVector,
    label: String,
    bg: Color,
    fg: Color,
    border: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .height(38.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, Modifier.size(14.dp), tint = fg)
        Spacer(Modifier.width(5.dp))
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = fg)
    }
}

@Composable
private fun ReguBadge(regu: String?) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(SimapTealChipBg)
            .border(1.dp, Color(0xFF99D9DC), RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.People, null, Modifier.size(12.dp), tint = Color(0xFF0F766E))
        Spacer(Modifier.width(4.dp))
        Text(
            regu?.ifBlank { "Tanpa regu" } ?: "Tanpa regu",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF0F766E),
        )
    }
}

@Composable
private fun StatusPill(status: String?, isActive: Boolean?) {
    val aktif = isAktif(status, isActive)
    val selesai = isSelesai(status)
    val bg = when {
        aktif -> EmeraldBg
        selesai -> Color(0xFFF1F5F9)
        else -> Color(0xFFF1F5F9)
    }
    val fg = when {
        aktif -> EmeraldFg
        selesai -> Slate600
        else -> Slate600
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (aktif) {
            val pulse = rememberInfiniteTransition(label = "pulse")
            val alpha by pulse.animateFloat(
                initialValue = 1f,
                targetValue = 0.3f,
                animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
                label = "pulseAlpha",
            )
            Box(Modifier.size(5.dp).alpha(alpha).background(Color(0xFF10B981), CircleShape))
            Spacer(Modifier.width(4.dp))
        }
        Text(labelStatus(status), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = fg)
    }
}

@Composable
private fun ProgressTrack(percent: Int) {
    val p = percent.coerceIn(0, 100)
    Box(
        Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xFFF1F5F9))
            .border(1.dp, HairLine, RoundedCornerShape(999.dp)),
    ) {
        if (p > 0) {
            Box(
                Modifier
                    .fillMaxWidth(p / 100f)
                    .height(8.dp)
                    .background(Brush.horizontalGradient(listOf(TealBarStart, TealBarEnd)), RoundedCornerShape(999.dp)),
            )
        }
    }
}

@Composable
private fun PaginationRow(
    dari: Int,
    sampai: Int,
    total: Int,
    page: Int,
    totalPages: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit,
) {
    Column(Modifier.fillMaxWidth().padding(top = 2.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            if (total == 0) "Tidak ada data" else "Menampilkan $dari–$sampai dari $total penugasan",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Slate500,
        )
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            HalamanTombol(Icons.Rounded.ChevronLeft, null, page > 1, onPrev)
            Box(
                Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(TealBarEnd),
                contentAlignment = Alignment.Center,
            ) {
                Text("$page", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            if (totalPages > 1) {
                Text("/ $totalPages", fontSize = 11.sp, color = Slate400)
            }
            HalamanTombol(Icons.Rounded.ChevronRight, null, page < totalPages, onNext)
        }
    }
}

@Composable
private fun HalamanTombol(icon: ImageVector, cd: String?, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (enabled) SubtleBg else Color(0xFFF8FAFC))
            .border(1.dp, CardBorder, RoundedCornerShape(6.dp))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, cd, Modifier.size(14.dp), tint = if (enabled) Slate600 else Slate200)
    }
}

// ===== Footer =====

@Composable
private fun FooterKartu(total: Int, terakhirMuat: String?) {
    Column(
        Modifier.fillMaxWidth().padding(bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "© ARFF BDJ DEPARTMENT • INJOURNEY AIRPORTS",
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            color = Slate500,
            textAlign = TextAlign.Center,
            letterSpacing = 0.3.sp,
        )
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Versi ${BuildConfig.VERSION_NAME}", fontSize = 9.sp, color = Slate400)
            Text("•", fontSize = 9.sp, color = Slate400)
            Text("simap.arffsnia.com", fontSize = 9.sp, color = SimapTealDark)
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(Color(0x99E2E8F0))
                .padding(horizontal = 10.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(5.dp).background(Color(0xFF10B981), CircleShape))
            Spacer(Modifier.width(4.dp))
            Text("Data: $total", fontSize = 9.sp, fontWeight = FontWeight.Medium, color = Slate500)
            Text("  |  ", fontSize = 9.sp, color = Slate400)
            Text("Muat: ${terakhirMuat ?: "-"}", fontSize = 9.sp, fontWeight = FontWeight.Medium, color = Slate500)
        }
    }
}

// ===== Form tambah / edit =====

private data class FormSeed(val p: Penugasan?)

private fun ymdHariIni(): String {
    val c = calendarku()
    return String.format(
        Locale.US, "%04d-%02d-%02d",
        c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH),
    )
}

private fun millisKeYmd(ms: Long): String {
    val c = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = ms }
    return String.format(
        Locale.US, "%04d-%02d-%02d",
        c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH),
    )
}

private fun ymdKeMillis(s: String): Long? {
    val p = s.split("-")
    if (p.size != 3) return null
    val y = p[0].toIntOrNull() ?: return null
    val m = p[1].toIntOrNull() ?: return null
    val d = p[2].toIntOrNull() ?: return null
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(y, m - 1, d, 12, 0, 0)
    }.timeInMillis
}

/** Tanggal awal/akhir sebuah bulan (bulan 1..12). */
private fun awalBulan(tahun: Int, bulan: Int): String =
    String.format(Locale.US, "%04d-%02d-01", tahun, bulan)

private fun akhirBulan(tahun: Int, bulan: Int): String {
    val c = Calendar.getInstance().apply {
        clear()
        set(tahun, bulan - 1, 1, 12, 0, 0)
    }
    val hari = c.getActualMaximum(Calendar.DAY_OF_MONTH)
    return String.format(Locale.US, "%04d-%02d-%02d", tahun, bulan, hari)
}

private fun warnaHex(hex: String?): Color {
    val s = (hex ?: "").removePrefix("#")
    return runCatching { Color(0xFF000000L or s.toLong(16)) }.getOrElse { Color(0xFF0EA5E9) }
}

/** Ray casting: titik [lat,lng] di dalam polygon? */
private fun titikDiPolygon(lat: Double, lng: Double, poly: List<List<Double>>): Boolean {
    if (poly.size < 3) return false
    var inside = false
    var j = poly.size - 1
    for (i in poly.indices) {
        val yi = poly[i].getOrNull(0) ?: 0.0
        val xi = poly[i].getOrNull(1) ?: 0.0
        val yj = poly[j].getOrNull(0) ?: 0.0
        val xj = poly[j].getOrNull(1) ?: 0.0
        val antara = (yi > lat) != (yj > lat)
        if (antara) {
            val xPotong = (xj - xi) * (lat - yi) / ((yj - yi).takeIf { it != 0.0 } ?: 1e-12) + xi
            if (lng < xPotong) inside = !inside
        }
        j = i
    }
    return inside
}

@Composable
private fun FieldLabel(text: String, wajib: Boolean = false) {
    Row(Modifier.padding(bottom = 4.dp)) {
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Slate600)
        if (wajib) Text(" *", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoseFg)
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    placeholder: String = "",
    singleLine: Boolean = true,
    minHeight: Int = 42,
    wajib: Boolean = false,
) {
    Column(Modifier.fillMaxWidth()) {
        FieldLabel(label, wajib)
        Box(
            Modifier
                .fillMaxWidth()
                .heightIn(min = minHeight.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(Color.White)
                .border(1.dp, CardBorder, RoundedCornerShape(9.dp))
                .padding(horizontal = 10.dp, vertical = 10.dp),
        ) {
            if (value.isEmpty() && placeholder.isNotEmpty()) {
                Text(placeholder, fontSize = 12.sp, color = Slate400)
            }
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
}

@Composable
private fun PilihBox(
    label: String,
    nilai: String,
    kosong: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    menu: (@Composable () -> Unit)? = null,
) {
    Column(modifier) {
        FieldLabel(label)
        Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 42.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(Color.White)
                .border(1.dp, CardBorder, RoundedCornerShape(9.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 10.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                nilai,
                fontSize = 12.sp,
                color = if (kosong) Slate400 else Slate900,
                maxLines = 1,
                modifier = Modifier.weight(1f),
            )
            Icon(Icons.Rounded.ChevronRight, null, Modifier.size(13.dp), tint = Slate400)
        }
        menu?.invoke()
        }
    }
}

@Composable
private fun TanggalField(label: String, value: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(modifier) {
        FieldLabel(label, wajib = true)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 42.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(Color.White)
                .border(1.dp, CardBorder, RoundedCornerShape(9.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 10.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(tanggalPenuh(value), fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Slate900,
                modifier = Modifier.weight(1f))
            Icon(Icons.Rounded.DateRange, null, Modifier.size(13.dp), tint = Slate400)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PetaZonasi(
    daftar: List<ZonasiOpt>,
    terpilih: Set<Int>,
    onToggle: (Int) -> Unit,
) {
    var lantai by remember { mutableStateOf("") }
    val lantaiList = remember(daftar) {
        listOf("") + daftar.mapNotNull { it.lantai?.takeIf { l -> l.isNotBlank() } }.distinct().sorted()
    }
    val tampil = remember(daftar, lantai) {
        if (lantai.isEmpty()) daftar.filter { it.poly.isNotEmpty() }
        else daftar.filter { it.poly.isNotEmpty() && it.lantai == lantai }
    }

    val latMin = tampil.minOfOrNull { z -> z.poly.minOf { it.getOrNull(0) ?: 0.0 } } ?: 0.0
    val latMax = tampil.maxOfOrNull { z -> z.poly.maxOf { it.getOrNull(0) ?: 0.0 } } ?: 1.0
    val lngMin = tampil.minOfOrNull { z -> z.poly.minOf { it.getOrNull(1) ?: 0.0 } } ?: 0.0
    val lngMax = tampil.maxOfOrNull { z -> z.poly.maxOf { it.getOrNull(1) ?: 0.0 } } ?: 1.0
    val spanLat = (latMax - latMin).takeIf { it > 0 } ?: 1e-6
    val spanLng = (lngMax - lngMin).takeIf { it > 0 } ?: 1e-6

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            var buka by remember { mutableStateOf(false) }
            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                        .clickable { buka = true }
                        .padding(horizontal = 9.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        if (lantai.isEmpty()) "Semua Lantai" else "Lantai $lantai",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Slate700,
                    )
                    Icon(Icons.Rounded.ChevronRight, null, Modifier.size(11.dp), tint = Slate400)
                }
                DropdownMenu(expanded = buka, onDismissRequest = { buka = false }) {
                    lantaiList.forEach { l ->
                        DropdownMenuItem(
                            text = { Text(if (l.isEmpty()) "Semua Lantai" else "Lantai $l", fontSize = 12.sp) },
                            onClick = { lantai = l; buka = false },
                        )
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            Box(
                Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFFF1F5F9))
                    .border(1.dp, CardBorder, RoundedCornerShape(999.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            ) {
                Text("${terpilih.size} zonasi dipilih", fontSize = 9.sp, fontWeight = FontWeight.Medium, color = Slate600)
            }
        }

        Text("Ketuk polygon untuk pilih/lepas zonasi", fontSize = 9.sp, color = Slate400)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFE5E9EC))
                .border(1.dp, CardBorder, RoundedCornerShape(12.dp)),
        ) {
            if (tampil.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Tidak ada polygon zonasi", fontSize = 11.sp, color = Slate400)
                }
            } else {
                Canvas(Modifier.fillMaxSize().padding(8.dp)) {
                    tampil.forEach { z ->
                        val path = Path()
                        z.poly.forEachIndexed { i, pt ->
                            val lat = pt.getOrNull(0) ?: return@forEachIndexed
                            val lng = pt.getOrNull(1) ?: return@forEachIndexed
                            val x = ((lng - lngMin) / spanLng * size.width).toFloat()
                            val y = ((latMax - lat) / spanLat * size.height).toFloat()
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        path.close()
                        val dasar = warnaHex(z.warna)
                        val dipilih = z.id in terpilih
                        drawPath(path, dasar.copy(alpha = if (dipilih) 0.65f else 0.28f))
                        drawPath(
                            path,
                            dasar.copy(alpha = if (dipilih) 1f else 0.75f),
                            style = Stroke(width = if (dipilih) 3f else 1.5f),
                        )
                    }
                }
                androidx.compose.foundation.layout.Box(
                    Modifier
                        .matchParentSize()
                        .pointerInput(tampil) {
                            detectTapGestures { off ->
                                val w = size.width - 16f
                                val h = size.height - 16f
                                if (w <= 0 || h <= 0) return@detectTapGestures
                                val lng = lngMin + (off.x / w) * spanLng
                                val lat = latMax - (off.y / h) * spanLat
                                tampil.lastOrNull { titikDiPolygon(lat, lng, it.poly) }?.let { onToggle(it.id) }
                            }
                        },
                )
            }
        }
    }
}

@Composable
private fun ChipLokasi(rooms: List<SimpleItem>, terpilih: Set<Int>, onToggle: (Int) -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
            .padding(8.dp),
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            rooms.forEach { r ->
                val on = r.id in terpilih
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(7.dp))
                        .background(if (on) SimapTealChipBg else Color.White)
                        .border(1.dp, if (on) SimapTeal else CardBorder, RoundedCornerShape(7.dp))
                        .clickable { onToggle(r.id) }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        r.nama,
                        fontSize = 10.sp,
                        fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (on) Color(0xFF0F766E) else Slate600,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun PenugasanFormSheet(seed: FormSeed, onDismiss: () -> Unit, onSaved: () -> Unit) {
    val app = LocalContext.current.applicationContext as SimapApp
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val lama = seed.p

    var nama by remember { mutableStateOf(lama?.nama ?: "") }
    var reguId by remember { mutableStateOf(lama?.reguId) }
    var reguNama by remember { mutableStateOf(lama?.regu ?: "") }
    var status by remember { mutableStateOf(if (lama?.status == "nonaktif") "nonaktif" else "aktif") }
    var mulai by remember { mutableStateOf(lama?.tanggalMulai?.take(10) ?: ymdHariIni()) }
    var selesai by remember { mutableStateOf(lama?.tanggalSelesai?.take(10) ?: ymdHariIni()) }
    var keterangan by remember { mutableStateOf(lama?.keterangan ?: "") }
    var periodeId by remember { mutableStateOf(0) }
    var tahun by remember { mutableStateOf(calendarku().get(Calendar.YEAR)) }
    var zonasiIds by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var roomIds by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var options by remember { mutableStateOf<PenugasanOptions?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    var picker by remember { mutableStateOf<String?>(null) }
    var reguOpen by remember { mutableStateOf(false) }
    var statusOpen by remember { mutableStateOf(false) }
    var periodeOpen by remember { mutableStateOf(false) }
    var tahunOpen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        when (val r = app.adminRepo.penugasanOptions()) {
            is AppResult.Success -> options = r.data
            is AppResult.Error -> error = mapError(r.message)
        }
        if (lama != null) {
            when (val r = app.adminRepo.penugasanDetail(lama.id)) {
                is AppResult.Success -> {
                    zonasiIds = r.data.zonasiIds.toSet()
                    roomIds = r.data.roomIds.toSet()
                    if (!r.data.keterangan.isNullOrBlank()) keterangan = r.data.keterangan!!
                }
                is AppResult.Error -> Unit
            }
        }
    }

    fun isiDariPeriode(pid: Int, th: Int) {
        val p = options?.periode?.firstOrNull { it.id == pid } ?: return
        if (p.bulanMulai in 1..12) mulai = awalBulan(th, p.bulanMulai)
        if (p.bulanSelesai in 1..12) selesai = akhirBulan(th, p.bulanSelesai)
    }

    fun simpan() {
        error = null
        val rid = reguId
        when {
            nama.isBlank() -> { error = "Nama penugasan wajib diisi"; return }
            rid == null || rid <= 0 -> { error = "Pilih regu penanggung jawab"; return }
            zonasiIds.isEmpty() -> { error = "Pilih minimal satu zonasi"; return }
            strCmp(mulai, selesai) > 0 -> { error = "Tanggal selesai harus sama atau setelah tanggal mulai"; return }
        }
        saving = true
        scope.launch {
            val body = PenugasanSaveRequest(
                id = lama?.id,
                nama = nama.trim(),
                reguId = rid,
                tanggalMulai = mulai,
                tanggalSelesai = selesai,
                keterangan = keterangan.ifBlank { null },
                status = status,
                zonasiIds = zonasiIds.sorted(),
                roomIds = roomIds.sorted(),
            )
            val r = if (lama == null) app.adminRepo.penugasanCreate(body)
            else app.adminRepo.penugasanUpdate(body)
            saving = false
            when (r) {
                is AppResult.Success -> onSaved()
                is AppResult.Error -> error = mapError(r.message)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
    ) {
        Column(Modifier.fillMaxWidth()) {
            // Header gradien
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(HeroStart, HeroEnd)))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(26.dp).clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.CheckCircle, null, Modifier.size(14.dp), tint = Color.White)
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    if (lama == null) "Tambah Penugasan" else "Edit Penugasan",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Rounded.Close, "Tutup", Modifier.size(16.dp), tint = Color.White)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 560.dp)
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                FormField(
                    label = "Nama Penugasan",
                    value = nama,
                    onChange = { nama = it },
                    placeholder = "Mis. Pengecekan APAR Terminal A",
                    wajib = true,
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PilihBox(
                        "Regu", reguNama.ifBlank { "Pilih Regu" }, reguNama.isBlank(),
                        Modifier.weight(1f),
                        onClick = { reguOpen = true },
                        menu = {
                            DropdownMenu(expanded = reguOpen, onDismissRequest = { reguOpen = false }) {
                                options?.regu?.forEach { r ->
                                    DropdownMenuItem(
                                        text = { Text(r.nama, fontSize = 12.sp) },
                                        onClick = { reguId = r.id; reguNama = r.nama; reguOpen = false },
                                    )
                                }
                            }
                        },
                    )
                    PilihBox(
                        "Status", if (status == "aktif") "Aktif" else "Nonaktif",
                        false, Modifier.weight(1f),
                        onClick = { statusOpen = true },
                        menu = {
                            DropdownMenu(expanded = statusOpen, onDismissRequest = { statusOpen = false }) {
                                DropdownMenuItem(
                                    text = { Text("Aktif", fontSize = 12.sp) },
                                    onClick = { status = "aktif"; statusOpen = false },
                                )
                                DropdownMenuItem(
                                    text = { Text("Nonaktif", fontSize = 12.sp) },
                                    onClick = { status = "nonaktif"; statusOpen = false },
                                )
                            }
                        },
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PilihBox(
                        "Periode Penugasan",
                        options?.periode?.firstOrNull { it.id == periodeId }?.nama ?: "— pilih periode —",
                        periodeId == 0,
                        Modifier.weight(1.3f),
                        onClick = { periodeOpen = true },
                        menu = {
                            DropdownMenu(expanded = periodeOpen, onDismissRequest = { periodeOpen = false }) {
                                options?.periode?.forEach { pp ->
                                    DropdownMenuItem(
                                        text = { Text(pp.nama, fontSize = 12.sp) },
                                        onClick = {
                                            periodeId = pp.id
                                            isiDariPeriode(pp.id, tahun)
                                            periodeOpen = false
                                        },
                                    )
                                }
                            }
                        },
                    )
                    PilihBox(
                        "Tahun", "$tahun", false, Modifier.weight(0.7f),
                        onClick = { tahunOpen = true },
                        menu = {
                            DropdownMenu(expanded = tahunOpen, onDismissRequest = { tahunOpen = false }) {
                                val kini = calendarku().get(Calendar.YEAR)
                                (kini - 1..kini + 1).forEach { th ->
                                    DropdownMenuItem(
                                        text = { Text("$th", fontSize = 12.sp) },
                                        onClick = {
                                            tahun = th
                                            if (periodeId != 0) isiDariPeriode(periodeId, th)
                                            tahunOpen = false
                                        },
                                    )
                                }
                            }
                        },
                    )
                }
                Text(
                    "Memilih periode mengisi tanggal di bawah sampai hari terakhir bulan itu.",
                    fontSize = 9.sp,
                    color = Slate400,
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TanggalField("Mulai", mulai, Modifier.weight(1f)) { picker = "mulai" }
                    TanggalField("Selesai", selesai, Modifier.weight(1f)) { picker = "selesai" }
                }

                FormField(
                    label = "Keterangan (opsional)",
                    value = keterangan,
                    onChange = { keterangan = it },
                    placeholder = "Tuliskan catatan khusus terkait penugasan...",
                    singleLine = false,
                    minHeight = 60,
                )

                HorizontalDivider(color = HairLine)
                FieldLabel("Peta Zonasi")
                PetaZonasi(
                    daftar = options?.zonasi ?: emptyList(),
                    terpilih = zonasiIds,
                    onToggle = { id ->
                        zonasiIds = if (id in zonasiIds) zonasiIds - id else zonasiIds + id
                    },
                )

                FieldLabel("Zonasi", wajib = true)
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        options?.zonasi?.forEach { z ->
                            val on = z.id in zonasiIds
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable {
                                        zonasiIds = if (on) zonasiIds - z.id else zonasiIds + z.id
                                    }
                                    .padding(vertical = 5.dp, horizontal = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    Modifier
                                        .size(13.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(if (on) SimapTeal else Color.White)
                                        .border(1.dp, if (on) SimapTeal else Slate200, RoundedCornerShape(3.dp)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (on) {
                                        Icon(Icons.Rounded.CheckCircle, null, Modifier.size(9.dp), tint = Color.White)
                                    }
                                }
                                Spacer(Modifier.width(7.dp))
                                Box(Modifier.size(8.dp).clip(CircleShape).background(warnaHex(z.warna)))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    z.nama,
                                    fontSize = 11.sp,
                                    fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal,
                                    color = Slate700,
                                )
                            }
                        }
                    }
                }

                FieldLabel("Lokasi")
                Text(
                    "Terisi otomatis dari zona; boleh ditambah lokasi di luar zona.",
                    fontSize = 9.sp,
                    color = Slate400,
                )
                ChipLokasi(
                    rooms = options?.rooms ?: emptyList(),
                    terpilih = roomIds,
                    onToggle = { id -> roomIds = if (id in roomIds) roomIds - id else roomIds + id },
                )

                error?.let {
                    Text(it, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = RoseFg)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White)
                            .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                            .clickable(enabled = !saving, onClick = onDismiss),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Batal", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Slate700)
                    }
                    Row(
                        modifier = Modifier
                            .weight(2f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SimapPillStart)
                            .clickable(enabled = !saving) { simpan() },
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (saving) {
                            CircularProgressIndicator(Modifier.size(15.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(Modifier.width(7.dp))
                            Text("Menyimpan...", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        } else {
                            Text("Simpan Penugasan", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }

    picker?.let { which ->
        val dpState = rememberDatePickerState(
            initialSelectedDateMillis = ymdKeMillis(if (which == "mulai") mulai else selesai),
        )
        DatePickerDialog(
            onDismissRequest = { picker = null },
            confirmButton = {
                TextButton(onClick = {
                    val ms = dpState.selectedDateMillis
                    if (ms != null) {
                        if (which == "mulai") mulai = millisKeYmd(ms) else selesai = millisKeYmd(ms)
                    }
                    picker = null
                }) { Text("Pilih", color = SimapTeal, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { picker = null }) { Text("Batal", color = Slate500) }
            },
        ) { DatePicker(state = dpState) }
    }
}

@Composable
private fun HapusPenugasanDialog(penugasan: Penugasan, onDismiss: () -> Unit, onDeleted: () -> Unit) {
    val app = LocalContext.current.applicationContext as SimapApp
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text("Hapus penugasan?", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column {
                Text(
                    "${penugasan.nama}\nRegu ${penugasan.regu ?: "-"} · ${penugasan.total} aset. " +
                        "Relasi zonasi dan lokasinya ikut terhapus dan tidak bisa dikembalikan.",
                    fontSize = 13.sp,
                    color = Slate600,
                )
                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, fontSize = 12.sp, color = RoseFg, fontWeight = FontWeight.Medium)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !busy,
                onClick = {
                    scope.launch {
                        busy = true
                        when (val r = app.adminRepo.penugasanDelete(penugasan.id)) {
                            is AppResult.Success -> { busy = false; onDeleted() }
                            is AppResult.Error -> { busy = false; error = mapError(r.message) }
                        }
                    }
                },
            ) {
                Text(
                    if (busy) "Menghapus..." else "Hapus",
                    color = RoseFg,
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        dismissButton = {
            TextButton(enabled = !busy, onClick = onDismiss) { Text("Batal", color = Slate500) }
        },
    )
}

/** Bandingkan dua tanggal YYYY-MM-DD tanpa java.time (minSdk 24). */
private fun strCmp(a: String, b: String): Int {
    val pa = a.split("-").mapNotNull { it.toIntOrNull() }
    val pb = b.split("-").mapNotNull { it.toIntOrNull() }
    if (pa.size != 3 || pb.size != 3) return 0
    for (i in 0..2) {
        if (pa[i] != pb[i]) return pa[i].compareTo(pb[i])
    }
    return 0
}

// ===== Detail =====

private val DetailBg = Color(0xFFF5F8FA)
private val MetricTealBg = Color(0xFFDFF5F6)
private val MetricTealBorder = Color(0xFFBCE8EA)
private val MetricTealFg = Color(0xFF0D6268)
private val MetricGreenBg = Color(0xFFDDF6E6)
private val MetricGreenBorder = Color(0xFFBDECCB)
private val MetricGreenFg = Color(0xFF1B6F36)
private val MetricAmberBg = Color(0xFFFFF2D6)
private val MetricAmberBorder = Color(0xFFFFE1A6)
private val MetricAmberFg = Color(0xFF9A5703)
private val MetricIndigoBg = Color(0xFFE7EDFC)
private val MetricIndigoBorder = Color(0xFFCFDBF8)
private val MetricIndigoFg = Color(0xFF384898)
private val TabActive = Color(0xFF00899B)
private val BadgeBelumBg = Color(0xFFFDE8E8)
private val BadgeBelumFg = Color(0xFFE02424)
private val BadgeBelumBorder = Color(0xFFFBD5D5)
private val SlugBg = Color(0xFFFFF1F2)
private val SlugFg = Color(0xFFE11D48)
private val SlugBorder = Color(0xFFFECDD3)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PenugasanDetailScreen(
    id: Int,
    onBack: () -> Unit,
    onOpenAsset: (String) -> Unit,
    onPeriksa: (Int, String, String, Int) -> Unit,
    onNotif: (() -> Unit)? = null,
    onProfile: (() -> Unit)? = null,
) {
    val app = LocalContext.current.applicationContext as SimapApp
    val user = remember { app.authRepo.currentUser() }

    var tab by remember { mutableStateOf("belum") }
    var categoryId by remember { mutableStateOf(0) }
    var minggu by remember { mutableStateOf(0) }
    var query by remember { mutableStateOf("") }
    var limit by remember { mutableStateOf(10) }
    var page by remember { mutableStateOf(1) }
    var data by remember { mutableStateOf<PenugasanAssetsResponse?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(id, tab, categoryId, minggu, query, limit, page) {
        loading = true
        when (val r = app.adminRepo.penugasanAssets(id, tab, categoryId, minggu, query, page, limit)) {
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
            Column(Modifier.fillMaxSize().background(DetailBg).statusBarsPadding()) {
                TopBar(user = user, onNotif = onNotif, onProfile = onProfile)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                        .padding(top = 12.dp, bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    val d = data
                    when {
                        d != null -> {
                            BannerDetail(d, onBack)
                            OverviewDetail(d)
                            MetrikGrid(d)
                            FilterCard(
                                tab = tab,
                                onTab = { tab = it; page = 1 },
                                belum = d.dariFilter.belum,
                                sudah = d.dariFilter.sudah,
                                kategori = d.kategori,
                                categoryId = categoryId,
                                onCategory = { categoryId = it; page = 1 },
                                mingguList = d.minggu,
                                minggu = minggu,
                                onMinggu = { minggu = it; page = 1 },
                                limit = limit,
                                onLimit = { limit = it; page = 1 },
                                query = query,
                                onQuery = { query = it; page = 1 },
                                totalAset = d.total,
                            )
                            DaftarAset(d, loading, onOpenAsset, onPeriksa)
                            Pager(d) { page = it }
                        }
                        loading -> Box(
                            Modifier.fillMaxWidth().height(200.dp),
                            contentAlignment = Alignment.Center,
                        ) { CircularProgressIndicator(color = SimapTeal) }
                        error != null -> Text(error!!, color = RoseFg, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun BannerDetail(d: PenugasanAssetsResponse, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(HeroStart, HeroEnd))),
    ) {
        OrnamenHeader(
            Modifier.align(Alignment.BottomEnd).offset(x = 24.dp, y = 24.dp),
            ukuran = 120.dp,
            alpha = 0.16f,
        )
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.ChevronLeft, "Kembali",
                    Modifier.size(16.dp).clickable(onClick = onBack),
                    tint = Color.White.copy(alpha = 0.9f),
                )
                Spacer(Modifier.width(4.dp))
                Text("Home", fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.clickable(onClick = onBack))
                Text(" / ", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                Text("Detail Penugasan", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(28.dp).clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.20f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.Assignment, null, Modifier.size(15.dp), tint = Color.White)
                }
                Spacer(Modifier.width(8.dp))
                Text("Detail Penugasan", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(Modifier.height(5.dp))
            Text(
                "Pantau progres pengecekan APAR untuk ${d.regu ?: "-"}. Periode penugasan: " +
                    "${tanggalPenuh(d.tanggalMulai)} – ${tanggalPenuh(d.tanggalSelesai)}.",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.92f),
            )
        }
    }
}

@Composable
private fun OverviewDetail(d: PenugasanAssetsResponse) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF0F766E), Color(0xFF0891B2))))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(28.dp).clip(RoundedCornerShape(7.dp))
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.CheckCircle, null, Modifier.size(15.dp), tint = Color.White)
            }
            Spacer(Modifier.width(8.dp))
            Text(
                d.nama,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.weight(1f),
            )
            StatusPill(d.status, d.status == "aktif")
        }

        Spacer(Modifier.height(10.dp))
        MetaBaris(Icons.Rounded.People, "Regu: ", d.regu ?: "-")
        Spacer(Modifier.height(6.dp))
        MetaBaris(
            Icons.Rounded.DateRange, "",
            "${tanggalPenuh(d.tanggalMulai)} – ${tanggalPenuh(d.tanggalSelesai)}",
        )
        if (d.zonasi.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            MetaBaris(Icons.Rounded.Place, "", d.zonasi.joinToString(", "))
        }

        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Progres Pengecekan", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White,
                modifier = Modifier.weight(1f))
            Text("${d.persen}% Selesai", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFA7F3D0))
        }
        Spacer(Modifier.height(6.dp))
        Box(
            Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(999.dp))
                .background(Color.Black.copy(alpha = 0.20f)).padding(2.dp),
        ) {
            if (d.persen > 0) {
                Box(
                    Modifier.fillMaxWidth(d.persen / 100f).height(6.dp)
                        .background(Color(0xFF34D399), RoundedCornerShape(999.dp)),
                )
            }
        }

        Spacer(Modifier.height(10.dp))
        Text(
            "Dihitung dari pengecekan pada rentang tanggal penugasan ini " +
                "(${tanggalPenuh(d.tanggalMulai)} – ${tanggalPenuh(d.tanggalSelesai)}). " +
                "Siapa pun yang mengecek — termasuk anggota regu lain — tetap tercatat dan dihitung, " +
                "lengkap dengan nama & regunya.",
            fontSize = 10.sp,
            color = Color(0xFFCFFAFE).copy(alpha = 0.85f),
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.12f))
                .padding(8.dp),
        )
    }
}

@Composable
private fun MetaBaris(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.size(13.dp), tint = Color.White.copy(alpha = 0.8f))
        Spacer(Modifier.width(6.dp))
        Text(
            label + value,
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.95f),
        )
    }
}

@Composable
private fun MetrikGrid(d: PenugasanAssetsResponse) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetrikKartu(d.total.toString(), "Total APAR", Icons.Rounded.Widgets,
                MetricTealBg, MetricTealBorder, MetricTealFg, Modifier.weight(1f))
            MetrikKartu(d.checked.toString(), "Sudah Dicek", Icons.Rounded.CheckCircle,
                MetricGreenBg, MetricGreenBorder, MetricGreenFg, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetrikKartu((d.total - d.checked).coerceAtLeast(0).toString(), "Belum Dicek", Icons.Rounded.DateRange,
                MetricAmberBg, MetricAmberBorder, MetricAmberFg, Modifier.weight(1f))
            MetrikKartu("${d.persen}%", "Progres", Icons.Rounded.ShowChart,
                MetricIndigoBg, MetricIndigoBorder, MetricIndigoFg, Modifier.weight(1f))
        }
    }
}

@Composable
private fun MetrikKartu(
    nilai: String,
    label: String,
    icon: ImageVector,
    bg: Color,
    border: Color,
    fg: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(nilai, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = fg)
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = fg.copy(alpha = 0.9f))
        }
        Box(
            Modifier.size(38.dp).clip(RoundedCornerShape(12.dp))
                .background(border.copy(alpha = 0.75f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, Modifier.size(18.dp), tint = fg)
        }
    }
}

@Composable
private fun FilterCard(
    tab: String,
    onTab: (String) -> Unit,
    belum: Int,
    sudah: Int,
    kategori: List<PenugasanKategori>,
    categoryId: Int,
    onCategory: (Int) -> Unit,
    mingguList: List<PenugasanMinggu>,
    minggu: Int,
    onMinggu: (Int) -> Unit,
    limit: Int,
    onLimit: (Int) -> Unit,
    query: String,
    onQuery: (String) -> Unit,
    totalAset: Int,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TabAset("Belum Dicek ($belum)", tab == "belum", true, Modifier.weight(1f)) { onTab("belum") }
            TabAset("Sudah Dicek ($sudah)", tab == "sudah", false, Modifier.weight(1f)) { onTab("sudah") }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val opsiKategori = listOf(0 to "Semua ($totalAset APAR)") +
                kategori.map { it.id to "${it.nama} (${it.n})" }
            PilihDropdown("Lihat per:", opsiKategori, categoryId, onCategory)

            val opsiMinggu = listOf(0 to "Semua Minggu") +
                mingguList.map { m -> m.n to "Minggu ${m.n} (${tanggalPenuh(m.mulai)}–${tanggalPenuh(m.selesai)})" }
            PilihDropdown("Minggu:", opsiMinggu, minggu, onMinggu)

            HorizontalDivider(color = HairLine)

            Row(verticalAlignment = Alignment.CenterVertically) {
                PilihDropdown(
                    label = null,
                    opsi = listOf(10 to "10 entri", 25 to "25 entri", 50 to "50 entri"),
                    terpilih = limit,
                    onPick = onLimit,
                    modifier = Modifier.width(104.dp),
                )
                Spacer(Modifier.width(8.dp))
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SubtleBg)
                        .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.Search, null, Modifier.size(14.dp), tint = Slate400)
                    Spacer(Modifier.width(6.dp))
                    Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (query.isEmpty()) {
                            Text("Cari kode/lokasi...", fontSize = 11.sp, color = Slate400)
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
            }
        }
    }
}

@Composable
private fun TabAset(label: String, aktif: Boolean, jam: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (aktif) TabActive else Color.White)
            .border(1.dp, if (aktif) TabActive else CardBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            if (jam) Icons.Rounded.DateRange else Icons.Rounded.CheckCircle,
            null,
            Modifier.size(14.dp),
            tint = if (aktif) Color.White else Color(0xFF059669),
        )
        Spacer(Modifier.width(5.dp))
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (aktif) Color.White else Slate600,
        )
    }
}

@Composable
private fun PilihDropdown(
    label: String?,
    opsi: List<Pair<Int, String>>,
    terpilih: Int,
    onPick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var open by remember { mutableStateOf(false) }
    val teks = opsi.firstOrNull { it.first == terpilih }?.second ?: opsi.firstOrNull()?.second ?: "-"
    Column(modifier) {
        if (label != null) {
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Slate500)
            Spacer(Modifier.height(4.dp))
        }
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SubtleBg)
                    .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                    .clickable { open = true }
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    teks,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Slate700,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
                Icon(Icons.Rounded.ChevronRight, null, Modifier.size(13.dp), tint = Slate400)
            }
            DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                opsi.forEach { (id, nama) ->
                    DropdownMenuItem(
                        text = { Text(nama, fontSize = 12.sp) },
                        onClick = { onPick(id); open = false },
                    )
                }
            }
        }
    }
}

@Composable
private fun DaftarAset(
    d: PenugasanAssetsResponse,
    loading: Boolean,
    onOpenAsset: (String) -> Unit,
    onPeriksa: (Int, String, String, Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "DAFTAR ASET APAR",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Slate500,
                letterSpacing = 0.5.sp,
                modifier = Modifier.weight(1f),
            )
            if (loading) {
                CircularProgressIndicator(Modifier.size(14.dp), color = SimapTeal, strokeWidth = 2.dp)
            } else {
                Text("Hal. ${d.page} dari ${d.pages}", fontSize = 11.sp, color = Slate400)
            }
        }

        if (d.items.isEmpty()) {
            Text(
                "Tidak ada aset pada filter ini.",
                fontSize = 12.sp,
                color = Slate400,
                modifier = Modifier.padding(vertical = 16.dp),
            )
        }

        d.items.forEachIndexed { i, a ->
            AsetKartu(
                nomor = (d.page - 1) * d.limit + i + 1,
                aset = a,
                onDetail = { onOpenAsset(a.slug) },
                onPeriksa = { onPeriksa(a.id, a.slug, a.nama, a.categoryId) },
            )
        }
    }
}

@Composable
private fun AsetKartu(nomor: Int, aset: PenugasanAset, onDetail: () -> Unit, onPeriksa: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(24.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center,
            ) {
                Text("$nomor", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate600)
            }
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(SlugBg)
                    .border(1.dp, SlugBorder, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            ) {
                Text(
                    aset.slug,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SlugFg,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                )
            }
            Spacer(Modifier.weight(1f))
            if (aset.checked) {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(MetricGreenBg)
                        .border(1.dp, MetricGreenBorder, RoundedCornerShape(999.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    Text("Sudah", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MetricGreenFg)
                }
            } else {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(BadgeBelumBg)
                        .border(1.dp, BadgeBelumBorder, RoundedCornerShape(999.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    Text("Belum", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BadgeBelumFg)
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF8FAFC))
                .border(1.dp, HairLine, RoundedCornerShape(12.dp))
                .padding(10.dp),
        ) {
            KolomAset("Nama Aset", aset.nama, Modifier.weight(1f))
            KolomAset("Merek", aset.merek ?: "-", Modifier.weight(1f))
            KolomAset("Lokasi", aset.lokasi ?: "-", Modifier.weight(1f), tint = SimapTealDark)
        }

        if (aset.checked && !aset.checkedAt.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Dicek ${tanggalPenuh(aset.checkedAt?.take(10))} oleh ${aset.checkedBy ?: "-"}" +
                    (aset.checkedRegu?.let { " · $it" } ?: ""),
                fontSize = 10.sp,
                color = Slate500,
            )
        }

        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier
                    .height(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF1F5F9))
                    .clickable(onClick = onDetail)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.Visibility, null, Modifier.size(13.dp), tint = Slate600)
                Spacer(Modifier.width(5.dp))
                Text("Detail", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
            }
            Spacer(Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .height(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(TabActive)
                    .clickable(onClick = onPeriksa)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.CheckCircle, null, Modifier.size(13.dp), tint = Color.White)
                Spacer(Modifier.width(5.dp))
                Text("Periksa", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
    }
}

@Composable
private fun KolomAset(label: String, value: String, modifier: Modifier = Modifier, tint: Color = Slate700) {
    Column(modifier) {
        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Medium, color = Slate400)
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = tint, maxLines = 2)
    }
}

@Composable
private fun Pager(d: PenugasanAssetsResponse, onPage: (Int) -> Unit) {
    val dari = if (d.hits == 0) 0 else (d.page - 1) * d.limit + 1
    val sampai = minOf(d.page * d.limit, d.hits)
    val nomor: List<Int?> = run {
        val out = mutableListOf<Int?>()
        out += 1
        if (d.page > 3) out += null
        for (p in maxOf(2, d.page - 1)..minOf(d.pages - 1, d.page + 1)) out += p
        if (d.page < d.pages - 2) out += null
        if (d.pages > 1) out += d.pages
        out.distinct()
    }

    Column(Modifier.fillMaxWidth().padding(top = 2.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "Menampilkan $dari–$sampai dari ${d.hits} entri",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Slate500,
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
            HalamanTombol(Icons.Rounded.ChevronLeft, "Sebelumnya", d.page > 1) { onPage(d.page - 1) }
            nomor.forEach { n ->
                if (n == null) {
                    Text("...", fontSize = 11.sp, color = Slate400, modifier = Modifier.padding(horizontal = 2.dp))
                } else {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (n == d.page) TabActive else Color.White)
                            .border(1.dp, if (n == d.page) TabActive else CardBorder, RoundedCornerShape(10.dp))
                            .clickable(enabled = n != d.page) { onPage(n) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "$n",
                            fontSize = 11.sp,
                            fontWeight = if (n == d.page) FontWeight.Bold else FontWeight.Medium,
                            color = if (n == d.page) Color.White else Slate700,
                        )
                    }
                }
            }
            HalamanTombol(Icons.Rounded.ChevronRight, "Berikutnya", d.page < d.pages) { onPage(d.page + 1) }
        }
    }
}
