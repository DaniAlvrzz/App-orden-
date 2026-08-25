package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.data.model.AppThemeMode

private val AetherDarkColorScheme = darkColorScheme(
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

private val AetherLightColorScheme = lightColorScheme(
    primary = Color(0xFF00838F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB2EBF2),
    onPrimaryContainer = Color(0xFF00363D),
    
    secondary = Color(0xFF6200EA),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEDE7F6),
    onSecondaryContainer = Color(0xFF2C007C),
    
    tertiary = Color(0xFF00796B),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE0F2F1),
    onTertiaryContainer = Color(0xFF003919),
    
    background = AetherLightBackground,
    onBackground = AetherLightTextPrimary,
    surface = AetherLightSurface,
    onSurface = AetherLightTextPrimary,
    surfaceVariant = AetherLightSurfaceElevated,
    onSurfaceVariant = AetherLightTextSecondary,
    outline = AetherLightBorder,
    outlineVariant = AetherLightBorderLight,
    error = Color(0xFFD32F2F),
    onError = Color.White
)

@Composable
fun AetherOSTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> systemInDark
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    MyApplicationTheme(darkTheme = isDark, dynamicColor = dynamicColor, content = content)
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) AetherDarkColorScheme else AetherLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
