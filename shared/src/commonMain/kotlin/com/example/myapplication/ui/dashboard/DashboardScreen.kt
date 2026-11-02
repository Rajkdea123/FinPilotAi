package com.example.myapplication.ui.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.domain.model.Category
import com.example.myapplication.domain.model.Expense
import com.example.myapplication.domain.model.MonthSummary
import com.example.myapplication.ui.components.ExpenseRow
import com.example.myapplication.ui.components.SyncStatusIndicator
import com.example.myapplication.ui.icons.AppIcons
import com.example.myapplication.ui.theme.AppTheme
import com.example.myapplication.ui.theme.ThemeMode
import com.example.myapplication.util.MoneyFormatter
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject

@Composable
fun DashboardScreen(
    onNavigateToInsights: () -> Unit = {},
    onNavigateToTransactions: () -> Unit = {},
    viewModel: DashboardViewModel = koinInject(),
    syncManager: com.example.myapplication.sync.SyncManager = koinInject()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val syncStatus by syncManager.syncStatus.collectAsStateWithLifecycle()

    DashboardContent(
        uiState = uiState,
        syncStatus = syncStatus,
        onNavigateToInsights = onNavigateToInsights,
        onNavigateToTransactions = onNavigateToTransactions,
        onRefresh = { viewModel.onEvent(DashboardEvent.Refresh) }
    )
}

@Composable
fun DashboardContent(
    uiState: DashboardUiState,
    syncStatus: com.example.myapplication.sync.SyncStatus,
    onNavigateToInsights: () -> Unit = {},
    onNavigateToTransactions: () -> Unit = {},
    onRefresh: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 20.dp)
        ) {
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
                        onRetry = onRefresh
                    )
                }
            } else {
                item {
                    BalanceCard(
                        summary = uiState.summary ?: MonthSummary(0, 0, 500000L),
                        onRefresh = onRefresh
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(14.dp))
                }

                item {
                    AiInsightCard(
                        insight = uiState.insight.ifBlank { "No insight available" },
                        onClick = onNavigateToInsights
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }

                item {
                    RecentTransactionsHeader(onSeeAll = onNavigateToTransactions)
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        uiState.recentExpenses.take(5).forEach { expense ->
                            ExpenseRow(expense = expense)
                        }
                    }
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
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = greeting,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$monthName overview",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

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
    summary: MonthSummary,
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

    val percentage = if (summary.budgetPaise > 0) {
        (summary.totalPaise.toFloat() / summary.budgetPaise.toFloat() * 100).coerceIn(0f, 100f)
    } else 0f

    val deltaPercent = if (summary.previousMonthTotalPaise > 0) {
        ((summary.totalPaise - summary.previousMonthTotalPaise).toFloat() / summary.previousMonthTotalPaise.toFloat() * 100).toInt()
    } else 0

    val (chipText, chipColor) = when {
        deltaPercent > 0 -> Pair("▲ $deltaPercent% vs last month", AppTheme.colors.warning)
        deltaPercent < 0 -> Pair("▼ ${kotlin.math.abs(deltaPercent)}% vs last month", MaterialTheme.colorScheme.primary)
        else -> Pair("No change vs last month", MaterialTheme.colorScheme.onSurfaceVariant)
    }

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
                    text = chipText,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = chipColor,
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

private val sampleExpenses = listOf(
    Expense("1", 45000, Category.Food, "Zomato", "UPI", Instant.fromEpochMilliseconds(1700000000000L)),
    Expense("2", 30000, Category.Transport, "Metro card", "Card", Instant.fromEpochMilliseconds(1699900000000L)),
    Expense("3", 200000, Category.Bills, "Rent", "Bank", Instant.fromEpochMilliseconds(1699800000000L)),
    Expense("4", 150000, Category.Shopping, "Amazon", "Card", Instant.fromEpochMilliseconds(1699700000000L)),
    Expense("5", 80000, Category.Entertainment, "Cinema", "Cash", Instant.fromEpochMilliseconds(1699600000000L)),
    Expense("6", 50000, Category.Food, "Swiggy", "UPI", Instant.fromEpochMilliseconds(1699500000000L)),
    Expense("7", 120000, Category.Bills, "Electricity", "UPI", Instant.fromEpochMilliseconds(1699400000000L)),
    Expense("8", 90000, Category.Shopping, "Myntra", "Card", Instant.fromEpochMilliseconds(1699300000000L))
)

@Preview(name = "0 Items - Dark", showBackground = true)
@Composable
fun Dashboard0ItemsDarkPreview() {
    AppTheme(themeMode = ThemeMode.Dark) {
        DashboardContent(
            uiState = DashboardUiState(
                summary = MonthSummary(0, 0, 500000L),
                recentExpenses = emptyList(),
                insight = "Food spending is 28% above your monthly average.",
                isLoading = false
            ),
            syncStatus = com.example.myapplication.sync.SyncStatus(isOnline = true, isSyncing = false, pendingChanges = 0)
        )
    }
}

@Preview(name = "3 Items - Light", showBackground = true)
@Composable
fun Dashboard3ItemsLightPreview() {
    AppTheme(themeMode = ThemeMode.Light) {
        DashboardContent(
            uiState = DashboardUiState(
                summary = MonthSummary(4250000, 3900000, 5000000L),
                recentExpenses = sampleExpenses.take(3),
                insight = "Food spending is 28% above your monthly average.",
                isLoading = false
            ),
            syncStatus = com.example.myapplication.sync.SyncStatus(isOnline = true, isSyncing = false, pendingChanges = 0)
        )
    }
}

@Preview(name = "8 Items (Shows 5) - Dark", showBackground = true)
@Composable
fun Dashboard8ItemsShows5DarkPreview() {
    AppTheme(themeMode = ThemeMode.Dark) {
        DashboardContent(
            uiState = DashboardUiState(
                summary = MonthSummary(4250000, 3900000, 5000000L),
                recentExpenses = sampleExpenses, // 8 items passed, composable renders max 5
                insight = "Food spending is 28% above your monthly average.",
                isLoading = false
            ),
            syncStatus = com.example.myapplication.sync.SyncStatus(isOnline = true, isSyncing = false, pendingChanges = 0)
        )
    }
}
