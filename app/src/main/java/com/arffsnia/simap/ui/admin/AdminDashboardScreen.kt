package com.arffsnia.simap.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arffsnia.simap.SimapApp
import com.arffsnia.simap.data.dto.CoveragePoint
import com.arffsnia.simap.data.dto.DashboardResponse
import com.arffsnia.simap.data.dto.MissingAsset
import com.arffsnia.simap.data.repo.AppResult
import com.arffsnia.simap.ui.mapError
import com.arffsnia.simap.ui.theme.Good
import com.arffsnia.simap.ui.theme.Sky
import com.arffsnia.simap.ui.theme.Slate400
import com.arffsnia.simap.ui.theme.Slate50
import com.arffsnia.simap.ui.theme.Teal
import com.arffsnia.simap.ui.theme.Warn

@Composable
fun AdminDashboardScreen(onMissing: () -> Unit, onLogout: () -> Unit) {
    val app = LocalContext.current.applicationContext as SimapApp
    val user = remember { app.authRepo.currentUser() }
    val name = user?.fullName ?: user?.username ?: "Admin"

    var data by remember { mutableStateOf<DashboardResponse?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var missingPreview by remember { mutableStateOf<List<MissingAsset>>(emptyList()) }
    var refreshKey by remember { mutableStateOf(0) }

    LaunchedEffect(refreshKey) {
        loading = true
        when (val r = app.adminRepo.dashboard()) {
            is AppResult.Success -> { data = r.data; error = null }
            is AppResult.Error -> error = mapError(r.message)
        }
        loading = false
        // Pratinjau aset belum dicek (3 teratas)
        when (val r = app.adminRepo.missing()) {
            is AppResult.Success -> missingPreview = r.data.take(3)
            is AppResult.Error -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate50),
    ) {
        ScreenHero(name, "Super Admin", onLogout)

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        if (error != null && data == null) {
            Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(10.dp))
                OutlinedButton(onClick = { refreshKey++ }) {
                    Text("Coba lagi")
                }
            }
            return@Column
        }

        val summary = data?.summary

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            // KPI
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 14.dp)) {
                            StatCard(
                                label = "Terperiksa",
                                value = summary?.terperiksa?.toString() ?: "-",
                                caption = summary?.let { s -> if (s.totalAset > 0) "${s.terperiksa * 100 / s.totalAset}% Aset" else null },
                                color = Teal,
                                modifier = Modifier.weight(1f),
                            )
                            StatCard(
                                label = "Belum dicek",
                                value = summary?.belumDicek?.toString() ?: "-",
                                caption = summary?.belumDicek?.let { "$it Overdue" },
                                color = Warn,
                                modifier = Modifier.weight(1f),
                            )
                            StatCard(
                                label = "Regu Aktif",
                                value = summary?.jumlahRegu?.toString() ?: "-",
                                caption = null,
                                color = Sky,
                                modifier = Modifier.weight(1f),
                            )
                        }

            // Chart coverage
            SectionLabel("Presentasi pengecekan periode")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Column(Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Coverage Inspeksi (2026)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Tingkat pemenuhan target standar ICAO",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate400,
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(Teal.copy(alpha = 0.12f), RoundedCornerShape(999.dp))
                                .padding(horizontal = 10.dp, vertical = 3.dp),
                        ) {
                            Text("2026", style = MaterialTheme.typography.labelSmall, color = Teal, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    val points = data?.coverage.orEmpty()
                    if (points.isEmpty()) {
                        Text("Belum ada data", style = MaterialTheme.typography.bodySmall, color = Slate400)
                    } else {
                        CoverageLineChart(points)
                    }
                }
            }

            // Aset belum dicek
            SectionLabel("Aset belum dicek")
            if (missingPreview.isEmpty()) {
                Text("Tidak ada aset yang perlu dicek.", style = MaterialTheme.typography.bodySmall, color = Slate400)
            } else {
                missingPreview.forEach { MissingRow(it) }
            }
            OutlinedButton(
                onClick = onMissing,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            ) {
                Text("Lihat semua (${data?.missingTotal ?: 0} aset)")
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, caption: String?, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Slate400)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = color)
            caption?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = Slate400) }
        }
    }
}

@Composable
private fun CoverageLineChart(points: List<CoveragePoint>) {
    val teal = Teal
    val textMeasurer = rememberTextMeasurer()
    val pctStyle = TextStyle(fontSize = 9.sp, color = Teal, fontWeight = FontWeight.Bold)

    Column(Modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
        ) {
            val w = size.width
            val h = size.height
            val padTop = 20f
            val chartH = h - padTop
            val n = points.size
            val stepX = if (n > 1) w / (n - 1) else w

            fun yOf(pct: Int): Float = padTop + chartH * (1f - pct.coerceIn(0, 100) / 100f)

            // Garis bantu putus-putus
            listOf(0f, 0.5f).forEach { frac ->
                val y = padTop + chartH * frac
                drawLine(
                    color = Slate400.copy(alpha = 0.25f),
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)),
                )
            }

            if (n >= 2) {
                val line = Path()
                val area = Path()
                points.forEachIndexed { i, p ->
                    val x = i * stepX
                    val y = yOf(p.pct)
                    if (i == 0) {
                        line.moveTo(x, y)
                        area.moveTo(x, y)
                    } else {
                        line.lineTo(x, y)
                        area.lineTo(x, y)
                    }
                }
                val baseY = padTop + chartH
                area.lineTo((n - 1) * stepX, baseY)
                area.lineTo(0f, baseY)
                area.close()

                drawPath(
                    area,
                    brush = Brush.verticalGradient(
                        colors = listOf(teal.copy(alpha = 0.32f), Color.Transparent),
                        startY = padTop,
                        endY = baseY,
                    ),
                )
                drawPath(
                    line,
                    color = teal,
                    style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round),
                )

                points.forEachIndexed { i, p ->
                    val x = i * stepX
                    val y = yOf(p.pct)
                    drawCircle(Color.White, radius = 7f, center = Offset(x, y))
                    drawCircle(teal, radius = 7f, center = Offset(x, y), style = Stroke(width = 3f))

                    val layout = textMeasurer.measure("${p.pct}%", pctStyle)
                    drawText(
                        layout,
                        topLeft = Offset(
                            x - layout.size.width / 2f,
                            (y - layout.size.height - 6f).coerceAtLeast(0f),
                        ),
                    )
                }
            }
        }

        // Label periode di bawah chart
        Row(Modifier.fillMaxWidth()) {
            points.forEach { p ->
                Text(
                    text = p.label.substringAfterLast(' ').ifBlank { p.label },
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate400,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun MissingRow(a: MissingAsset) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(a.nama, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
            Text(
                listOfNotNull(a.slug, a.lokasi).joinToString(" · "),
                style = MaterialTheme.typography.labelSmall,
                color = Slate400,
            )
        }
        StatusChip(
            text = if (a.urgency == "danger") "Lewat jadwal" else "Perlu dicek",
            tone = if (a.urgency == "danger") ChipTone.BAD else ChipTone.WARN,
        )
    }
}