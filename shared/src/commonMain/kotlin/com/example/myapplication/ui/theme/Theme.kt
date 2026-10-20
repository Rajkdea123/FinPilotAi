package com.example.myapplication.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Dark theme colors
private val DarkBackground = Color(0xFF0B1220)
private val DarkSurface = Color(0xFF131C2E)
private val DarkSurfaceVariant = Color(0xFF1B2740)
private val DarkOutline = Color(0xFF26344F)
private val DarkOnBackground = Color(0xFFEAF0FA)
private val DarkOnSurfaceVariant = Color(0xFF9AA8C1)
private val DarkPrimary = Color(0xFF2DD4A7)
private val DarkOnPrimary = Color(0xFF06251C)

// Light theme colors
private val LightBackground = Color(0xFFF4F7FB)
private val LightSurface = Color(0xFFFFFFFF)
private val LightSurfaceVariant = Color(0xFFE8EEF7)
private val LightOutline = Color(0xFFD5DEEC)
private val LightOnBackground = Color(0xFF0B1220)
private val LightOnSurfaceVariant = Color(0xFF51607A)
private val LightPrimary = Color(0xFF0B7F63)
private val LightOnPrimary = Color(0xFFFFFFFF)

private val DarkColorScheme: ColorScheme = darkColorScheme(
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    outline = DarkOutline,
    onBackground = DarkOnBackground,
    onSurfaceVariant = DarkOnSurfaceVariant,
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary
)

private val LightColorScheme: ColorScheme = lightColorScheme(
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    outline = LightOutline,
    onBackground = LightOnBackground,
    onSurfaceVariant = LightOnSurfaceVariant,
    primary = LightPrimary,
    onPrimary = LightOnPrimary
)

private val LocalAppColors = staticCompositionLocalOf<AppColors> {
    error("AppColors not provided")
}

enum class ThemeMode {
    System,
    Light,
    Dark
}

@Composable
fun AppTheme(
    themeMode: ThemeMode = ThemeMode.System,
    accent: Accent = Accent.Mint,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Dark -> true
        ThemeMode.Light -> false
    }

    val primaryColor = if (darkTheme) accent.darkPrimary else accent.lightPrimary
    val onPrimaryColor = if (darkTheme) accent.darkOnPrimary else accent.lightOnPrimary

    val colorScheme = if (darkTheme) {
        DarkColorScheme.copy(
            primary = primaryColor,
            onPrimary = onPrimaryColor
        )
    } else {
        LightColorScheme.copy(
            primary = primaryColor,
            onPrimary = onPrimaryColor
        )
    }

    val appColors = if (darkTheme) darkAppColors else lightAppColors
    val typography = AppTypography()

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}

object AppTheme {
    val colors: AppColors
        @Composable
        get() = LocalAppColors.current
}
