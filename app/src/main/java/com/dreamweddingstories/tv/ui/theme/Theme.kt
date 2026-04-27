@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)

package com.dreamweddingstories.tv.ui.theme

import androidx.compose.runtime.Composable
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

// ═══════════════════════════════════════════════════════
// CINEMATIC LUXURY EDITORIAL — Theme
// ═══════════════════════════════════════════════════════

private val CinematicColorScheme = darkColorScheme(
    primary = AccentGold,
    onPrimary = DarkBackground,
    secondary = AccentGoldDeep,
    onSecondary = DarkBackground,
    tertiary = GoldShimmer,
    onTertiary = DarkBackground,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = CardBackground,
    onSurfaceVariant = TextSecondary,
    error = ErrorAmber,
    onError = DarkBackground,
    border = GoldDivider
)

@Composable
fun DreamWeddingTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CinematicColorScheme,
        typography = Typography,
        content = content
    )
}
