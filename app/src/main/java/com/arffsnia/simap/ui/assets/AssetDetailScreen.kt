package com.arffsnia.simap.ui.assets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.arffsnia.simap.BuildConfig
import com.arffsnia.simap.SimapApp
import com.arffsnia.simap.data.dto.AssetDetail
import com.arffsnia.simap.data.dto.AssetHistoryResponse
import com.arffsnia.simap.data.repo.AppResult
import com.arffsnia.simap.ui.mapError
import com.arffsnia.simap.ui.theme.PlusJakarta
import com.arffsnia.simap.ui.theme.PlusJakartaTypography
import com.arffsnia.simap.ui.theme.SimapTeal
import com.arffsnia.simap.ui.theme.Slate400
import com.arffsnia.simap.ui.theme.Slate900

internal val DetBorder = Color(0xFFE2E8F0)
internal val DetHair = Color(0xFFF1F5F9)
internal val DetSlate700 = Color(0xFF334155)
internal val DetSlate500 = Color(0xFF64748B)

/** Detail Aset — dipakai dari Data Aset, peta, notifikasi, penugasan, dan hasil scan. */
@Composable
fun AssetDetailScreen(
    slug: String,
    onBack: () -> Unit,
    onInspect: (AssetDetail) -> Unit,
) {
    val app = LocalContext.current.applicationContext as SimapApp

    var detail by remember { mutableStateOf<AssetDetail?>(null) }
    var riwayat by remember { mutableStateOf<AssetHistoryResponse?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var tahun by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(slug) {
        loading = true
        when (val r = app.assetRepo.assetBySlug(slug)) {
            is AppResult.Success -> { detail = r.data; error = null }
            is AppResult.Error -> error = mapError(r.message)
        }
        when (val h = app.adminRepo.assetHistory(slug, null)) {
            is AppResult.Success -> {
                riwayat = h.data
                tahun = h.data.tahun.firstOrNull()
            }
            is AppResult.Error -> Unit
        }
        loading = false
    }

    LaunchedEffect(tahun) {
        val t = tahun ?: return@LaunchedEffect
        (app.adminRepo.assetHistory(slug, t) as? AppResult.Success)?.let { riwayat = it.data }
    }

    MaterialTheme(colorScheme = MaterialTheme.colorScheme, typography = PlusJakartaTypography) {
        CompositionLocalProvider(
            LocalTextStyle provides TextStyle(
                fontFamily = PlusJakarta, fontSize = 12.sp,
                platformStyle = PlatformTextStyle(includeFontPadding = false),
            ),
        ) {
            Column(Modifier.fillMaxSize().background(Color(0xFFF1F5F9)).statusBarsPadding()) {
                val d = detail
                when {
                    loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = SimapTeal)
                    }

                    d == null -> Column(
                        Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(error ?: "Aset tidak ditemukan", fontSize = 12.sp, color = Color(0xFFBE123C))
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "Kembali",
                            fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SimapTeal,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF0FDFA))
                                .clickable { onBack() }
                                .padding(horizontal = 14.dp, vertical = 7.dp),
                        )
                    }

                    else -> Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(11.dp),
                    ) {
                        HeroDetail()
                        KartuJudul(d, onBack, onInspect)
                        SectionsInformasi(d)
                        SectionsKondisi(d)
                        SectionsMeta(d)
                        SectionsPeta(d)
                        SectionsRiwayat(riwayat, tahun) { tahun = it }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroDetail() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.linearGradient(listOf(Color(0xFF0D9488), Color(0xFF06B6D4))))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text("Detail Aset", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Home", fontSize = 9.sp, color = Color.White.copy(alpha = 0.8f))
                Text(" / ", fontSize = 9.sp, color = Color.White.copy(alpha = 0.6f))
                Text("Detail Aset", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
        Icon(Icons.Rounded.Inventory2, null, Modifier.size(28.dp), tint = Color.White.copy(alpha = 0.25f))
    }
}

@Composable
private fun KartuJudul(a: AssetDetail, onBack: () -> Unit, onInspect: (AssetDetail) -> Unit) {
    val app = LocalContext.current.applicationContext as SimapApp
    val token = app.session.token

    Column(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, DetBorder, RoundedCornerShape(14.dp)),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(Color(0xFF0D9488), Color(0xFF06B6D4))))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Detail Aset :", fontSize = 9.sp, color = Color(0xFFA7F3D0))
                Spacer(Modifier.weight(1f))
                Row(
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F766E))
                        .clickable { onBack() }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.ArrowBack, null, Modifier.size(11.dp), tint = Color.White)
                    Spacer(Modifier.width(3.dp))
                    Text("Kembali", fontSize = 9.sp, fontWeight = FontWeight.Medium, color = Color.White)
                }
            }
            Text(a.nama, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(
                a.slug,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFFD1FAE5),
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF0F766E))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            )
            Text(
                "Periksa aset ini",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF065F46),
                modifier = Modifier
                    .padding(top = 3.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(Color(0xFF6EE7B7))
                    .clickable { onInspect(a) }
                    .padding(horizontal = 9.dp, vertical = 4.dp),
            )
        }

        Row(
            Modifier
                .fillMaxWidth()
                .background(Color(0xFFF8FAFC))
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F172A)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (!a.fotoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = a.fotoUrl,
                            contentDescription = "Foto ${a.nama}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        Icon(Icons.Rounded.Inventory2, null, Modifier.size(26.dp), tint = Color.White.copy(alpha = 0.5f))
                    }
                }
                Text(
                    "DOKUMENTASI FISIK",
                    fontSize = 8.sp, fontWeight = FontWeight.SemiBold, color = DetSlate500,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .border(1.dp, DetHair, RoundedCornerShape(8.dp))
                        .padding(5.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    AsyncImage(
                        model = "${BuildConfig.API_BASE_URL}api/assets/qr.php?slug=${a.slug}&token=$token",
                        contentDescription = "QR ${a.slug}",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                Text(
                    a.slug,
                    fontSize = 9.sp, fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace, color = Slate900,
                    modifier = Modifier.padding(top = 3.dp),
                )
                Text("Scan QR identitas", fontSize = 8.sp, color = Slate400)
            }
        }
    }
}
