package com.example.myapplication.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.icons.AppIcons
import com.example.myapplication.ui.theme.AppTheme

@Composable
fun AppTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    prefix: String? = null,
    readOnly: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        if (readOnly && onClick != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onClick)
                    .border(
                        width = 1.dp,
                        color = if (error != null) AppTheme.colors.danger else MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(14.dp)
                    ),
                color = MaterialTheme.colorScheme.surface
            ) {
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = value,
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                prefix = if (prefix != null) {
                    { Text(text = prefix, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                } else null,
                singleLine = true,
                readOnly = readOnly,
                shape = RoundedCornerShape(14.dp),
                isError = error != null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = if (error != null) AppTheme.colors.danger else MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = if (error != null) AppTheme.colors.danger else MaterialTheme.colorScheme.outline,
                    errorBorderColor = AppTheme.colors.danger,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                ),
                keyboardOptions = keyboardOptions,
                textStyle = MaterialTheme.typography.bodyLarge
            )
        }

        if (error != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = error,
                color = AppTheme.colors.danger,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
            )
        }
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            )
        }
    }
}

@Composable
fun ScreenTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(44.dp)
                .semantics { contentDescription = "Back" }
        ) {
            Icon(
                painter = rememberVectorPainter(AppIcons.BackIcon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.size(8.dp))

        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}
