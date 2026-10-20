package com.example.myapplication.ui.sync

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.rotate
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
import com.example.myapplication.domain.model.AppSettings
import com.example.myapplication.domain.model.ConflictChoice
import com.example.myapplication.domain.model.ConflictVersion
import com.example.myapplication.domain.model.SyncConflict
import com.example.myapplication.domain.model.SyncStatus
import com.example.myapplication.ui.components.AppSwitch
import com.example.myapplication.ui.icons.AppIcons
import com.example.myapplication.ui.theme.AppTheme
import com.example.myapplication.ui.theme.ThemeMode
import com.example.myapplication.util.MoneyFormatter
import kotlinx.datetime.Clock

@Composable
fun SyncScreen(
    viewModel: SyncViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SyncContent(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}

@Composable
fun SyncContent(
    uiState: SyncUiState,
    onEvent: (SyncEvent) -> Unit
) {
    var showBiometricError by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.biometricError) {
        if (uiState.biometricError != null) {
            showBiometricError = true
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
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 1. Header (48dp high)
            Header(onBack = { onEvent(SyncEvent.BackClicked) })

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Status card
            StatusCard(
                status = uiState.status,
                isOnline = uiState.isOnline,
                onSyncNow = { onEvent(SyncEvent.SyncNowClicked) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Conflict cards
            if (uiState.conflicts.isNotEmpty()) {
                uiState.conflicts.forEach { conflict ->
                    ConflictCard(
                        conflict = conflict,
                        selectedChoice = uiState.selectedConflictChoice[conflict.id],
                        onSelectChoice = { choice ->
                            onEvent(SyncEvent.SelectConflictChoice(conflict.id, choice))
                        },
                        onResolve = { choice ->
                            onEvent(SyncEvent.ResolveConflict(conflict.id, choice))
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Settings section
            SettingsSection(
                settings = uiState.settings,
                onToggleAutoSync = { onEvent(SyncEvent.ToggleAutoSync) },
                onToggleSmartNotifications = { onEvent(SyncEvent.ToggleSmartNotifications) },
                onToggleBiometricLock = { onEvent(SyncEvent.ToggleBiometricLock) }
            )

            Spacer(modifier = Modifier.height(100.dp))
        }

        // Snackbar for biometric error
        if (showBiometricError && uiState.biometricError != null) {
            Snackbar(
                message = uiState.biometricError!!,
                onDismiss = {
                    showBiometricError = false
                    onEvent(SyncEvent.ClearBiometricError)
                }
            )
        }
    }
}

@Composable
private fun Header(onBack: () -> Unit) {
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
                .clickable(onClick = onBack)
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
            text = "Sync and devices",
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
private fun StatusCard(
    status: SyncStatus?,
    isOnline: Boolean,
    onSyncNow: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (status) {
                        is SyncStatus.Offline -> {
                            Icon(
                                painter = rememberVectorPainter(AppIcons.SparkleIcon),
                                contentDescription = null,
                                tint = AppTheme.colors.warning,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "Offline · ${status.pendingCount} changes waiting",
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        is SyncStatus.Syncing -> {
                            val rotation by animateFloatAsState(
                                targetValue = 360f,
                                animationSpec = tween(durationMillis = 1000),
                                label = "rotation"
                            )
                            Icon(
                                painter = rememberVectorPainter(AppIcons.SparkleIcon),
                                contentDescription = null,
                                tint = AppTheme.colors.ai,
                                modifier = Modifier
                                    .size(22.dp)
                                    .rotate(rotation)
                            )
                            Text(
                                text = "Syncing ${status.pendingCount} changes…",
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        is SyncStatus.Synced -> {
                            Icon(
                                painter = rememberVectorPainter(AppIcons.SparkleIcon),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "All changes synced",
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        is SyncStatus.Error -> {
                            Icon(
                                painter = rememberVectorPainter(AppIcons.SparkleIcon),
                                contentDescription = null,
                                tint = AppTheme.colors.danger,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = status.message,
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        null -> {
                            Text(
                                text = "Loading...",
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                if (status is SyncStatus.Synced || status is SyncStatus.Error) {
                    TextButton(
                        onClick = onSyncNow,
                        enabled = isOnline,
                        modifier = Modifier.semantics { contentDescription = "Sync now" }
                    ) {
                        Text(
                            text = "Sync now",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            when (status) {
                is SyncStatus.Offline -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Everything is saved on this phone and will sync when you reconnect.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
                    )
                }

                is SyncStatus.Synced -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    val timeAgo = getTimeAgo(status.lastSyncedAt)
                    Text(
                        text = "Last synced $timeAgo",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
                    )
                }

                else -> {}
            }
        }
    }
}

@Composable
private fun ConflictCard(
    conflict: SyncConflict,
    selectedChoice: ConflictChoice?,
    onSelectChoice: (ConflictChoice) -> Unit,
    onResolve: (ConflictChoice) -> Unit
) {
    AnimatedVisibility(
        visible = true,
        exit = fadeOut(tween(250)) + shrinkVertically(tween(250))
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "1 conflict to resolve",
                    color = AppTheme.colors.danger,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ConflictVersionCard(
                        version = conflict.local,
                        isSelected = selectedChoice == ConflictChoice.Local,
                        onClick = { onSelectChoice(ConflictChoice.Local) }
                    )

                    ConflictVersionCard(
                        version = conflict.remote,
                        isSelected = selectedChoice == ConflictChoice.Remote,
                        onClick = { onSelectChoice(ConflictChoice.Remote) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val choice = selectedChoice ?: ConflictChoice.Latest
                        onResolve(choice)
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
                        text = if (selectedChoice != null) "Keep selected" else "Keep latest edit",
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
private fun androidx.compose.foundation.layout.RowScope.ConflictVersionCard(
    version: ConflictVersion,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick)
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(14.dp)
                    )
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = version.deviceLabel,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = MoneyFormatter.formatPaise(version.amountPaise),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
private fun SettingsSection(
    settings: AppSettings,
    onToggleAutoSync: () -> Unit,
    onToggleSmartNotifications: () -> Unit,
    onToggleBiometricLock: () -> Unit
) {
    Text(
        text = "Settings",
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.headlineSmall.copy(
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    )

    Spacer(modifier = Modifier.height(4.dp))

    SettingRow(
        label = "Auto-sync on Wi-Fi",
        checked = settings.autoSyncOnWifi,
        onCheckedChange = { onToggleAutoSync() }
    )

    SettingRow(
        label = "Smart notifications",
        checked = settings.smartNotifications,
        onCheckedChange = { onToggleSmartNotifications() }
    )

    SettingRow(
        label = "Biometric lock",
        checked = settings.biometricLock,
        onCheckedChange = { onToggleBiometricLock() }
    )
}

@Composable
private fun SettingRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp)
        )

        AppSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun androidx.compose.foundation.layout.BoxScope.Snackbar(
    message: String,
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

            TextButton(onClick = onDismiss) {
                Text(
                    text = "Dismiss",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

private fun getTimeAgo(instant: kotlinx.datetime.Instant): String {
    val now = Clock.System.now()
    val seconds = (now - instant).inWholeSeconds
    return when {
        seconds < 60 -> "just now"
        seconds < 3600 -> "${seconds / 60} min ago"
        seconds < 86400 -> "${seconds / 3600} hours ago"
        else -> "${seconds / 86400} days ago"
    }
}

// Previews

@Preview(name = "Offline with conflict - Dark", showBackground = true)
@Composable
fun SyncOfflineConflictDarkPreview() {
    AppTheme(themeMode = ThemeMode.Dark) {
        SyncContent(
            uiState = SyncUiState(
                status = SyncStatus.Offline(pendingCount = 3),
                conflicts = listOf(
                    SyncConflict(
                        id = "conflict1",
                        expenseId = "expense1",
                        local = ConflictVersion(
                            deviceLabel = "This phone",
                            amountPaise = 500_00L,
                            metadata = com.example.myapplication.domain.model.SyncMetadata(
                                id = "meta1",
                                version = 1L,
                                updatedAt = 1000L,
                                deviceId = "android123"
                            )
                        ),
                        remote = ConflictVersion(
                            deviceLabel = "iPhone",
                            amountPaise = 600_00L,
                            metadata = com.example.myapplication.domain.model.SyncMetadata(
                                id = "meta2",
                                version = 2L,
                                updatedAt = 2000L,
                                deviceId = "iphone456"
                            )
                        )
                    )
                ),
                isOnline = false,
                settings = AppSettings()
            ),
            onEvent = {}
        )
    }
}

@Preview(name = "Syncing - Light", showBackground = true)
@Composable
fun SyncSyncingLightPreview() {
    AppTheme(themeMode = ThemeMode.Light) {
        SyncContent(
            uiState = SyncUiState(
                status = SyncStatus.Syncing(pendingCount = 3),
                conflicts = emptyList(),
                isOnline = true,
                settings = AppSettings()
            ),
            onEvent = {}
        )
    }
}

@Preview(name = "Synced - Dark", showBackground = true)
@Composable
fun SyncSyncedDarkPreview() {
    AppTheme(themeMode = ThemeMode.Dark) {
        SyncContent(
            uiState = SyncUiState(
                status = SyncStatus.Synced(lastSyncedAt = Clock.System.now()),
                conflicts = emptyList(),
                isOnline = true,
                settings = AppSettings()
            ),
            onEvent = {}
        )
    }
}

@Preview(name = "Error - Light", showBackground = true)
@Composable
fun SyncErrorLightPreview() {
    AppTheme(themeMode = ThemeMode.Light) {
        SyncContent(
            uiState = SyncUiState(
                status = SyncStatus.Error("Network error", pendingCount = 3),
                conflicts = emptyList(),
                isOnline = false,
                settings = AppSettings()
            ),
            onEvent = {}
        )
    }
}
