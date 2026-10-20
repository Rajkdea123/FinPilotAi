package com.example.myapplication.ui.budget

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.domain.model.Category
import com.example.myapplication.ui.components.PrimaryButton
import com.example.myapplication.ui.components.ScreenTopBar
import com.example.myapplication.ui.icons.AppIcons
import com.example.myapplication.ui.theme.AppTheme
import com.example.myapplication.ui.theme.ThemeMode
import com.example.myapplication.util.MoneyFormatter
import org.koin.compose.koinInject

@Composable
fun BudgetScreen(
    onBack: (message: String?) -> Unit,
    viewModel: BudgetViewModel = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onBack("Budget saved")
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
                title = "Monthly budget",
                onBack = { onBack(null) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Total budget card
                TotalBudgetCard(
                    totalPaise = uiState.totalPaise,
                    aiSuggestionPaise = uiState.aiSuggestionPaise,
                    onEditTotal = { viewModel.onEvent(BudgetEvent.EditTotalClicked) },
                    onApplySuggestion = { viewModel.onEvent(BudgetEvent.ApplySuggestion) }
                )

                Spacer(modifier = Modifier.height(22.dp))

                // Heading "By category"
                Text(
                    text = "By category",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Category rows
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Category.entries.forEach { category ->
                        val limitPaise = uiState.categoryLimits[category] ?: 0L
                        val spentPaise = uiState.categorySpent[category] ?: 0L

                        CategoryBudgetRow(
                            category = category,
                            limitPaise = limitPaise,
                            spentPaise = spentPaise,
                            onClick = { viewModel.onEvent(BudgetEvent.EditCategoryClicked(category)) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Unassigned / Over-total message line
                val sumLimits = uiState.categoryLimits.values.sum()
                val diffPaise = uiState.totalPaise - sumLimits

                if (diffPaise >= 0) {
                    Text(
                        text = "${MoneyFormatter.formatPaise(diffPaise)} left to assign",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
                    )
                } else {
                    Text(
                        text = "${MoneyFormatter.formatPaise(-diffPaise)} over the total",
                        color = AppTheme.colors.danger,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                if (uiState.totalError != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = uiState.totalError!!,
                        color = AppTheme.colors.danger,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Save budget button pinned at bottom
            PrimaryButton(
                text = "Save budget",
                onClick = { viewModel.onEvent(BudgetEvent.SaveClicked) },
                enabled = uiState.isValid,
                isLoading = uiState.isSaving,
                modifier = Modifier.padding(bottom = 36.dp)
            )
        }

        // Edit Total Dialog
        if (uiState.isEditingTotal) {
            EditTotalDialog(
                currentTotalPaise = uiState.totalPaise,
                onConfirm = { newTotalPaise ->
                    viewModel.onEvent(BudgetEvent.TotalChanged(newTotalPaise))
                    viewModel.onEvent(BudgetEvent.DismissEditTotal)
                },
                onDismiss = { viewModel.onEvent(BudgetEvent.DismissEditTotal) }
            )
        }

        // Edit Category Limit Dialog
        uiState.editingCategory?.let { category ->
            val currentLimitPaise = uiState.categoryLimits[category] ?: 0L
            val maxLimit = uiState.totalPaise

            EditCategoryDialog(
                category = category,
                currentLimitPaise = currentLimitPaise,
                maxLimitPaise = maxLimit,
                onConfirm = { newLimitPaise ->
                    viewModel.onEvent(BudgetEvent.CategoryLimitChanged(category, newLimitPaise))
                    viewModel.onEvent(BudgetEvent.DismissEditCategory)
                },
                onDismiss = { viewModel.onEvent(BudgetEvent.DismissEditCategory) }
            )
        }
    }
}

@Composable
private fun TotalBudgetCard(
    totalPaise: Long,
    aiSuggestionPaise: Long,
    onEditTotal: () -> Unit,
    onApplySuggestion: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Total budget",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = MoneyFormatter.formatPaise(totalPaise),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier
                    .clickable(onClick = onEditTotal)
                    .semantics { contentDescription = "Total budget: ${MoneyFormatter.formatPaise(totalPaise)}. Tap to edit." }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // AI Suggestion Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onApplySuggestion)
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = rememberVectorPainter(AppIcons.SparkleIcon),
                    contentDescription = null,
                    tint = AppTheme.colors.ai,
                    modifier = Modifier.size(18.dp)
                )

                Text(
                    text = "AI suggests ${MoneyFormatter.formatPaise(aiSuggestionPaise)} based on your last 3 months",
                    color = AppTheme.colors.ai,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

@Composable
private fun CategoryBudgetRow(
    category: Category,
    limitPaise: Long,
    spentPaise: Long,
    onClick: () -> Unit
) {
    val categoryColor = when (category) {
        Category.Food -> AppTheme.colors.warning
        Category.Shopping -> AppTheme.colors.ai
        Category.Bills -> MaterialTheme.colorScheme.primary
        Category.Transport -> AppTheme.colors.info
        Category.Entertainment -> AppTheme.colors.danger
    }

    val isOverLimit = limitPaise > 0L && spentPaise > limitPaise
    val barFillColor = if (isOverLimit) AppTheme.colors.danger else categoryColor

    val ratio = if (limitPaise > 0L) (spentPaise.toFloat() / limitPaise.toFloat()).coerceIn(0f, 1f) else 0f
    val animatedRatio by animateFloatAsState(
        targetValue = ratio,
        animationSpec = tween(400),
        label = "category_ratio_$category"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
            .semantics {
                contentDescription = "${category.name}: spent ${MoneyFormatter.formatPaise(spentPaise)} of ${MoneyFormatter.formatPaise(limitPaise)} limit."
            }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category.name,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                )
            )

            Text(
                text = "${MoneyFormatter.formatPaise(spentPaise)} / ${MoneyFormatter.formatPaise(limitPaise)}",
                color = if (isOverLimit) AppTheme.colors.danger else MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedRatio)
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(barFillColor)
            )
        }
    }
}

@Composable
private fun EditTotalDialog(
    currentTotalPaise: Long,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var textValue by remember { mutableStateOf((currentTotalPaise / 100).toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit total budget") },
        text = {
            Column {
                OutlinedTextField(
                    value = textValue,
                    onValueChange = { textValue = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Total budget (₹)") },
                    prefix = { Text("₹") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rupees = textValue.toLongOrNull() ?: 0L
                    onConfirm(rupees * 100)
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun EditCategoryDialog(
    category: Category,
    currentLimitPaise: Long,
    maxLimitPaise: Long,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var textValue by remember { mutableStateOf((currentLimitPaise / 100).toString()) }
    val maxRupees = (maxLimitPaise / 100).coerceAtLeast(1L).toFloat()
    var sliderValue by remember { mutableStateOf((currentLimitPaise / 100).toFloat().coerceIn(0f, maxRupees)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit ${category.name} limit") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = textValue,
                    onValueChange = { input ->
                        val clean = input.filter { it.isDigit() }
                        textValue = clean
                        val valFloat = clean.toFloatOrNull() ?: 0f
                        sliderValue = valFloat.coerceIn(0f, maxRupees)
                    },
                    label = { Text("Limit (₹)") },
                    prefix = { Text("₹") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Column {
                    Text(
                        text = "Adjust slider",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = sliderValue,
                        onValueChange = { newRupees ->
                            sliderValue = newRupees
                            textValue = newRupees.toInt().toString()
                        },
                        valueRange = 0f..maxRupees
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rupees = textValue.toLongOrNull() ?: sliderValue.toLong()
                    onConfirm(rupees * 100)
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(name = "Monthly Budget - Dark", showBackground = true)
@Composable
fun BudgetDarkPreview() {
    AppTheme(themeMode = ThemeMode.Dark) {
        BudgetScreen(onBack = {})
    }
}

@Preview(name = "Monthly Budget - Light", showBackground = true)
@Composable
fun BudgetLightPreview() {
    AppTheme(themeMode = ThemeMode.Light) {
        BudgetScreen(onBack = {})
    }
}
