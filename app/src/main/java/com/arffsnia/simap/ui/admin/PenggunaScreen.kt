package com.arffsnia.simap.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.arffsnia.simap.SimapApp
import com.arffsnia.simap.data.dto.SimpleItem
import com.arffsnia.simap.data.dto.UserItem
import com.arffsnia.simap.data.dto.UserStats
import com.arffsnia.simap.data.dto.UsersResponse
import com.arffsnia.simap.data.repo.AppResult
import com.arffsnia.simap.ui.mapError
import com.arffsnia.simap.ui.theme.SimapTeal
import com.arffsnia.simap.ui.theme.Slate400
import com.arffsnia.simap.ui.theme.Slate900
import kotlinx.coroutines.launch

private val UsrBorder = Color(0xFFE2E8F0)
private val UsrHair = Color(0xFFF1F5F9)
private val UsrSlate700 = Color(0xFF334155)

private val LEVELS = listOf("Super Admin", "Admin Regu", "User")

@Composable
fun PenggunaScreen(onBack: (() -> Unit)? = null) {
    val app = LocalContext.current.applicationContext as SimapApp
    val scope = rememberCoroutineScope()

    var data by remember { mutableStateOf<UsersResponse?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var page by remember { mutableStateOf(1) }
    var limit by remember { mutableStateOf(10) }
    var search by remember { mutableStateOf("") }
    var pesan by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableStateOf(0) }
    var formOpen by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<UserItem?>(null) }
    var hapusTarget by remember { mutableStateOf<UserItem?>(null) }
    var daftarRegu by remember { mutableStateOf<List<SimpleItem>>(emptyList()) }

    LaunchedEffect(Unit) {
        (app.adminRepo.reguList() as? AppResult.Success)?.let { daftarRegu = it.data }
    }

    LaunchedEffect(page, limit, search, refreshKey) {
        loading = data == null
        when (val r = app.adminRepo.users(page, limit, search, null, null)) {
            is AppResult.Success -> { data = r.data; error = null }
            is AppResult.Error -> error = mapError(r.message)
        }
        loading = false
    }

    fun reload(msg: String) { pesan = msg; refreshKey++ }

    val stats = data?.stats ?: UserStats()
    val items = data?.items.orEmpty()

    Column(Modifier.fillMaxSize().background(Color(0xFFF0F4F8)).statusBarsPadding()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp)
                .padding(top = 10.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            BannerPengguna(onBack)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("RINGKASAN PENGGUNA", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                Spacer(Modifier.weight(1f))
                Text(
                    "Live Sync",
                    fontSize = 8.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F766E),
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFFF0FDFA))
                        .border(1.dp, Color(0xFF99F6E4), RoundedCornerShape(999.dp))
                        .padding(horizontal = 6.dp, vertical = 1.dp),
                )
            }

            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                MetrikPengguna("Total Pengguna", stats.total, listOf(Color(0xFF14B8A6), Color(0xFF0D9488)))
                MetrikPengguna("Administrator", stats.admin, listOf(Color(0xFF0D9488), Color(0xFF14B8A6)))
                MetrikPengguna("User Biasa", stats.user, listOf(Color(0xFF14B8A6), Color(0xFF0891B2)))
                MetrikPengguna("Baru (30 Hari)", stats.baru, listOf(Color(0xFF0891B2), Color(0xFF0284C7)))
                MetrikPengguna("Nonaktif", stats.nonaktif, listOf(Color(0xFF94A3B8), Color(0xFF64748B)))
            }

            // ===== Daftar pengguna =====
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(15.dp))
                    .background(Color.White)
                    .border(1.dp, UsrBorder, RoundedCornerShape(15.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Daftar Pengguna", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SimapTeal)
                        .clickable { editTarget = null; formOpen = true },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.Add, null, Modifier.size(13.dp), tint = Color.White)
                    Spacer(Modifier.width(4.dp))
                    Text("Tambah Pengguna", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Tampil", fontSize = 8.sp, color = Color(0xFF64748B))
                    listOf(10, 25, 50).forEach { n ->
                        Text(
                            "$n",
                            fontSize = 8.sp,
                            fontWeight = if (limit == n) FontWeight.Bold else FontWeight.Medium,
                            color = if (limit == n) Color.White else UsrSlate700,
                            modifier = Modifier
                                .clip(RoundedCornerShape(5.dp))
                                .background(if (limit == n) SimapTeal else Color(0xFFF1F5F9))
                                .clickable { limit = n; page = 1 }
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                    Row(
                        Modifier
                            .weight(1f)
                            .height(30.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, UsrBorder, RoundedCornerShape(9.dp))
                            .padding(horizontal = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Rounded.Search, null, Modifier.size(11.dp), tint = Slate400)
                        Spacer(Modifier.width(4.dp))
                        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                            if (search.isEmpty()) {
                                Text("Cari nama, email, username...", fontSize = 8.5.sp, color = Slate400)
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
                }

                pesan?.let {
                    Text(
                        it, fontSize = 8.5.sp, color = Color(0xFF065F46),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFECFDF5))
                            .padding(6.dp),
                    )
                }

                when {
                    loading -> Box(Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = SimapTeal)
                    }
                    error != null -> Text(error!!, fontSize = 10.sp, color = Color(0xFFBE123C))
                    items.isEmpty() -> Text("Tidak ada pengguna yang cocok.", fontSize = 9.sp, color = Slate400)
                    else -> items.forEachIndexed { i, u ->
                        KartuPengguna(
                            u = u,
                            nomor = (page - 1) * limit + i + 1,
                            onToggle = {
                                scope.launch {
                                    when (val r = app.adminRepo.userToggle(u.id)) {
                                        is AppResult.Success -> reload(
                                            (u.fullName ?: "User") + if (u.isActive == 1) " dinonaktifkan." else " diaktifkan."
                                        )
                                        is AppResult.Error -> pesan = mapError(r.message)
                                    }
                                }
                            },
                            onEdit = { editTarget = u; formOpen = true },
                            onHapus = { hapusTarget = u },
                        )
                    }
                }

                data?.let {
                    val dari = if (it.items.isEmpty()) 0 else (it.page - 1) * it.limit + 1
                    val sampai = (dari + it.items.size - 1).coerceAtMost(it.total)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Menampilkan $dari-$sampai dari ${it.total} pengguna", fontSize = 8.sp, color = Color(0xFF64748B))
                        Spacer(Modifier.weight(1f))
                        TombolHalaman("Prev", it.page > 1) { page = it.page - 1 }
                        Text(
                            "${it.page}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White,
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(SimapTeal)
                                .padding(horizontal = 7.dp, vertical = 2.dp),
                        )
                        TombolHalaman("Next", it.page * it.limit < it.total) { page = it.page + 1 }
                    }
                }
            }
        }
    }

    if (formOpen) {
        DialogPenggunaForm(
            awal = editTarget,
            daftarRegu = daftarRegu,
            onDismiss = { formOpen = false },
            onSimpan = { body ->
                scope.launch {
                    val t = editTarget
                    val res = if (t == null) app.adminRepo.userCreate(body) else app.adminRepo.userUpdate(body + ("id" to t.id))
                    when (res) {
                        is AppResult.Success -> { formOpen = false; reload(if (t == null) "Pengguna baru ditambahkan." else "Perubahan disimpan.") }
                        is AppResult.Error -> pesan = mapError(res.message)
                    }
                }
            },
        )
    }

    hapusTarget?.let { u ->
        DialogHapusPengguna(
            nama = u.fullName ?: u.username ?: "user #${u.id}",
            onDismiss = { hapusTarget = null },
            onHapus = {
                scope.launch {
                    when (val r = app.adminRepo.userDelete(u.id)) {
                        is AppResult.Success -> { hapusTarget = null; reload("Pengguna dihapus.") }
                        is AppResult.Error -> pesan = mapError(r.message)
                    }
                }
            },
        )
    }
}

@Composable
private fun BannerPengguna(onBack: (() -> Unit)?) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF0D9488), Color(0xFF06B6D4))))
            .padding(13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Home", fontSize = 9.sp, color = Color.White.copy(alpha = 0.8f),
                    modifier = if (onBack != null) Modifier.clickable { onBack() } else Modifier,
                )
                Text(" / ", fontSize = 9.sp, color = Color.White.copy(alpha = 0.6f))
                Text("Pengguna", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
            Text("Pengguna", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
            Text(
                "Kelola akun, hak akses peran, & otentikasi tim",
                fontSize = 8.5.sp, color = Color.White.copy(alpha = 0.9f),
            )
        }
        Box(
            Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Rounded.PersonOutline, null, Modifier.size(17.dp), tint = Color.White) }
    }
}

@Composable
private fun MetrikPengguna(label: String, nilai: Int, warna: List<Color>) {
    Column(
        Modifier
            .width(104.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.linearGradient(warna))
            .padding(9.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(Color.White.copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Rounded.PersonOutline, null, Modifier.size(12.dp), tint = Color.White) }
        Text("$nilai", fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        Text(label.uppercase(), fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.92f))
    }
}

@Composable
private fun KartuPengguna(
    u: UserItem,
    nomor: Int,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onHapus: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, UsrBorder, RoundedCornerShape(12.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box {
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFFF1F5F9))
                        .border(1.dp, UsrBorder, RoundedCornerShape(999.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (!u.profileUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = u.profileUrl,
                            contentDescription = u.fullName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        Icon(Icons.Rounded.PersonOutline, null, Modifier.size(17.dp), tint = Color(0xFFCBD5E1))
                    }
                }
                Text(
                    "$nomor",
                    fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFF334155))
                        .padding(horizontal = 3.dp, vertical = 0.5.dp),
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    u.fullName?.ifBlank { u.username ?: "-" } ?: "-",
                    fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate900, maxLines = 1,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "@${u.username ?: "-"}",
                        fontSize = 8.5.sp, fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace, color = Color(0xFFBE123C), maxLines = 1,
                    )
                    Spacer(Modifier.width(4.dp))
                    ChipLevel(u.level)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Switch(
                    checked = u.isActive == 1,
                    onCheckedChange = { onToggle() },
                    modifier = Modifier.height(22.dp).width(36.dp),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF10B981),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFCBD5E1),
                        uncheckedBorderColor = Color.Transparent,
                    ),
                )
                Text(
                    if (u.isActive == 1) "Aktif" else "Nonaktif",
                    fontSize = 7.sp, fontWeight = FontWeight.Bold,
                    color = if (u.isActive == 1) Color(0xFF059669) else Slate400,
                )
            }
        }

        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(9.dp))
                .background(Color(0xFFF8FAFC))
                .padding(7.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            BarisInfo("Email", u.email ?: "—")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Regu", fontSize = 8.sp, color = Slate400, modifier = Modifier.width(46.dp))
                if (!u.reguNama.isNullOrBlank()) {
                    Text(
                        u.reguNama,
                        fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = Color.White,
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(SimapTeal)
                            .padding(horizontal = 5.dp, vertical = 1.dp),
                    )
                } else {
                    Text("—", fontSize = 8.sp, color = Slate400)
                }
            }
            BarisInfo("Dibuat", formatWaktu(u.createdAt), mono = true)
            BarisInfo("Diperbarui", formatWaktu(u.updatedAt), mono = true)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
            Spacer(Modifier.weight(1f))
            Row(
                Modifier
                    .clip(RoundedCornerShape(7.dp))
                    .background(Color(0xFFFEF3C7))
                    .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(7.dp))
                    .clickable { onEdit() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.Edit, null, Modifier.size(10.dp), tint = Color(0xFFB45309))
                Spacer(Modifier.width(3.dp))
                Text("Edit", fontSize = 8.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF92400E))
            }
            Row(
                Modifier
                    .clip(RoundedCornerShape(7.dp))
                    .background(Color(0xFFFFE4E6))
                    .border(1.dp, Color(0xFFFECDD3), RoundedCornerShape(7.dp))
                    .clickable { onHapus() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.DeleteOutline, null, Modifier.size(10.dp), tint = Color(0xFFBE123C))
                Spacer(Modifier.width(3.dp))
                Text("Hapus", fontSize = 8.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF9F1239))
            }
        }
    }
}

@Composable
private fun BarisInfo(label: String, nilai: String, mono: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 8.sp, color = Slate400, modifier = Modifier.width(46.dp))
        Text(
            nilai,
            fontSize = 8.sp,
            fontWeight = if (mono) FontWeight.Normal else FontWeight.Medium,
            fontFamily = if (mono) FontFamily.Monospace else FontFamily.Default,
            color = if (mono) Color(0xFF475569) else UsrSlate700,
            maxLines = 1,
        )
    }
}

@Composable
private fun ChipLevel(level: String?) {
    val (latar, teks, label) = when (level) {
        "Super Admin" -> Triple(Color(0xFFE11D48), Color.White, "Super Admin")
        "Admin Regu" -> Triple(Color(0xFFF97316), Color.White, "Admin Regu")
        else -> Triple(Color(0xFFCCFBF1), Color(0xFF115E59), "User")
    }
    Text(
        label,
        fontSize = 7.sp, fontWeight = FontWeight.Bold, color = teks,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(latar)
            .padding(horizontal = 5.dp, vertical = 1.dp),
    )
}

private fun formatWaktu(s: String?): String {
    if (s.isNullOrBlank()) return "—"
    return try {
        val t = s.replace("T", " ")
        val tgl = t.substring(0, 10).split("-")
        val jam = if (t.length >= 19) t.substring(11, 19) else ""
        val bulan = listOf("Jan","Feb","Mar","Apr","Mei","Jun","Jul","Agu","Sep","Okt","Nov","Des")
        val b = bulan.getOrNull(tgl[1].toInt() - 1) ?: tgl[1]
        "${tgl[2].trimStart('0')} $b ${tgl[0]} - $jam"
    } catch (e: Exception) {
        s
    }
}

@Composable
private fun TombolHalaman(label: String, aktif: Boolean, onClick: () -> Unit) {
    Text(
        label,
        fontSize = 8.sp,
        fontWeight = FontWeight.Medium,
        color = if (aktif) UsrSlate700 else Color(0xFFCBD5E1),
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, UsrBorder, RoundedCornerShape(6.dp))
            .clickable(enabled = aktif) { onClick() }
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

// ===== Dialog form tambah / edit =====
@Composable
private fun DialogPenggunaForm(
    awal: UserItem?,
    daftarRegu: List<SimpleItem>,
    onDismiss: () -> Unit,
    onSimpan: (Map<String, Any?>) -> Unit,
) {
    var nama by remember { mutableStateOf(awal?.fullName ?: "") }
    var email by remember { mutableStateOf(awal?.email ?: "") }
    var username by remember { mutableStateOf(awal?.username ?: "") }
    var level by remember { mutableStateOf(awal?.level ?: "User") }
    var regu by remember { mutableStateOf(awal?.reguNama ?: "") }
    var reguId by remember { mutableStateOf(awal?.reguId) }
    var sandi by remember { mutableStateOf("") }

    val valid = nama.isNotBlank() && email.isNotBlank() && username.isNotBlank() &&
        (awal != null || sandi.length >= 6)

    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                if (awal == null) "Tambah Pengguna" else "Edit Pengguna",
                fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900,
            )
            InputPengguna("Nama Lengkap", nama) { nama = it }
            InputPengguna("Email", email) { email = it }
            InputPengguna("Username", username) { username = it }
            DropdownPilihan("Level", level, LEVELS.map { it to it }) { level = it }
            DropdownPilihan(
                "Regu",
                regu.ifBlank { "Tanpa regu" },
                listOf("" to "Tanpa regu") + daftarRegu.map { it.nama to it.nama },
            ) { namaRegu ->
                regu = namaRegu
                reguId = daftarRegu.firstOrNull { it.nama == namaRegu }?.id
            }
            InputPengguna(
                if (awal == null) "Password (min 6)" else "Password baru (opsional)",
                sandi,
                password = true,
            ) { sandi = it }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TombolForm("Batal", false, true) { onDismiss() }
                TombolForm(if (awal == null) "Tambah" else "Simpan", true, valid) {
                    val body = mutableMapOf<String, Any?>(
                        "full_name" to nama.trim(),
                        "email" to email.trim(),
                        "username" to username.trim(),
                        "level" to level,
                        "regu_id" to reguId,
                    )
                    if (sandi.isNotBlank()) body["password"] = sandi
                    onSimpan(body)
                }
            }
        }
    }
}

@Composable
private fun DialogHapusPengguna(nama: String, onDismiss: () -> Unit, onHapus: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Text("Hapus Pengguna?", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
            Text(
                "Akun \"$nama\" akan dihapus permanen. Riwayat pengecekan yang pernah dibuat tetap tersimpan.",
                fontSize = 9.5.sp, color = UsrSlate700,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TombolForm("Batal", false, true) { onDismiss() }
                TombolForm("Hapus", true, true, merah = true) { onHapus() }
            }
        }
    }
}

@Composable
private fun InputPengguna(label: String, value: String, password: Boolean = false, onChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label.uppercase(), fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Slate400)
        Box(
            Modifier
                .fillMaxWidth()
                .height(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF8FAFC))
                .border(1.dp, UsrBorder, RoundedCornerShape(8.dp))
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
                visualTransformation = if (password) androidx.compose.ui.text.input.PasswordVisualTransformation()
                else androidx.compose.ui.text.input.VisualTransformation.None,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = if (password) androidx.compose.ui.text.input.KeyboardType.Password
                    else androidx.compose.ui.text.input.KeyboardType.Text,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun DropdownPilihan(
    label: String,
    nilai: String,
    opsi: List<Pair<String, String>>,
    onPilih: (String) -> Unit,
) {
    var buka by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label.uppercase(), fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Slate400)
        Box {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, UsrBorder, RoundedCornerShape(8.dp))
                    .clickable { buka = true }
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(nilai, fontSize = 9.5.sp, color = UsrSlate700, maxLines = 1)
                Spacer(Modifier.weight(1f))
                Text("▾", fontSize = 9.sp, color = Slate400)
            }
            DropdownMenu(expanded = buka, onDismissRequest = { buka = false }) {
                opsi.forEach { (v, l) ->
                    DropdownMenuItem(text = { Text(l, fontSize = 11.sp) }, onClick = { onPilih(v); buka = false })
                }
            }
        }
    }
}

@Composable
private fun RowScope.TombolForm(
    label: String,
    utama: Boolean,
    aktif: Boolean,
    merah: Boolean = false,
    onClick: () -> Unit,
) {
    val latar = when {
        !utama -> Color(0xFFF1F5F9)
        merah -> Color(0xFFE11D48)
        else -> SimapTeal
    }
    Box(
        Modifier
            .weight(1f)
            .height(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (aktif) latar else Color(0xFFCBD5E1))
            .clickable(enabled = aktif) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = if (utama) Color.White else UsrSlate700)
    }
}