package com.example.myapplication.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.icons.AppIcons
import com.example.myapplication.ui.theme.AppTheme
import com.example.myapplication.ui.theme.ThemeMode

@Composable
fun ForgotPasswordScreen(
    onBackToSignIn: () -> Unit,
    viewModel: ForgotPasswordViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        Crossfade(
            targetState = uiState.step,
            label = "forgot_password_step",
            modifier = Modifier.fillMaxSize()
        ) { step ->
            when (step) {
                ForgotPasswordStep.Form -> {
                    FormStep(
                        email = uiState.email,
                        emailError = uiState.emailError,
                        isLoading = uiState.isLoading,
                        onEmailChanged = { viewModel.onEvent(ForgotPasswordEvent.EmailChanged(it)) },
                        onSendResetLink = {
                            focusManager.clearFocus()
                            viewModel.onEvent(ForgotPasswordEvent.SendResetLinkClicked)
                        },
                        onBackToSignIn = onBackToSignIn
                    )
                }
                ForgotPasswordStep.Sent -> {
                    SentStep(
                        email = uiState.email,
                        resendSecondsLeft = uiState.resendSecondsLeft,
                        onResend = { viewModel.onEvent(ForgotPasswordEvent.ResendClicked) },
                        onBackToSignIn = onBackToSignIn
                    )
                }
            }
        }
    }
}

@Composable
private fun FormStep(
    email: String,
    emailError: String?,
    isLoading: Boolean,
    onEmailChanged: (String) -> Unit,
    onSendResetLink: () -> Unit,
    onBackToSignIn: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // ScreenTopBar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackToSignIn,
                modifier = Modifier
                    .size(44.dp)
                    .semantics { contentDescription = "Back to sign in" }
            ) {
                Icon(
                    painter = rememberVectorPainter(AppIcons.BackIcon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Forgot password",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Subtitle
        Text(
            text = "Enter your email and we'll send you a reset link.",
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Email field
        LabeledTextField(
            label = "Email",
            value = email,
            onValueChange = onEmailChanged,
            isError = emailError != null,
            errorMessage = emailError,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done,
            onImeAction = onSendResetLink
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Send reset link button
        Button(
            onClick = onSendResetLink,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Send reset link",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun SentStep(
    email: String,
    resendSecondsLeft: Int,
    onResend: () -> Unit,
    onBackToSignIn: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // ScreenTopBar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackToSignIn,
                modifier = Modifier
                    .size(44.dp)
                    .semantics { contentDescription = "Back to sign in" }
            ) {
                Icon(
                    painter = rememberVectorPainter(AppIcons.BackIcon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Forgot password",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Success tile
        Surface(
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(32.dp)),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = rememberVectorPainter(AppIcons.BellIcon),
                    contentDescription = "Email sent",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(44.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Headline
        Text(
            text = "Check your email",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            ),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Message with email
        val messageText = "We sent a reset link to the address you entered."
        Text(
            text = messageText,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = email,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Resend button
        TextButton(
            onClick = onResend,
            enabled = resendSecondsLeft == 0,
            modifier = Modifier
                .size(
                    width = 120.dp,
                    height = 48.dp
                )
                .semantics {
                    contentDescription = if (resendSecondsLeft > 0) {
                        "Resend in $resendSecondsLeft seconds"
                    } else {
                        "Resend reset link"
                    }
                }
        ) {
            Text(
                text = if (resendSecondsLeft > 0) {
                    "Resend in ${resendSecondsLeft}s"
                } else {
                    "Resend"
                },
                color = if (resendSecondsLeft > 0) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.primary
                },
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Back to sign in button
        OutlinedButton(
            onClick = onBackToSignIn,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onBackground
            ),
            border = ButtonDefaults.outlinedButtonBorder
        ) {
            Text(
                text = "Back to sign in",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            )
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun LabeledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    errorMessage: String?,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    onImeAction: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    Column {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .onFocusChanged { isFocused = it.isFocused },
            isError = isError,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(onAny = { onImeAction() }),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                errorContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = if (isError) AppTheme.colors.danger else MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = if (isError) AppTheme.colors.danger else MaterialTheme.colorScheme.outline,
                errorIndicatorColor = AppTheme.colors.danger,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                errorTextColor = MaterialTheme.colorScheme.onBackground
            )
        )

        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = AppTheme.colors.danger,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Preview(name = "Form - Dark", showBackground = true)
@Composable
fun ForgotPasswordScreenFormDarkPreview() {
    AppTheme(themeMode = ThemeMode.Dark) {
        ForgotPasswordScreen(onBackToSignIn = {})
    }
}

@Preview(name = "Form - Light", showBackground = true)
@Composable
fun ForgotPasswordScreenFormLightPreview() {
    AppTheme(themeMode = ThemeMode.Light) {
        ForgotPasswordScreen(onBackToSignIn = {})
    }
}

@Preview(name = "Error - Dark", showBackground = true)
@Composable
fun ForgotPasswordScreenErrorDarkPreview() {
    AppTheme(themeMode = ThemeMode.Dark) {
        ForgotPasswordScreen(onBackToSignIn = {})
    }
}

@Preview(name = "Loading - Light", showBackground = true)
@Composable
fun ForgotPasswordScreenLoadingLightPreview() {
    AppTheme(themeMode = ThemeMode.Light) {
        ForgotPasswordScreen(onBackToSignIn = {})
    }
}

@Preview(name = "Sent - Dark", showBackground = true)
@Composable
fun ForgotPasswordScreenSentDarkPreview() {
    AppTheme(themeMode = ThemeMode.Dark) {
        ForgotPasswordScreen(onBackToSignIn = {})
    }
}

@Preview(name = "Sent - Light", showBackground = true)
@Composable
fun ForgotPasswordScreenSentLightPreview() {
    AppTheme(themeMode = ThemeMode.Light) {
        ForgotPasswordScreen(onBackToSignIn = {})
    }
}
