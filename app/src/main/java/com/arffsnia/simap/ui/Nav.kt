package com.arffsnia.simap.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Assignment
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Model hak akses terpusat. SATU sumber kebenaran untuk role -> label + tabs.
 * Tambah/ganti fitur per role = ubah satu baris di [RoleNav.items], bukan
 * menabur `if (level == ...)` di tiap layar.
 */
enum class Role(val label: String) {
    PETUGAS("Petugas ARFF"),
    ADMIN_REGU("Admin Regu"),
    SUPER_ADMIN("Super Admin");

    val isSuper: Boolean get() = this == SUPER_ADMIN
    val isAdminRegu: Boolean get() = this == ADMIN_REGU

    companion object {
        /** Nilai `user.level` dari backend: "Super Admin" / "Admin Regu" / "User". */
        fun fromLevel(level: String?): Role = when (level?.trim()?.lowercase()) {
            "super admin" -> SUPER_ADMIN
            "admin regu" -> ADMIN_REGU
            else -> PETUGAS
        }
    }
}

data class NavItem(
    val key: String,
    val label: String,
    val icon: ImageVector,
    val root: Screen,
)

object RoleNav {
    fun items(role: Role): List<NavItem> = when (role) {
        Role.PETUGAS -> listOf(
            NavItem("home", "Beranda", Icons.Rounded.Home, Screen.Home),
            NavItem("scan", "Cek", Icons.Rounded.QrCodeScanner, Screen.Scan),
            NavItem("daftar", "Daftar", Icons.Rounded.FormatListBulleted, Screen.Assets),
            NavItem("notif", "Notif", Icons.Rounded.Notifications, Screen.Notif),
            NavItem("lainnya", "Lainnya", Icons.Rounded.Apps, Screen.More),
        )

        Role.ADMIN_REGU -> listOf(
            NavItem("home", "Beranda", Icons.Rounded.Home, Screen.Home),
            NavItem("penugasan", "Penugasan", Icons.Rounded.Assignment, Screen.Penugasan),
            NavItem("daftar", "Daftar", Icons.Rounded.FormatListBulleted, Screen.Assets),
            NavItem("notif", "Notif", Icons.Rounded.Notifications, Screen.Notif),
            NavItem("lainnya", "Lainnya", Icons.Rounded.Apps, Screen.More),
        )

        Role.SUPER_ADMIN -> listOf(
            NavItem("home", "Beranda", Icons.Rounded.Home, Screen.Home),
            NavItem("penugasan", "Penugasan", Icons.Rounded.Assignment, Screen.Penugasan),
            NavItem("validasi", "Validasi", Icons.Rounded.Verified, Screen.Validasi),
            NavItem("aset", "Data Aset", Icons.Rounded.Inventory2, Screen.DataAset),
            NavItem("lainnya", "Lainnya", Icons.Rounded.Apps, Screen.More),
        )
    }
}