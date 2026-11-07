package com.kidcurate.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// Primary: Royal Indigo (confidence, learning, playful)
val BrandPrimary = Color(0xFF4F46E5)
val BrandPrimaryLight = Color(0xFF6366F1)
val BrandPrimaryDark = Color(0xFF3730A3)
val BrandPrimaryContainer = Color(0xFFEEF2FF)

// Accent: Warm Sunshine Gold (stars, energy, quests)
val BrandAccent = Color(0xFFF59E0B)
val BrandAccentLight = Color(0xFFFBBF24)
val BrandAccentDark = Color(0xFFD97706)
val BrandAccentContainer = Color(0xFFFEF3C7)

// Success: Mint Emerald (achievements, correct answers)
val BrandSuccess = Color(0xFF10B981)
val BrandSuccessLight = Color(0xFF34D399)
val BrandSuccessContainer = Color(0xFFD1FAE5)

// Supporting playful colors
val BrandCoral = Color(0xFFEF4444)
val BrandCoralLight = Color(0xFFF87171)
val BrandCoralContainer = Color(0xFFFEE2E2)
val BrandSky = Color(0xFF0EA5E9)
val BrandSkyContainer = Color(0xFFE0F2FE)
val BrandViolet = Color(0xFF8B5CF6)
val BrandVioletContainer = Color(0xFFF3E8FF)

// Dynamic Theme-Aware Tokens that automatically match active Light / Dark theme
val BackgroundLight: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.background

val BackgroundDark: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.background

val BackgroundCard: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surface

val SurfaceCard: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surfaceVariant

val SurfaceCardLight: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surfaceVariant

val SurfaceCardBorder: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.outline

// Dynamic Container Tokens (Material 3 Theme Adaptive)
val PrimaryContainerTheme: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.primaryContainer

val OnPrimaryContainerTheme: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onPrimaryContainer

val SecondaryContainerTheme: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.secondaryContainer

val TertiaryContainerTheme: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.tertiaryContainer

// High-contrast, friendly text
val TextPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onBackground

val TextSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onSurfaceVariant

val TextMuted: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.outline
