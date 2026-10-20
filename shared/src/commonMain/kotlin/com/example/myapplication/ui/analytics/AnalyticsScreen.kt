package com.example.myapplication.ui.analytics

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.repository.AnalyticsAggregator
import com.example.myapplication.domain.model.AnalyticsSummary
import com.example.myapplication.domain.model.Category
import com.example.myapplication.domain.model.CategorySpend
import com.example.myapplication.domain.model.Period
import com.example.myapplication.domain.model.SpendPoint
import com.example.myapplication.ui.theme.AppTheme
import com.example.myapplication.ui.theme.ThemeMode
import com.example.myapplication.util.MoneyFormatter

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AnalyticsContent(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}

@Composable
fun AnalyticsContent(
    uiState: AnalyticsUiState,
    onEvent: (AnalyticsEvent) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 1. Header (48dp high)
            Header()

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Period selector
            PeriodSelector(
                selectedPeriod = uiState.period,
                onPeriodSelected = { onEvent(AnalyticsEvent.PeriodSelected(it)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.error != null && uiState.summary == null) {
                ErrorCard(
                    error = uiState.error,
                    onRetry = { onEvent(AnalyticsEvent.Retry) }
                )
            } else {
                uiState.summary?.let { summary ->
                    // 3. Chart card
                    ChartCard(
                        summary = summary,
                        selectedIndex = uiState.selectedIndex,
                        onBarSelected = { onEvent(AnalyticsEvent.BarSelected(it)) }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4. Category breakdown card
                    if (summary.categories.isNotEmpty()) {
                        CategoryBreakdownCard(categories = summary.categories)
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        if (uiState.isLoading && uiState.summary != null) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 20.dp)
                    .size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
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
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Analytics",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
private fun PeriodSelector(
    selectedPeriod: Period,
    onPeriodSelected: (Period) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .selectableGroup(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Period.entries.forEach { period ->
                val isSelected = period == selectedPeriod
                val periodName = period.name

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.surfaceVariant
                            else MaterialTheme.colorScheme.surface
                        )
                        .selectable(
                            selected = isSelected,
                            onClick = { onPeriodSelected(period) },
                            role = Role.Tab
                        )
                        .semantics {
                            selected = isSelected
                            role = Role.Tab
                            contentDescription = "$periodName tab"
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = periodName,
                        color = if (isSelected) MaterialTheme.colorScheme.onBackground
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ChartCard(
    summary: AnalyticsSummary,
    selectedIndex: Int?,
    onBarSelected: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            val isEmpty = summary.points.isEmpty() || summary.totalPaise == 0L

            val isBarSelected = selectedIndex != null && selectedIndex in summary.points.indices
            val displayLabel = if (isBarSelected) {
                summary.points[selectedIndex!!].label
            } else {
                when (summary.period) {
                    Period.Week -> "This week"
                    Period.Month -> "This month"
                    Period.Year -> "This year"
                }
            }

            val displayAmount = if (isBarSelected) {
                summary.points[selectedIndex!!].amountPaise
            } else {
                summary.totalPaise
            }

            Text(
                text = displayLabel,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = MoneyFormatter.formatPaise(displayAmount),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.semantics {
                    contentDescription = "$displayLabel total: ${MoneyFormatter.formatPaise(displayAmount)}"
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            if (isEmpty) {
                EmptyChartState()
            } else {
                BarChart(
                    points = summary.points,
                    selectedIndex = selectedIndex,
                    onBarSelected = onBarSelected
                )
            }
        }
    }
}

@Composable
private fun EmptyChartState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Nothing here yet",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "No spending recorded for this period",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun BarChart(
    points: List<SpendPoint>,
    selectedIndex: Int?,
    onBarSelected: (Int) -> Unit
) {
    val maxVal = points.maxOfOrNull { it.amountPaise }?.takeIf { it > 0 } ?: 1L
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

    // Animate height fractions for bars
    var startAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(points, summaryKey(points)) {
        startAnimation = false
        startAnimation = true
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        // Bar canvas area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                points.forEachIndexed { index, point ->
                    val targetFraction = if (point.amountPaise > 0) {
                        (point.amountPaise.toFloat() / maxVal.toFloat()).coerceAtLeast(0.04f)
                    } else 0f

                    val animatedFraction by animateFloatAsState(
                        targetValue = if (startAnimation) targetFraction else 0f,
                        animationSpec = tween(durationMillis = 400),
                        label = "bar_height_$index"
                    )

                    val isSelected = index == selectedIndex
                    val formattedAmount = MoneyFormatter.formatPaise(point.amountPaise)
                        .replace("₹", "")

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clickable { onBarSelected(index) }
                            .semantics {
                                contentDescription = "${point.label}: $formattedAmount rupees"
                                selected = isSelected
                            },
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Canvas(
                            modifier = Modifier
                                .width(28.dp)
                                .fillMaxSize()
                        ) {
                            if (animatedFraction > 0f) {
                                val chartHeight = size.height
                                val barWidth = size.width
                                val barHeight = chartHeight * animatedFraction
                                val top = chartHeight - barHeight
                                val cornerRadiusPx = 8.dp.toPx().coerceAtMost(barHeight / 2)

                                val path = Path().apply {
                                    addRoundRect(
                                        RoundRect(
                                            rect = androidx.compose.ui.geometry.Rect(0f, top, barWidth, chartHeight),
                                            topLeft = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                                            topRight = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                                            bottomRight = CornerRadius.Zero,
                                            bottomLeft = CornerRadius.Zero
                                        )
                                    )
                                }

                                drawPath(
                                    path = path,
                                    color = if (isSelected) primaryColor else surfaceVariantColor
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // X-axis labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            points.forEachIndexed { index, point ->
                Text(
                    text = point.label,
                    color = if (index == selectedIndex) MaterialTheme.colorScheme.onBackground
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        fontWeight = if (index == selectedIndex) FontWeight.Bold else FontWeight.Normal
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

private fun summaryKey(points: List<SpendPoint>): String {
    return points.joinToString(",") { "${it.label}_${it.amountPaise}" }
}

@Composable
private fun CategoryBreakdownCard(
    categories: List<CategorySpend>
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            categories.forEach { item ->
                CategoryRow(categorySpend = item)
            }
        }
    }
}

@Composable
private fun CategoryRow(
    categorySpend: CategorySpend
) {
    val categoryColor = when (categorySpend.category) {
        Category.Food -> AppTheme.colors.warning
        Category.Shopping -> AppTheme.colors.ai
        Category.Bills -> MaterialTheme.colorScheme.primary
        Category.Transport -> AppTheme.colors.info
        Category.Entertainment -> AppTheme.colors.danger
    }

    var animateProgress by remember { mutableStateOf(false) }
    LaunchedEffect(categorySpend) {
        animateProgress = true
    }

    val animatedRatio by animateFloatAsState(
        targetValue = if (animateProgress) categorySpend.ratio.coerceIn(0f, 1f) else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "category_ratio_${categorySpend.category}"
    )

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = categorySpend.category.name,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            )

            Text(
                text = MoneyFormatter.formatPaise(categorySpend.amountPaise),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedRatio)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(categoryColor)
            )
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

// Previews for Light and Dark themes across Week, Month, Year, Loading, and Empty states

@Preview(name = "Week - Dark", showBackground = true)
@Composable
fun AnalyticsWeekDarkPreview() {
    AppTheme(themeMode = ThemeMode.Dark) {
        val summary = AnalyticsAggregator.getSampleAnalytics(Period.Week)
        AnalyticsContent(
            uiState = AnalyticsUiState(
                period = Period.Week,
                summary = summary,
                selectedIndex = summary.highlightIndex
            ),
            onEvent = {}
        )
    }
}

@Preview(name = "Week - Light", showBackground = true)
@Composable
fun AnalyticsWeekLightPreview() {
    AppTheme(themeMode = ThemeMode.Light) {
        val summary = AnalyticsAggregator.getSampleAnalytics(Period.Week)
        AnalyticsContent(
            uiState = AnalyticsUiState(
                period = Period.Week,
                summary = summary,
                selectedIndex = summary.highlightIndex
            ),
            onEvent = {}
        )
    }
}

@Preview(name = "Month - Dark", showBackground = true)
@Composable
fun AnalyticsMonthDarkPreview() {
    AppTheme(themeMode = ThemeMode.Dark) {
        val summary = AnalyticsAggregator.getSampleAnalytics(Period.Month)
        AnalyticsContent(
            uiState = AnalyticsUiState(
                period = Period.Month,
                summary = summary,
                selectedIndex = summary.highlightIndex
            ),
            onEvent = {}
        )
    }
}

@Preview(name = "Month - Light", showBackground = true)
@Composable
fun AnalyticsMonthLightPreview() {
    AppTheme(themeMode = ThemeMode.Light) {
        val summary = AnalyticsAggregator.getSampleAnalytics(Period.Month)
        AnalyticsContent(
            uiState = AnalyticsUiState(
                period = Period.Month,
                summary = summary,
                selectedIndex = summary.highlightIndex
            ),
            onEvent = {}
        )
    }
}

@Preview(name = "Year - Dark", showBackground = true)
@Composable
fun AnalyticsYearDarkPreview() {
    AppTheme(themeMode = ThemeMode.Dark) {
        val summary = AnalyticsAggregator.getSampleAnalytics(Period.Year)
        AnalyticsContent(
            uiState = AnalyticsUiState(
                period = Period.Year,
                summary = summary,
                selectedIndex = summary.highlightIndex
            ),
            onEvent = {}
        )
    }
}

@Preview(name = "Year - Light", showBackground = true)
@Composable
fun AnalyticsYearLightPreview() {
    AppTheme(themeMode = ThemeMode.Light) {
        val summary = AnalyticsAggregator.getSampleAnalytics(Period.Year)
        AnalyticsContent(
            uiState = AnalyticsUiState(
                period = Period.Year,
                summary = summary,
                selectedIndex = summary.highlightIndex
            ),
            onEvent = {}
        )
    }
}

@Preview(name = "Loading - Dark", showBackground = true)
@Composable
fun AnalyticsLoadingDarkPreview() {
    AppTheme(themeMode = ThemeMode.Dark) {
        val summary = AnalyticsAggregator.getSampleAnalytics(Period.Month)
        AnalyticsContent(
            uiState = AnalyticsUiState(
                period = Period.Month,
                summary = summary,
                isLoading = true,
                selectedIndex = summary.highlightIndex
            ),
            onEvent = {}
        )
    }
}

@Preview(name = "Empty - Dark", showBackground = true)
@Composable
fun AnalyticsEmptyDarkPreview() {
    AppTheme(themeMode = ThemeMode.Dark) {
        AnalyticsContent(
            uiState = AnalyticsUiState(
                period = Period.Month,
                summary = AnalyticsSummary(
                    period = Period.Month,
                    totalPaise = 0L,
                    points = emptyList(),
                    highlightIndex = 0,
                    categories = emptyList()
                )
            ),
            onEvent = {}
        )
    }
}

@Preview(name = "Empty - Light", showBackground = true)
@Composable
fun AnalyticsEmptyLightPreview() {
    AppTheme(themeMode = ThemeMode.Light) {
        AnalyticsContent(
            uiState = AnalyticsUiState(
                period = Period.Month,
                summary = AnalyticsSummary(
                    period = Period.Month,
                    totalPaise = 0L,
                    points = emptyList(),
                    highlightIndex = 0,
                    categories = emptyList()
                )
            ),
            onEvent = {}
        )
    }
}
