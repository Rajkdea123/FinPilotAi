package com.example.myapplication.ui.insights

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.domain.model.InsightType
import com.example.myapplication.domain.model.SubscriptionStatus
import com.example.myapplication.ui.icons.AppIcons
import com.example.myapplication.ui.theme.AppTheme
import com.example.myapplication.ui.theme.ThemeMode
import com.example.myapplication.util.MoneyFormatter

@Composable
fun InsightsScreen(
    viewModel: InsightsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    InsightsContent(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}

@Composable
fun InsightsContent(
    uiState: InsightsUiState,
    onEvent: (InsightsEvent) -> Unit
) {
    var showSnackbar by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.lastDismissed) {
        if (uiState.lastDismissed != null) {
            showSnackbar = true
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
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 1. Header (48dp high)
            Header()

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                LoadingShimmer()
            } else if (uiState.error != null) {
                ErrorCard(
                    error = uiState.error,
                    onRetry = { onEvent(InsightsEvent.Retry) }
                )
            } else {
                // 2. Insight cards
                if (uiState.insights.isEmpty()) {
                    EmptyInsightsState()
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.insights,
                            key = { it.id }
                        ) { insight ->
                            AnimatedVisibility(
                                visible = true,
                                exit = fadeOut(tween(250)) + shrinkVertically(tween(250))
                            ) {
                                InsightCard(
                                    insight = insight,
                                    onDismiss = { onEvent(InsightsEvent.DismissClicked(insight.id)) },
                                    onReview = { onEvent(InsightsEvent.ReviewClicked(insight.id)) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 3. Subscriptions section
                SubscriptionsSection(
                    subscriptions = uiState.subscriptions,
                    monthlyTotal = uiState.monthlySubscriptionTotalPaise,
                    onReview = { subscriptionId ->
                        // Find subscription and set as review target
                        val subscription = uiState.subscriptions.find { it.id == subscriptionId }
                        if (subscription != null) {
                            onEvent(InsightsEvent.MarkCancelled(subscriptionId))
                        }
                    }
                )

                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // Snackbar for undo dismiss
        if (showSnackbar && uiState.lastDismissed != null) {
            Snackbar(
                message = "Insight dismissed",
                onUndo = {
                    onEvent(InsightsEvent.UndoDismiss)
                    showSnackbar = false
                },
                onDismiss = { showSnackbar = false }
            )
        }

        // Review modal bottom sheet
        uiState.reviewTarget?.let { subscription ->
            ReviewBottomSheet(
                subscription = subscription,
                onMarkCancelled = { onEvent(InsightsEvent.MarkCancelled(subscription.id)) },
                onKeepIt = { onEvent(InsightsEvent.ReviewClosed) }
            )
        }
    }
}

@Composable
private fun Header() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { /* System back button handled by navigation */ }
                .semantics { contentDescription = "Back" },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = rememberVectorPainter(AppIcons.SyncIcon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(24.dp)
            )
        }

        Text(
            text = "Insights",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.size(44.dp))
    }
}

@Composable
private fun InsightCard(
    insight: com.example.myapplication.domain.model.Insight,
    onDismiss: () -> Unit,
    onReview: () -> Unit
) {
    val backgroundColor = when (insight.type) {
        InsightType.UnusedSubscription -> MaterialTheme.colorScheme.surface
        InsightType.SpendingAlert -> MaterialTheme.colorScheme.surfaceVariant
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            when (insight.type) {
                InsightType.UnusedSubscription -> {
                    Text(
                        text = insight.title,
                        color = AppTheme.colors.danger,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = insight.message,
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 15.sp,
                            lineHeight = (15 * 1.45).sp
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onReview,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppTheme.colors.danger,
                                contentColor = AppTheme.colors.onDanger
                            )
                        ) {
                            Text(
                                text = "Review",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onBackground
                            )
                        ) {
                            Text(
                                text = "Dismiss",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                InsightType.SpendingAlert -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = insight.title,
                                color = AppTheme.colors.ai,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = insight.message,
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 15.sp,
                                    lineHeight = (15 * 1.45).sp
                                )
                            )
                        }

                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .semantics { contentDescription = "Dismiss insight" }
                        ) {
                            Text(
                                text = "Dismiss",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape,
    border: BorderStroke,
    colors: androidx.compose.material3.ButtonColors = ButtonDefaults.outlinedButtonColors(),
    content: @Composable () -> Unit
) {
    androidx.compose.material3.OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = shape,
        border = border,
        colors = colors
    ) {
        content()
    }
}

@Composable
private fun SubscriptionsSection(
    subscriptions: List<com.example.myapplication.domain.model.Subscription>,
    monthlyTotal: Long,
    onReview: (String) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Subscriptions",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Text(
                text = "${MoneyFormatter.formatPaise(monthlyTotal)} / month",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        subscriptions.forEach { subscription ->
            SubscriptionRow(
                subscription = subscription,
                onReview = { onReview(subscription.id) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SubscriptionRow(
    subscription: com.example.myapplication.domain.model.Subscription,
    onReview: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = subscription.name,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
            )

            val subtitle = when {
                subscription.status == SubscriptionStatus.Cancelled -> "Cancelled"
                subscription.lastUsedDaysAgo != null && subscription.lastUsedDaysAgo >= 14 -> {
                    "Unused for ${subscription.lastUsedDaysAgo} days"
                }
                else -> "Renews in ${subscription.renewsInDays} days"
            }

            val subtitleColor = when {
                subscription.status == SubscriptionStatus.Cancelled -> MaterialTheme.colorScheme.onSurfaceVariant
                subscription.lastUsedDaysAgo != null && subscription.lastUsedDaysAgo >= 14 -> AppTheme.colors.warning
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }

            Text(
                text = subtitle,
                color = subtitleColor,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp)
            )
        }

        if (subscription.status == SubscriptionStatus.Cancelled) {
            Text(
                text = "Cancelled",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
            )
        } else {
            Text(
                text = MoneyFormatter.formatPaise(subscription.amountPaise),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun ReviewBottomSheet(
    subscription: com.example.myapplication.domain.model.Subscription,
    onMarkCancelled: () -> Unit,
    onKeepIt: () -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onKeepIt
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = subscription.name,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = MoneyFormatter.formatPaise(subscription.amountPaise),
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )

                    Text(
                        text = "Renews in ${subscription.renewsInDays} days",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (subscription.lastUsedDaysAgo != null && subscription.lastUsedDaysAgo >= 14) {
                    Text(
                        text = "Unused for ${subscription.lastUsedDaysAgo} days",
                        color = AppTheme.colors.warning,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Text(
                    text = "Note: Cancelling must be done with the provider. This only updates your tracker.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onMarkCancelled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = "Mark as cancelled",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )
                }

                TextButton(
                    onClick = onKeepIt,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Keep it",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyInsightsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = rememberVectorPainter(AppIcons.SparkleIcon),
            contentDescription = null,
            tint = AppTheme.colors.ai,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "You're all caught up",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Copilot will tell you when something needs attention.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LoadingShimmer() {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(2) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {}
        }
    }
}

@Composable
private fun ErrorCard(
    error: String,
    onRetry: () -> Unit
) {
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

@Composable
private fun androidx.compose.foundation.layout.BoxScope.Snackbar(
    message: String,
    onUndo: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .align(Alignment.BottomCenter),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium
            )

            TextButton(
                onClick = onUndo,
                modifier = Modifier.semantics { contentDescription = "Undo dismiss" }
            ) {
                Text(
                    text = "Undo",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

// Previews

@Preview(name = "Default - Dark", showBackground = true)
@Composable
fun InsightsDefaultDarkPreview() {
    AppTheme(themeMode = ThemeMode.Dark) {
        InsightsContent(
            uiState = InsightsUiState(
                insights = listOf(
                    com.example.myapplication.domain.model.Insight(
                        id = "1",
                        type = InsightType.UnusedSubscription,
                        title = "Unused subscription",
                        message = "You have not used your Swiggy subscription in 25 days. Consider cancelling it.",
                        createdAt = kotlinx.datetime.Clock.System.now()
                    ),
                    com.example.myapplication.domain.model.Insight(
                        id = "2",
                        type = InsightType.SpendingAlert,
                        title = "Spending alert",
                        message = "Your food spending is 28% higher than your monthly average.",
                        createdAt = kotlinx.datetime.Clock.System.now()
                    )
                ),
                subscriptions = listOf(
                    com.example.myapplication.domain.model.Subscription(
                        id = "1",
                        name = "Swiggy One",
                        amountPaise = 149_00L,
                        renewsInDays = 6,
                        lastUsedDaysAgo = 25,
                        status = SubscriptionStatus.Active
                    ),
                    com.example.myapplication.domain.model.Subscription(
                        id = "2",
                        name = "Streaming",
                        amountPaise = 199_00L,
                        renewsInDays = 12,
                        lastUsedDaysAgo = null,
                        status = SubscriptionStatus.Active
                    )
                ),
                monthlySubscriptionTotalPaise = 348_00L
            ),
            onEvent = {}
        )
    }
}

@Preview(name = "Empty - Light", showBackground = true)
@Composable
fun InsightsEmptyLightPreview() {
    AppTheme(themeMode = ThemeMode.Light) {
        InsightsContent(
            uiState = InsightsUiState(
                insights = emptyList(),
                subscriptions = listOf(
                    com.example.myapplication.domain.model.Subscription(
                        id = "1",
                        name = "Swiggy One",
                        amountPaise = 149_00L,
                        renewsInDays = 6,
                        lastUsedDaysAgo = 25,
                        status = SubscriptionStatus.Active
                    )
                ),
                monthlySubscriptionTotalPaise = 149_00L
            ),
            onEvent = {}
        )
    }
}

@Preview(name = "Loading - Dark", showBackground = true)
@Composable
fun InsightsLoadingDarkPreview() {
    AppTheme(themeMode = ThemeMode.Dark) {
        InsightsContent(
            uiState = InsightsUiState(isLoading = true),
            onEvent = {}
        )
    }
}

@Preview(name = "Review Sheet - Light", showBackground = true)
@Composable
fun InsightsReviewSheetLightPreview() {
    AppTheme(themeMode = ThemeMode.Light) {
        InsightsContent(
            uiState = InsightsUiState(
                insights = emptyList(),
                subscriptions = listOf(
                    com.example.myapplication.domain.model.Subscription(
                        id = "1",
                        name = "Swiggy One",
                        amountPaise = 149_00L,
                        renewsInDays = 6,
                        lastUsedDaysAgo = 25,
                        status = SubscriptionStatus.Active
                    )
                ),
                monthlySubscriptionTotalPaise = 149_00L,
                reviewTarget = com.example.myapplication.domain.model.Subscription(
                    id = "1",
                    name = "Swiggy One",
                    amountPaise = 149_00L,
                    renewsInDays = 6,
                    lastUsedDaysAgo = 25,
                    status = SubscriptionStatus.Active
                )
            ),
            onEvent = {}
        )
    }
}
