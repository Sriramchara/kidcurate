package com.kidcurate.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF6366F1),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF312E81),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = Color(0xFFF59E0B),
    onSecondary = Color(0xFF0B0F19),
    secondaryContainer = Color(0xFF451A03),
    onSecondaryContainer = Color(0xFFFEF3C7),
    tertiary = Color(0xFF10B981),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF064E3B),
    onTertiaryContainer = Color(0xFFD1FAE5),
    background = Color(0xFF0B0F19),
    surface = Color(0xFF111827),
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1F2937),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF374151),
)

val LightColorScheme = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = Color.White,
    primaryContainer = BrandPrimaryContainer,
    onPrimaryContainer = BrandPrimary,
    secondary = BrandAccent,
    onSecondary = Color.White,
    secondaryContainer = BrandAccentContainer,
    onSecondaryContainer = BrandAccentDark,
    tertiary = BrandSuccess,
    onTertiary = Color.White,
    tertiaryContainer = BrandSuccessContainer,
    onTertiaryContainer = BrandSuccess,
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFE2E8F0),
)

@Composable
fun KidCurateTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}

