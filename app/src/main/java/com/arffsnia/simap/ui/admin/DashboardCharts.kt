package com.arffsnia.simap.ui.admin

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arffsnia.simap.data.dto.CoverageIns

/** Grafik garis tren coverage; label sumbu X ditulis pemanggil di bawah Canvas. */
@Composable
internal fun LineChart(points: List<CoverageIns>, modifier: Modifier = Modifier, aktif: Boolean = true) {
    val values = if (points.isEmpty()) listOf(0f, 0f) else points.map { it.pct.coerceIn(0, 100).toFloat() }
    Canvas(modifier.fillMaxWidth().height(140.dp)) {
        val kiri = 4.dp.toPx()
        val kanan = size.width - 4.dp.toPx()
        val atas = 10.dp.toPx()
        val bawah = size.height - 10.dp.toPx()

        // Garis bantu 0 / 50 / 100 %
        listOf(0f, 0.5f, 1f).forEach { f ->
            val y = bawah - (bawah - atas) * f
            drawLine(
                color = if (f == 0f) Color(0xFFE2E8F0) else Color(0xFFF1F5F9),
                start = Offset(kiri, y),
                end = Offset(kanan, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = if (f == 0f) null else PathEffect.dashPathEffect(floatArrayOf(6f, 6f)),
            )
        }

        val langkah = if (values.size <= 1) 0f else (kanan - kiri) / (values.size - 1)
        val koordinat = values.mapIndexed { i, v ->
            Offset(kiri + langkah * i, bawah - (bawah - atas) * (v / 100f))
        }

        // Area gradient di bawah kurva
        val area = Path().apply {
            moveTo(koordinat.first().x, bawah)
            koordinat.forEachIndexed { i, o ->
                if (i == 0) lineTo(o.x, o.y) else lineTo(o.x, o.y)
            }
            lineTo(koordinat.last().x, bawah)
            close()
        }
        drawPath(
            path = area,
            brush = Brush.verticalGradient(
                listOf(Color(0xFF0D9488).copy(alpha = 0.28f), Color(0xFF0D9488).copy(alpha = 0.02f)),
            ),
        )

        // Kurva
        val garis = Path().apply {
            koordinat.forEachIndexed { i, o -> if (i == 0) moveTo(o.x, o.y) else lineTo(o.x, o.y) }
        }
        drawPath(garis, Color(0xFF0D9488), style = Stroke(width = 2.4f.dp.toPx(), cap = StrokeCap.Round))

        // Titik
        koordinat.forEachIndexed { i, o ->
            val terakhir = i == koordinat.lastIndex
            if (terakhir && aktif) {
                drawCircle(Color(0xFF14B8A6).copy(alpha = 0.25f), radius = 7.dp.toPx(), center = o)
            }
            drawCircle(Color.White, radius = if (terakhir) 4.dp.toPx() else 3.dp.toPx(), center = o)
            drawCircle(
                color = if (terakhir) Color(0xFF0F766E) else Color(0xFF0D9488),
                radius = if (terakhir) 4.dp.toPx() else 3.dp.toPx(),
                center = o,
                style = Stroke(width = 2.dp.toPx()),
            )
        }
    }
}

/** Donut chart; [parts] = pasangan warna + porsi (0..1). */
@Composable
internal fun DonutChart(
    parts: List<Pair<Color, Float>>,
    tengahAtas: String,
    tengahBawah: String,
    modifier: Modifier = Modifier,
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val tebal = 14.dp.toPx()
            val inset = tebal / 2
            val kotak = Size(size.width - tebal, size.height - tebal)
            drawArc(
                color = Color(0xFFF1F5F9),
                startAngle = 0f, sweepAngle = 360f, useCenter = false,
                topLeft = Offset(inset, inset), size = kotak,
                style = Stroke(width = tebal),
            )
            var mulai = -90f
            parts.filter { it.second > 0f }.forEach { (warna, porsi) ->
                val sapuan = 360f * porsi.coerceIn(0f, 1f)
                drawArc(
                    color = warna,
                    startAngle = mulai, sweepAngle = sapuan, useCenter = false,
                    topLeft = Offset(inset, inset), size = kotak,
                    style = Stroke(width = tebal, cap = StrokeCap.Butt),
                )
                mulai += sapuan
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(tengahAtas, fontSize = 9.sp, color = Color(0xFF94A3B8))
            Text(tengahBawah, fontSize = 17.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
        }
    }
}

/** Gauge radial 3 bagian (lengkap / sebagian / belum). */
@Composable
internal fun GaugeChart(
    parts: List<Pair<Color, Float>>,
    tengahAtas: String,
    tengahBawah: String,
    modifier: Modifier = Modifier,
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val tebal = 12.dp.toPx()
            val inset = tebal / 2
            val kotak = Size(size.width - tebal, size.height - tebal)
            drawArc(
                color = Color(0xFFF1F5F9),
                startAngle = 0f, sweepAngle = 360f, useCenter = false,
                topLeft = Offset(inset, inset), size = kotak,
                style = Stroke(width = tebal),
            )
            var mulai = -90f
            parts.filter { it.second > 0f }.forEach { (warna, porsi) ->
                val sapuan = 360f * porsi.coerceIn(0f, 1f)
                drawArc(
                    color = warna,
                    startAngle = mulai, sweepAngle = sapuan, useCenter = false,
                    topLeft = Offset(inset, inset), size = kotak,
                    style = Stroke(width = tebal),
                )
                mulai += sapuan
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(tengahAtas, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F766E))
            Text(tengahBawah, fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
        }
    }
}
