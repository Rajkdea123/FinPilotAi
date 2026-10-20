package com.example.myapplication.ui.notifications

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.components.AppSwitch
import com.example.myapplication.ui.components.ScreenTopBar
import com.example.myapplication.ui.icons.AppIcons
import com.example.myapplication.ui.theme.AppTheme
import com.example.myapplication.ui.theme.ThemeMode
import org.koin.compose.koinInject

@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    viewModel: NotificationsViewModel = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()

    NotificationsContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBack = onBack
    )
}

@Composable
fun NotificationsContent(
    uiState: NotificationsUiState,
    onEvent: (NotificationsEvent) -> Unit,
    onBack: () -> Unit
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

            // 1. ScreenTopBar
            ScreenTopBar(
                title = "Notifications",
                onBack = onBack
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Permission banner
            if (uiState.permissionStatus == com.example.myapplication.domain.model.PermissionStatus.Denied ||
                uiState.permissionStatus == com.example.myapplication.domain.model.PermissionStatus.NotDetermined) {
                PermissionBanner(
                    status = uiState.permissionStatus,
                    onOpenSettings = { onEvent(NotificationsEvent.OpenSystemSettingsClicked) },
                    onRequestPermission = { onEvent(NotificationsEvent.RequestPermissionClicked) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 3. Master card
            MasterCard(
                masterEnabled = uiState.masterEnabled,
                onToggle = { onEvent(NotificationsEvent.MasterToggled) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Alerts heading
            Text(
                text = "Alerts",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = MaterialTheme.typography.headlineSmall.fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 5. Alert rows
            AlertRow(
                title = "Spending alerts",
                subtitle = "When a category is above your average",
                checked = uiState.prefs.spendingAlerts,
                onCheckedChange = { onEvent(NotificationsEvent.SpendingAlertsToggled) },
                enabled = uiState.masterEnabled
            )

            AlertRow(
                title = "Unused subscriptions",
                subtitle = "Remind me about plans I do not use",
                checked = uiState.prefs.unusedSubscriptions,
                onCheckedChange = { onEvent(NotificationsEvent.UnusedSubscriptionsToggled) },
                enabled = uiState.masterEnabled
            )

            AlertRow(
                title = "Budget warnings",
                subtitle = "Before I reach my monthly budget",
                checked = uiState.prefs.budgetWarnings,
                onCheckedChange = { onEvent(NotificationsEvent.BudgetWarningsToggled) },
                enabled = uiState.masterEnabled,
                onClick = { onEvent(NotificationsEvent.ToggleBudgetThreshold) }
            )

            // 6. Budget threshold expandable section
            AnimatedVisibility(
                visible = uiState.showBudgetThreshold && uiState.masterEnabled,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically()
            ) {
                BudgetThresholdSection(
                    selectedThreshold = uiState.prefs.budgetThresholdPercent,
                    onThresholdSelected = { percent ->
                        onEvent(NotificationsEvent.ThresholdSelected(percent))
                    }
                )
            }

            AlertRow(
                title = "Weekly summary",
                subtitle = "A recap every Sunday",
                checked = uiState.prefs.weeklySummary,
                onCheckedChange = { onEvent(NotificationsEvent.WeeklySummaryToggled) },
                enabled = uiState.masterEnabled
            )

            AlertRow(
                title = "Sync issues",
                subtitle = "If changes cannot be synced",
                checked = uiState.prefs.syncIssues,
                onCheckedChange = { onEvent(NotificationsEvent.SyncIssuesToggled) },
                enabled = uiState.masterEnabled
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 7. Footer note
            Text(
                text = "You can change these any time. Alerts are generated on your device and your account, never shared with advertisers.",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 13.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun PermissionBanner(
    status: com.example.myapplication.domain.model.PermissionStatus,
    onOpenSettings: () -> Unit,
    onRequestPermission: () -> Unit
) {
    val isDenied = status == com.example.myapplication.domain.model.PermissionStatus.Denied
    val title = if (isDenied) "Notifications are blocked" else "Allow notifications"
    val buttonText = if (isDenied) "Open settings" else "Allow"

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Permission banner: $title"
            },
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = rememberVectorPainter(AppIcons.BellIcon),
                    contentDescription = null,
                    tint = AppTheme.colors.warning,
                    modifier = Modifier.size(22.dp)
                )
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Turn them on in system settings to receive alerts.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = if (isDenied) onOpenSettings else onRequestPermission,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun MasterCard(
    masterEnabled: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = rememberVectorPainter(AppIcons.SparkleIcon),
                    contentDescription = null,
                    tint = AppTheme.colors.ai,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = "Smart notifications",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "AI-powered alerts about your spending",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AppSwitch(
                checked = masterEnabled,
                onCheckedChange = { onToggle() }
            )
        }
    }
}

@Composable
private fun AlertRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val alpha = if (enabled) 1f else 0.4f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .alpha(alpha)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 13.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        AppSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            modifier = Modifier.semantics {
                stateDescription = if (checked) "On" else "Off"
                contentDescription = "$title switch"
            }
        )
    }
}

@Composable
private fun BudgetThresholdSection(
    selectedThreshold: Int,
    onThresholdSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Warn me at",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ThresholdChip(
                percent = 80,
                selected = selectedThreshold == 80,
                onClick = { onThresholdSelected(80) }
            )
            ThresholdChip(
                percent = 90,
                selected = selectedThreshold == 90,
                onClick = { onThresholdSelected(90) }
            )
            ThresholdChip(
                percent = 100,
                selected = selectedThreshold == 100,
                onClick = { onThresholdSelected(100) }
            )
        }
    }
}

@Composable
private fun ThresholdChip(
    percent: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onBackground
    }

    Surface(
        modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .clickable(onClick = onClick)
            .semantics {
                stateDescription = if (selected) "Selected" else "Not selected"
                contentDescription = "$percent percent threshold"
            },
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$percent%",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                ),
                color = textColor
            )
        }
    }
}

// Previews

@Preview(name = "Default - Light", showBackground = true)
@Composable
fun NotificationsDefaultLightPreview() {
    AppTheme(themeMode = ThemeMode.Light) {
        NotificationsContent(
            uiState = NotificationsUiState(
                masterEnabled = true,
                prefs = com.example.myapplication.domain.model.NotificationPrefs(),
                permissionStatus = com.example.myapplication.domain.model.PermissionStatus.Granted
            ),
            onEvent = {},
            onBack = {}
        )
    }
}

@Preview(name = "Default - Dark", showBackground = true)
@Composable
fun NotificationsDefaultDarkPreview() {
    AppTheme(themeMode = ThemeMode.Dark) {
        NotificationsContent(
            uiState = NotificationsUiState(
                masterEnabled = true,
                prefs = com.example.myapplication.domain.model.NotificationPrefs(),
                permissionStatus = com.example.myapplication.domain.model.PermissionStatus.Granted
            ),
            onEvent = {},
            onBack = {}
        )
    }
}

@Preview(name = "Master Off - Light", showBackground = true)
@Composable
fun NotificationsMasterOffLightPreview() {
    AppTheme(themeMode = ThemeMode.Light) {
        NotificationsContent(
            uiState = NotificationsUiState(
                masterEnabled = false,
                prefs = com.example.myapplication.domain.model.NotificationPrefs(),
                permissionStatus = com.example.myapplication.domain.model.PermissionStatus.Granted
            ),
            onEvent = {},
            onBack = {}
        )
    }
}

@Preview(name = "Master Off - Dark", showBackground = true)
@Composable
fun NotificationsMasterOffDarkPreview() {
    AppTheme(themeMode = ThemeMode.Dark) {
        NotificationsContent(
            uiState = NotificationsUiState(
                masterEnabled = false,
                prefs = com.example.myapplication.domain.model.NotificationPrefs(),
                permissionStatus = com.example.myapplication.domain.model.PermissionStatus.Granted
            ),
            onEvent = {},
            onBack = {}
        )
    }
}

@Preview(name = "Permission Denied - Light", showBackground = true)
@Composable
fun NotificationsPermissionDeniedLightPreview() {
    AppTheme(themeMode = ThemeMode.Light) {
        NotificationsContent(
            uiState = NotificationsUiState(
                masterEnabled = true,
                prefs = com.example.myapplication.domain.model.NotificationPrefs(),
                permissionStatus = com.example.myapplication.domain.model.PermissionStatus.Denied
            ),
            onEvent = {},
            onBack = {}
        )
    }
}

@Preview(name = "Permission Denied - Dark", showBackground = true)
@Composable
fun NotificationsPermissionDeniedDarkPreview() {
    AppTheme(themeMode = ThemeMode.Dark) {
        NotificationsContent(
            uiState = NotificationsUiState(
                masterEnabled = true,
                prefs = com.example.myapplication.domain.model.NotificationPrefs(),
                permissionStatus = com.example.myapplication.domain.model.PermissionStatus.Denied
            ),
            onEvent = {},
            onBack = {}
        )
    }
}

@Preview(name = "Budget Threshold Expanded - Light", showBackground = true)
@Composable
fun NotificationsBudgetThresholdLightPreview() {
    AppTheme(themeMode = ThemeMode.Light) {
        NotificationsContent(
            uiState = NotificationsUiState(
                masterEnabled = true,
                prefs = com.example.myapplication.domain.model.NotificationPrefs(
                    budgetWarnings = true,
                    budgetThresholdPercent = 90
                ),
                permissionStatus = com.example.myapplication.domain.model.PermissionStatus.Granted,
                showBudgetThreshold = true
            ),
            onEvent = {},
            onBack = {}
        )
    }
}

@Preview(name = "Budget Threshold Expanded - Dark", showBackground = true)
@Composable
fun NotificationsBudgetThresholdDarkPreview() {
    AppTheme(themeMode = ThemeMode.Dark) {
        NotificationsContent(
            uiState = NotificationsUiState(
                masterEnabled = true,
                prefs = com.example.myapplication.domain.model.NotificationPrefs(
                    budgetWarnings = true,
                    budgetThresholdPercent = 90
                ),
                permissionStatus = com.example.myapplication.domain.model.PermissionStatus.Granted,
                showBudgetThreshold = true
            ),
            onEvent = {},
            onBack = {}
        )
    }
}
