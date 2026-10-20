package com.example.myapplication.ui.profile

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.icons.AppIcons
import com.example.myapplication.ui.theme.AppTheme
import org.koin.compose.koinInject

@Composable
fun ProfileScreen(
    snackbarMessage: String? = null,
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToBudget: () -> Unit = {},
    onNavigateToDevices: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToAppearance: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    viewModel: ProfileViewModel = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        if (!snackbarMessage.isNullOrBlank()) {
            snackbarHostState.showSnackbar(snackbarMessage)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.navigationEvents.collect { event ->
            when (event) {
                ProfileNavigationEvent.NavigateToEditProfile -> onNavigateToEditProfile()
                ProfileNavigationEvent.NavigateToBudget -> onNavigateToBudget()
                ProfileNavigationEvent.NavigateToDevices -> onNavigateToDevices()
                ProfileNavigationEvent.NavigateToNotifications -> onNavigateToNotifications()
                ProfileNavigationEvent.NavigateToAppearance -> onNavigateToAppearance()
                ProfileNavigationEvent.NavigateToLogin -> onNavigateToLogin()
            }
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 84.dp),
        ) {
            // Header
            ProfileHeader()

            Spacer(modifier = Modifier.height(16.dp))

            // Content
            val profileErr = uiState.error
            when {
                uiState.isLoading -> {
                    ProfileCardShimmer()
                }

                profileErr != null -> {
                    ErrorCard(
                        error = profileErr,
                        onRetry = { viewModel.onEvent(ProfileEvent.Retry) }
                    )
                }

                else -> {
                    ProfileContent(
                        uiState = uiState,
                        onEvent = { viewModel.onEvent(it) }
                    )
                }
            }
        }

        // Sign out dialog
        if (uiState.showSignOutDialog) {
            SignOutDialog(
                onConfirm = { viewModel.onEvent(ProfileEvent.SignOutConfirmed) },
                onDismiss = { viewModel.onEvent(ProfileEvent.SignOutDismissed) }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 84.dp)
        )
    }
}

@Composable
private fun ProfileHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = "Profile",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = MaterialTheme.typography.headlineSmall.fontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun ProfileContent(
    uiState: ProfileUiState,
    onEvent: (ProfileEvent) -> Unit
) {
    val profile = uiState.profile ?: return
    val stats = uiState.stats ?: return

    // Profile card
    ProfileCard(
        profile = profile,
        stats = stats,
        onEditClick = { onEvent(ProfileEvent.EditClicked) }
    )

    Spacer(modifier = Modifier.height(12.dp))

    // AI Copilot card
    AICopilotCard(
        smartNotificationsEnabled = true // TODO: Get from settings
    )

    Spacer(modifier = Modifier.height(20.dp))

    // Account heading
    Text(
        text = "Account",
        style = MaterialTheme.typography.titleMedium.copy(
            fontFamily = MaterialTheme.typography.headlineSmall.fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        ),
        color = MaterialTheme.colorScheme.onBackground
    )

    Spacer(modifier = Modifier.height(20.dp))

    // Account rows
    AccountRow(
        title = "Monthly budget",
        subtitle = "Set limits per category",
        onClick = { onEvent(ProfileEvent.BudgetClicked) }
    )

    AccountRow(
        title = "Devices and sync",
        subtitle = "Android, iPhone",
        onClick = { onEvent(ProfileEvent.DevicesClicked) }
    )

    AccountRow(
        title = "Notifications",
        subtitle = "Alerts and reminders",
        onClick = { onEvent(ProfileEvent.NotificationsClicked) }
    )

    AccountRow(
        title = "Appearance",
        subtitle = uiState.themeLabel,
        onClick = { onEvent(ProfileEvent.AppearanceClicked) }
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Sign out
    SignOutRow(
        onClick = { onEvent(ProfileEvent.SignOutClicked) }
    )
}

@Composable
private fun ProfileCard(
    profile: com.example.myapplication.domain.model.UserProfile,
    stats: com.example.myapplication.domain.model.ProfileStats,
    onEditClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Avatar row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(
                            BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                            CircleShape
                        )
                        .semantics {
                            contentDescription = "Profile photo of ${profile.name}"
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (profile.avatarUrl != null) {
                        // TODO: Load avatar image with image loading library
                        Text(
                            text = profile.name.first().toString(),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = MaterialTheme.typography.headlineSmall.fontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 26.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text(
                            text = profile.name.first().toString(),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = MaterialTheme.typography.headlineSmall.fontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 26.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Name and email
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = profile.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = MaterialTheme.typography.headlineSmall.fontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = profile.email,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Edit button
                Button(
                    onClick = onEditClick,
                    modifier = Modifier
                        .height(40.dp)
                        .padding(horizontal = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent
                    ),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    )
                ) {
                    Text(
                        text = "Edit",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Divider
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outline,
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatColumn(
                    value = stats.expenseCount.toString(),
                    label = "Expenses"
                )
                StatColumn(
                    value = stats.categoryCount.toString(),
                    label = "Categories"
                )
                StatColumn(
                    value = stats.deviceCount.toString(),
                    label = "Devices"
                )
            }
        }
    }
}

@Composable
private fun StatColumn(
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = MaterialTheme.typography.headlineSmall.fontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AICopilotCard(
    smartNotificationsEnabled: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = rememberVectorPainter(AppIcons.SparkleIcon),
                contentDescription = null,
                tint = AppTheme.colors.ai,
                modifier = Modifier.size(18.dp)
            )
            Column {
                Text(
                    text = "AI Copilot",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    color = AppTheme.colors.ai
                )
                Text(
                    text = if (smartNotificationsEnabled) {
                        "Auto-categorize, smart alerts and chat insights are on."
                    } else {
                        "Smart alerts are off."
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AccountRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = "$title, $subtitle"
            },
        color = androidx.compose.ui.graphics.Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
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
            Text(
                text = "›",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SignOutRow(
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = "Sign out"
            },
        color = androidx.compose.ui.graphics.Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "Sign out",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = AppTheme.colors.danger
            )
        }
    }
}

@Composable
private fun ProfileCardShimmer() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(20.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(14.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .padding(horizontal = 16.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(3) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp, 20.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(50.dp, 12.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                }
            }
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
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
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
private fun SignOutDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Sign out?",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                text = "Your data stays safe on this device.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.colors.danger
                )
            ) {
                Text("Sign out")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
