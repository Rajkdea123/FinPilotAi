package com.example.myapplication.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import myapplication.shared.generated.resources.Res
import myapplication.shared.generated.resources.dmsans_bold
import myapplication.shared.generated.resources.dmsans_medium
import myapplication.shared.generated.resources.dmsans_regular
import myapplication.shared.generated.resources.sora_bold
import org.jetbrains.compose.resources.Font

@Composable
fun AppTypography(): Typography {
    val soraBold = FontFamily(Font(Res.font.sora_bold))
    val dmSansRegular = FontFamily(Font(Res.font.dmsans_regular))
    val dmSansMedium = FontFamily(Font(Res.font.dmsans_medium))
    val dmSansBold = FontFamily(Font(Res.font.dmsans_bold))

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
