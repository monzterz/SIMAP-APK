package com.arffsnia.simap.ui.more

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.arffsnia.simap.SimapApp
import com.arffsnia.simap.ui.Role
import com.arffsnia.simap.ui.theme.Slate400
import com.arffsnia.simap.ui.theme.Slate50
import com.arffsnia.simap.ui.theme.Teal

@Composable
fun MoreScreen(onSync: () -> Unit, onLogout: () -> Unit) {
    val app = LocalContext.current.applicationContext as SimapApp
    val user = remember { app.authRepo.currentUser() }
    val name = user?.fullName ?: user?.username ?: "User"
    val role = remember { Role.fromLevel(user?.level) }
    val initial = name.firstOrNull()?.uppercase() ?: "?"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate50)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        // Profil
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 12.dp, bottom = 16.dp)) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Teal, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(initial, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.padding(5.dp))
            Column {
                Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(role.label, style = MaterialTheme.typography.bodySmall, color = Slate400)
            }
        }

        MenuRow(Icons.Rounded.Autorenew, "Sinkronisasi", "Semua terkirim", onClick = onSync)
        MenuRow(Icons.Rounded.History, "Riwayat cek", "Akan datang")
        MenuRow(Icons.Rounded.Map, "Peta sebaran", "Akan datang")
        Spacer(Modifier.height(8.dp))
        MenuRow(
            icon = Icons.Rounded.Autorenew,
            title = "Keluar",
            subtitle = "",
            danger = true,
            onClick = onLogout,
        )
    }
}

@Composable
private fun MenuRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    danger: Boolean = false,
) {
    val accent = if (danger) com.arffsnia.simap.ui.theme.Bad else Teal
    Card(
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(accent.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.padding(5.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                if (subtitle.isNotEmpty()) {
                    Text(subtitle, style = MaterialTheme.typography.labelSmall, color = Slate400)
                }
            }
        }
    }
}