package com.example.myapplication.ui.theme

import androidx.compose.ui.graphics.Color

enum class Accent(
    val label: String,
    val lightPrimary: Color,
    val lightOnPrimary: Color,
    val darkPrimary: Color,
    val darkOnPrimary: Color
) {
    Mint(
        label = "Mint",
        lightPrimary = Color(0xFF0B7F63),
        lightOnPrimary = Color(0xFFFFFFFF),
        darkPrimary = Color(0xFF2DD4A7),
        darkOnPrimary = Color(0xFF06251C)
    ),
    Violet(
        label = "Violet",
        lightPrimary = Color(0xFF6B5CE7),
        lightOnPrimary = Color(0xFFFFFFFF),
        darkPrimary = Color(0xFF9B8CFF),
        darkOnPrimary = Color(0xFF110B3B)
    ),
    Sky(
        label = "Sky",
        lightPrimary = Color(0xFF0284C7),
        lightOnPrimary = Color(0xFFFFFFFF),
        darkPrimary = Color(0xFF38BDF8),
        darkOnPrimary = Color(0xFF032B42)
    ),
    Amber(
        label = "Amber",
        lightPrimary = Color(0xFFD97706),
        lightOnPrimary = Color(0xFFFFFFFF),
        darkPrimary = Color(0xFFF59E0B),
        darkOnPrimary = Color(0xFF3B2100)
    ),
    Coral(
        label = "Coral",
        lightPrimary = Color(0xFFE11D48),
        lightOnPrimary = Color(0xFFFFFFFF),
        darkPrimary = Color(0xFFFB7185),
        darkOnPrimary = Color(0xFF4C0519)
    )
}

// Dark theme colors
private val DarkBackground = Color(0xFF0B1220)
private val DarkSurface = Color(0xFF131C2E)
private val DarkSurfaceVariant = Color(0xFF1B2740)
private val DarkOutline = Color(0xFF26344F)
private val DarkOnBackground = Color(0xFFEAF0FA)
private val DarkOnSurfaceVariant = Color(0xFF9AA8C1)
private val DarkPrimary = Color(0xFF2DD4A7)
private val DarkOnPrimary = Color(0xFF06251C)
private val DarkAi = Color(0xFF9B8CFF)
private val DarkWarning = Color(0xFFFFB547)
private val DarkDanger = Color(0xFFFF7A90)
private val DarkOnDanger = Color(0xFF2B0710)
private val DarkInfo = Color(0xFF4CC9F0)

// Light theme colors
private val LightBackground = Color(0xFFF4F7FB)
private val LightSurface = Color(0xFFFFFFFF)
private val LightSurfaceVariant = Color(0xFFE8EEF7)
private val LightOutline = Color(0xFFD5DEEC)
private val LightOnBackground = Color(0xFF0B1220)
private val LightOnSurfaceVariant = Color(0xFF51607A)
private val LightPrimary = Color(0xFF0B7F63)
private val LightOnPrimary = Color(0xFFFFFFFF)
private val LightAi = Color(0xFF6B5CE7)
private val LightWarning = Color(0xFF9A5B00)
private val LightDanger = Color(0xFFC42F4B)
private val LightOnDanger = Color(0xFFFFFFFF)
private val LightInfo = Color(0xFF0A6F96)

data class AppColors(
    val ai: Color,
    val warning: Color,
    val danger: Color,
    val onDanger: Color,
    val info: Color
)

val darkAppColors = AppColors(
    ai = DarkAi,
    warning = DarkWarning,
    danger = DarkDanger,
    onDanger = DarkOnDanger,
    info = DarkInfo
)

val lightAppColors = AppColors(
    ai = LightAi,
    warning = LightWarning,
    danger = LightDanger,
    onDanger = LightOnDanger,
    info = LightInfo
)
