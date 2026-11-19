package com.arffsnia.simap.ui.admin

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Print
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.arffsnia.simap.SimapApp
import com.arffsnia.simap.data.dto.LaporanBaris
import com.arffsnia.simap.data.dto.LaporanHarianLive
import com.arffsnia.simap.data.dto.SimpleItem
import com.arffsnia.simap.data.repo.AppResult
import com.arffsnia.simap.ui.mapError
import com.arffsnia.simap.ui.theme.PlusJakarta
import com.arffsnia.simap.ui.theme.PlusJakartaTypography
import com.arffsnia.simap.ui.theme.SimapTeal
import com.arffsnia.simap.ui.theme.Slate400
import com.arffsnia.simap.ui.theme.Slate900
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

private val LhBg = Color(0xFFF1F5F9)
private val LhBorder = Color(0xFFE2E8F0)
private val LhHair = Color(0xFFF1F5F9)
private val LhSubtle = Color(0xFFF8FAFC)
private val LhSlate700 = Color(0xFF334155)
private val LhSlate500 = Color(0xFF64748B)
private val LhRose = Color(0xFFE11D48)

private val ZONA_WITA: TimeZone = TimeZone.getTimeZone("Asia/Makassar")

/** Layar Laporan Harian: metrik + baris pengecekan APAR per tanggal/regu/shift. */
@Composable
fun LaporanHarianScreen(
    onBack: (() -> Unit)? = null,
    onNotif: (() -> Unit)? = null,
    onProfile: (() -> Unit)? = null,
) {
    val app = LocalContext.current.applicationContext as SimapApp
    val user = remember { app.authRepo.currentUser() }

    // draft = isi filter; dipakai ke API hanya saat "Tampilkan" ditekan.
    var tglDraft by remember { mutableStateOf(hariIni()) }
    var reguDraft by remember { mutableStateOf(0) }
    var shiftDraft by remember { mutableStateOf("") }

    var tanggal by remember { mutableStateOf(tglDraft) }
    var reguId by remember { mutableStateOf(0) }
    var shift by remember { mutableStateOf("") }

    var reguList by remember { mutableStateOf<List<SimpleItem>>(emptyList()) }
    var data by remember { mutableStateOf<LaporanHarianLive?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableStateOf(0) }
    var cetakBuka by remember { mutableStateOf(false) }

    LaunchedEffect(refreshKey) {
        // Gagal ambil daftar regu bukan alasan memblokir halaman: filter cukup kosong.
        (app.adminRepo.reguList() as? AppResult.Success)?.let { reguList = it.data }
    }

    LaunchedEffect(tanggal, reguId, shift, refreshKey) {
        loading = true
        when (val r = app.adminRepo.laporanHarian(tanggal, reguId, shift)) {
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
            Column(Modifier.fillMaxSize().background(LhBg).statusBarsPadding()) {
                TopBar(user = user, onNotif = onNotif, onProfile = onProfile)

                // Dialog PDF: di luar Column scroll supaya menutup seluruh layar.
                if (cetakBuka) {
                    DialogCetak(
                        tanggalAwal = tanggal,
                        reguIdAwal = if (reguId > 0) reguId else (reguList.firstOrNull()?.id ?: 0),
                        shiftAwal = shift,
                        regu = reguList,
                        chiefs = data?.chiefs ?: emptyList(),
                        jabatanOpsi = data?.jabatan ?: emptyList(),
                        onDismiss = { cetakBuka = false },
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp)
                        .padding(top = 12.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    BannerLaporan(onBack)
                    KartuAksiCetak(onCetak = { cetakBuka = true })

                    val d = data
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MetrikKartu(
                            label = "Total APAR",
                            nilai = (d?.totalAset ?: 0).toString(),
                            catatan = "Unit Penugasan",
                            dari = Color(0xFF0284C7), ke = Color(0xFF0369A1),
                            ikon = Icons.Rounded.Inventory2,
                            modifier = Modifier.weight(1f),
                        )
                        MetrikKartu(
                            label = "Dicek Hari Ini",
                            nilai = (d?.checked ?: 0).toString(),
                            catatan = "Selesai Inspeksi",
                            dari = Color(0xFF10B981), ke = Color(0xFF059669),
                            ikon = Icons.Rounded.CheckCircle,
                            modifier = Modifier.weight(1f),
                        )
                        MetrikKartu(
                            label = "Progres",
                            nilai = "${d?.persen ?: 0}%",
                            catatan = "Laju Shift Aktif",
                            dari = Color(0xFFF59E0B), ke = Color(0xFFD97706),
                            ikon = Icons.Rounded.ShowChart,
                            modifier = Modifier.weight(1f),
                        )
                    }

                    KartuFilterLaporan(
                        tanggal = tglDraft,
                        onTanggal = { tglDraft = it },
                        regu = reguList,
                        reguId = reguDraft,
                        onRegu = { reguDraft = it },
                        shift = shiftDraft,
                        onShift = { shiftDraft = it },
                        onTampilkan = {
                            tanggal = tglDraft
                            reguId = reguDraft
                            shift = shiftDraft
                        },
                    )

                    d?.let { RingkasanPenugasan(it) }

                    when {
                        loading -> Box(
                            Modifier.fillMaxWidth().height(150.dp),
                            contentAlignment = Alignment.Center,
                        ) { CircularProgressIndicator(color = SimapTeal) }

                        error != null -> Text(error!!, fontSize = 12.sp, color = Color(0xFFBE123C))

                        d != null && d.items.isEmpty() -> Text(
                            "Belum ada pengecekan pada ${ddmm(tanggal)}" +
                                if (shift.isNotBlank()) " (shift ${shift})" else "" + ".",
                            fontSize = 11.sp,
                            color = Slate400,
                            modifier = Modifier.padding(vertical = 14.dp),
                        )

                        d != null -> {
                            d.items.forEachIndexed { i, baris -> KartuPengecekan(i + 1, baris) }
                            if (d.items.isNotEmpty()) {
                                Text(
                                    "Menampilkan ${d.items.size} dari ${d.checked} data",
                                    fontSize = 10.sp,
                                    color = Slate400,
                                    modifier = Modifier.padding(top = 2.dp, start = 2.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BannerLaporan(onBack: (() -> Unit)?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF0D9488), Color(0xFF06B6D4)))),
    ) {
        OrnamenHeader(Modifier.align(Alignment.BottomEnd).padding(4.dp), ukuran = 104.dp, alpha = 0.16f)
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Home",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = if (onBack != null) Modifier.clickable { onBack() } else Modifier,
                )
                Icon(
                    Icons.Rounded.ChevronRight, null,
                    Modifier.size(11.dp), tint = Color.White.copy(alpha = 0.6f),
                )
                Text("Laporan Harian", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Text(
                "Laporan Harian",
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
            )
            Text(
                "Monitoring real-time kesiapan proteksi kebakaran bandara",
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.85f),
            )
        }
    }
}

@Composable
private fun MetrikKartu(
    label: String,
    nilai: String,
    catatan: String,
    dari: Color,
    ke: Color,
    ikon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.linearGradient(listOf(dari, ke)))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(ikon, null, Modifier.size(13.dp), tint = Color.White.copy(alpha = 0.9f))
            Spacer(Modifier.width(4.dp))
            Text(
                label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.9f),
                maxLines = 1,
            )
        }
        Text(nilai, fontSize = 21.sp, fontWeight = FontWeight.Black, color = Color.White)
        Text(catatan, fontSize = 8.sp, color = Color.White.copy(alpha = 0.85f), maxLines = 1)
    }
}

@Composable
private fun KartuFilterLaporan(
    tanggal: String,
    onTanggal: (String) -> Unit,
    regu: List<SimpleItem>,
    reguId: Int,
    onRegu: (Int) -> Unit,
    shift: String,
    onShift: (String) -> Unit,
    onTampilkan: () -> Unit,
) {
    var picker by remember { mutableStateOf(false) }
    var reguOpen by remember { mutableStateOf(false) }
    var shiftOpen by remember { mutableStateOf(false) }
    val reguNama = regu.firstOrNull { it.id == reguId }?.nama

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, LhBorder, RoundedCornerShape(14.dp))
            .padding(11.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KolomFilter("Tanggal", Modifier.weight(1f)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(LhSubtle)
                        .border(1.dp, LhBorder, RoundedCornerShape(8.dp))
                        .clickable { picker = true }
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(ddmm(tanggal), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = LhSlate700)
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Rounded.DateRange, null, Modifier.size(13.dp), tint = Slate400)
                }
            }
            KolomFilter("Regu", Modifier.weight(1f)) {
                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(LhSubtle)
                            .border(1.dp, LhBorder, RoundedCornerShape(8.dp))
                            .clickable { reguOpen = true }
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            reguNama ?: "Semua Regu",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = LhSlate700,
                            maxLines = 1,
                        )
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Rounded.ChevronRight, null, Modifier.size(12.dp), tint = Slate400)
                    }
                    DropdownMenu(expanded = reguOpen, onDismissRequest = { reguOpen = false }) {
                        DropdownMenuItem(
                            text = { Text("Semua Regu", fontSize = 12.sp) },
                            onClick = { onRegu(0); reguOpen = false },
                        )
                        regu.forEach { r ->
                            DropdownMenuItem(
                                text = { Text(r.nama, fontSize = 12.sp) },
                                onClick = { onRegu(r.id); reguOpen = false },
                            )
                        }
                    }
                }
            }
            KolomFilter("Shift", Modifier.weight(1f)) {
                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(LhSubtle)
                            .border(1.dp, LhBorder, RoundedCornerShape(8.dp))
                            .clickable { shiftOpen = true }
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            if (shift.isBlank()) "Semua" else shift.replaceFirstChar { it.uppercase() },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = LhSlate700,
                        )
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Rounded.ChevronRight, null, Modifier.size(12.dp), tint = Slate400)
                    }
                    DropdownMenu(expanded = shiftOpen, onDismissRequest = { shiftOpen = false }) {
                        listOf("" to "Semua Shift", "pagi" to "Shift Pagi", "malam" to "Shift Malam")
                            .forEach { (v, label) ->
                                DropdownMenuItem(
                                    text = { Text(label, fontSize = 12.sp) },
                                    onClick = { onShift(v); shiftOpen = false },
                                )
                            }
                    }
                }
            }
        }

        HorizontalDivider(color = LhHair)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFF0FDFA))
                    .border(1.dp, Color(0xFF99F6E4), RoundedCornerShape(6.dp))
                    .padding(horizontal = 7.dp, vertical = 3.dp),
            ) {
                Text(
                    reguNama ?: "Semua Regu",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF115E59),
                )
            }
            Spacer(Modifier.weight(1f))
            Button(
                onClick = { onTampilkan(); picker = false },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            ) {
                Text("Tampilkan", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }

    if (picker) {
        val dpState = rememberDatePickerState(initialSelectedDateMillis = ymdKeMillis(tanggal))
        DatePickerDialog(
            onDismissRequest = { picker = false },
            confirmButton = {
                TextButton(onClick = {
                    dpState.selectedDateMillis?.let { onTanggal(millisKeYmd(it)) }
                    picker = false
                }) { Text("Pilih", color = SimapTeal, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { picker = false }) { Text("Batal", color = LhSlate500) }
            },
        ) { DatePicker(state = dpState) }
    }
}

@Composable
private fun KolomFilter(label: String, modifier: Modifier = Modifier, isi: @Composable () -> Unit) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            label.uppercase(),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = LhSlate500,
        )
        isi()
    }
}

@Composable
private fun RingkasanPenugasan(d: LaporanHarianLive) {
    val s = d.summary.firstOrNull() ?: return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, LhBorder, RoundedCornerShape(14.dp))
            .padding(11.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 7.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(Modifier.weight(1f)) {
                Text("PENUGASAN", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Slate400)
                Text(
                    s.penugasanNama ?: "-",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("REGU BERTUGAS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Slate400)
                Text(
                    s.reguNama ?: "-",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LhSlate700,
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .background(LhHair)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Progres Hari Ini", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = LhSlate500)
            Spacer(Modifier.weight(1f))
            Text(
                "${s.checkedToday} / ${s.total} Unit",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = LhSlate700,
            )
        }
        ProgressBar(if (s.total > 0) s.checkedToday * 100 / s.total else 0)
        if (d.summary.size > 1) {
            Text(
                "+${d.summary.size - 1} penugasan lain pada periode ini",
                fontSize = 9.sp,
                color = Slate400,
            )
        }
    }
}

@Composable
private fun KartuPengecekan(nomor: Int, b: LaporanBaris) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, LhBorder, RoundedCornerShape(14.dp))
            .padding(11.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(LhHair)
                    .border(1.dp, LhBorder, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text("$nomor", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = LhSlate700)
            }
            Spacer(Modifier.width(7.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        b.nama,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Slate900,
                        maxLines = 1,
                    )
                    if (!b.merek.isNullOrBlank()) {
                        Spacer(Modifier.width(5.dp))
                        Text(
                            b.merek!!.uppercase(),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate400,
                            maxLines = 1,
                        )
                    }
                }
                Text(
                    b.slug,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = LhRose,
                    fontFamily = FontFamily.Monospace,
                )
            }
            StatusChip("Selesai", ChipTone.GOOD)
        }

        HorizontalDivider(color = LhHair)

        Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetaBaris(Icons.Rounded.Place, "Lokasi", b.lokasi, Modifier.weight(1f))
            MetaBaris(Icons.Rounded.Inventory2, "Kategori", b.jenis, Modifier.weight(1f))
        }
        Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetaBaris(Icons.Rounded.Person, "Regu", b.reguNama, Modifier.weight(1f))
            MetaBaris(Icons.Rounded.Person, "Pemeriksa", b.pemeriksa, Modifier.weight(1f))
        }

        HorizontalDivider(color = LhBorder)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Waktu Cek:", fontSize = 9.sp, color = Slate400)
            Spacer(Modifier.weight(1f))
            Text(
                waktuPanjang(b.checkedAt),
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = LhSlate500,
                fontFamily = FontFamily.Monospace,
            )
        }
    }
}

@Composable
private fun MetaBaris(ikon: ImageVector, label: String, nilai: String?, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxHeight(), verticalAlignment = Alignment.CenterVertically) {
        Icon(ikon, null, Modifier.size(12.dp), tint = Slate400)
        Spacer(Modifier.width(4.dp))
        Text("$label:", fontSize = 9.sp, color = LhSlate500, maxLines = 1)
        Spacer(Modifier.width(3.dp))
        Text(
            nilai?.ifBlank { "-" } ?: "-",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = LhSlate700,
            maxLines = 1,
        )
    }
}

// ===== Cetak PDF =====

@Composable
private fun KartuAksiCetak(onCetak: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF0D9488), Color(0xFF06B6D4))))
            .padding(horizontal = 11.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.DateRange, null, Modifier.size(15.dp), tint = Color.White)
        }
        Spacer(Modifier.width(8.dp))
        Column {
            Text("LAPORAN HARIAN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Pengecekan APAR ARFF", fontSize = 9.sp, color = Color.White.copy(alpha = 0.85f))
        }
        Spacer(Modifier.weight(1f))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .clickable { onCetak() }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.Print, null, Modifier.size(12.dp), tint = Color(0xFF0F766E))
            Spacer(Modifier.width(4.dp))
            Text("Cetak PDF", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F766E))
        }
    }
}

@Composable
private fun DialogCetak(
    tanggalAwal: String,
    reguIdAwal: Int,
    shiftAwal: String,
    regu: List<SimpleItem>,
    chiefs: List<String>,
    jabatanOpsi: List<String>,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val app = context.applicationContext as SimapApp
    val scope = rememberCoroutineScope()

    var tanggal by remember { mutableStateOf(tanggalAwal.ifBlank { hariIni() }) }
    var reguId by remember { mutableStateOf(reguIdAwal) }
    var shift by remember { mutableStateOf(shiftAwal) }
    var jabatan by remember { mutableStateOf("") }
    var chief by remember { mutableStateOf(chiefs.firstOrNull() ?: "") }
    var fotos by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var picker by remember { mutableStateOf(false) }

    val pilihFoto = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        // Server memuat maksimal 6 foto per PDF.
        fotos = uris.filterNotNull().take(6)
    }

    val reguNama = regu.firstOrNull { it.id == reguId }?.nama ?: "— pilih regu —"
    val bisaKirim = !busy && fotos.isNotEmpty() && reguId > 0 && chief.isNotBlank()

    Dialog(
        onDismissRequest = { if (!busy) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 24.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White),
        ) {
            // ===== Header =====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(Color(0xFF0D9488), Color(0xFF06B6D4))))
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(Color.White.copy(alpha = 0.20f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.Print, null, Modifier.size(16.dp), tint = Color.White)
                }
                Spacer(Modifier.width(9.dp))
                Column(Modifier.weight(1f)) {
                    Text("Cetak Laporan Harian", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(
                        "SIMAP ARFF BDJ • Injourney Airports",
                        fontSize = 9.sp,
                        color = Color.White.copy(alpha = 0.85f),
                    )
                }
                IconButton(onClick = { if (!busy) onDismiss() }, modifier = Modifier.size(26.dp)) {
                    Icon(Icons.Rounded.Close, "Tutup", Modifier.size(15.dp), tint = Color.White.copy(alpha = 0.85f))
                }
            }

            // ===== Isi form =====
            Column(
                modifier = Modifier
                    .heightIn(max = 430.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                KolomFilter("Tanggal") {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(LhSubtle)
                            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(10.dp))
                            .clickable { picker = true }
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(ddmm(tanggal), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = LhSlate700)
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Rounded.DateRange, null, Modifier.size(14.dp), tint = Slate400)
                    }
                }

                DropdownKotak(
                    label = "Regu",
                    nilai = reguNama,
                    opsi = regu.map { it.id.toString() to it.nama },
                    onPilih = { reguId = it.toIntOrNull() ?: 0 },
                )

                DropdownKotak(
                    label = "Shift",
                    nilai = if (shift.isBlank()) "Semua Shift" else "Shift ${shift.replaceFirstChar { c -> c.uppercase() }}",
                    opsi = listOf(
                        "" to "Semua Shift",
                        "pagi" to "Shift Pagi (07.00 - 15.00)",
                        "malam" to "Shift Malam (23.00 - 07.00)",
                    ),
                    onPilih = { shift = it },
                )

                KolomFilter("Jabatan Penandatangan") {
                    DropdownKotak(
                        label = "",
                        nilai = if (jabatan.isBlank()) "ARFF Operation Chief (default)" else "$jabatan — ${jabatanPanjang(jabatan)}",
                        opsi = listOf("" to "ARFF Operation Chief (default)") +
                            jabatanOpsi.map { it to "$it — ${jabatanPanjang(it)}" },
                        onPilih = { jabatan = it },
                    )
                    Text(
                        "Jabatan ini menentukan gelar pada TTD chief (kiri tetap Faria Sulistiawan).",
                        fontSize = 9.sp,
                        color = LhSlate500,
                    )
                }

                DropdownKotak(
                    label = "Penandatanganan (Chief)",
                    nilai = chief.ifBlank { "— pilih chief —" },
                    opsi = chiefs.map { it to it },
                    onPilih = { chief = it },
                )

                // ===== Foto dokumentasi =====
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "FOTO DOKUMENTASI *",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = LhSlate500,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                            .clickable { pilihFoto.launch("image/*") }
                            .padding(horizontal = 10.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .border(1.dp, LhBorder, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Rounded.Add, null, Modifier.size(15.dp), tint = Color(0xFF0D9488))
                        }
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Pilih Foto", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LhSlate700)
                            Text(
                                if (fotos.isEmpty()) "Belum ada file dipilih" else "${fotos.size} foto terpilih siap diunggah",
                                fontSize = 9.sp,
                                color = if (fotos.isEmpty()) LhSlate500 else Color(0xFF0F766E),
                            )
                        }
                        Text(
                            "Multi-upload",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0D9488),
                            modifier = Modifier
                                .clip(RoundedCornerShape(5.dp))
                                .background(Color(0xFFF0FDFA))
                                .border(1.dp, Color(0xFF99F6E4), RoundedCornerShape(5.dp))
                                .padding(horizontal = 6.dp, vertical = 3.dp),
                        )
                    }
                    Text(
                        "Wajib minimal 1 foto, boleh lebih dari satu (maks. 6). Foto tampil di halaman 3 PDF.",
                        fontSize = 9.sp,
                        color = LhSlate500,
                    )
                    if (fotos.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            fotos.take(4).forEach { uri ->
                                AsyncImage(
                                    model = uri,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, LhBorder, RoundedCornerShape(8.dp)),
                                )
                            }
                            Box(
                                Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                                    .clickable { fotos = emptyList() },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("Hapus", fontSize = 8.sp, color = LhSlate500)
                            }
                        }
                    }
                }

                // ===== Ringkasan parameter otomatis =====
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(1.dp, LhBorder, RoundedCornerShape(12.dp))
                        .padding(9.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        "Parameter Otomatis SIMAP",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = LhSlate700,
                    )
                    Text("Format: A4 Resmi BDJ  •  Header: otomatis SIMAP", fontSize = 9.sp, color = LhSlate500)
                    Text("Watermark: ARFF Aktif  •  Lampiran: maks. 6 foto", fontSize = 9.sp, color = LhSlate500)
                    Text("TTD digital + QR verifikasi ikut tercetak.", fontSize = 9.sp, color = LhSlate500)
                }

                error?.let { Text(it, fontSize = 10.sp, color = Color(0xFFBE123C)) }
            }

            // ===== Footer aksi =====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(Modifier.weight(1f))
                Row(
                    modifier = Modifier
                        .height(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF1F5F9))
                        .border(1.dp, LhBorder, RoundedCornerShape(10.dp))
                        .clickable(enabled = !busy) { onDismiss() }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Batal", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = LhSlate700)
                }
                Row(
                    modifier = Modifier
                        .height(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (bisaKirim) Color(0xFF0D9488) else Color(0xFF94A3B8))
                        .clickable(enabled = bisaKirim) {
                            val payload = fotos.mapNotNull { uri ->
                                runCatching {
                                    val nama = uri.lastPathSegment?.substringAfterLast('/') ?: "foto.jpg"
                                    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                                        ?: ByteArray(0)
                                    if (bytes.isEmpty()) null else nama to bytes
                                }.getOrNull()
                            }
                            if (payload.isEmpty()) {
                                error = "Foto tidak terbaca. Pilih foto lain."
                            } else {
                                scope.launch {
                                    busy = true
                                    error = null
                                    when (val r = app.adminRepo.laporanHarianPdf(
                                        tanggal = tanggal,
                                        reguId = reguId,
                                        shift = shift,
                                        chief = chief,
                                        jabatan = jabatan,
                                        foto = payload,
                                    )) {
                                        is AppResult.Success -> {
                                            busy = false
                                            val pesan = bukaPdf(context, r.data, tanggal)
                                            if (pesan != null) error = pesan else onDismiss()
                                        }
                                        is AppResult.Error -> {
                                            busy = false
                                            error = mapError(r.message)
                                        }
                                    }
                                }
                            }
                        }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (busy) {
                        CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp, color = Color.White)
                        Spacer(Modifier.width(6.dp))
                        Text("Membuat...", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    } else {
                        Icon(Icons.Rounded.Print, null, Modifier.size(12.dp), tint = Color.White)
                        Spacer(Modifier.width(5.dp))
                        Text("Buat PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }

    if (picker) {
        val dpState = rememberDatePickerState(initialSelectedDateMillis = ymdKeMillis(tanggal))
        DatePickerDialog(
            onDismissRequest = { picker = false },
            confirmButton = {
                TextButton(onClick = {
                    dpState.selectedDateMillis?.let { tanggal = millisKeYmd(it) }
                    picker = false
                }) { Text("Pilih", color = SimapTeal, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { picker = false }) { Text("Batal", color = LhSlate500) }
            },
        ) { DatePicker(state = dpState) }
    }
}

@Composable
private fun DropdownKotak(
    label: String,
    nilai: String,
    opsi: List<Pair<String, String>>,
    onPilih: (String) -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    KolomFilter(label) {
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(LhSubtle)
                    .border(1.dp, LhBorder, RoundedCornerShape(8.dp))
                    .clickable { open = true }
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(nilai, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = LhSlate700, maxLines = 1)
                Spacer(Modifier.weight(1f))
                Icon(Icons.Rounded.ChevronRight, null, Modifier.size(12.dp), tint = Slate400)
            }
            DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                opsi.forEach { (v, l) ->
                    DropdownMenuItem(
                        text = { Text(l, fontSize = 12.sp) },
                        onClick = { onPilih(v); open = false },
                    )
                }
            }
        }
    }
}

private fun jabatanPanjang(kode: String): String = when (kode) {
    "PLT" -> "Pelaksana Tugas"
    "PH" -> "Pelaksana Harian"
    "PGS" -> "Pengganti Sementara"
    else -> kode
}

/** Simpan PDF ke cache lalu buka dengan aplikasi PDF. Null = sukses, selain itu pesan error. */
private fun bukaPdf(context: android.content.Context, bytes: ByteArray, tanggal: String): String? = try {
    val dir = File(context.cacheDir, "laporan").apply { mkdirs() }
    val file = File(dir, "Laporan_Harian_${tanggal.replace("-", "")}.pdf")
    file.writeBytes(bytes)
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val buka = Intent(Intent.ACTION_VIEW)
        .setDataAndType(uri, "application/pdf")
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        context.startActivity(buka)
    } catch (e: Exception) {
        // Tidak ada pembaca PDF: tawarkan bagikan/simpan lewat aplikasi lain.
        val kirim = Intent(Intent.ACTION_SEND)
            .setType("application/pdf")
            .putExtra(Intent.EXTRA_STREAM, uri)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(Intent.createChooser(kirim, "Buka PDF").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
    null
} catch (e: Exception) {
    "Gagal menyimpan PDF: ${e.message ?: "tidak diketahui"}"
}

// ===== util tanggal =====

private fun hariIni(): String {
    val c = Calendar.getInstance(ZONA_WITA)
    return String.format(
        Locale.US, "%04d-%02d-%02d",
        c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH),
    )
}

private fun ymdKeMillis(ymd: String): Long? {
    val p = ymd.split("-")
    if (p.size != 3) return null
    val y = p[0].toIntOrNull() ?: return null
    val m = p[1].toIntOrNull() ?: return null
    val d = p[2].toIntOrNull() ?: return null
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear(); set(y, m - 1, d, 12, 0, 0)
    }.timeInMillis
}

private fun millisKeYmd(ms: Long): String {
    val c = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = ms }
    return String.format(
        Locale.US, "%04d-%02d-%02d",
        c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH),
    )
}

/** "2026-10-22" -> "22/10/2026" */
private fun ddmm(ymd: String): String {
    val p = ymd.split("-")
    return if (p.size == 3) "${p[2]}/${p[1]}/${p[0]}" else ymd
}

/** "2026-10-22 20:57:18" -> "22 Okt 2026 - 20:57:18" */
private fun waktuPanjang(iso: String?): String {
    if (iso.isNullOrBlank()) return "-"
    return try {
        val masuk = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val keluar = SimpleDateFormat("dd MMM yyyy - HH:mm:ss", Locale("id", "ID"))
        masuk.parse(iso)?.let { keluar.format(it) } ?: iso
    } catch (e: Exception) {
        iso
    }
}
