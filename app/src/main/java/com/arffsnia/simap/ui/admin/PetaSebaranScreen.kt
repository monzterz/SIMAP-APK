package com.arffsnia.simap.ui.admin

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.arffsnia.simap.SimapApp
import com.arffsnia.simap.data.dto.AssetMapItem
import com.arffsnia.simap.data.dto.ZonasiOpt
import com.arffsnia.simap.data.repo.AppResult
import com.arffsnia.simap.ui.mapError
import com.arffsnia.simap.ui.theme.PlusJakarta
import com.arffsnia.simap.ui.theme.PlusJakartaTypography
import com.arffsnia.simap.ui.theme.SimapTeal
import com.arffsnia.simap.ui.theme.Slate400
import com.arffsnia.simap.ui.theme.Slate900
import com.google.gson.Gson

private val PetaBorder = Color(0xFFE2E8F0)

/** Peta sebaran aset: latar peta ubin OSM/Esri lewat Leaflet (sama seperti web). */
@Composable
fun PetaSebaranScreen(
    onBack: (() -> Unit)? = null,
    onOpen: (String) -> Unit,
    onNotif: (() -> Unit)? = null,
    onProfile: (() -> Unit)? = null,
) {
    val app = LocalContext.current.applicationContext as SimapApp
    val user = remember { app.authRepo.currentUser() }

    var semua by remember { mutableStateOf<List<AssetMapItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var filter by remember { mutableStateOf("semua") }
    var dipilih by remember { mutableStateOf<AssetMapItem?>(null) }
    var satelit by remember { mutableStateOf(false) }
    var halamanSiap by remember { mutableStateOf(false) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    var tanpaKoordinat by remember { mutableStateOf(0) }
    var zonasi by remember { mutableStateOf<List<ZonasiOpt>>(emptyList()) }
    var mode by remember { mutableStateOf("sebaran") }
    var zonaPilih by remember { mutableStateOf("") }
    var lantaiPilih by remember { mutableStateOf("1") }

    LaunchedEffect(Unit) {
        when (val r = app.adminRepo.assetsMap("semua")) {
            is AppResult.Success -> {
                semua = r.data.items
                tanpaKoordinat = r.data.tanpaKoordinat
                error = null
            }
            is AppResult.Error -> error = mapError(r.message)
        }
        // Poligon zonasi untuk lapisan peta (sumber sama dengan form penugasan).
        when (val o = app.adminRepo.penugasanOptions()) {
            is AppResult.Success -> zonasi = o.data.zonasi
            is AppResult.Error -> Unit
        }
        loading = false
    }

    val tampil = remember(semua, filter) {
        if (filter == "semua") semua else semua.filter { it.status == filter }
    }

    // Dorong data ke Leaflet setiap data/filter berubah dan halaman sudah siap.
    LaunchedEffect(tampil, halamanSiap) {
        if (!halamanSiap) return@LaunchedEffect
        val json = Gson().toJson(tampil.map {
            mapOf(
                "slug" to it.slug, "nama" to it.nama, "room" to (it.room ?: ""),
                "category" to (it.category ?: ""), "status" to it.status,
                "periode" to (it.periode ?: ""), "lat" to it.lat, "lng" to it.lng,
            )
        })
        webView?.evaluateJavascript("setAssets($json);", null)
    }

    LaunchedEffect(zonasi, halamanSiap) {
        if (!halamanSiap || zonasi.isEmpty()) return@LaunchedEffect
        val json = Gson().toJson(zonasi.map {
            mapOf(
                "nama" to it.nama,
                "warna" to (it.warna ?: "#0ea5e9"),
                "lantai" to (it.lantai ?: ""),
                "poly" to it.poly,
            )
        })
        webView?.evaluateJavascript("setZones($json);", null)
    }

    LaunchedEffect(mode, zonaPilih, lantaiPilih, halamanSiap) {
        if (!halamanSiap) return@LaunchedEffect
        webView?.evaluateJavascript(
            "setFilter('$mode', '${zonaPilih.replace("'", "")}', '${lantaiPilih.replace("'", "")}');",
            null,
        )
    }

    fun hitung(status: String) = if (status == "semua") semua.size else semua.count { it.status == status }

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
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp)
                        .padding(top = 10.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    BannerPeta(onBack)

                    if (loading) {
                        Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = SimapTeal)
                        }
                        return@Column
                    }
                    error?.let {
                        Text(it, fontSize = 11.sp, color = Color(0xFFBE123C))
                        return@Column
                    }

                    BarisFilter(filter, ::hitung) { filter = it; dipilih = null }

                    BarisFilterPeta(
                        mode = mode,
                        onMode = { mode = it },
                        zona = zonasi.map { it.nama }.distinct().sorted(),
                        zonaPilih = zonaPilih,
                        onZona = { zonaPilih = it },
                        lantai = zonasi.mapNotNull { it.lantai?.takeIf { l -> l.isNotBlank() } }.distinct().sorted(),
                        lantaiPilih = lantaiPilih,
                        onLantai = { lantaiPilih = it },
                    )

                    Box(
                        Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, PetaBorder, RoundedCornerShape(14.dp))
                            .background(Color(0xFFE9ECE7)),
                    ) {
                        PetaWeb(
                            onSiap = { halamanSiap = true },
                            onWebView = { webView = it },
                            onPin = { slug -> dipilih = semua.firstOrNull { it.slug == slug } },
                            onZona = { nama, lantai ->
                                zonaPilih = nama
                                if (lantai.isNotBlank()) lantaiPilih = lantai
                                mode = "sebaran"
                                dipilih = null
                            },
                        )
                        Row(
                            Modifier.align(Alignment.TopEnd).padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            TombolPeta(if (satelit) "Peta" else "Satelit") {
                                satelit = !satelit
                                webView?.evaluateJavascript("gantiTipe(${!satelit});", null)
                            }
                            TombolPeta("Pusatkan") {
                                val t = tampil.firstOrNull()
                                if (t != null) webView?.evaluateJavascript("fokus(${t.lat}, ${t.lng});", null)
                            }
                        }

                        // Info ringkas (tidak ditampilkan di mode zonasi: gambar zona sudah jelas)
                        if (mode != "zonasi") Text(
                            "${tampil.size} titik" +
                                (if (zonaPilih.isNotBlank() || lantaiPilih.isNotBlank()) " (difilter)" else "") +
                                if (tanpaKoordinat > 0) " • $tanpaKoordinat tanpa koordinat" else "",
                            fontSize = 9.sp,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color(0xCC0F172A))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                        )

                        // Kartu aset terpilih menempel di bawah peta
                        dipilih?.let { a ->
                            Box(Modifier.align(Alignment.BottomCenter).padding(8.dp)) {
                                KartuAset(a) { onOpen(a.slug) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun PetaWeb(
    onSiap: () -> Unit,
    onWebView: (WebView) -> Unit,
    onPin: (String) -> Unit,
    onZona: (String, String) -> Unit,
) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                // API 30+ memblokir file:// secara default; tanpa ini peta kosong.
                settings.allowFileAccess = true
                settings.allowContentAccess = true
                addJavascriptInterface(PinBridge(onPin, onZona), "Android")
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        onSiap()
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        request: android.webkit.WebResourceRequest?,
                        error: android.webkit.WebResourceError?,
                    ) {
                        android.util.Log.w("SIMAP_PETA", "gagal muat: ${error?.description}")
                    }
                }
                webChromeClient = object : android.webkit.WebChromeClient() {
                    override fun onConsoleMessage(msg: android.webkit.ConsoleMessage?): Boolean {
                        android.util.Log.i("SIMAP_PETA", "console: ${msg?.message()}")
                        return true
                    }
                }
                loadUrl("file:///android_asset/peta.html")
                onWebView(this)
            }
        },
    )
}

private class PinBridge(
    private val onPin: (String) -> Unit,
    private val onZona: (String, String) -> Unit,
) {
    @JavascriptInterface
    fun onPin(slug: String, nama: String, room: String, category: String, status: String, periode: String) {
        if (slug.isNotBlank()) onPin(slug)
    }

    @JavascriptInterface
    fun onZona(nama: String, lantai: String) {
        onZona(nama, lantai)
    }

    @JavascriptInterface
    fun onLog(pesan: String) {
        android.util.Log.i("SIMAP_PETA", pesan)
    }
}

@Composable
private fun BannerPeta(onBack: (() -> Unit)?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF0D9488), Color(0xFF06B6D4)))),
    ) {
    OrnamenHeader(Modifier.align(Alignment.BottomEnd), ukuran = 104.dp, alpha = 0.16f)
    Column(
        modifier = Modifier
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Home", fontSize = 9.sp, color = Color.White.copy(alpha = 0.8f),
                modifier = if (onBack != null) Modifier.clickable { onBack() } else Modifier)
            Text(" / ", fontSize = 9.sp, color = Color.White.copy(alpha = 0.6f))
            Text("Peta Sebaran Aset", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Peta Sebaran Aset", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Spacer(Modifier.weight(1f))
            Text(
                "BDJ Airport",
                fontSize = 9.sp,
                color = Color.White,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.White.copy(alpha = 0.20f))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            )
        }
    }
    }
}

@Composable
private fun BarisFilter(filter: String, hitung: (String) -> Int, onFilter: (String) -> Unit) {
    val opsi = listOf(
        Triple("semua", "Semua", Color(0xFF0D9488)),
        Triple("aktif", "Aktif", Color(0xFF10B981)),
        Triple("maintenance", "Maintenance", Color(0xFFF59E0B)),
        Triple("rusak", "Rusak", Color(0xFF1E293B)),
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        opsi.forEach { (kunci, label, warna) ->
            val aktif = filter == kunci
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (aktif) warna.copy(alpha = 0.12f) else Color.White)
                    .border(1.dp, if (aktif) warna else PetaBorder, RoundedCornerShape(999.dp))
                    .clickable { onFilter(kunci) }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(6.dp).background(warna, CircleShape))
                Spacer(Modifier.width(4.dp))
                Text(label, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                Spacer(Modifier.width(3.dp))
                Text("${hitung(kunci)}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = warna)
            }
        }
    }
}

@Composable
private fun TombolPeta(label: String, onClick: () -> Unit) {
    Text(
        label,
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF0F766E),
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.95f))
            .border(1.dp, PetaBorder, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 5.dp),
    )
}

@Composable
private fun KartuAset(a: AssetMapItem, onDetail: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, PetaBorder, RoundedCornerShape(14.dp))
            .padding(11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(a.slug, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate900)
                Spacer(Modifier.width(5.dp))
                Text(
                    labelStatus(a.status),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(warnaStatus(a.status))
                        .padding(horizontal = 4.dp, vertical = 1.dp),
                )
            }
            Text(a.nama, fontSize = 10.sp, color = Slate400, maxLines = 1)
            Text(
                listOfNotNull(a.room, a.category).joinToString(" • "),
                fontSize = 9.sp, color = Slate400, maxLines = 1,
            )
        }
        Text(
            "Detail",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0097A7))
                .clickable { onDetail() }
                .padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}

private fun warnaStatus(s: String): Color = when (s) {
    "maintenance" -> Color(0xFFF59E0B)
    "rusak" -> Color(0xFF1E293B)
    else -> Color(0xFF10B981)
}

private fun labelStatus(s: String): String = when (s) {
    "maintenance" -> "Maintenance"
    "rusak" -> "Rusak"
    "aktif" -> "Aktif"
    else -> "Semua"
}

@Composable
private fun BarisFilterPeta(
    mode: String,
    onMode: (String) -> Unit,
    zona: List<String>,
    zonaPilih: String,
    onZona: (String) -> Unit,
    lantai: List<String>,
    lantaiPilih: String,
    onLantai: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Toggle Zonasi / Sebaran
        Row(
            Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF1F5F9))
                .padding(2.dp),
        ) {
            listOf("zonasi" to "Zonasi", "sebaran" to "Sebaran").forEach { (kunci, label) ->
                val aktif = mode == kunci
                Text(
                    label,
                    fontSize = 9.sp,
                    fontWeight = if (aktif) FontWeight.Bold else FontWeight.Medium,
                    color = if (aktif) Color.White else Color(0xFF475569),
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (aktif) Color(0xFF0097A7) else Color.Transparent)
                        .clickable { onMode(kunci) }
                        .padding(horizontal = 7.dp, vertical = 3.dp),
                )
            }
        }

        PilihanKecil(
            label = zonaPilih.ifBlank { "Semua Zona" },
            opsi = listOf("" to "Semua Zona") + zona.map { it to it },
            onPilih = onZona,
            modifier = Modifier.weight(1f),
        )

        if (lantai.isNotEmpty()) {
            PilihanKecil(
                label = if (lantaiPilih.isBlank()) "Semua Lt" else "Lt $lantaiPilih",
                opsi = listOf("" to "Semua Lt") + lantai.map { it to "Lt $it" },
                onPilih = onLantai,
                modifier = Modifier.width(96.dp),
            )
        }
    }
}

@Composable
private fun PilihanKecil(
    label: String,
    opsi: List<Pair<String, String>>,
    onPilih: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var buka by remember { mutableStateOf(false) }
    Box(modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .border(1.dp, PetaBorder, RoundedCornerShape(8.dp))
                .clickable { buka = true }
                .padding(horizontal = 7.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = Slate900, maxLines = 1)
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
