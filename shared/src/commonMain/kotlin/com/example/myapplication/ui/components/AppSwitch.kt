package com.example.myapplication.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp

@Composable
fun AppSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }

    val trackColor by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(durationMillis = 150),
        label = "trackColor"
    )

    val knobColor by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 150),
        label = "knobColor"
    )

    val knobOffsetDp by animateFloatAsState(
        targetValue = if (checked) 20f else 0f,
        animationSpec = tween(durationMillis = 150),
        label = "knobOffset"
    )

    val stateDescription = if (checked) "On" else "Off"

    Surface(
        modifier = modifier
            .size(width = 48.dp, height = 28.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled
            ) {
                onCheckedChange(!checked)
            }
            .semantics {
                role = Role.Switch
                this.stateDescription = stateDescription
            },
        shape = RoundedCornerShape(14.dp),
        color = trackColor
    ) {
        Box(
            modifier = Modifier
                .padding(3.dp)
                .size(22.dp)
                .clip(CircleShape)
                .background(knobColor)
                .offset(x = with(LocalDensity.current) { knobOffsetDp.toDp() })
        )
    }
}
