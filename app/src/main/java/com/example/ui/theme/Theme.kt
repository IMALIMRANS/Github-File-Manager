package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CleanBluePrimaryDark,
    onPrimary = Color(0xFF001D35),
    primaryContainer = CleanBlueContainerDark,
    onPrimaryContainer = CleanBlueOnContainerDark,
    secondary = Color(0xFF38BDF8),
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = Color(0xFF1E293B),
    onSecondaryContainer = Color(0xFFF1F5F9),
    tertiary = CleanMediaIcon,
    onTertiary = Color.White,
    background = CleanCanvasDark,
    onBackground = CleanTextPrimaryDark,
    surface = CleanSurfaceDark,
    onSurface = CleanTextPrimaryDark,
    surfaceVariant = CleanCardDark,
    onSurfaceVariant = CleanTextSecondaryDark,
    outline = CleanBorderDark,
    outlineVariant = CleanBorderDark.copy(alpha = 0.5f),
    error = CleanRedAccent,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = CleanBluePrimary,
    onPrimary = Color.White,
    primaryContainer = CleanBlueContainer,
    onPrimaryContainer = CleanBlueOnContainer,
    secondary = CleanTextPrimaryLight,
    onSecondary = Color.White,
    secondaryContainer = CleanPillBackground,
    onSecondaryContainer = CleanPillText,
    tertiary = CleanMediaIcon,
    onTertiary = Color.White,
    background = CleanCanvasLight,
    onBackground = CleanTextPrimaryLight,
    surface = CleanSurfaceLight,
    onSurface = CleanTextPrimaryLight,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = CleanTextSecondaryLight,
    outline = CleanBorderLight,
    outlineVariant = CleanBorderSubtleLight,
    error = CleanRedAccent,
    onError = Color.White
)

@Composable
fun GitHubFileManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
