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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arffsnia.simap.ui.theme.Slate900
import com.arffsnia.simap.data.dto.CoverageIns
import com.arffsnia.simap.data.dto.KategoriStat

/** Kerangka kartu dashboard: judul + badge opsional di kanan. */
@Composable
internal fun KartuDash(
    judul: String,
    badge: String? = null,
    isi: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, DashBorder, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(judul, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate900)
            Spacer(Modifier.weight(1f))
            if (badge != null) {
                Text(
                    badge,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0F766E),
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFFECFDF5))
                        .padding(horizontal = 7.dp, vertical = 2.dp),
                )
            }
        }
        HorizontalDivider(color = DashHair)
        isi()
    }
}

@Composable
internal fun KartuCoverage(points: List<CoverageIns>, active: CoverageIns?, years: List<Int> = emptyList()) {
    val tahunAktif = active?.label?.take(4)?.toIntOrNull()
    val daftarTahun = years.ifEmpty {
        points.mapNotNull { it.label.take(4).toIntOrNull() }.distinct().sortedDescending()
    }
    var mode by remember { mutableStateOf("aktif") }
    var pilihTahun by remember { mutableStateOf(0) }
    // Default tab Pertahun: tahun lalu (kalau ada), supaya langsung kelihatan perbandingan.
    val tahunTampil = if (pilihTahun in daftarTahun) pilihTahun
    else (daftarTahun.firstOrNull { it != tahunAktif } ?: tahunAktif ?: 0)

    val tampil = when (mode) {
        "aktif" -> if (tahunAktif == null) points
        else points.filter { it.label.take(4).toIntOrNull() == tahunAktif }
        "tahun" -> points.filter { it.label.take(4).toIntOrNull() == tahunTampil }
        else -> points.takeLast(8)
    }

    KartuDash("Tren Coverage Inspeksi", "% aset dicek tiap periode") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF1F5F9))
                    .padding(2.dp),
            ) {
                TabKecil("${tahunAktif ?: "Tahun"} (Aktif)", mode == "aktif") { mode = "aktif" }
                TabKecil("Semua", mode == "semua") { mode = "semua" }
                TabKecil("Pertahun", mode == "tahun") { mode = "tahun" }
            }
            Spacer(Modifier.weight(1f))
            if (mode == "aktif") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(5.dp).background(Color(0xFF10B981), RoundedCornerShape(999.dp)))
                    Spacer(Modifier.width(4.dp))
                    Text("Periode Berjalan", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF047857))
                }
            }
        }

        // Pemilih tahun (hanya di tab Pertahun)
        if (mode == "tahun" && daftarTahun.isNotEmpty()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                daftarTahun.forEach { th ->
                    val dipilih = th == tahunTampil
                    Text(
                        th.toString(),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (dipilih) Color.White else DashSlate500,
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (dipilih) Color(0xFF0D9488) else Color(0xFFF1F5F9))
                            .clickable { pilihTahun = th }
                            .padding(horizontal = 10.dp, vertical = 3.dp),
                    )
                }
            }
        }

        if (mode == "tahun") {
            val rata = if (tampil.isEmpty()) 0 else tampil.sumOf { it.pct } / tampil.size
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Tahun $tahunTampil", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Slate900)
                Spacer(Modifier.weight(1f))
                Text("${tampil.size} periode · rata-rata $rata%", fontSize = 9.sp, color = DashSlate500)
            }
        }

        if (active != null && mode != "tahun") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF0FDFA))
                    .border(1.dp, Color(0xFF99F6E4), RoundedCornerShape(12.dp))
                    .padding(9.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "AKTIF",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF0D9488))
                            .padding(horizontal = 5.dp, vertical = 1.dp),
                    )
                    Spacer(Modifier.width(5.dp))
                    Text("Periode ${active.label}", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Slate900)
                    Spacer(Modifier.weight(1f))
                    Text(
                        "${active.pct}% Tercapai",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F766E),
                    )
                }
                ProgressBar(active.pct)
                Row {
                    Text(
                        "Dicek: ${active.checked} aset (${active.pct}%)",
                        fontSize = 9.sp,
                        color = DashSlate500,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        "Sisa: ${(active.total - active.checked).coerceAtLeast(0)} aset",
                        fontSize = 9.sp,
                        color = DashSlate500,
                    )
                }
            }
        }

        if (mode == "tahun" && tampil.isEmpty()) {
            Text("Belum ada inspeksi tercatat di tahun ini.", fontSize = 9.sp, color = DashSlate500)
        } else {
            LineChart(points = tampil, aktif = active != null && mode == "aktif")

            Row(Modifier.fillMaxWidth()) {
                tampil.forEach { p ->
                    Text(
                        p.label.replace(" ", "\n"),
                        fontSize = 7.sp,
                        color = DashSlate500,
                        maxLines = 2,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun TabKecil(label: String, aktif: Boolean, onClick: () -> Unit) {
    Text(
        label,
        fontSize = 8.sp,
        fontWeight = FontWeight.SemiBold,
        color = if (aktif) Color(0xFF0F766E) else DashSlate500,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (aktif) Color.White else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 7.dp, vertical = 3.dp),
    )
}

@Composable
internal fun KartuDistribusi(kategori: List<KategoriStat>) {
    val warna = listOf(
        Color(0xFF14B8A6), Color(0xFF0EA5E9), Color(0xFFF59E0B),
        Color(0xFF9333EA), Color(0xFFF43F5E), Color(0xFF10B981), Color(0xFF6366F1),
    )
    val total = kategori.sumOf { it.total }

    KartuDash("Distribusi Aset per Jenis") {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Box(Modifier.size(126.dp)) {
                DonutChart(
                    parts = kategori.mapIndexed { i, k ->
                        warna[i % warna.size] to (if (total > 0) k.total.toFloat() / total else 0f)
                    },
                    tengahAtas = "Total",
                    tengahBawah = total.toString(),
                    modifier = Modifier.fillMaxWidth().height(126.dp),
                )
            }
        }
        HorizontalDivider(color = DashHair)
        kategori.chunked(2).forEach { baris ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                baris.forEach { k ->
                    val i = kategori.indexOf(k)
                    Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(7.dp).background(warna[i % warna.size], RoundedCornerShape(999.dp)))
                        Spacer(Modifier.width(5.dp))
                        Text(
                            k.nama,
                            fontSize = 8.sp,
                            color = DashSlate700,
                            maxLines = 1,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(k.total.toString(), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Slate900)
                    }
                }
                if (baris.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}
