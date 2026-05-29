// Architected by Khalid Hasan Limon
package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.*

enum class ActiveTab {
    INVENTORY, OPERATIONS, ANALYTICS, SETTINGS
}

@Composable
fun MainLayoutScreen(
    viewModel: InventoryViewModel = viewModel()
) {
    var activeTab by remember { mutableStateOf(ActiveTab.INVENTORY) }
    
    val items by viewModel.inventoryState.collectAsState()
    val logs by viewModel.auditLogsState.collectAsState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            // Floating Frosted Glass Bottom Navigation Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .glassmorphic(percentage = 24.dp)
                        .padding(vertical = 8.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TabIconItem(
                        icon = Icons.Default.Inventory,
                        label = "Ledger",
                        isSelected = activeTab == ActiveTab.INVENTORY,
                        onClick = { activeTab = ActiveTab.INVENTORY }
                    )
                    
                    TabIconItem(
                        icon = Icons.Default.QrCodeScanner,
                        label = "Operations",
                        isSelected = activeTab == ActiveTab.OPERATIONS,
                        onClick = { activeTab = ActiveTab.OPERATIONS }
                    )
                    
                    TabIconItem(
                        icon = Icons.Default.Analytics,
                        label = "Velocity",
                        isSelected = activeTab == ActiveTab.ANALYTICS,
                        onClick = { activeTab = ActiveTab.ANALYTICS }
                    )

                    TabIconItem(
                        icon = Icons.Default.Settings,
                        label = "Settings",
                        isSelected = activeTab == ActiveTab.SETTINGS,
                        onClick = { activeTab = ActiveTab.SETTINGS }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // Elegant Slide / Dissolve transition on Screen Switches
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(400)) + slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(400))) togetherWith 
                    (fadeOut(animationSpec = tween(400)) + slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(400)))
                },
                label = "panel_view_shift"
            ) { targetTab ->
                when (targetTab) {
                    ActiveTab.INVENTORY -> InventoryDashboardScreen(
                        viewModel = viewModel
                    )
                    ActiveTab.OPERATIONS -> OperationsScreen(
                        viewModel = viewModel,
                        items = items
                    )
                    ActiveTab.ANALYTICS -> HistoricalGraphScreen(
                        items = items,
                        logs = logs
                    )
                    ActiveTab.SETTINGS -> SettingsScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

// Custom Glassmorphic Modifier extension matching percentage curve
fun Modifier.glassmorphic(percentage: androidx.compose.ui.unit.Dp): Modifier = this.glassmorphic(cornerRadius = percentage)

@Composable
fun RowScope.TabIconItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val activeColor = NeonCyan
    val inactiveColor = GlassTextSecondary
    
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Crisp, high-fidelity active pill highlight container
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (isSelected) activeColor.copy(alpha = 0.15f) else Color.Transparent)
                .padding(horizontal = 16.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) activeColor else inactiveColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontFamily = FontFamily.SansSerif,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 11.sp,
            color = if (isSelected) activeColor else inactiveColor,
            letterSpacing = 0.5.sp
        )
    }
}
