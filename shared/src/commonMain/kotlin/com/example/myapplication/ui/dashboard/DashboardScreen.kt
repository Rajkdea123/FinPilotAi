package com.example.myapplication.ui.dashboard

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
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
import com.example.myapplication.ui.components.ExpenseRow
import com.example.myapplication.ui.components.SyncStatusIndicator
import com.example.myapplication.ui.icons.AppIcons
import com.example.myapplication.ui.theme.AppTheme
import org.koin.compose.koinInject
import com.example.myapplication.ui.theme.ThemeMode
import com.example.myapplication.util.MoneyFormatter
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToInsights: () -> Unit = {},
    onNavigateToTransactions: () -> Unit = {},
    viewModel: DashboardViewModel = viewModel(),
    syncManager: com.example.myapplication.sync.SyncManager = koinInject()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val syncStatus by syncManager.syncStatus.collectAsStateWithLifecycle()
    var isRefreshing by remember { mutableStateOf(false) }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            viewModel.onEvent(DashboardEvent.Refresh)
            isRefreshing = false
        },
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 100.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Header(onNavigateToInsights = onNavigateToInsights, syncStatus = syncStatus)
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            val errorMsg = uiState.error
            if (uiState.isLoading) {
                item {
                    LoadingSkeleton()
                }
            } else if (errorMsg != null) {
                item {
                    ErrorCard(
                        error = errorMsg,
                        onRetry = { viewModel.onEvent(DashboardEvent.Refresh) }
                    )
                }
            } else {
                item {
                    BalanceCard(
                        summary = uiState.summary!!,
                        onRefresh = { viewModel.onEvent(DashboardEvent.Refresh) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(14.dp))
                }

                item {
                    AiInsightCard(
                        insight = uiState.insight,
                        onClick = onNavigateToInsights
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }

                item {
                    RecentTransactionsHeader(onSeeAll = onNavigateToTransactions)
                }

                items(uiState.recentExpenses) { expense ->
                    ExpenseRow(expense = expense)
                }
            }
        }
    }
}

@Composable
private fun Header(
    onNavigateToInsights: () -> Unit = {},
    syncStatus: com.example.myapplication.sync.SyncStatus
) {
    val currentTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val hour = currentTime.hour
    val greeting = when (hour) {
        in 0..11 -> "Good morning"
        in 12..17 -> "Good afternoon"
        else -> "Good evening"
    }
    val monthName = currentTime.month.name.lowercase().replaceFirstChar { it.uppercase() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = greeting,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
            )
            Text(
                text = "$monthName overview",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SyncStatusIndicator(
                status = com.example.myapplication.ui.components.SyncStatus(
                    isSyncing = syncStatus.isSyncing,
                    isOnline = syncStatus.isOnline,
                    pendingChanges = syncStatus.pendingChanges
                )
            )
            IconButton(
                onClick = onNavigateToInsights,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .semantics { contentDescription = "Notifications" }
            ) {
                Icon(
                    painter = rememberVectorPainter(AppIcons.BellIcon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun BalanceCard(
    summary: com.example.myapplication.domain.model.MonthSummary,
    onRefresh: () -> Unit
) {
    var animateProgress by remember { mutableFloatStateOf(0f) }
    val progress by animateFloatAsState(
        targetValue = animateProgress,
        animationSpec = androidx.compose.animation.core.tween(1000),
        label = "progress"
    )

    LaunchedEffect(Unit) {
        animateProgress = 1f
    }

    val percentage = (summary.totalPaise.toFloat() / summary.budgetPaise.toFloat() * 100).coerceIn(0f, 100f)
    val previousPercentage = if (summary.previousMonthTotalPaise > 0) {
        ((summary.totalPaise - summary.previousMonthTotalPaise).toFloat() / summary.previousMonthTotalPaise.toFloat() * 100).toInt()
    } else 0
    val isIncrease = summary.totalPaise > summary.previousMonthTotalPaise

    val progressColor = when {
        percentage > 100f -> AppTheme.colors.danger
        percentage > 85f -> AppTheme.colors.warning
        else -> MaterialTheme.colorScheme.primary
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Spent this month",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = MoneyFormatter.formatPaise(summary.totalPaise),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.semantics {
                    contentDescription = "Spent this month: ${MoneyFormatter.formatPaise(summary.totalPaise).replace("₹", "")} rupees"
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = "${if (isIncrease) "▲" else "▼"} ${kotlin.math.abs(previousPercentage)}% vs last month",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = if (isIncrease) AppTheme.colors.warning else MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Budget used",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
                )
                Text(
                    text = "${percentage.toInt()}%",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress * (percentage / 100f))
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(progressColor)
                )
            }
        }
    }
}

@Composable
private fun AiInsightCard(insight: String, onClick: () -> Unit = {}) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
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
                    contentDescription = "AI insight",
                    tint = AppTheme.colors.ai,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "AI insight",
                    color = AppTheme.colors.ai,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = insight,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 15.sp,
                    lineHeight = (15 * 1.45).sp
                )
            )
        }
    }
}

@Composable
private fun RecentTransactionsHeader(onSeeAll: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Recent",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        )
        TextButton(onClick = onSeeAll) {
            Text(
                text = "See all",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
            )
        }
    }
}

@Composable
private fun ExpenseRow(expense: com.example.myapplication.domain.model.Expense) {
    val categoryColor = when (expense.category) {
        Category.Food -> AppTheme.colors.warning
        Category.Transport -> AppTheme.colors.info
        Category.Bills -> MaterialTheme.colorScheme.primary
        Category.Shopping -> AppTheme.colors.ai
        Category.Entertainment -> AppTheme.colors.danger
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = rememberVectorPainter(AppIcons.DeviceIcon),
                        contentDescription = "${expense.category} icon",
                        tint = categoryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column {
                Text(
                    text = expense.merchant,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                )
                Text(
                    text = "${expense.category.name} · ${expense.paymentMethod}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp)
                )
            }
        }

        Text(
            text = MoneyFormatter.formatPaise(expense.amountPaise),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
private fun LoadingSkeleton() {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {}
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {}
        repeat(3) {
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
private fun ErrorCard(error: String, onRetry: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = error,
                color = AppTheme.colors.danger,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Preview(name = "Loaded - Dark", showBackground = true)
@Composable
fun DashboardScreenDarkPreview() {
    AppTheme(themeMode = ThemeMode.Dark) {
        DashboardScreen()
    }
}

@Preview(name = "Loaded - Light", showBackground = true)
@Composable
fun DashboardScreenLightPreview() {
    AppTheme(themeMode = ThemeMode.Light) {
        DashboardScreen()
    }
}

@Preview(name = "Loading - Dark", showBackground = true)
@Composable
fun DashboardScreenLoadingDarkPreview() {
    AppTheme(themeMode = ThemeMode.Dark) {
        // Preview would need a loading state - simplified for now
        DashboardScreen()
    }
}
