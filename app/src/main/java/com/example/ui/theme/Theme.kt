package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CosmicDarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = CosmicVoid,
    primaryContainer = SpaceSurfaceElevated,
    onPrimaryContainer = ElectricCyan,
    secondary = PropulsionOrange,
    onSecondary = Color.White,
    secondaryContainer = SpaceSurfaceVariant,
    onSecondaryContainer = PropulsionOrangeLight,
    tertiary = OrbitBlue,
    onTertiary = CosmicVoid,
    background = CosmicBackground,
    onBackground = TextPrimary,
    surface = SpaceSurface,
    onSurface = TextPrimary,
    surfaceVariant = SpaceSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = SpaceBorder,
    outlineVariant = SpaceBorderHighlight,
    error = LaserRed,
    onError = Color.White
)

@Composable
fun CosmoFrontierTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CosmicDarkColorScheme,
        typography = Typography,
        content = content
    )
}
