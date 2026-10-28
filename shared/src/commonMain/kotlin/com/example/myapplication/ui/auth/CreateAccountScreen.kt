package com.example.myapplication.ui.auth

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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.domain.model.PasswordStrength
import com.example.myapplication.ui.icons.AppIcons
import com.example.myapplication.ui.theme.AppTheme
import com.example.myapplication.ui.theme.ThemeMode

@Composable
fun CreateAccountScreen(
    onSignUpSuccess: () -> Unit,
    onBackToLogin: () -> Unit,
    viewModel: SignUpViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading && 
            uiState.nameError == null && 
            uiState.emailError == null && 
            uiState.passwordError == null && 
            uiState.confirmPasswordError == null && 
            uiState.generalError == null &&
            uiState.name.isNotBlank() &&
            uiState.email.isNotBlank() &&
            uiState.password.isNotBlank()) {
            onSignUpSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
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
                    onClick = onBackToLogin,
                    modifier = Modifier
                        .size(44.dp)
                        .semantics { contentDescription = "Back to login" }
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
                    text = "Create account",
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
                text = "Start tracking your money in minutes.",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Full name field
            LabeledTextField(
                label = "Full name",
                value = uiState.name,
                onValueChange = { viewModel.onEvent(SignUpEvent.NameChanged(it)) },
                isError = uiState.nameError != null,
                errorMessage = uiState.nameError,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
                onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Email field
            LabeledTextField(
                label = "Email",
                value = uiState.email,
                onValueChange = { viewModel.onEvent(SignUpEvent.EmailChanged(it)) },
                isError = uiState.emailError != null,
                errorMessage = uiState.emailError,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
                onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Password field
            LabeledTextField(
                label = "Password",
                value = uiState.password,
                onValueChange = { viewModel.onEvent(SignUpEvent.PasswordChanged(it)) },
                isError = uiState.passwordError != null,
                errorMessage = uiState.passwordError,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next,
                isPassword = true,
                isPasswordVisible = uiState.isPasswordVisible,
                onTogglePasswordVisibility = { viewModel.onEvent(SignUpEvent.TogglePasswordVisibility) },
                onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
            )

            // Password strength bar
            if (uiState.password.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                PasswordStrengthBar(strength = uiState.passwordStrength)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Confirm password field
            LabeledTextField(
                label = "Confirm password",
                value = uiState.confirmPassword,
                onValueChange = { viewModel.onEvent(SignUpEvent.ConfirmPasswordChanged(it)) },
                isError = uiState.confirmPasswordError != null,
                errorMessage = uiState.confirmPasswordError,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                isPassword = true,
                isPasswordVisible = uiState.isConfirmPasswordVisible,
                onTogglePasswordVisibility = { viewModel.onEvent(SignUpEvent.ToggleConfirmPasswordVisibility) },
                onImeAction = {
                    focusManager.clearFocus()
                    viewModel.onEvent(SignUpEvent.SignUpClicked)
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Terms checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = uiState.acceptedTerms,
                    onCheckedChange = { viewModel.onEvent(SignUpEvent.TermsToggled) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .size(24.dp)
                        .semantics { contentDescription = if (uiState.acceptedTerms) "Terms accepted" else "Terms not accepted" }
                )

                Spacer(modifier = Modifier.width(8.dp))

                val termsText = buildAnnotatedString {
                    append("I agree to the ")
                    withStyle(
                        SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append("Terms")
                    }
                    append(" and ")
                    withStyle(
                        SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append("Privacy Policy")
                    )
                }

                Text(
                    text = termsText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .clickable {
                            // TODO: Navigate to Terms and Privacy Policy
                        }
                        .semantics { contentDescription = "Terms and Privacy Policy link" }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // General error banner
            uiState.generalError?.let { error ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(12.dp),
                        color = AppTheme.colors.danger,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Create account button
            Button(
                onClick = { viewModel.onEvent(SignUpEvent.SignUpClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                enabled = uiState.acceptedTerms && !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Create account",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Already have an account
            Row(
                modifier = Modifier.padding(bottom = 36.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp)
                )
                Text(
                    text = "Sign in",
                    modifier = Modifier
                        .clickable { onBackToLogin() }
                        .semantics { contentDescription = "Sign in" },
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun PasswordStrengthBar(strength: PasswordStrength) {
    val (fillColor, label) = when (strength) {
        PasswordStrength.Weak -> AppTheme.colors.danger to "Weak"
        PasswordStrength.Fair -> AppTheme.colors.warning to "Fair"
        PasswordStrength.Strong -> MaterialTheme.colorScheme.primary to "Strong"
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.semantics { contentDescription = "Password strength: $label" }
        ) {
            Box(
                modifier = Modifier
                    .height(4.dp)
                    .weight(1f)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(2.dp))
                        .background(fillColor)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
            )
        }
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
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    onTogglePasswordVisibility: (() -> Unit)? = null,
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
            visualTransformation = if (isPassword && !isPasswordVisible) PasswordVisualTransformation() else VisualTransformation.None,
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
            ),
            trailingIcon = if (isPassword) {
                {
                    IconButton(
                        onClick = { onTogglePasswordVisibility?.invoke() },
                        modifier = Modifier
                            .size(44.dp)
                            .semantics {
                                contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
                            }
                    ) {
                        Icon(
                            painter = rememberVectorPainter(
                                if (isPasswordVisible) AppIcons.BellIcon else AppIcons.SyncIcon
                            ),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else null
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

@Preview(name = "Default - Dark", showBackground = true)
@Composable
fun CreateAccountScreenDarkPreview() {
    AppTheme(themeMode = ThemeMode.Dark) {
        CreateAccountScreen(onSignUpSuccess = {}, onBackToLogin = {})
    }
}

@Preview(name = "Default - Light", showBackground = true)
@Composable
fun CreateAccountScreenLightPreview() {
    AppTheme(themeMode = ThemeMode.Light) {
        CreateAccountScreen(onSignUpSuccess = {}, onBackToLogin = {})
    }
}

@Preview(name = "Error - Dark", showBackground = true)
@Composable
fun CreateAccountScreenErrorDarkPreview() {
    AppTheme(themeMode = ThemeMode.Dark) {
        CreateAccountScreen(onSignUpSuccess = {}, onBackToLogin = {})
    }
}

@Preview(name = "Loading - Light", showBackground = true)
@Composable
fun CreateAccountScreenLoadingLightPreview() {
    AppTheme(themeMode = ThemeMode.Light) {
        CreateAccountScreen(onSignUpSuccess = {}, onBackToLogin = {})
    }
}
