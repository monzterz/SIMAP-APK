package com.arffsnia.simap.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Teal,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = TealContainer,
    onPrimaryContainer = TealDark,
    secondary = Sky,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = SkyContainer,
    onSecondaryContainer = Slate900,
    background = Slate50,
    onBackground = Slate900,
    surface = androidx.compose.ui.graphics.Color.White,
    onSurface = Slate900,
    error = Bad,
)

@Composable
fun SimapTheme(content: @Composable () -> Unit) {
    // Lock to light (flat modern) — dark mode belum jadi prioritas.
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography(),
        content = content,
    )
}