package com.arffsnia.simap.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.arffsnia.simap.R
import com.arffsnia.simap.ui.theme.Teal

@Composable
fun LoginScreen(onLoggedIn: () -> Unit, vm: LoginViewModel = viewModel()) {
    var showPass by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                // Hijau hanya tipis di dasar layar; bagian atas tetap putih bersih.
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to Color.White,
                        0.60f to Color.White,
                        0.78f to Color(0xFFEAF8F5),
                        0.90f to Color(0xFFB7E7DF),
                        1.0f to Teal,
                    ),
                ),
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(32.dp))

            // Logo bertumpuk: SIMAP (atas) + InJourney Airports (bawah), langsung di gradient
            AsyncImage(
                model = R.drawable.simap,
                contentDescription = "SIMAP",
                contentScale = ContentScale.Fit,
                modifier = Modifier.height(42.dp),
            )
            Spacer(Modifier.height(12.dp))
            AsyncImage(
                model = R.drawable.logoin,
                contentDescription = "InJourney Airports",
                contentScale = ContentScale.Fit,
                modifier = Modifier.width(146.dp),
            )
            Spacer(Modifier.height(20.dp))
            Text(
                "Log in to start your session",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF161616),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Sistem Inspeksi Alat Pemadam Api Ringan",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF575757),
            )

            Spacer(Modifier.height(28.dp))

            OutlinedTextField(
                value = vm.username,
                onValueChange = { vm.username = it; vm.clearError() },
                label = { Text("Username") },
                leadingIcon = { Icon(Icons.Rounded.Person, null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = vm.password,
                onValueChange = { vm.password = it; vm.clearError() },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Rounded.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = { showPass = !showPass }) {
                        Icon(
                            imageVector = if (showPass) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                            contentDescription = "Tampilkan password",
                        )
                    }
                },
                visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth()) {
                Text(
                    "Forgot Password",
                    style = MaterialTheme.typography.bodySmall,
                    color = Teal,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            vm.error?.let {
                Spacer(Modifier.height(12.dp))
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { vm.login(onLoggedIn) },
                enabled = !vm.loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                if (vm.loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Log In", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(40.dp))

            // Footer: teks gelap karena area bawah kini didominasi putih
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Sistem Informasi Manajemen Alat Pemadam (SIMAP)",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF0F766E),
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    "Copyright © 2026 PT Angkasa Pura Indonesia",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                )
                Text(
                    "All Rights Reserved",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8),
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}