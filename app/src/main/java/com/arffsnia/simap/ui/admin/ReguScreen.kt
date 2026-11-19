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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.rounded.Save
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Dialog
import com.arffsnia.simap.SimapApp
import com.arffsnia.simap.data.dto.ReguFull
import com.arffsnia.simap.data.dto.ReguStats
import com.arffsnia.simap.data.dto.ReguUser
import com.arffsnia.simap.data.repo.AppResult
import com.arffsnia.simap.ui.mapError
import com.arffsnia.simap.ui.theme.SimapTeal
import com.arffsnia.simap.ui.theme.Slate400
import com.arffsnia.simap.ui.theme.Slate900
import kotlinx.coroutines.launch

private val TealSoft = Color(0xFFCCFBF1)
private val UsrBorderRegu = Color(0xFFE2E8F0)
private val TealInk = Color(0xFF0F766E)

@Composable
fun ReguScreen(onBack: (() -> Unit)? = null) {
    val app = LocalContext.current.applicationContext as SimapApp
    val scope = rememberCoroutineScope()

    var items by remember { mutableStateOf<List<ReguFull>>(emptyList()) }
    var stats by remember { mutableStateOf(ReguStats()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var pesan by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableStateOf(0) }

    var formOpen by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<ReguFull?>(null) }
    var hapusTarget by remember { mutableStateOf<ReguFull?>(null) }
    var anggotaTarget by remember { mutableStateOf<ReguFull?>(null) }

    LaunchedEffect(refreshKey) {
        loading = items.isEmpty()
        when (val r = app.adminRepo.reguAll()) {
            is AppResult.Success -> {
                items = r.data.items
                stats = r.data.stats
                error = null
            }
            is AppResult.Error -> error = mapError(r.message)
        }
        loading = false
    }

    fun reload(msg: String) {
        pesan = msg
        refreshKey++
    }

    Column(Modifier.fillMaxSize().background(Color(0xFFF4F7FB)).statusBarsPadding()) {
        BannerRegu(onBack)

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp)
                .padding(top = 10.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                KartuMetrikRegu("Total Regu", stats.totalRegu, Modifier.weight(1f))
                KartuMetrikRegu("Anggota", stats.totalAnggota, Modifier.weight(1f))
                KartuMetrikRegu("Regu Aktif", stats.reguAktif, Modifier.weight(1f))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(26.dp).clip(RoundedCornerShape(7.dp)).background(TealSoft),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Rounded.Groups, null, Modifier.size(14.dp), tint = TealInk) }
                Spacer(Modifier.width(7.dp))
                Text("Daftar Regu", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate900)
                Spacer(Modifier.weight(1f))
                Row(
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SimapTeal)
                        .clickable { editTarget = null; formOpen = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.Add, null, Modifier.size(12.dp), tint = Color.White)
                    Spacer(Modifier.width(3.dp))
                    Text("Tambah Regu", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }

            pesan?.let {
                Text(
                    it, fontSize = 9.sp, color = Color(0xFF065F46),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFECFDF5))
                        .padding(7.dp),
                )
            }

            when {
                loading -> Box(Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SimapTeal)
                }
                error != null -> Text(error!!, fontSize = 10.sp, color = Color(0xFFBE123C))
                items.isEmpty() -> Text("Belum ada regu. Tambahkan regu pertama.", fontSize = 10.sp, color = Slate400)
                else -> items.forEach { r ->
                    KartuRegu(
                        r = r,
                        onAnggota = { anggotaTarget = r },
                        onEdit = { editTarget = r; formOpen = true },
                        onHapus = { hapusTarget = r },
                    )
                }
            }
        }
    }

    if (formOpen) {
        DialogReguForm(
            awal = editTarget,
            onDismiss = { formOpen = false },
            onSimpan = { nama, ket ->
                scope.launch {
                    val t = editTarget
                    val res = if (t == null) app.adminRepo.reguCreate(nama, ket)
                    else app.adminRepo.reguUpdate(t.id, nama, ket)
                    when (res) {
                        is AppResult.Success -> { formOpen = false; reload("Regu \"$nama\" tersimpan.") }
                        is AppResult.Error -> pesan = mapError(res.message)
                    }
                }
            },
        )
    }

    anggotaTarget?.let { r ->
        DialogAnggotaRegu(
            regu = r,
            onDismiss = { anggotaTarget = null },
            onSimpan = { ids ->
                scope.launch {
                    when (val res = app.adminRepo.reguSetAnggota(r.id, ids)) {
                        is AppResult.Success -> { anggotaTarget = null; reload("Anggota ${r.nama} diperbarui (${ids.size}).") }
                        is AppResult.Error -> pesan = mapError(res.message)
                    }
                }
            },
        )
    }

    hapusTarget?.let { r ->
        DialogKonfirmasiHapus(
            nama = r.nama,
            jmlAnggota = r.jmlAnggota,
            onDismiss = { hapusTarget = null },
            onHapus = {
                scope.launch {
                    when (val res = app.adminRepo.reguDelete(r.id)) {
                        is AppResult.Success -> { hapusTarget = null; reload("Regu \"${r.nama}\" dihapus.") }
                        is AppResult.Error -> pesan = mapError(res.message)
                    }
                }
            },
        )
    }
}

@Composable
private fun BannerRegu(onBack: (() -> Unit)?) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(Brush.linearGradient(listOf(Color(0xFF0D9488), Color(0xFF06B6D4)))),
    ) {
    OrnamenHeader(Modifier.align(Alignment.BottomEnd), ukuran = 104.dp, alpha = 0.16f)
    Column(
        Modifier
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Home",
                fontSize = 9.sp,
                color = Color.White.copy(alpha = 0.8f),
                modifier = if (onBack != null) Modifier.clickable { onBack() } else Modifier,
            )
            Text(" / ", fontSize = 9.sp, color = Color.White.copy(alpha = 0.6f))
            Text("Kelola Regu", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
        Text("Kelola Regu", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        Text("Regu & anggota ARFF BDJ", fontSize = 9.sp, color = Color.White.copy(alpha = 0.88f))
    }
    }
}

@Composable
private fun KartuMetrikRegu(label: String, nilai: Int, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SimapTeal)
            .padding(9.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Groups, null, Modifier.size(10.dp), tint = Color.White.copy(alpha = 0.9f))
            Spacer(Modifier.width(3.dp))
            Text(label, fontSize = 8.sp, color = Color.White.copy(alpha = 0.92f), maxLines = 1)
        }
        Text("$nilai", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
    }
}

@Composable
private fun KartuRegu(
    r: ReguFull,
    onAnggota: () -> Unit,
    onEdit: () -> Unit,
    onHapus: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
            .padding(11.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(7.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (r.jmlAnggota > 0) Color(0xFF10B981) else Color(0xFFCBD5E1)),
            )
            Spacer(Modifier.width(6.dp))
            Column(Modifier.weight(1f)) {
                Text(r.nama, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate900)
                Text(
                    "${r.jmlAnggota} Anggota" + (r.keterangan?.takeIf { it.isNotBlank() }?.let { " • $it" } ?: ""),
                    fontSize = 8.sp, color = Slate400, maxLines = 1,
                )
            }
            AksiKecil(Icons.Rounded.PersonAdd, Color(0xFF0F766E), Color(0xFFCCFBF1)) { onAnggota() }
            Spacer(Modifier.width(4.dp))
            AksiKecil(Icons.Rounded.Edit, Color(0xFFB45309), Color(0xFFFEF3C7)) { onEdit() }
            Spacer(Modifier.width(4.dp))
            AksiKecil(Icons.Rounded.DeleteOutline, Color(0xFFBE123C), Color(0xFFFFE4E6)) { onHapus() }
        }

        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF8FAFC))
                .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text("ANGGOTA DITUGASKAN", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Slate400)
            if (r.anggota.isEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Belum ada anggota yang didaftarkan", fontSize = 8.sp, color = Slate400)
                    Spacer(Modifier.weight(1f))
                    Text(
                        "+ Tugaskan",
                        fontSize = 8.sp, fontWeight = FontWeight.SemiBold, color = TealInk,
                        modifier = Modifier.clickable { onAnggota() },
                    )
                }
            } else {
                r.anggota.chunked(2).forEach { baris ->
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        baris.forEach { a ->
                            Row(
                                Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(999.dp))
                                    .padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Rounded.PersonOutline, null, Modifier.size(9.dp), tint = Slate400)
                                Spacer(Modifier.width(3.dp))
                                Text(a.nama, fontSize = 8.sp, color = Color(0xFF334155), maxLines = 1)
                            }
                        }
                        if (baris.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun AksiKecil(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    teks: Color,
    latar: Color,
    onClick: () -> Unit,
) {
    Box(
        Modifier
            .size(26.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(latar)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) { Icon(icon, null, Modifier.size(13.dp), tint = teks) }
}

@Composable
private fun DialogReguForm(awal: ReguFull?, onDismiss: () -> Unit, onSimpan: (String, String) -> Unit) {
    var nama by remember { mutableStateOf(awal?.nama ?: "") }
    var ket by remember { mutableStateOf(awal?.keterangan ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Text(
                if (awal == null) "Tambah Regu" else "Edit Regu",
                fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900,
            )
            KolomInput("Nama Regu", nama) { nama = it }
            KolomInput("Keterangan (opsional)", ket) { ket = it }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TombolDialog("Batal", false) { onDismiss() }
                TombolDialog(
                    if (awal == null) "Tambah" else "Simpan",
                    true,
                    aktif = nama.isNotBlank(),
                ) { onSimpan(nama.trim(), ket.trim()) }
            }
        }
    }
}

@Composable
private fun DialogAnggotaRegu(regu: ReguFull, onDismiss: () -> Unit, onSimpan: (List<Int>) -> Unit) {
    val app = LocalContext.current.applicationContext as SimapApp
    var users by remember { mutableStateOf<List<ReguUser>>(emptyList()) }
    var dipilih by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var loading by remember { mutableStateOf(true) }
    var cari by remember { mutableStateOf("") }

    LaunchedEffect(regu.id) {
        when (val r = app.adminRepo.reguAnggota(regu.id)) {
            is AppResult.Success -> {
                users = r.data.items
                dipilih = r.data.items.filter { it.terpilih }.map { it.id }.toSet()
            }
            is AppResult.Error -> Unit
        }
        loading = false
    }

    val tampil = users.filter { u ->
        cari.isBlank() ||
            u.nama.contains(cari, true) ||
            (u.username ?: "").contains(cari, true) ||
            (u.level ?: "").contains(cari, true)
    }

    Dialog(onDismissRequest = onDismiss, properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                    .background(Color.White)
                    .navigationBarsPadding(),
            ) {
                // ===== Header =====
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(Color(0xFF0D9488), Color(0xFF06B6D4))))
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.PersonAdd, null, Modifier.size(15.dp), tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Kelola Anggota: ${regu.nama}",
                        fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White,
                        maxLines = 1, modifier = Modifier.weight(1f),
                    )
                    Box(
                        Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center,
                    ) { Text("\u2715", fontSize = 15.sp, color = Color.White.copy(alpha = 0.95f)) }
                }

                // ===== Body =====
                Column(
                    Modifier
                        .weight(1f)
                        .padding(horizontal = 14.dp)
                        .padding(top = 11.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        "PILIH ANGGOTA",
                        fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B),
                    )
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(9.dp))
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (cari.isEmpty()) {
                            Text("Cari nama, username, atau level...", fontSize = 10.sp, color = Slate400)
                        }
                        BasicTextField(
                            value = cari,
                            onValueChange = { cari = it },
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 10.sp, color = Slate900),
                            cursorBrush = SolidColor(SimapTeal),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    Box(
                        Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(Color.White)
                            .border(1.dp, UsrBorderRegu, RoundedCornerShape(9.dp))
                            .padding(3.dp),
                    ) {
                        if (loading) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = SimapTeal, strokeWidth = 2.dp)
                            }
                        } else {
                            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                                tampil.forEachIndexed { idx, u ->
                                    val aktif = u.id in dipilih
                                    if (idx > 0) {
                                        Box(
                                            Modifier
                                                .fillMaxWidth()
                                                .height(1.dp)
                                                .background(Color(0xFFF1F5F9)),
                                        )
                                    }
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(7.dp))
                                            .background(if (aktif) Color(0xFFEFF6FF) else Color.Transparent)
                                            .clickable { dipilih = if (aktif) dipilih - u.id else dipilih + u.id }
                                            .padding(horizontal = 9.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Box(
                                            Modifier
                                                .size(15.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(if (aktif) Color(0xFF2563EB) else Color.White)
                                                .border(
                                                    1.dp,
                                                    if (aktif) Color(0xFF2563EB) else Color(0xFF94A3B8),
                                                    RoundedCornerShape(3.dp),
                                                ),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            if (aktif) Icon(Icons.Rounded.Check, null, Modifier.size(10.dp), tint = Color.White)
                                        }
                                        Spacer(Modifier.width(11.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                u.nama.uppercase(),
                                                fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                                color = Color(0xFF334155), maxLines = 1,
                                            )
                                            Text(
                                                "${u.username ?: "-"} · ${(u.level ?: "User").uppercase()}",
                                                fontSize = 8.5.sp, color = Slate400, maxLines = 1,
                                            )
                                        }
                                    }
                                }
                                if (tampil.isEmpty()) {
                                    Text(
                                        "Tidak ada pengguna yang cocok.",
                                        fontSize = 9.sp, color = Slate400,
                                        modifier = Modifier.padding(10.dp),
                                    )
                                }
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(bottom = 2.dp)) {
                        Text("ⓘ", fontSize = 9.sp, color = Slate400)
                        Spacer(Modifier.width(5.dp))
                        Text(
                            "Centang pengguna yang menjadi anggota regu ini, lalu klik Simpan. " +
                                "${dipilih.size} dari ${users.size} dipilih.",
                            fontSize = 9.sp, color = Color(0xFF64748B),
                        )
                    }
                }

                // ===== Footer =====
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(0.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .height(40.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(Color(0xFF222834))
                            .clickable { onDismiss() }
                            .padding(horizontal = 18.dp),
                        contentAlignment = Alignment.Center,
                    ) { Text("Batal", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Color.White) }
                    Spacer(Modifier.width(9.dp))
                    Row(
                        Modifier
                            .height(40.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(SimapTeal)
                            .clickable { onSimpan(dipilih.toList()) }
                            .padding(horizontal = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Rounded.Save, null, Modifier.size(12.dp), tint = Color.White)
                        Spacer(Modifier.width(5.dp))
                        Text("Simpan", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogKonfirmasiHapus(nama: String, jmlAnggota: Int, onDismiss: () -> Unit, onHapus: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Text("Hapus Regu?", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
            Text(
                "\"$nama\" akan dihapus." + if (jmlAnggota > 0) " $jmlAnggota anggota akan dilepas dari regu ini (akun tetap ada)." else "",
                fontSize = 9.5.sp, color = Color(0xFF334155),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TombolDialog("Batal", false) { onDismiss() }
                TombolDialog("Hapus", true, merah = true) { onHapus() }
            }
        }
    }
}

@Composable
private fun KolomInput(label: String, value: String, onChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label.uppercase(), fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Slate400)
        Box(
            Modifier
                .fillMaxWidth()
                .height(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF8FAFC))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (value.isEmpty()) Text(label, fontSize = 9.sp, color = Slate400)
            BasicTextField(
                value = value,
                onValueChange = onChange,
                singleLine = true,
                textStyle = TextStyle(fontSize = 9.5.sp, color = Slate900),
                cursorBrush = SolidColor(SimapTeal),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun RowScope.TombolDialog(
    label: String,
    utama: Boolean,
    aktif: Boolean = true,
    merah: Boolean = false,
    onClick: () -> Unit,
) {
    val latar = when {
        !utama -> Color(0xFFF1F5F9)
        merah -> Color(0xFFE11D48)
        else -> SimapTeal
    }
    val teks = if (utama) Color.White else Color(0xFF334155)
    Box(
        Modifier
            .weight(1f)
            .height(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (aktif) latar else Color(0xFFCBD5E1))
            .clickable(enabled = aktif) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = teks)
    }
}
