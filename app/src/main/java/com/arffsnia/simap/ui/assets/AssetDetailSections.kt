package com.arffsnia.simap.ui.assets

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.arffsnia.simap.data.dto.AssetDetail
import com.arffsnia.simap.data.dto.AssetHistoryItem
import com.arffsnia.simap.data.dto.AssetHistoryResponse
import com.arffsnia.simap.ui.formatTanggal
import com.arffsnia.simap.ui.theme.Slate400
import com.arffsnia.simap.ui.theme.Slate900

private val kondisi = listOf(
    Triple("pressure", "Tekanan", "Pressure Gauge"),
    Triple("pin", "Pin / Segel", "Segel Pengaman"),
    Triple("hose", "Selang (Hose)", "Kondisi Nozzle"),
    Triple("klem", "Klem / Bracket", "Dudukan Aset"),
    Triple("handle", "Handle / Tuas", "Tuas Pemicu"),
    Triple("fisik", "Fisik Tabung", "Karat / Korosi"),
)

@Composable
private fun JudulSection(judul: String, badge: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Brush.linearGradient(listOf(Color(0xFF0D9488), Color(0xFF06B6D4))))
            .padding(horizontal = 11.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(judul, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(Modifier.weight(1f))
        Text(
            badge.uppercase(),
            fontSize = 7.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White.copy(alpha = 0.22f))
                .padding(horizontal = 5.dp, vertical = 2.dp),
        )
    }
}

@Composable
private fun KartuSection(warna: Color, isi: @Composable () -> Unit) {
    Column(
        Modifier
            .padding(horizontal = 12.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, DetBorder, RoundedCornerShape(14.dp)),
    ) {
        isi()
    }
}

@Composable
private fun BarisInfo(label: String, nilai: String, chip: Boolean = false, monospace: Boolean = false) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 11.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 10.sp, color = DetSlate500, modifier = Modifier.weight(1f))
        Text(
            nilai,
            fontSize = 10.sp,
            fontWeight = if (chip) FontWeight.SemiBold else FontWeight.Medium,
            fontFamily = if (monospace) FontFamily.Monospace else FontFamily.Default,
            color = if (chip) Color(0xFF115E59) else Slate900,
            modifier = if (chip) {
                Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFF0FDFA))
                    .padding(horizontal = 5.dp, vertical = 1.dp)
            } else Modifier,
        )
    }
}

@Composable
internal fun SectionsInformasi(a: AssetDetail) {
    KartuSection(Color.White) {
        JudulSection("Informasi Aset", "Master Data")
        BarisInfo("Kode Aset", a.slug, monospace = true)
        HorizontalDivider(color = DetHair)
        BarisInfo("Nama", a.nama)
        HorizontalDivider(color = DetHair)
        BarisInfo("Merek", a.merek?.ifBlank { "-" } ?: "-")
        HorizontalDivider(color = DetHair)
        BarisInfo("Jenis", a.category?.ifBlank { "-" } ?: "-", chip = true)
        HorizontalDivider(color = DetHair)
        BarisInfo("Berat", a.berat?.ifBlank { "-" } ?: "-")
        HorizontalDivider(color = DetHair)
        BarisInfo("Lokasi", a.room?.ifBlank { "-" } ?: "-")
        HorizontalDivider(color = DetHair)
        BarisInfo("Detail Lokasi", a.lokasi?.ifBlank { "-" } ?: "-")
        HorizontalDivider(color = DetHair)
        BarisInfo("Zonasi", "-")
        HorizontalDivider(color = DetHair)
        BarisInfo("Tahun Pengadaan", a.tahun?.toString() ?: "-")
    }
}

@Composable
internal fun SectionsKondisi(a: AssetDetail) {
    val hasil = a.latestCheck?.result ?: emptyMap()
    KartuSection(Color.White) {
        JudulSection("Kondisi Pengecekan", "Fisik Terakhir")
        Column(Modifier.padding(9.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            kondisi.chunked(2).forEach { baris ->
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    baris.forEach { (kunci, label, sub) ->
                        val nilai = hasil[kunci]
                        KartuKondisi(label, sub, nilai, Modifier.weight(1f))
                    }
                    if (baris.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun KartuKondisi(label: String, sub: String, nilai: String?, modifier: Modifier = Modifier) {
    val baik = nilai != null && nilai.equals("Baik", true)
    val warna = when {
        nilai.isNullOrBlank() -> Color(0xFF94A3B8)
        baik -> Color(0xFF059669)
        else -> Color(0xFFD97706)
    }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, warna.copy(alpha = 0.35f), RoundedCornerShape(9.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = DetSlate700, maxLines = 1,
                modifier = Modifier.weight(1f))
            Text(
                nilai?.ifBlank { "-" } ?: "—",
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold,
                color = warna,
                maxLines = 1,
            )
        }
        Text(sub.uppercase(), fontSize = 7.sp, fontWeight = FontWeight.SemiBold, color = Slate400)
        HorizontalDivider(color = DetHair)
    }
}

@Composable
internal fun SectionsMeta(a: AssetDetail) {
    val ctx = LocalContext.current
    val lat = a.latestCheck?.coords?.lat ?: a.coords?.lat ?: 0.0
    val lng = a.latestCheck?.coords?.lng ?: a.coords?.lng ?: 0.0
    val valid = lat != 0.0 && lng != 0.0

    KartuSection(Color.White) {
        JudulSection("Info Pengecekan", "Audit Trail")
        BarisInfo("Terakhir Dicek Oleh", a.latestCheck?.user?.ifBlank { "-" } ?: "-")
        HorizontalDivider(color = DetHair)
        BarisInfo("Created", a.createdAt?.let { formatTanggal(it) } ?: "-", monospace = true)
        HorizontalDivider(color = DetHair)
        BarisInfo("Updated", a.updatedAt?.let { formatTanggal(it) } ?: "-", monospace = true)
        HorizontalDivider(color = DetHair)
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 11.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Koordinat", fontSize = 10.sp, color = DetSlate500, modifier = Modifier.weight(1f))
            Text(
                if (valid) "%.6f, %.6f".format(lat, lng) else "-",
                fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold,
                color = Color(0xFFDB2777),
            )
            if (valid) {
                Spacer(Modifier.width(5.dp))
                Text(
                    "Maps",
                    fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White,
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color(0xFF0D9488))
                        .clickable {
                            ctx.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=$lat,$lng")),
                            )
                        }
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
        }
    }
}

@Composable
internal fun SectionsPeta(a: AssetDetail) {
    val ctx = LocalContext.current
    val lat = a.coords?.lat ?: a.latestCheck?.coords?.lat ?: 0.0
    val lng = a.coords?.lng ?: a.latestCheck?.coords?.lng ?: 0.0
    val valid = lat != 0.0 && lng != 0.0

    KartuSection(Color.White) {
        JudulSection("Lokasi Peta", "GPS")
        Box(
            Modifier
                .fillMaxWidth()
                .height(190.dp)
                .background(Color(0xFFE2E8F0)),
            contentAlignment = Alignment.Center,
        ) {
            if (valid) {
                PetaPratinjau(lat = lat, lng = lng, slug = a.slug)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "▼",
                        fontSize = 20.sp,
                        color = Color(0xFFDC2626),
                    )
                    Text(
                        a.slug,
                        fontSize = 7.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF0F172A))
                            .padding(horizontal = 4.dp, vertical = 1.dp),
                    )
                }
                Text(
                    "Buka Maps",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xCC0F172A))
                        .clickable {
                            ctx.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=$lat,$lng")),
                            )
                        }
                        .padding(horizontal = 7.dp, vertical = 3.dp),
                )
            } else {
                Text("Koordinat aset belum tersedia", fontSize = 9.sp, color = Slate400)
            }
        }
        Text(
            "Titik peta diambil dari koordinat terakhir yang tersimpan.",
            fontSize = 8.sp, color = DetSlate500,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
        )
    }
}

@Composable
internal fun SectionsRiwayat(h: AssetHistoryResponse?, tahun: Int?, onTahun: (Int) -> Unit) {
    KartuSection(Color.White) {
        JudulSection("Riwayat Kondisi Aset", "Log Periodik")

        val daftarTahun = h?.tahun ?: emptyList()
        if (daftarTahun.size > 1) {
            Row(
                Modifier.fillMaxWidth().background(Color(0xFFF8FAFC)).padding(9.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                daftarTahun.forEach { th ->
                    val aktif = th == tahun
                    Text(
                        "$th",
                        fontSize = 9.sp,
                        fontWeight = if (aktif) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (aktif) Color.White else DetSlate700,
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (aktif) Color(0xFF0D9488) else Color.White)
                            .border(1.dp, if (aktif) Color(0xFF0D9488) else DetBorder, RoundedCornerShape(999.dp))
                            .clickable { onTahun(th) }
                            .padding(horizontal = 10.dp, vertical = 3.dp),
                    )
                }
            }
        }

        val items = h?.items ?: emptyList()
        if (items.isEmpty()) {
            Text("Belum ada riwayat pengecekan.", fontSize = 9.sp, color = Slate400,
                modifier = Modifier.padding(11.dp))
            return@KartuSection
        }

        Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
            items.forEachIndexed { i, it ->
                EntriRiwayat(it, pertama = i == 0, terakhir = i == items.lastIndex)
            }
        }
    }
}

@Composable
private fun EntriRiwayat(it: AssetHistoryItem, pertama: Boolean, terakhir: Boolean) {
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Box(Modifier.width(14.dp).fillMaxHeight()) {
            if (!terakhir) {
                Box(
                    Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 6.dp)
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(Color(0xFFE2E8F0)),
                )
            }
            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 3.dp)
                    .size(8.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (pertama) Color(0xFF0D9488) else Color(0xFF94A3B8)),
            )
        }
        Spacer(Modifier.width(6.dp))
        Column(
            Modifier.weight(1f).padding(bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                it.checkedAt?.let { formatTanggal(it) } ?: "-",
                fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate900,
            )
            Text(
                listOfNotNull(it.user.ifBlank { null }, it.periode.ifBlank { null }).joinToString(" · "),
                fontSize = 9.sp, color = DetSlate500,
            )
            kondisi.chunked(3).forEach { baris ->
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    baris.forEach { (kunci, label, _) ->
                        val nilai = it.checks[kunci] ?: ""
                        val baik = nilai.equals("Baik", true)
                        Text(
                            "$label: ${nilai.ifBlank { "-" }}",
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            color = if (baik) Color(0xFF047857) else Color(0xFF92400E),
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (baik) Color(0xFFECFDF5) else Color(0xFFFFFBEB))
                                .border(1.dp, if (baik) Color(0xFFA7F3D0) else Color(0xFFFDE68A), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp),
                        )
                    }
                }
            }
            if (!it.fotoUrl.isNullOrBlank()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(7.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(1.dp, DetBorder, RoundedCornerShape(7.dp))
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AsyncImage(
                        model = it.fotoUrl,
                        contentDescription = "Dokumentasi ${it.checkedAt}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(5.dp)),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Foto dokumentasi pengecekan", fontSize = 8.sp, color = DetSlate500)
                }
            }
        }
    }
}


@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun PetaPratinjau(lat: Double, lng: Double, slug: String) {
    var siap by remember { mutableStateOf(false) }
    var wv by remember { mutableStateOf<android.webkit.WebView?>(null) }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            android.webkit.WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.allowFileAccess = true
                settings.allowContentAccess = true
                settings.domStorageEnabled = true
                webViewClient = object : android.webkit.WebViewClient() {
                    override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        siap = true
                    }
                }
                loadUrl("file:///android_asset/peta.html")
                wv = this
            }
        },
    )

    LaunchedEffect(siap, lat, lng) {
        if (!siap) return@LaunchedEffect
        wv?.evaluateJavascript("pratinjauAset($lat, $lng, '$slug');", null)
    }
}
