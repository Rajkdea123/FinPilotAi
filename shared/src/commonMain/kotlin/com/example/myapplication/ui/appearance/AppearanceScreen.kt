package com.example.myapplication.ui.appearance

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.components.PrimaryButton
import com.example.myapplication.ui.components.ScreenTopBar
import com.example.myapplication.ui.icons.AppIcons
import com.example.myapplication.ui.theme.Accent
import com.example.myapplication.ui.theme.AppTheme
import com.example.myapplication.ui.theme.ThemeMode
import org.koin.compose.koinInject

@Composable
fun AppearanceScreen(
    onBack: (message: String?) -> Unit,
    viewModel: AppearanceViewModel = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onBack("Appearance updated")
        }
    }

    LaunchedEffect(uiState.navigateBack) {
        if (uiState.navigateBack) {
            onBack(null)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .imePadding()
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            ScreenTopBar(
                title = "Appearance",
                onBack = { viewModel.onEvent(AppearanceEvent.BackClicked) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Theme Option Cards
                ThemeOptionCard(
                    title = "Dark",
                    subtitle = "Midnight Mint, default",
                    themeMode = ThemeMode.Dark,
                    isSelected = uiState.themeMode == ThemeMode.Dark,
                    onSelect = { viewModel.onEvent(AppearanceEvent.ThemeSelected(ThemeMode.Dark)) }
                )

                ThemeOptionCard(
                    title = "Light",
                    subtitle = "Bright ground, deeper mint",
                    themeMode = ThemeMode.Light,
                    isSelected = uiState.themeMode == ThemeMode.Light,
                    onSelect = { viewModel.onEvent(AppearanceEvent.ThemeSelected(ThemeMode.Light)) }
                )

                ThemeOptionCard(
                    title = "System",
                    subtitle = "Follows your phone",
                    themeMode = ThemeMode.System,
                    isSelected = uiState.themeMode == ThemeMode.System,
                    onSelect = { viewModel.onEvent(AppearanceEvent.ThemeSelected(ThemeMode.System)) }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Heading "Accent color"
                Text(
                    text = "Accent color",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Row of five 48dp circles for Mint, Violet, Sky, Amber, Coral
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isDarkTheme = when (uiState.themeMode) {
                        ThemeMode.System -> isSystemInDarkTheme()
                        ThemeMode.Dark -> true
                        ThemeMode.Light -> false
                    }

                    Accent.entries.forEach { accent ->
                        val isSelected = uiState.accent == accent
                        val circleColor = if (isDarkTheme) accent.darkPrimary else accent.lightPrimary
                        val iconColor = if (isDarkTheme) accent.darkOnPrimary else accent.lightOnPrimary

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(circleColor)
                                .then(
                                    if (isSelected) {
                                        Modifier.border(
                                            width = 3.dp,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            shape = CircleShape
                                        )
                                    } else Modifier
                                )
                                .clickable { viewModel.onEvent(AppearanceEvent.AccentSelected(accent)) }
                                .semantics {
                                    contentDescription = "${accent.label} accent color"
                                    selected = isSelected
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    painter = rememberVectorPainter(AppIcons.CheckIcon),
                                    contentDescription = null,
                                    tint = iconColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Apply Button
            PrimaryButton(
                text = "Apply",
                onClick = { viewModel.onEvent(AppearanceEvent.ApplyClicked) },
                isLoading = uiState.isSaving,
                modifier = Modifier.padding(bottom = 36.dp)
            )
        }
    }
}

@Composable
private fun ThemeOptionCard(
    title: String,
    subtitle: String,
    themeMode: ThemeMode,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(
                border = BorderStroke(2.dp, borderColor),
                shape = RoundedCornerShape(18.dp)
            )
            .selectable(
                selected = isSelected,
                onClick = onSelect,
                role = Role.RadioButton
            )
            .semantics {
                contentDescription = "$title theme: $subtitle"
                selected = isSelected
                role = Role.RadioButton
            },
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Swatch tile (56dp)
            ThemeSwatch(themeMode = themeMode)

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                )

                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp)
                )
            }

            RadioButton(
                selected = isSelected,
                onClick = onSelect
            )
        }
    }
}

@Composable
private fun ThemeSwatch(themeMode: ThemeMode) {
    val bgColor = when (themeMode) {
        ThemeMode.Dark -> Color(0xFF0B1220)
        ThemeMode.Light -> Color(0xFFF4F7FB)
        ThemeMode.System -> Color(0xFF131C2E)
    }

    val barColor = when (themeMode) {
        ThemeMode.Dark -> Color(0xFF2DD4A7)
        ThemeMode.Light -> Color(0xFF0B7F63)
        ThemeMode.System -> Color(0xFF9AA8C1)
    }

    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(10.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(barColor)
        )
    }
}

@Preview(name = "Appearance - Dark", showBackground = true)
@Composable
fun AppearanceDarkPreview() {
    AppTheme(themeMode = ThemeMode.Dark) {
        AppearanceScreen(onBack = {})
    }
}

@Preview(name = "Appearance - Light", showBackground = true)
@Composable
fun AppearanceLightPreview() {
    AppTheme(themeMode = ThemeMode.Light) {
        AppearanceScreen(onBack = {})
    }
}
