package com.example.myapplication.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.icons.AppIcons
import com.example.myapplication.ui.theme.AppTheme

data class SyncStatus(
    val isSyncing: Boolean = false,
    val isOnline: Boolean = true,
    val pendingChanges: Int = 0
)

@Composable
fun SyncStatusIndicator(
    status: SyncStatus,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (status.isSyncing) 360f else 0f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(1000),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "rotation"
    )

    Row(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!status.isOnline) {
            // Offline indicator
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(AppTheme.colors.danger, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Offline",
                color = AppTheme.colors.danger,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
            )
        } else if (status.isSyncing) {
            // Syncing indicator
            Icon(
                painter = rememberVectorPainter(AppIcons.SyncIcon),
                contentDescription = "Syncing",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(16.dp)
                    .rotate(rotation)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Syncing...",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
            )
        } else if (status.pendingChanges > 0) {
            // Pending changes indicator
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(AppTheme.colors.warning, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${status.pendingChanges} pending",
                color = AppTheme.colors.warning,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
            )
        } else {
            // Synced indicator
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(AppTheme.colors.success, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Synced",
                color = AppTheme.colors.success,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
            )
        }
    }
}
