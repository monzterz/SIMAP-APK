package com.arffsnia.simap.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import com.arffsnia.simap.ui.admin.ScreenHero
import com.arffsnia.simap.ui.theme.Teal
import com.arffsnia.simap.ui.theme.Warn

@Composable
fun HomeScreen(
    levelLabel: String,
    onScan: () -> Unit,
    onDaftar: () -> Unit,
    onSync: () -> Unit,
    onMissing: () -> Unit,
    onLogout: () -> Unit,
) {
    val app = LocalContext.current.applicationContext as SimapApp
    val user = remember { app.authRepo.currentUser() }
    val name = user?.fullName ?: user?.username ?: "Petugas"

    Scaffold { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad),
        ) {
            ScreenHero(name, levelLabel, onLogout)

            Column(Modifier.padding(16.dp)) {
                // Reminder aset belum dicek
                Card(
                    onClick = onMissing,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
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
                                .background(Warn.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                                .padding(8.dp),
                        ) {
                            Icon(Icons.Rounded.Notifications, null, tint = Warn)
                        }
                        Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                            Text("Aset belum dicek", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Lihat APAR yang belum dicek periode ini",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            )
                        }
                        Icon(Icons.Rounded.ChevronRight, null, tint = Color.Gray)
                    }
                }

                Spacer(Modifier.height(18.dp))
                Text(
                    "Aksi cepat",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = onScan,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Rounded.QrCodeScanner, null)
                    Spacer(Modifier.padding(4.dp))
                    Text("Scan QR Aset")
                }

                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDaftar, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Rounded.Inventory2, null, tint = Teal)
                        Spacer(Modifier.padding(4.dp))
                        Text("Daftar")
                    }
                    OutlinedButton(onClick = onSync, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Rounded.Autorenew, null, tint = Teal)
                        Spacer(Modifier.padding(4.dp))
                        Text("Sinkron")
                    }
                }
            }
        }
    }
}