package com.arffsnia.simap.ui.admin

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arffsnia.simap.SimapApp
import com.arffsnia.simap.data.dto.BulanStat
import com.arffsnia.simap.data.dto.CoverageIns
import com.arffsnia.simap.data.dto.DashboardMobile
import com.arffsnia.simap.data.dto.InspeksiStat
import com.arffsnia.simap.data.dto.KategoriStat
import com.arffsnia.simap.data.dto.LogItem
import com.arffsnia.simap.data.dto.LogResponse
import com.arffsnia.simap.data.dto.LokasiStat
import com.arffsnia.simap.data.repo.AppResult
import com.arffsnia.simap.ui.mapError
import com.arffsnia.simap.ui.theme.PlusJakarta
import com.arffsnia.simap.ui.theme.PlusJakartaTypography
import com.arffsnia.simap.ui.theme.SimapTeal
import com.arffsnia.simap.ui.theme.Slate400
import com.arffsnia.simap.ui.theme.Slate900
import java.text.SimpleDateFormat
import java.util.Locale

internal val DashBg = Color(0xFFF1F5F9)
internal val DashBorder = Color(0xFFE2E8F0)
internal val DashHair = Color(0xFFF1F5F9)
internal val DashSlate700 = Color(0xFF334155)
internal val DashSlate500 = Color(0xFF64748B)

@Composable
fun AdminDashboardScreen(
    onMissing: () -> Unit,
    onLogout: () -> Unit,
    onNotif: (() -> Unit)? = null,
    onProfile: (() -> Unit)? = null,
) {
    val app = LocalContext.current.applicationContext as SimapApp
    var user by remember { mutableStateOf(app.authRepo.currentUser()) }
    val nama = user?.fullName ?: user?.username ?: "Admin"

    var data by remember { mutableStateOf<DashboardMobile?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableStateOf(0) }

    var logQuery by remember { mutableStateOf("") }
    var logPage by remember { mutableStateOf(1) }
    var logData by remember { mutableStateOf<LogResponse?>(null) }

    LaunchedEffect(refreshKey) {
        // Ambil ulang profil (foto) dari server, lalu render.
        app.authRepo.refreshUser()
        user = app.authRepo.currentUser()
        loading = true
        when (val r = app.adminRepo.dashboardMobile()) {
            is AppResult.Success -> { data = r.data; error = null }
            is AppResult.Error -> error = mapError(r.message)
        }
        loading = false
    }

    LaunchedEffect(logQuery, logPage) {
        when (val r = app.adminRepo.logs(logQuery, logPage, 10)) {
            is AppResult.Success -> logData = r.data
            is AppResult.Error -> Unit
        }
    }

    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme,
        typography = PlusJakartaTypography,
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides TextStyle(
                fontFamily = PlusJakarta,
                fontSize = 12.sp,
                platformStyle = PlatformTextStyle(includeFontPadding = false),
            ),
        ) {
            Column(Modifier.fillMaxSize().background(DashBg).statusBarsPadding()) {
                TopBar(user = user, onNotif = onNotif, onProfile = onProfile)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp)
                        .padding(top = 12.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    HeroDash()
                    GreetingDash(nama, user?.level ?: "Super Admin", user?.profileUrl)

                    when {
                        loading -> Box(
                            Modifier.fillMaxWidth().height(160.dp),
                            contentAlignment = Alignment.Center,
                        ) { CircularProgressIndicator(color = SimapTeal) }

                        error != null && data == null -> Column(
                            Modifier.fillMaxWidth().padding(vertical = 18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(error!!, fontSize = 11.sp, color = Color(0xFFBE123C))
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Coba lagi",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SimapTeal,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF0FDFA))
                                    .clickable { refreshKey++ }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                            )
                        }

                        data != null -> {
                            val d = data!!
                            MetricDash(d)
                            BarisBelumDicek(d.summary.belumDicek, onMissing)
                            KartuCoverage(
                                points = d.coverage.all.ifEmpty { d.coverage.points },
                                active = d.coverage.active,
                                years = d.coverage.years,
                            )
                            KartuDistribusi(d.kategori)
                            KartuMaintenance(d.maintenance)
                            KartuInspeksi(d.inspeksi)
                            KartuLokasi(d.lokasi)
                            KartuAktivitas(
                                data = logData,
                                query = logQuery,
                                onQuery = { logQuery = it; logPage = 1 },
                                onPage = { logPage = it },
                            )
                        }
                    }
                }
            }
        }
    }
}
