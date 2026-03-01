package com.maintx.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val HighContrastLightColors = lightColorScheme(
    primary = WorkshopBlue,
    onPrimary = WorkshopWhite,
    secondary = WorkshopAmber,
    onSecondary = WorkshopBlack,
    tertiary = WorkshopTeal,
    onTertiary = WorkshopBlack,
    background = WorkshopWhite,
    onBackground = WorkshopBlack,
    surface = WorkshopSurfaceLight,
    onSurface = WorkshopBlack,
    error = WorkshopRed,
    onError = WorkshopWhite
)

private val HighContrastDarkColors = darkColorScheme(
    primary = WorkshopSky,
    onPrimary = WorkshopBlack,
    secondary = WorkshopAmber,
    onSecondary = WorkshopBlack,
    tertiary = WorkshopTeal,
    onTertiary = WorkshopBlack,
    background = WorkshopBlack,
    onBackground = WorkshopWhite,
    surface = WorkshopSurfaceDark,
    onSurface = WorkshopWhite,
    error = WorkshopRed,
    onError = WorkshopBlack
)

@Composable
fun MaintXTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) HighContrastDarkColors else HighContrastLightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
