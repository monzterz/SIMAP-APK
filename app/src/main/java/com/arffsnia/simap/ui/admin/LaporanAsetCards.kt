package com.arffsnia.simap.ui.admin

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.arffsnia.simap.data.dto.Asset
import com.arffsnia.simap.ui.formatTanggal
import com.arffsnia.simap.ui.theme.Slate400
import com.arffsnia.simap.ui.theme.Slate900
import java.io.File

internal val LapBorder = Color(0xFFE2E8F0)
internal val LapHair = Color(0xFFF1F5F9)
internal val LapSlate700 = Color(0xFF334155)
internal val LapSlate500 = Color(0xFF64748B)
internal val LapTeal = Color(0xFF0D9488)

/** Kartu aset pada laporan: data master + status 5 parameter terakhir. */
@Composable
internal fun KartuAsetLaporan(a: Asset, nomor: Int, onOpen: () -> Unit, tampilKondisi: Boolean) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(Color.White)
            .border(1.dp, LapBorder, RoundedCornerShape(13.dp))
            .clickable { onOpen() }
            .padding(11.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row {
            Box(
                Modifier
                    .size(74.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(Color(0xFFF1F5F9))
                    .border(1.dp, LapBorder, RoundedCornerShape(9.dp)),
            ) {
                if (!a.fotoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = a.fotoUrl,
                        contentDescription = a.nama,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Inventory2, null, Modifier.size(20.dp), tint = Slate400)
                    }
                }
                Text(
                    "#$nomor",
                    fontSize = 7.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(3.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xCC0F172A))
                        .padding(horizontal = 3.dp, vertical = 1.dp),
                )
            }
            Spacer(Modifier.width(9.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        a.slug,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFBE123C),
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFFFF1F2))
                            .border(1.dp, Color(0xFFFECDD3), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp),
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        a.category ?: "-",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF115E59),
                        maxLines = 1,
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color(0xFFF0FDFA))
                            .border(1.dp, Color(0xFF99F6E4), RoundedCornerShape(999.dp))
                            .padding(horizontal = 5.dp, vertical = 1.dp),
                    )
                }
                Text(
                    a.nama,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900,
                    maxLines = 1,
                    modifier = Modifier.padding(top = 2.dp),
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(1.dp, LapHair, RoundedCornerShape(6.dp))
                        .padding(5.dp),
                ) {
                    Spesifikasi("Merek", a.merek, Modifier.weight(1f))
                    Spesifikasi("Berat", a.berat, Modifier.weight(1f))
                    Spesifikasi("Tahun", a.tahun?.toString(), Modifier.weight(1f))
                }
                Text(
                    listOfNotNull(a.room, a.lokasi?.takeIf { it.isNotBlank() }).joinToString(" • "),
                    fontSize = 8.sp,
                    color = LapSlate500,
                    maxLines = 1,
                    modifier = Modifier.padding(top = 3.dp),
                )
            }
        }

        HorizontalDivider(color = LapHair)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Periode:", fontSize = 8.sp, color = LapSlate500)
            Spacer(Modifier.width(5.dp))
            Text(
                a.periode ?: "-",
                fontSize = 8.sp,
                fontWeight = FontWeight.Medium,
                color = LapSlate700,
                maxLines = 1,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFF1F5F9))
                    .padding(horizontal = 5.dp, vertical = 1.dp),
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Pengecekan Terakhir:", fontSize = 8.sp, color = LapSlate500)
            Spacer(Modifier.weight(1f))
            Text(
                a.lastChecked?.let { formatTanggal(it) } ?: "Belum pernah",
                fontSize = 8.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                color = LapSlate700,
            )
        }

        if (tampilKondisi) {
            val c = a.checks
            val nilai = listOf(
                "Pressure" to c?.pressure,
                "Pin Segel" to c?.pin,
                "Nozzle" to c?.hose,
                "Handle" to c?.handle,
                "Fisik" to c?.flak,
            )
            val baik = nilai.count { it.second.equals("Baik", true) }
            val semuaBaik = baik == nilai.size
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (semuaBaik) Color(0xFFECFDF5) else Color(0xFFFFF1F2))
                    .border(
                        1.dp,
                        if (semuaBaik) Color(0xFFA7F3D0) else Color(0xFFFECDD3),
                        RoundedCornerShape(9.dp),
                    )
                    .padding(7.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (semuaBaik) "Kondisi: Baik" else "Kondisi: ${nilai.size - baik} bermasalah",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (semuaBaik) Color(0xFF065F46) else Color(0xFF9F1239),
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        "$baik/${nilai.size} Lolos",
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (semuaBaik) Color(0xFF047857) else Color(0xFFBE123C),
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color.White)
                            .padding(horizontal = 4.dp, vertical = 1.dp),
                    )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    nilai.forEach { (label, val_) ->
                        val ok = val_?.equals("Baik", true) == true
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(5.dp))
                                .background(Color.White)
                                .border(1.dp, if (ok) Color(0xFFA7F3D0) else Color(0xFFFECDD3), RoundedCornerShape(5.dp))
                                .padding(vertical = 3.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                label.uppercase(),
                                fontSize = 6.sp,
                                color = Slate400,
                                maxLines = 1,
                            )
                            Text(
                                val_?.ifBlank { "-" } ?: "-",
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (ok) Color(0xFF047857) else Color(0xFFBE123C),
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Spesifikasi(label: String, nilai: String?, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label.uppercase(), fontSize = 6.sp, color = Slate400, maxLines = 1)
        Text(
            nilai?.ifBlank { "-" } ?: "-",
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = LapSlate700,
            maxLines = 1,
        )
    }
}

// ===== ekspor =====

internal fun csvAset(items: List<Asset>): String {
    val sb = StringBuilder("Kode,Nama,Kategori,Merek,Berat,Tahun,Lokasi,Detail Lokasi,Periode,Pengecekan Terakhir,Pressure,Pin,Nozzle,Handle,Fisik\n")
    fun esc(v: String?): String = "\"" + (v ?: "").replace("\"", "\"\"") + "\""
    items.forEach { a ->
        sb.append(
            listOf(
                a.slug, a.nama, a.category, a.merek, a.berat, a.tahun?.toString(),
                a.room, a.lokasi, a.periode, a.lastChecked,
                a.checks?.pressure, a.checks?.pin, a.checks?.hose, a.checks?.handle, a.checks?.flak,
            ).joinToString(",") { esc(it) },
        ).append("\n")
    }
    return sb.toString()
}

internal fun salinCsv(context: Context, csv: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("Laporan Data Aset", csv))
}

internal fun bagikanCsv(context: Context, csv: String, namaFile: String) {
    val dir = File(context.cacheDir, "laporan").apply { mkdirs() }
    val f = File(dir, "$namaFile.csv")
    f.writeText(csv)
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", f)
    val share = Intent(Intent.ACTION_SEND)
        .setType("text/csv")
        .putExtra(Intent.EXTRA_STREAM, uri)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(Intent.createChooser(share, "Bagikan CSV").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

internal fun bukaPdfAset(context: Context, bytes: ByteArray, namaFile: String): String? = try {
    val dir = File(context.cacheDir, "laporan").apply { mkdirs() }
    val f = File(dir, "$namaFile.pdf")
    f.writeBytes(bytes)
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", f)
    context.startActivity(
        Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, "application/pdf")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK),
    )
    null
} catch (e: Exception) {
    "Gagal membuka PDF: ${e.message ?: "aplikasi pembaca tidak ada"}"
}
