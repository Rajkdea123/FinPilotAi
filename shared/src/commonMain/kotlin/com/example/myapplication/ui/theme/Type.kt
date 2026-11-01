package com.example.myapplication.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun AppTypography(): Typography {
    val soraBold = FontFamily.SansSerif
    val dmSansRegular = FontFamily.SansSerif

    return Typography(
        headlineLarge = TextStyle(
            fontFamily = soraBold,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = dmSansRegular,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp
        )
    )
}
