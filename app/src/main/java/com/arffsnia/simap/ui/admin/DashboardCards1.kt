package com.arffsnia.simap.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.arffsnia.simap.ui.theme.Slate900
import com.arffsnia.simap.data.dto.DashboardMobile

@Composable
internal fun HeroDash() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF0D9488), Color(0xFF06B6D4)))),
    ) {
        OrnamenHeader(
            Modifier.align(Alignment.BottomEnd).padding(6.dp),
            ukuran = 100.dp,
            alpha = 0.16f,
        )
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Home", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                Text(" / ", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                Text("Dashboard", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Text("Dashboard", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
            Text(
                "Sistem Informasi Manajemen Aset & Pemeliharaan ARFF BDJ",
                fontSize = 9.sp,
                color = Color.White.copy(alpha = 0.88f),
            )
        }
    }
}

@Composable
internal fun GreetingDash(nama: String, level: String, fotoUrl: String? = null) {
    val inisial = nama.trim().split(" ")
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .take(2).joinToString("")
        .ifBlank { "?" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, DashBorder, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(44.dp)
                .background(Brush.linearGradient(listOf(Color(0xFFFBBF24), Color(0xFF0D9488))), CircleShape)
                .padding(2.dp),
        ) {
            Box(
                Modifier.size(40.dp).background(Color(0xFF0F172A), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (fotoUrl.isNullOrBlank()) {
                    Text(inisial, fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color(0xFFFCD34D))
                } else {
                    SubcomposeAsyncImage(
                        model = fotoUrl,
                        contentDescription = "Foto profil $nama",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(40.dp).clip(CircleShape),
                        loading = {
                            Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                                Text(inisial, fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color(0xFFFCD34D))
                            }
                        },
                        error = {
                            Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                                Text(inisial, fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color(0xFFFCD34D))
                            }
                        },
                        success = { SubcomposeAsyncImageContent() },
                    )
                }
            }
        }
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            Text("Halo,", fontSize = 10.sp, color = Color(0xFF94A3B8))
            Text(nama, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Slate900)
            Text(
                level,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF94A3B8),
                modifier = Modifier
                    .padding(top = 2.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFF1F5F9))
                    .padding(horizontal = 5.dp, vertical = 1.dp),
            )
        }
    }
}

@Composable
internal fun MetricDash(d: DashboardMobile) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MetrikKotak(
            "Total Aset", d.summary.totalAset.toString(),
            listOf(Color(0xFF0EA5E9), Color(0xFF0D9488)),
            Modifier.weight(1f),
        )
        MetrikKotak(
            "Total Lokasi", d.summary.totalLokasi.toString(),
            listOf(Color(0xFF10B981), Color(0xFF047857)),
            Modifier.weight(1f),
        )
        MetrikKotak(
            "Total Jenis", d.summary.totalJenis.toString(),
            listOf(Color(0xFFF59E0B), Color(0xFFEA580C)),
            Modifier.weight(1f),
        )
    }
}

@Composable
private fun MetrikKotak(label: String, nilai: String, warna: List<Color>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(warna))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            label.uppercase(),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.9f),
            maxLines = 1,
        )
        Text(nilai, fontSize = 21.sp, fontWeight = FontWeight.Black, color = Color.White)
    }
}

@Composable
internal fun BarisBelumDicek(jumlah: Int, onOpen: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFFFBEB))
            .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(12.dp))
            .clickable { onOpen() }
            .padding(horizontal = 11.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "$jumlah aset belum dicek periode ini",
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF92400E),
        )
        Spacer(Modifier.weight(1f))
        Text("Lihat", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
    }
}
