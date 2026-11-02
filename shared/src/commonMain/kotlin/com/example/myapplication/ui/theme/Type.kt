package com.example.myapplication.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun AppTypography(): Typography {
    val soraFontFamily = FontFamily.SansSerif
    val dmSansFontFamily = FontFamily.SansSerif

    return Typography(
        headlineLarge = TextStyle(
            fontFamily = soraFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = soraFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = soraFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp
        ),
        titleLarge = TextStyle(
            fontFamily = soraFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        ),
        titleMedium = TextStyle(
            fontFamily = soraFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = dmSansFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = dmSansFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp
        ),
        bodySmall = TextStyle(
            fontFamily = dmSansFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp
        )
    )
}
