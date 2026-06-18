package com.example.hyeonsaengtime.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = HyeonSaengAccentSoft,
    onPrimary = HyeonSaengText,
    secondary = HyeonSaengPrimarySoft,
    onSecondary = HyeonSaengText,
    tertiary = HyeonSaengAccent,
    background = HyeonSaengDarkBackground,
    onBackground = HyeonSaengSurface,
    surface = HyeonSaengDarkSurface,
    onSurface = HyeonSaengSurface,
    surfaceVariant = HyeonSaengDarkSurface,
    outline = HyeonSaengBorder
)

private val LightColorScheme = lightColorScheme(
    primary = HyeonSaengPrimary,
    onPrimary = HyeonSaengSurface,
    secondary = HyeonSaengPrimarySoft,
    onSecondary = HyeonSaengText,
    tertiary = HyeonSaengAccent,
    onTertiary = HyeonSaengText,
    background = HyeonSaengBackground,
    onBackground = HyeonSaengText,
    surface = HyeonSaengSurface,
    onSurface = HyeonSaengText,
    surfaceVariant = HyeonSaengSurfaceSoft,
    onSurfaceVariant = HyeonSaengTextMuted,
    outline = HyeonSaengBorder
)

@Composable
fun HyeonSaengTimeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor -> LightColorScheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
