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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arffsnia.simap.data.dto.LogItem
import com.arffsnia.simap.data.dto.LogResponse
import com.arffsnia.simap.ui.theme.SimapTeal
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
internal fun KartuAktivitas(
    data: LogResponse?,
    query: String,
    onQuery: (String) -> Unit,
    onPage: (Int) -> Unit,
) {
    KartuDash("Laporan Data Aktivitas") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF8FAFC))
                .border(1.dp, DashBorder, RoundedCornerShape(10.dp))
                .padding(horizontal = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.Search, null, Modifier.size(13.dp), tint = Color(0xFF94A3B8))
            Spacer(Modifier.width(6.dp))
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (query.isEmpty()) {
                    Text("Cari aktivitas, pengguna, keterangan...", fontSize = 9.sp, color = Color(0xFF94A3B8))
                }
                BasicTextFieldKu(query, onQuery)
            }
        }

        val d = data
        if (d == null) {
            Text("Memuat aktivitas...", fontSize = 9.sp, color = DashSlate500)
            return@KartuDash
        }
        if (d.items.isEmpty()) {
            Text("Tidak ada aktivitas.", fontSize = 9.sp, color = DashSlate500)
            return@KartuDash
        }

        d.items.forEach { item -> BarisLog(item) }

        HorizontalDivider(color = DashHair)
        Row(verticalAlignment = Alignment.CenterVertically) {
            val dari = (d.page - 1) * d.limit + 1
            val sampai = (dari + d.items.size - 1).coerceAtMost(d.total)
            Text("Menampilkan $dari–$sampai dari ${d.total}", fontSize = 8.sp, color = DashSlate500)
            Spacer(Modifier.weight(1f))
            TombolHalaman("‹", enabled = d.page > 1) { onPage(d.page - 1) }
            Text(
                "${d.page}/${d.pages}",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F766E),
                modifier = Modifier.padding(horizontal = 7.dp),
            )
            TombolHalaman("›", enabled = d.page < d.pages) { onPage(d.page + 1) }
        }
    }
}

@Composable
private fun BasicTextFieldKu(value: String, onValueChange: (String) -> Unit) {
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(fontSize = 9.sp, color = Color(0xFF0F172A)),
        cursorBrush = SolidColor(SimapTeal),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun TombolHalaman(label: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, DashBorder, RoundedCornerShape(6.dp))
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontSize = 11.sp, color = if (enabled) DashSlate700 else Color(0xFFCBD5E1))
    }
}

@Composable
private fun BarisLog(item: LogItem) {
    val warna = when {
        item.activity.contains("Hapus", true) -> Color(0xFFF43F5E)
        item.activity.contains("Login", true) -> Color(0xFF0EA5E9)
        item.activity.contains("Edit", true) || item.activity.contains("Ubah", true) -> Color(0xFFF59E0B)
        item.activity.contains("Nonaktif", true) -> Color(0xFF475569)
        else -> Color(0xFF0D9488)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, DashBorder, RoundedCornerShape(10.dp))
            .padding(9.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(item.user, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F766E))
            Spacer(Modifier.width(4.dp))
            Text(
                item.level.uppercase(),
                fontSize = 7.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF10B981))
                    .padding(horizontal = 3.dp, vertical = 1.dp),
            )
            Spacer(Modifier.weight(1f))
            Text(
                item.activity,
                fontSize = 8.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 1,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(warna)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            )
        }
        if (item.description.isNotBlank()) {
            Text(item.description, fontSize = 9.sp, color = DashSlate700, maxLines = 2)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Text(waktuLog(item.createdAt), fontSize = 8.sp, color = Color(0xFF94A3B8))
        }
    }
}

private fun waktuLog(iso: String): String {
    if (iso.isBlank()) return "-"
    return try {
        val masuk = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val keluar = SimpleDateFormat("d MMMM yyyy - HH:mm:ss", Locale("id", "ID"))
        masuk.parse(iso)?.let { keluar.format(it) } ?: iso
    } catch (e: Exception) {
        iso
    }
}
