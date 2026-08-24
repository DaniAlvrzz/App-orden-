package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AetherColorScheme = darkColorScheme(
    primary = AetherCyan,
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004F58),
    onPrimaryContainer = Color(0xFF9CF1FF),
    
    secondary = AetherViolet,
    onSecondary = Color(0xFF2C007C),
    secondaryContainer = Color(0xFF4A148C),
    onSecondaryContainer = Color(0xFFE8DDFF),
    
    tertiary = AetherEmerald,
    onTertiary = Color(0xFF003919),
    tertiaryContainer = Color(0xFF005327),
    onTertiaryContainer = Color(0xFF6DFFA5),
    
    background = AetherDarkBackground,
    onBackground = AetherTextPrimary,
    surface = AetherSurface,
    onSurface = AetherTextPrimary,
    surfaceVariant = AetherSurfaceCard,
    onSurfaceVariant = AetherTextSecondary,
    outline = AetherBorder,
    outlineVariant = AetherBorderLight,
    error = AetherCoral,
    onError = Color.White
)

@Composable
fun AetherOSTheme(
    darkTheme: Boolean = true, // Aether OS defaults to the Cyber-Zen dark experience
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MyApplicationTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Aether OS defaults to the Cyber-Zen dark experience
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = AetherDarkBackground.toArgb()
                window.navigationBarColor = AetherDarkBackground.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = AetherColorScheme,
        typography = Typography,
        content = content
    )
}
