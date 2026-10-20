package com.example.myapplication.ui.profile.edit

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.components.AppTextField
import com.example.myapplication.ui.components.PrimaryButton
import com.example.myapplication.ui.components.ScreenTopBar
import com.example.myapplication.ui.theme.AppTheme
import com.example.myapplication.ui.theme.ThemeMode
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: (message: String?) -> Unit,
    viewModel: EditProfileViewModel = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onBack("Profile updated")
        }
    }

    LaunchedEffect(uiState.navigateBack) {
        if (uiState.navigateBack) {
            onBack(null)
        }
    }

    LaunchedEffect(uiState.showPhotoSnackbar) {
        if (uiState.showPhotoSnackbar) {
            snackbarHostState.showSnackbar("Coming soon")
            viewModel.onEvent(EditProfileEvent.PhotoSnackbarDismissed)
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
                .imePadding()
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            ScreenTopBar(
                title = "Edit profile",
                onBack = { viewModel.onEvent(EditProfileEvent.BackClicked) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Avatar Section
                AvatarSection(
                    name = uiState.name,
                    avatarUrl = uiState.avatarUrl,
                    onChangePhotoClick = { viewModel.onEvent(EditProfileEvent.ChangePhotoClicked) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Full Name
                AppTextField(
                    label = "Full name",
                    value = uiState.name,
                    onValueChange = { viewModel.onEvent(EditProfileEvent.NameChanged(it)) },
                    error = uiState.nameError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )

                // Email
                AppTextField(
                    label = "Email",
                    value = uiState.email,
                    onValueChange = { viewModel.onEvent(EditProfileEvent.EmailChanged(it)) },
                    error = uiState.emailError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                // Phone
                AppTextField(
                    label = "Phone",
                    value = uiState.phone,
                    onValueChange = { viewModel.onEvent(EditProfileEvent.PhoneChanged(it)) },
                    error = uiState.phoneError,
                    prefix = "+91 ",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // Currency
                AppTextField(
                    label = "Currency",
                    value = uiState.currency,
                    onValueChange = {},
                    readOnly = true,
                    onClick = { viewModel.onEvent(EditProfileEvent.OpenCurrencySheet) }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Save button pinned at bottom with 36dp padding
            PrimaryButton(
                text = "Save changes",
                onClick = { viewModel.onEvent(EditProfileEvent.SaveClicked) },
                enabled = uiState.hasChanges,
                isLoading = uiState.isSaving,
                modifier = Modifier.padding(bottom = 36.dp)
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Discard Confirmation Dialog
        if (uiState.showDiscardDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.onEvent(EditProfileEvent.DismissDiscardClicked) },
                title = { Text("Discard changes?") },
                text = { Text("You have unsaved changes. Are you sure you want to discard them?") },
                confirmButton = {
                    Button(
                        onClick = { viewModel.onEvent(EditProfileEvent.ConfirmDiscardClicked) },
                        colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.danger)
                    ) {
                        Text("Discard")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.onEvent(EditProfileEvent.DismissDiscardClicked) }) {
                        Text("Keep editing")
                    }
                }
            )
        }

        // Currency Bottom Sheet
        if (uiState.currencySheetOpen) {
            CurrencyBottomSheet(
                selectedCurrency = uiState.currency,
                onCurrencySelected = { viewModel.onEvent(EditProfileEvent.CurrencySelected(it)) },
                onDismiss = { viewModel.onEvent(EditProfileEvent.DismissCurrencySheet) }
            )
        }
    }
}

@Composable
private fun AvatarSection(
    name: String,
    avatarUrl: String?,
    onChangePhotoClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val initialLetter = name.firstOrNull()?.uppercase() ?: "?"

        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                    CircleShape
                )
                .semantics {
                    contentDescription = "Profile photo of $name"
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initialLetter,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .heightIn(min = 48.dp)
                .clickable(onClick = onChangePhotoClick)
                .semantics { contentDescription = "Change photo" },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Change photo",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyBottomSheet(
    selectedCurrency: String,
    onCurrencySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Select currency",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            val currencies = listOf(
                "Indian Rupee (₹)" to true,
                "US Dollar ($)" to false,
                "Euro (€)" to false,
                "British Pound (£)" to false
            )

            currencies.forEach { (curr, enabled) ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(enabled = enabled) {
                            if (enabled) onCurrencySelected(curr)
                        },
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = curr,
                            color = if (enabled) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = if (curr == selectedCurrency) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                        RadioButton(
                            selected = curr == selectedCurrency,
                            onClick = { if (enabled) onCurrencySelected(curr) },
                            enabled = enabled
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Preview(name = "Edit Profile - Dark", showBackground = true)
@Composable
fun EditProfileDarkPreview() {
    AppTheme(themeMode = ThemeMode.Dark) {
        EditProfileScreen(onBack = {})
    }
}

@Preview(name = "Edit Profile - Light", showBackground = true)
@Composable
fun EditProfileLightPreview() {
    AppTheme(themeMode = ThemeMode.Light) {
        EditProfileScreen(onBack = {})
    }
}
