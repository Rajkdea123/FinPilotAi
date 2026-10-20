package com.example.myapplication.ui.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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

sealed class BottomNavRoute(val route: String) {
    object Home : BottomNavRoute("home")
    object Stats : BottomNavRoute("stats")
    object Add : BottomNavRoute("add")
    object AI : BottomNavRoute("ai")
    object Profile : BottomNavRoute("profile")
}

data class BottomNavItem(
    val route: BottomNavRoute,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun AppBottomBar(
    currentRoute: BottomNavRoute,
    onRouteSelected: (BottomNavRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem(BottomNavRoute.Home, "Home", AppIcons.DeviceIcon),
        BottomNavItem(BottomNavRoute.Stats, "Stats", AppIcons.SyncIcon),
        BottomNavItem(BottomNavRoute.Add, "", AppIcons.SparkleIcon),
        BottomNavItem(BottomNavRoute.AI, "AI", AppIcons.SparkleIcon),
        BottomNavItem(BottomNavRoute.Profile, "Profile", AppIcons.BellIcon)
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(84.dp)
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                RoundedCornerShape(0.dp)
            ),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                if (item.route == BottomNavRoute.Add) {
                    // Center Add button
                    Box(
                        modifier = Modifier
                            .offset(y = (-28).dp)
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable { onRouteSelected(item.route) }
                            .semantics { contentDescription = "Add expense" },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = rememberVectorPainter(item.icon),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                } else {
                    // Regular tab
                    val isSelected = currentRoute == item.route
                    Column(
                        modifier = Modifier
                            .padding(vertical = 8.dp, horizontal = 12.dp)
                            .clickable { onRouteSelected(item.route) }
                            .semantics {
                                contentDescription = "${item.label} tab"
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = rememberVectorPainter(item.icon),
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.label,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
                        )
                    }
                }
            }
        }
    }
}
