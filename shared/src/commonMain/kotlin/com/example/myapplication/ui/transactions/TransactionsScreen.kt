package com.example.myapplication.ui.transactions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PopupMenuPosition
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.domain.model.Category
import com.example.myapplication.domain.model.DayGroup
import com.example.myapplication.domain.model.Expense
import com.example.myapplication.domain.model.SortOrder
import com.example.myapplication.ui.components.ExpenseRow
import com.example.myapplication.ui.components.ScreenTopBar
import com.example.myapplication.ui.icons.AppIcons
import com.example.myapplication.ui.theme.AppTheme
import com.example.myapplication.ui.theme.ThemeMode
import com.example.myapplication.util.MoneyFormatter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TransactionsScreen(
    onBack: () -> Unit = {},
    onExpenseClick: (String) -> Unit = {},
    viewModel: TransactionsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    var showSortMenu by remember { mutableStateOf(false) }
    var showCategorySheet by remember { mutableStateOf(false) }
    var showPaymentSheet by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.lastDeleted) {
        if (uiState.lastDeleted != null) {
            coroutineScope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = "Expense deleted",
                    actionLabel = "Undo"
                )
                if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                    viewModel.onEvent(TransactionsEvent.UndoDelete)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            ScreenTopBar(
                title = "Transactions",
                onBack = onBack
            ) {
                Box {
                    IconButton(
                        onClick = { showSortMenu = true },
                        modifier = Modifier
                            .size(44.dp)
                            .semantics { contentDescription = "Sort transactions" }
                    ) {
                        Icon(
                            painter = rememberVectorPainter(AppIcons.SyncIcon),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        SortOrder.values().forEach { sortOrder ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = when (sortOrder) {
                                            SortOrder.NewestFirst -> "Newest first"
                                            SortOrder.OldestFirst -> "Oldest first"
                                            SortOrder.HighestAmount -> "Highest amount"
                                        },
                                        color = if (uiState.sort == sortOrder) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onBackground
                                        }
                                    )
                                },
                                onClick = {
                                    viewModel.onEvent(TransactionsEvent.SortSelected(sortOrder))
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SearchField(
                query = uiState.filter.query,
                onQueryChange = { viewModel.onEvent(TransactionsEvent.QueryChanged(it)) },
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            FilterChipsRow(
                activeCategories = uiState.filter.categories,
                activePaymentMethods = uiState.filter.paymentMethods,
                hasDateRange = uiState.filter.dateRange != null,
                onCategoryClick = { showCategorySheet = true },
                onPaymentClick = { showPaymentSheet = true },
                onDateClick = { showDatePicker = true },
                onClearFilters = { viewModel.onEvent(TransactionsEvent.ClearFilters) },
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            SummaryLine(
                count = uiState.resultCount,
                totalPaise = uiState.totalPaise,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            when {
                uiState.isLoading -> {
                    LoadingState()
                }
                uiState.error != null -> {
                    ErrorState(
                        error = uiState.error,
                        onRetry = { viewModel.onEvent(TransactionsEvent.Retry) }
                    )
                }
                uiState.groups.isEmpty() -> {
                    if (uiState.filter.query.isBlank() && 
                        uiState.filter.categories.isEmpty() && 
                        uiState.filter.paymentMethods.isEmpty() &&
                        uiState.filter.dateRange == null) {
                        EmptyState(onAddExpense = {})
                    } else {
                        NoMatchesState(onClearFilters = { viewModel.onEvent(TransactionsEvent.ClearFilters) })
                    }
                }
                else -> {
                    TransactionsList(
                        groups = uiState.groups,
                        onExpenseClick = { id -> viewModel.onEvent(TransactionsEvent.ExpenseClicked(id)) },
                        onDeleteExpense = { id -> viewModel.onEvent(TransactionsEvent.DeleteExpense(id)) }
                    )
                }
            }
        }
    }

    // Category bottom sheet
    if (showCategorySheet) {
        val categorySheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { showCategorySheet = false },
            sheetState = categorySheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Select categories",
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
                    val isSelected = category in uiState.filter.categories
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.onEvent(TransactionsEvent.CategorySelected(category))
                            },
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (isSelected) 
                            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                        else null
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
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
                            Spacer(modifier = Modifier.weight(1f))
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Deselect",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    // Payment method bottom sheet
    if (showPaymentSheet) {
        val paymentSheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { showPaymentSheet = false },
            sheetState = paymentSheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Select payment methods",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                val paymentMethods = listOf("UPI", "Card", "Cash", "Bank")
                
                paymentMethods.forEach { method ->
                    val isSelected = method in uiState.filter.paymentMethods
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.onEvent(TransactionsEvent.PaymentMethodSelected(method))
                            },
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (isSelected) 
                            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                        else null
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = method,
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Deselect",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    // Date range picker
    if (showDatePicker) {
        val datePickerState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { showDatePicker = false },
            sheetState = datePickerState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Date range",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                val thisMonth = kotlinx.datetime.LocalDate(today.year, today.month, 1)
                val lastMonth = thisMonth.minus(kotlin.datetime.DatePeriod(months = 1))
                val last3Months = thisMonth.minus(kotlin.datetime.DatePeriod(months = 3))
                
                val dateRanges = listOf(
                    "This month" to DateRange(thisMonth, today),
                    "Last month" to DateRange(lastMonth, thisMonth.minus(kotlin.datetime.DatePeriod(days = 1))),
                    "Last 3 months" to DateRange(last3Months, today)
                )
                
                dateRanges.forEach { (label, range) ->
                    val isSelected = uiState.filter.dateRange == range
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.onEvent(TransactionsEvent.DateRangeSelected(range))
                                showDatePicker = false
                            },
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (isSelected) 
                            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                        else null
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
                
                if (uiState.filter.dateRange != null) {
                    TextButton(
                        onClick = {
                            viewModel.onEvent(TransactionsEvent.DateRangeSelected(null))
                            showDatePicker = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Clear date range")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        placeholder = {
            Text(
                text = "Search merchant or note",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        leadingIcon = {
            Icon(
                painter = rememberVectorPainter(AppIcons.SearchIcon),
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = if (query.isNotBlank()) {
            {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else null,
        shape = RoundedCornerShape(26.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        ),
        singleLine = true
    )
}

@Composable
private fun FilterChipsRow(
    activeCategories: Set<Category>,
    activePaymentMethods: Set<String>,
    hasDateRange: Boolean,
    onCategoryClick: () -> Unit,
    onPaymentClick: () -> Unit,
    onDateClick: () -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasActiveFilters = activeCategories.isNotEmpty() || 
                          activePaymentMethods.isNotEmpty() || 
                          hasDateRange

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            label = "Category",
            count = activeCategories.size,
            isActive = activeCategories.isNotEmpty(),
            onClick = onCategoryClick
        )

        FilterChip(
            label = "Payment",
            count = activePaymentMethods.size,
            isActive = activePaymentMethods.isNotEmpty(),
            onClick = onPaymentClick
        )

        FilterChip(
            label = "Date",
            count = if (hasDateRange) 1 else 0,
            isActive = hasDateRange,
            onClick = onDateClick
        )

        if (hasActiveFilters) {
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(
                onClick = onClearFilters,
                modifier = Modifier.semantics { contentDescription = "Clear filters" }
            ) {
                Text(
                    text = "Clear",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    count: Int,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = if (isActive) "$label filter active, $count selected" else "$label filter"
            },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
    ) {
        Text(
            text = if (count > 0) "$label · $count" else label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
        )
    }
}

@Composable
private fun SummaryLine(
    count: Int,
    totalPaise: Long,
    modifier: Modifier = Modifier
) {
    Text(
        text = "$count transactions · ${MoneyFormatter.formatPaise(totalPaise)}",
        modifier = modifier,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
    )
}

@Composable
private fun TransactionsList(
    groups: List<DayGroup>,
    onExpenseClick: (String) -> Unit,
    onDeleteExpense: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 20.dp)
    ) {
        groups.forEach { group ->
            stickyHeader(key = group.date.toString()) {
                DayHeader(
                    label = group.label,
                    totalPaise = group.totalPaise
                )
            }

            items(
                items = group.items,
                key = { it.id }
            ) { expense ->
                SwipeableExpenseRow(
                    expense = expense,
                    onClick = { onExpenseClick(expense.id) },
                    onDelete = { onDeleteExpense(expense.id) }
                )
            }
        }
    }
}

@Composable
private fun DayHeader(
    label: String,
    totalPaise: Long
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = MoneyFormatter.formatPaise(totalPaise),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
            )
        }
    }
}

@Composable
private fun SwipeableExpenseRow(
    expense: Expense,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var isDeleted by remember { mutableStateOf(false) }
    var swipeOffset by remember { mutableStateOf(0f) }
    var showContextMenu by remember { mutableStateOf(false) }
    val maxSwipeWidth = 200f

    AnimatedVisibility(
        visible = !isDeleted,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp)
        ) {
            // Delete background (danger color with trash icon)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(AppTheme.colors.danger)
                    .align(Alignment.CenterEnd),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    painter = rememberVectorPainter(AppIcons.TrashIcon),
                    contentDescription = "Delete",
                    tint = AppTheme.colors.onDanger,
                    modifier = Modifier
                        .padding(end = 24.dp)
                        .size(24.dp)
                )
            }

            // Swipeable expense row
            Surface(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { x = swipeOffset.toInt() }
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (swipeOffset < -maxSwipeWidth * 0.4f) {
                                    // Swipe past 40% threshold - trigger delete
                                    isDeleted = true
                                    onDelete()
                                } else {
                                    // Snap back
                                    swipeOffset = 0f
                                }
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                val newOffset = (swipeOffset + dragAmount).coerceIn(-maxSwipeWidth, 0f)
                                swipeOffset = newOffset
                            }
                        )
                    }
                    .combinedClickable(
                        onClick = onClick,
                        onLongClick = { showContextMenu = true }
                    )
                    .clip(RoundedCornerShape(20.dp))
                    .semantics {
                        contentDescription = "${expense.merchant}, ${expense.category.name}, ${MoneyFormatter.formatPaise(expense.amountPaise).replace("₹", "")} rupees"
                    },
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                ExpenseRow(expense = expense)
            }

            // Context menu for accessibility (long-press alternative)
            DropdownMenu(
                expanded = showContextMenu,
                onDismissRequest = { showContextMenu = false },
                modifier = Modifier
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            "Delete",
                            color = AppTheme.colors.danger
                        )
                    },
                    onClick = {
                        showContextMenu = false
                        isDeleted = true
                        onDelete()
                    },
                    leadingIcon = {
                        Icon(
                            painter = rememberVectorPainter(AppIcons.TrashIcon),
                            contentDescription = null,
                            tint = AppTheme.colors.danger,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(5) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {}
        }
    }
}

@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = error,
            color = AppTheme.colors.danger,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
private fun EmptyState(onAddExpense: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = rememberVectorPainter(AppIcons.SparkleIcon),
            contentDescription = null,
            tint = AppTheme.colors.ai,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No expenses yet",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = Modifier.height(24.dp))
        TextButton(onClick = onAddExpense) {
            Text("Add expense")
        }
    }
}

@Composable
private fun NoMatchesState(onClearFilters: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = rememberVectorPainter(AppIcons.SearchIcon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No matches",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = Modifier.height(24.dp))
        TextButton(onClick = onClearFilters) {
            Text("Clear filters")
        }
    }
}

@Preview(name = "Default - Dark", showBackground = true)
@Composable
fun TransactionsScreenDarkPreview() {
    AppTheme(themeMode = ThemeMode.Dark) {
        TransactionsScreen()
    }
}

@Preview(name = "Default - Light", showBackground = true)
@Composable
fun TransactionsScreenLightPreview() {
    AppTheme(themeMode = ThemeMode.Light) {
        TransactionsScreen()
    }
}

@Preview(name = "Loading - Dark", showBackground = true)
@Composable
fun TransactionsScreenLoadingDarkPreview() {
    AppTheme(themeMode = ThemeMode.Dark) {
        TransactionsScreen()
    }
}

@Preview(name = "Empty - Light", showBackground = true)
@Composable
fun TransactionsScreenEmptyLightPreview() {
    AppTheme(themeMode = ThemeMode.Light) {
        TransactionsScreen()
    }
}
