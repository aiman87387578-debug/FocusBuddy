package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonPurple,
    onPrimary = Color.White,
    secondary = NeonPurpleLight,
    onSecondary = Color.White,
    tertiary = AccentViolet,
    background = ObsidianBg,
    onBackground = Color.White,
    surface = CharcoalCard,
    onSurface = Color.White,
    surfaceVariant = CharcoalCardElevated,
    onSurfaceVariant = TextSecondary,
    error = DangerRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme for ADHD sensory control
    dynamicColor: Boolean = false, // Disable dynamic colors to preserve our strict custom neon purple branding
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
