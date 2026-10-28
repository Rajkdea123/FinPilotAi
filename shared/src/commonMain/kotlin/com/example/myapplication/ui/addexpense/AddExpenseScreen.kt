package com.example.myapplication.ui.addexpense

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.domain.model.Category
import org.koin.compose.koinInject
import com.example.myapplication.ui.icons.AppIcons
import com.example.myapplication.ui.theme.AppTheme
import com.example.myapplication.ui.theme.ThemeMode
import com.example.myapplication.util.MoneyFormatter
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

@Composable
fun AddExpenseScreen(
    onBack: () -> Unit,
    onExpenseAdded: () -> Unit,
    expenseId: String? = null,
    viewModel: AddExpenseViewModel = viewModel { AddExpenseViewModel(koinInject(), koinInject(), koinInject(), expenseId) }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val showDatePicker = remember { mutableStateOf(false) }
    val showCategorySheet = remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSaving) {
        if (!uiState.isSaving && uiState.amountError == null && uiState.categoryError == null && uiState.amount.isNotBlank() && uiState.category != null) {
            // Check if we just finished saving by checking if we were saving before
            // This is a simple check - in production you'd use a more explicit success state
        }
    }

    // Listen for save completion
    LaunchedEffect(uiState.isSaving) {
        if (!uiState.isSaving && uiState.amount.isNotBlank() && uiState.category != null) {
            onExpenseAdded()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.onEvent(AddExpenseEvent.BackClicked); onBack() },
                modifier = Modifier
                    .size(44.dp)
                    .semantics { contentDescription = "Back" }
            ) {
                Icon(
                    painter = rememberVectorPainter(AppIcons.SyncIcon), // Using sync as back icon for now
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = if (uiState.isEditMode) "Edit expense" else "Add expense",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.size(44.dp)) // Spacer to balance layout
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // AI quick-entry card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = rememberVectorPainter(AppIcons.SparkleIcon),
                            contentDescription = "AI",
                            tint = AppTheme.colors.ai,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Describe it in words",
                            color = AppTheme.colors.ai,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.quickText,
                        onValueChange = { viewModel.onEvent(AddExpenseEvent.QuickTextChanged(it)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        placeholder = { Text("Paid ₹450 at Zomato") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background,
                            focusedBorderColor = AppTheme.colors.ai,
                            unfocusedBorderColor = AppTheme.colors.ai,
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        textStyle = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            // AI understood card
            if (uiState.parseState != com.example.myapplication.ui.addexpense.ParseState.Idle) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "AI understood",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        when (uiState.parseState) {
                            is com.example.myapplication.ui.addexpense.ParseState.Parsing -> {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    repeat(3) {
                                        Surface(
                                            modifier = Modifier
                                                .width(60.dp)
                                                .height(24.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant
                                        ) {}
                                    }
                                }
                            }
                            is com.example.myapplication.ui.addexpense.ParseState.Success -> {
                                val parsed = (uiState.parseState as com.example.myapplication.ui.addexpense.ParseState.Success).parsed
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    parsed.amountPaise?.let {
                                        Chip(text = MoneyFormatter.formatPaise(it), color = MaterialTheme.colorScheme.onBackground)
                                    }
                                    parsed.category?.let {
                                        val color = when (it) {
                                            Category.Food -> AppTheme.colors.warning
                                            Category.Transport -> AppTheme.colors.info
                                            Category.Bills -> MaterialTheme.colorScheme.primary
                                            Category.Shopping -> AppTheme.colors.ai
                                            Category.Entertainment -> AppTheme.colors.danger
                                        }
                                        Chip(text = it.name, color = color)
                                    }
                                    parsed.merchant?.let {
                                        Chip(text = it, color = MaterialTheme.colorScheme.onBackground)
                                    }
                                }
                            }
                            is com.example.myapplication.ui.addexpense.ParseState.Error -> {
                                Text(
                                    text = "Couldn't read that. Fill it in below.",
                                    color = AppTheme.colors.danger,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            else -> {}
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Manual fields
            AmountField(
                value = uiState.amount,
                onValueChange = { viewModel.onEvent(AddExpenseEvent.AmountChanged(it)) },
                error = uiState.amountError
            )

            CategoryField(
                selected = uiState.category,
                onClick = { showCategorySheet.value = true },
                error = uiState.categoryError
            )

            PaymentMethodField(
                selected = uiState.paymentMethod,
                onSelected = { viewModel.onEvent(AddExpenseEvent.PaymentMethodSelected(it)) }
            )

            DateField(
                date = uiState.date,
                onClick = { showDatePicker.value = true }
            )

            DescriptionField(
                value = uiState.description,
                onValueChange = { viewModel.onEvent(AddExpenseEvent.DescriptionChanged(it)) }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Save button
            Button(
                onClick = { viewModel.onEvent(AddExpenseEvent.SaveClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                enabled = !uiState.isSaving
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (uiState.isEditMode) "Save changes" else "Save expense",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }

    // Date picker dialog
    if (showDatePicker.value) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.date
                .atTime(12, 0)
                .toInstant(TimeZone.currentSystemDefault())
                .toEpochMilliseconds()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker.value = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            val date = kotlinx.datetime.Instant.fromEpochMilliseconds(it)
                                .toLocalDateTime(TimeZone.currentSystemDefault()).date
                            viewModel.onEvent(AddExpenseEvent.DateSelected(date))
                        }
                        showDatePicker.value = false
                    }
                ) {
                    Text("OK")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Category bottom sheet (simplified as dialog for now)
    if (showCategorySheet.value) {
        CategoryDialog(
            selected = uiState.category,
            onSelected = { 
                viewModel.onEvent(AddExpenseEvent.CategorySelected(it))
                showCategorySheet.value = false
            },
            onDismiss = { showCategorySheet.value = false }
        )
    }
}

@Composable
private fun AmountField(
    value: String,
    onValueChange: (String) -> Unit,
    error: String?
) {
    Column {
        Text(
            text = "Amount",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            prefix = { Text("₹") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            isError = error != null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                errorBorderColor = AppTheme.colors.danger,
                focusedBorderColor = if (error != null) AppTheme.colors.danger else MaterialTheme.colorScheme.outline,
                unfocusedBorderColor = if (error != null) AppTheme.colors.danger else MaterialTheme.colorScheme.outline,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            textStyle = MaterialTheme.typography.bodyLarge
        )

        if (error != null) {
            Text(
                text = error,
                color = AppTheme.colors.danger,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun CategoryField(
    selected: Category?,
    onClick: () -> Unit,
    error: String?
) {
    Column {
        Text(
            text = "Category",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .clickable(onClick = onClick)
                .then(
                    if (error != null) {
                        Modifier.border(1.dp, AppTheme.colors.danger, RoundedCornerShape(14.dp))
                    } else {
                        Modifier.border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                    }
                ),
            color = MaterialTheme.colorScheme.surface
        ) {
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = selected?.name ?: "Select category",
                    color = if (selected != null) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        if (error != null) {
            Text(
                text = error,
                color = AppTheme.colors.danger,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun PaymentMethodField(
    selected: String,
    onSelected: (String) -> Unit
) {
    Column {
        Text(
            text = "Payment method",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val methods = listOf("UPI", "Card", "Cash", "Bank")
            methods.forEach { method ->
                val isSelected = selected == method
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onSelected(method) },
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null
                ) {
                    Text(
                        text = method,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                }
            }
        }
    }
}

@Composable
private fun DateField(
    date: kotlinx.datetime.LocalDate,
    onClick: () -> Unit
) {
    Column {
        Text(
            text = "Date",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .clickable(onClick = onClick)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                val displayDate = if (date == today) "Today" else date.toString()
                Text(
                    text = displayDate,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun DescriptionField(
    value: String,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(
            text = "Description (optional)",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.outline,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            ),
            textStyle = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun Chip(text: String, color: androidx.compose.ui.graphics.Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = color,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
private fun CategoryDialog(
    selected: Category?,
    onSelected: (Category) -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
            Text(
                text = "Select category",
                style = MaterialTheme.typography.headlineSmall
            )
            
            val categories = listOf(
                Category.Food to AppTheme.colors.warning,
                Category.Transport to AppTheme.colors.info,
                Category.Bills to MaterialTheme.colorScheme.primary,
                Category.Shopping to AppTheme.colors.ai,
                Category.Entertainment to AppTheme.colors.danger
            )
            
            categories.forEach { (category, color) ->
                val isSelected = selected == category
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelected(category) }
                        .then(
                            if (isSelected) {
                                Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                            } else {
                                Modifier
                            }
                        ),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = rememberVectorPainter(AppIcons.DeviceIcon),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = category.name,
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
}

@Preview(name = "Idle - Dark", showBackground = true)
@Composable
fun AddExpenseScreenDarkPreview() {
    AppTheme(themeMode = ThemeMode.Dark) {
        AddExpenseScreen(onBack = {}, onExpenseAdded = {})
    }
}

@Preview(name = "Idle - Light", showBackground = true)
@Composable
fun AddExpenseScreenLightPreview() {
    AppTheme(themeMode = ThemeMode.Light) {
        AddExpenseScreen(onBack = {}, onExpenseAdded = {})
    }
}
