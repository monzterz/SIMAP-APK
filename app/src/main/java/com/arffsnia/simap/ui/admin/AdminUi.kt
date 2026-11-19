package com.arffsnia.simap.ui.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.arffsnia.simap.R
import com.arffsnia.simap.ui.theme.Good
import com.arffsnia.simap.ui.theme.Sky
import com.arffsnia.simap.ui.theme.Slate200
import com.arffsnia.simap.ui.theme.Slate600
import com.arffsnia.simap.ui.theme.Teal
import com.arffsnia.simap.ui.theme.Bad
import com.arffsnia.simap.ui.theme.Warn

enum class ChipTone { GOOD, WARN, BAD, SKY, SLATE, TEAL }

@Composable
fun StatusChip(text: String, tone: ChipTone = ChipTone.SLATE, modifier: Modifier = Modifier) {
    val (fg, bg) = when (tone) {
        ChipTone.GOOD -> Good to Color(0xFFDCFCE7)
        ChipTone.WARN -> Warn to Color(0xFFFEF3C7)
        ChipTone.BAD -> Bad to Color(0xFFFEE2E2)
        ChipTone.SKY -> Sky to Color(0xFFE0F2FE)
        ChipTone.TEAL -> Teal to Color(0xFFCCFBF1)
        ChipTone.SLATE -> Slate600 to Color(0xFFF1F5F9)
    }
    Box(
        modifier = modifier
            .background(bg, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = Slate600,
        fontWeight = FontWeight.Bold,
        modifier = modifier.padding(top = 18.dp, bottom = 8.dp),
    )
}

@Composable
fun ProgressBar(percent: Int, modifier: Modifier = Modifier) {
    val p = percent.coerceIn(0, 100)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .background(Slate200, RoundedCornerShape(6.dp)),
    ) {
        if (p > 0) {
            Box(
                Modifier
                    .fillMaxWidth(p / 100f)
                    .height(8.dp)
                    .background(Brush.horizontalGradient(listOf(Teal, Good)), RoundedCornerShape(6.dp)),
            )
        }
    }
}

@Composable
fun ScreenHero(name: String, subtitle: String, onLogout: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Teal, Sky))),
    ) {
        OrnamenHeader(
            Modifier.align(Alignment.BottomEnd).offset(x = 28.dp, y = 20.dp),
            ukuran = 140.dp,
            alpha = 0.14f,
        )
        Column(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 20.dp),
        ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
        ) {
            Column {
                Text("Halo,", style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.9f))
                Text(
                    name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                )
            }
            IconButton(onClick = onLogout) {
                Icon(Icons.Rounded.Logout, "Keluar", tint = Color.White)
            }
        }
        }
    }
}

/**
 * Ornamen header: watermark grf.png (putih transparan) dipakai seragam di semua banner gradien.
 * Taruh di dalam Box banner yang sudah punya clip(RoundedCornerShape(...)) supaya ikut terpotong.
 */
@Composable
fun OrnamenHeader(
    modifier: Modifier = Modifier,
    ukuran: Dp = 128.dp,
    alpha: Float = 0.18f,
) {
    Image(
        painter = painterResource(R.drawable.grf),
        contentDescription = null,
        modifier = modifier.size(ukuran).alpha(alpha),
        contentScale = ContentScale.Fit,
    )
}