package com.arffsnia.simap.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arffsnia.simap.data.dto.BulanStat
import com.arffsnia.simap.data.dto.InspeksiStat
import com.arffsnia.simap.data.dto.LokasiStat

@Composable
internal fun KartuMaintenance(bulan: List<BulanStat>) {
    val maks = (bulan.maxOfOrNull { it.total } ?: 0).coerceAtLeast(1)

    KartuDash("Maintenance per Bulan", "6 bulan terakhir") {
        Row(
            Modifier.fillMaxWidth().height(110.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            bulan.forEach { b ->
                Column(
                    Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        b.total.toString(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (b.total > 0) Color(0xFF0F766E) else DashSlate500,
                    )
                    Spacer(Modifier.height(2.dp))
                    Box(
                        Modifier
                            .width(22.dp)
                            .height((72 * b.total / maks).coerceAtLeast(if (b.total > 0) 6 else 2).dp)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(if (b.total > 0) Color(0xFF14B8A6) else Color(0xFFE2E8F0)),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(b.label, fontSize = 8.sp, color = DashSlate500, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
internal fun KartuInspeksi(s: InspeksiStat) {
    val total = if (s.total > 0) s.total else 1
    KartuDash("Status Inspeksi Aset") {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            GaugeChart(
                parts = listOf(
                    Color(0xFF14B8A6) to s.lengkap.toFloat() / total,
                    Color(0xFFFBBF24) to s.sebagian.toFloat() / total,
                    Color(0xFF94A3B8) to s.belum.toFloat() / total,
                ),
                tengahAtas = "Lengkap",
                tengahBawah = "${s.lengkap}/${s.total}",
                modifier = Modifier.size(112.dp),
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            LegendaGauge("Lengkap", s.lengkap, s.total, Color(0xFF14B8A6))
            Spacer(Modifier.width(10.dp))
            LegendaGauge("Sebagian", s.sebagian, s.total, Color(0xFFFBBF24))
            Spacer(Modifier.width(10.dp))
            LegendaGauge("Belum", s.belum, s.total, Color(0xFF94A3B8))
        }
    }
}

@Composable
private fun LegendaGauge(label: String, nilai: Int, total: Int, warna: Color) {
    val persen = if (total > 0) (nilai * 100 / total) else 0
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(7.dp).background(warna, RoundedCornerShape(999.dp)))
        Spacer(Modifier.width(4.dp))
        Text("$label — $nilai", fontSize = 8.sp, color = DashSlate700)
        Text(" ($persen%)", fontSize = 8.sp, color = DashSlate500)
    }
}

@Composable
internal fun KartuLokasi(lokasi: List<LokasiStat>) {
    val maks = (lokasi.maxOfOrNull { it.total } ?: 0).coerceAtLeast(1)
    KartuDash("Aset per Lokasi", "Top ${lokasi.size}") {
        if (lokasi.isEmpty()) {
            Text("Belum ada data lokasi.", fontSize = 9.sp, color = DashSlate500)
        }
        lokasi.forEach { l ->
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        l.nama,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        color = DashSlate700,
                        maxLines = 1,
                        modifier = Modifier.weight(1f),
                    )
                    Text(l.total.toString(), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F766E))
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFFF1F5F9)),
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(l.total.toFloat() / maks)
                            .height(6.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color(0xFF0EA5E9)),
                    )
                }
            }
        }
    }
}
