package com.arffsnia.simap.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arffsnia.simap.SimapApp
import com.arffsnia.simap.ui.admin.AdminDashboardScreen
import com.arffsnia.simap.ui.admin.DataAsetScreen
import com.arffsnia.simap.ui.admin.LaporanAsetScreen
import com.arffsnia.simap.ui.admin.PenggunaScreen
import com.arffsnia.simap.ui.admin.ReguScreen
import com.arffsnia.simap.ui.admin.LaporanHarianScreen
import com.arffsnia.simap.ui.admin.MissingScreen
import com.arffsnia.simap.ui.admin.NotifikasiScreen
import com.arffsnia.simap.ui.admin.PetaSebaranScreen
import com.arffsnia.simap.ui.admin.PengingatScreen
import com.arffsnia.simap.ui.admin.PengajuanDetailScreen
import com.arffsnia.simap.ui.admin.PenugasanDetailScreen
import com.arffsnia.simap.ui.admin.PenugasanScreen
import com.arffsnia.simap.ui.admin.ValidasiScreen
import com.arffsnia.simap.ui.assets.AssetDetailScreen
import com.arffsnia.simap.ui.assets.AssetListScreen
import com.arffsnia.simap.ui.home.HomeScreen
import com.arffsnia.simap.ui.inspection.InspectionScreen
import com.arffsnia.simap.ui.login.LoginScreen
import com.arffsnia.simap.ui.more.MoreScreen
import com.arffsnia.simap.ui.scan.ScanScreen
import com.arffsnia.simap.ui.sync.SyncScreen
import com.arffsnia.simap.ui.theme.Slate400
import com.arffsnia.simap.ui.theme.Teal

sealed class Screen {
    object Home : Screen()
    object Scan : Screen()
    object Assets : Screen()
    object Missing : Screen()
    object Notif : Screen()
    object More : Screen()
    object Penugasan : Screen()
    object Validasi : Screen()
    object Pengingat : Screen()
    object LaporanHarian : Screen()
    object PetaSebaran : Screen()
    object DataAset : Screen()
    object LaporanAset : Screen()
    object Regu : Screen()
    object Pengguna : Screen()
    data class Detail(val slug: String) : Screen()
    data class Inspection(
        val assetId: Int,
        val slug: String,
        val nama: String,
        val categoryId: Int,
    ) : Screen()
    data class PenugasanDetail(val id: Int) : Screen()
    data class PengajuanDetail(val item: com.arffsnia.simap.data.dto.ValidasiItem) : Screen()
    object Sync : Screen()
}

@Composable
fun AppRoot() {
    val app = LocalContext.current.applicationContext as SimapApp
    var loggedIn by remember { mutableStateOf(app.session.isLoggedIn) }

    if (!loggedIn) {
        LoginScreen(onLoggedIn = { loggedIn = true })
        return
    }

    val user = app.authRepo.currentUser()
    val role = Role.fromLevel(user?.level)
    val tabs = RoleNav.items(role)

    var selectedKey by remember { mutableStateOf(tabs.first().key) }
    val pushed = remember { mutableStateListOf<Screen>() }

    // Reset ke tab pertama bila role berganti (mis. logout -> login beda role).
    LaunchedEffect(role) {
        selectedKey = tabs.first().key
        pushed.clear()
    }

    fun push(s: Screen) = pushed.add(s)
    fun pop() { if (pushed.isNotEmpty()) pushed.removeAt(pushed.lastIndex) }
    fun select(item: NavItem) { selectedKey = item.key; pushed.clear() }

    /** Buka layar: pilih tab bila tersedia di role ini, kalau tidak push. */
    fun goto(s: Screen) {
        val tab = tabs.firstOrNull { it.root == s }
        if (tab != null) { selectedKey = tab.key; pushed.clear() } else push(s)
    }

    fun logout() {
        app.authRepo.logout()
        pushed.clear()
        loggedIn = false
    }

    val currentTab = tabs.firstOrNull { it.key == selectedKey } ?: tabs.first()
    val current: Screen = pushed.lastOrNull() ?: currentTab.root

    BackHandler(enabled = pushed.isNotEmpty()) { pop() }

    Column(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f)) {
            when (current) {
                is Screen.Home -> {
                    if (role.isSuper) {
                        AdminDashboardScreen(
                            onMissing = { push(Screen.Missing) },
                            onLogout = { logout() },
                            onNotif = { push(Screen.Notif) },
                            onProfile = { push(Screen.More) },
                        )
                    } else {
                        HomeScreen(
                            levelLabel = role.label,
                            onScan = { goto(Screen.Scan) },
                            onDaftar = { goto(Screen.Assets) },
                            onSync = { push(Screen.Sync) },
                            onMissing = { goto(Screen.Missing) },
                            onLogout = { logout() },
                        )
                    }
                }

                is Screen.Scan -> ScanScreen(
                    onBack = { pop() },
                    onScanned = { slug -> push(Screen.Detail(slug)) },
                )

                is Screen.Assets -> AssetListScreen(
                    onBack = { pop() },
                    onDetail = { push(Screen.Detail(it)) },
                )

                is Screen.Detail -> AssetDetailScreen(
                    slug = current.slug,
                    onBack = { pop() },
                    onInspect = { d ->
                        push(Screen.Inspection(d.id, d.slug, d.nama, d.categoryId))
                    },
                )

                is Screen.Inspection -> InspectionScreen(
                    assetId = current.assetId,
                    slug = current.slug,
                    nama = current.nama,
                    categoryId = current.categoryId,
                    onBack = { pop() },
                    onDone = { pop() },
                )

                is Screen.Missing -> {
                    val hasNotifTab = tabs.any { it.root is Screen.Missing }
                    val back: (() -> Unit)? = if (hasNotifTab) null else ::pop
                    MissingScreen(
                        onBack = back,
                        onOpen = { slug -> push(Screen.Detail(slug)) },
                    )
                }

                is Screen.More -> MoreScreen(
                    onSync = { push(Screen.Sync) },
                    onLogout = { logout() },
                    onNotif = { push(Screen.Notif) },
                    onLaporan = { push(Screen.LaporanHarian) },
                    onPeta = { push(Screen.PetaSebaran) },
                    onPengingat = { push(Screen.Pengingat) },
                    onLaporanAset = { push(Screen.LaporanAset) },
                    onRegu = { push(Screen.Regu) },
                    onPengguna = { push(Screen.Pengguna) },
                )

                is Screen.Regu -> ReguScreen(onBack = { pop() })

                is Screen.Pengguna -> PenggunaScreen(onBack = { pop() })

                is Screen.LaporanAset -> LaporanAsetScreen(
                    onOpen = { slug -> push(Screen.Detail(slug)) },
                    onBack = { pop() },
                    onNotif = { push(Screen.Notif) },
                    onProfile = { push(Screen.More) },
                )

                is Screen.PetaSebaran -> PetaSebaranScreen(
                    onBack = { pop() },
                    onOpen = { slug -> push(Screen.Detail(slug)) },
                    onNotif = { push(Screen.Notif) },
                    onProfile = { push(Screen.More) },
                )

                is Screen.LaporanHarian -> LaporanHarianScreen(
                    onBack = { pop() },
                    onNotif = { push(Screen.Notif) },
                    onProfile = { push(Screen.More) },
                )

                is Screen.DataAset -> DataAsetScreen(
                    onOpen = { slug -> push(Screen.Detail(slug)) },
                    onNotif = { push(Screen.Notif) },
                    onProfile = { push(Screen.More) },
                )

                is Screen.Penugasan -> PenugasanScreen(
                    onOpen = { id -> push(Screen.PenugasanDetail(id)) },
                    onNotif = { push(Screen.Notif) },
                    onProfile = { push(Screen.More) },
                )

                is Screen.Notif -> NotifikasiScreen(
                    onOpen = { slug -> push(Screen.Detail(slug)) },
                    onProfile = { push(Screen.More) },
                )

                is Screen.PenugasanDetail -> PenugasanDetailScreen(
                    id = current.id,
                    onBack = { pop() },
                    onOpenAsset = { slug -> push(Screen.Detail(slug)) },
                    onPeriksa = { assetId, slug, nama, categoryId ->
                        push(Screen.Inspection(assetId, slug, nama, categoryId))
                    },
                    onNotif = { push(Screen.Notif) },
                    onProfile = { push(Screen.More) },
                )

                is Screen.Validasi -> ValidasiScreen(
                    onOpen = { push(Screen.PengajuanDetail(it)) },
                    onNotif = { push(Screen.Notif) },
                    onProfile = { push(Screen.More) },
                )

                is Screen.PengajuanDetail -> PengajuanDetailScreen(
                    item = current.item,
                    onBack = { pop() },
                    onChanged = { pop() },
                    onNotif = { push(Screen.Notif) },
                    onProfile = { push(Screen.More) },
                )

                is Screen.Pengingat -> PengingatScreen(
                    onNotif = { push(Screen.Notif) },
                    onProfile = { push(Screen.More) },
                )

                is Screen.Sync -> SyncScreen(onBack = { pop() })
            }
        }

        // Bottom nav hanya di layar root (sembunyi saat ada layar push/detail).
        if (pushed.isEmpty()) {
            FloatingBottomNav(
                items = tabs,
                selectedKey = selectedKey,
                onSelect = { select(it) },
            )
        }
    }
}

// ===== Bottom navigation pill melayang =====

@Composable
private fun FloatingBottomNav(
    items: List<NavItem>,
    selectedKey: String,
    onSelect: (NavItem) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp)
            .navigationBarsPadding(),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            shadowElevation = 10.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items.forEach { item ->
                    FloatingNavItem(
                        item = item,
                        selected = item.key == selectedKey,
                        onClick = { onSelect(item) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun FloatingNavItem(
    item: NavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconSize by animateDpAsState(
        targetValue = if (selected) 28.dp else 24.dp,
        label = "navIconSize",
    )
    val tint = if (selected) Teal else Slate400

    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = tint,
            modifier = Modifier.size(iconSize),
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = item.label,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = tint,
        )
    }
}

@Composable
fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    androidx.activity.compose.BackHandler(enabled = enabled, onBack = onBack)
}