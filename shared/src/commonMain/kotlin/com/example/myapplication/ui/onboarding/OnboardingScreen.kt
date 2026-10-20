package com.example.myapplication.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.icons.AppIcons
import com.example.myapplication.ui.theme.AppTheme
import com.example.myapplication.ui.theme.ThemeMode
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = viewModel { OnboardingViewModel() }
) {
    val event by viewModel.event.collectAsStateWithLifecycle()

    LaunchedEffect(event) {
        if (event is OnboardingEvent.Finished) {
            onFinished()
        }
    }

    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()
    var currentPage by remember { mutableIntStateOf(0) }

    LaunchedEffect(pagerState.currentPage) {
        currentPage = pagerState.currentPage
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
    ) {
        // Skip button (hidden on last page)
        if (currentPage < 2) {
            TextButton(
                onClick = { viewModel.onSkip() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .semantics { contentDescription = "Skip onboarding" }
            ) {
                Text(
                    text = "Skip",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Illustration card
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp),
                        shape = RoundedCornerShape(28.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.animation.AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(tween(300)),
                                exit = fadeOut(tween(300))
                            ) {
                                when (page) {
                                    0 -> Page1Illustration()
                                    1 -> Page2Illustration()
                                    2 -> Page3Illustration()
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Headline
                    Text(
                        text = when (page) {
                            0 -> "Just type it."
                            1 -> "Works offline."
                            2 -> "Stay in the loop."
                            else -> ""
                        },
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 30.sp,
                            lineHeight = (30 * 1.2).sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Description
                    Text(
                        text = when (page) {
                            0 -> "Describe a purchase in plain words and Copilot picks the category for you."
                            1 -> "Add expenses anywhere. Changes sync across your devices when you reconnect."
                            2 -> "Allow notifications for smart alerts and unused subscription reminders."
                            else -> ""
                        },
                        style = MaterialTheme.typography.bodyLarge.copy(
                            lineHeight = (16 * 1.5).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Bottom area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Page indicator dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { index ->
                        val isActive = index == currentPage
                        val width by animateDpAsState(
                            targetValue = if (isActive) 28.dp else 8.dp,
                            animationSpec = tween(300),
                            label = "dotWidth"
                        )
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(width)
                                .clip(CircleShape)
                                .background(
                                    if (isActive) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .semantics {
                                    contentDescription = "Page ${index + 1} of 3"
                                }
                        )
                    }
                }

                // Next/Get started button
                Button(
                    onClick = {
                        if (currentPage < 2) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(currentPage + 1)
                            }
                        } else {
                            viewModel.onFinished()
                        }
                    },
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
                        text = if (currentPage < 2) "Next" else "Get started",
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
private fun Page1Illustration() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(horizontal = 20.dp)
    ) {
        // Input box
        Box(
            modifier = Modifier
                .width(280.dp)
                .border(
                    BorderStroke(1.dp, AppTheme.colors.ai),
                    RoundedCornerShape(16.dp)
                )
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.background)
                .padding(14.dp, 16.dp)
        ) {
            Text(
                text = "Paid ₹450 at Zomato",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // AI sorted row
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.foundation.Image(
                painter = rememberVectorPainter(AppIcons.SparkleIcon),
                contentDescription = "AI sparkle icon",
                modifier = Modifier.size(16.dp),
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(AppTheme.colors.ai)
            )
            Text(
                text = "Sorted by AI",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                ),
                color = AppTheme.colors.ai
            )
        }

        // Chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Chip(text = "₹450", color = MaterialTheme.colorScheme.onBackground)
            Chip(text = "Food", color = AppTheme.colors.warning)
            Chip(text = "Zomato", color = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Composable
private fun Page2Illustration() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Android device card
        DeviceCard(label = "Android")

        // Sync icon
        androidx.compose.foundation.Image(
            painter = rememberVectorPainter(AppIcons.SyncIcon),
            contentDescription = "Sync icon",
            modifier = Modifier.size(32.dp),
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(MaterialTheme.colorScheme.primary)
        )

        // iPhone device card
        DeviceCard(label = "iPhone")
    }
}

@Composable
private fun DeviceCard(label: String) {
    Surface(
        modifier = Modifier.size(120.dp, 150.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            androidx.compose.foundation.Image(
                painter = rememberVectorPainter(AppIcons.DeviceIcon),
                contentDescription = "Device icon",
                modifier = Modifier.size(28.dp),
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun Page3Illustration() {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(horizontal = 20.dp)
    ) {
        NotificationRow(
            icon = AppIcons.BellIcon,
            label = "Smart alerts",
            color = MaterialTheme.colorScheme.primary
        )
        NotificationRow(
            icon = AppIcons.SyncIcon,
            label = "Background sync",
            color = AppTheme.colors.info
        )
        NotificationRow(
            icon = AppIcons.SparkleIcon,
            label = "AI insights",
            color = AppTheme.colors.ai
        )
    }
}

@Composable
private fun NotificationRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: androidx.compose.ui.graphics.Color) {
    Surface(
        modifier = Modifier
            .width(280.dp)
            .clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.foundation.Image(
                painter = rememberVectorPainter(icon),
                contentDescription = "$label icon",
                modifier = Modifier.size(24.dp),
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(color)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
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
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            ),
            color = color
        )
    }
}

@Preview(name = "Page 1 - Dark", showBackground = true)
@Composable
fun OnboardingPage1DarkPreview() {
    AppTheme(themeMode = ThemeMode.Dark) {
        OnboardingScreen(onFinished = {})
    }
}

@Preview(name = "Page 1 - Light", showBackground = true)
@Composable
fun OnboardingPage1LightPreview() {
    AppTheme(themeMode = ThemeMode.Light) {
        OnboardingScreen(onFinished = {})
    }
}

@Preview(name = "Page 2 - Dark", showBackground = true)
@Composable
fun OnboardingPage2DarkPreview() {
    AppTheme(themeMode = ThemeMode.Dark) {
        OnboardingScreen(onFinished = {})
    }
}

@Preview(name = "Page 2 - Light", showBackground = true)
@Composable
fun OnboardingPage2LightPreview() {
    AppTheme(themeMode = ThemeMode.Light) {
        OnboardingScreen(onFinished = {})
    }
}

@Preview(name = "Page 3 - Dark", showBackground = true)
@Composable
fun OnboardingPage3DarkPreview() {
    AppTheme(themeMode = ThemeMode.Dark) {
        OnboardingScreen(onFinished = {})
    }
}

@Preview(name = "Page 3 - Light", showBackground = true)
@Composable
fun OnboardingPage3LightPreview() {
    AppTheme(themeMode = ThemeMode.Light) {
        OnboardingScreen(onFinished = {})
    }
}
