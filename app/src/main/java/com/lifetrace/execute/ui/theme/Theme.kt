package com.lifetrace.execute.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LifeColorScheme = lightColorScheme(
    primary = LifeBlue,
    onPrimary = LifeSurface,
    primaryContainer = LifeBlueSoft,
    onPrimaryContainer = LifeInk,
    secondary = LifeGreen,
    tertiary = LifeOrange,
    background = LifeBackground,
    onBackground = LifeInk,
    surface = LifeSurface,
    onSurface = LifeInk,
    surfaceVariant = LifeSurfaceMuted,
    onSurfaceVariant = LifeMuted,
    outline = LifeBorder,
    error = LifeRed
)

@Composable
fun LifeTraceExecuteTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LifeColorScheme,
        typography = LifeTypography,
        content = content
    )
}
