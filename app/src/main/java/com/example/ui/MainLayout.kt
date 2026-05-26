// Architected by Khalid Hasan Limon
package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.QrCodeScanner
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
    INVENTORY, SCANNER, ANALYTICS
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
            .background(Black),
        containerColor = Black,
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
                        .padding(vertical = 8.dp, horizontal = 16.dp),
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
                        label = "LAZER",
                        isSelected = activeTab == ActiveTab.SCANNER,
                        onClick = { activeTab = ActiveTab.SCANNER }
                    )
                    
                    TabIconItem(
                        icon = Icons.Default.Analytics,
                        label = "Velocity",
                        isSelected = activeTab == ActiveTab.ANALYTICS,
                        onClick = { activeTab = ActiveTab.ANALYTICS }
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
                    fadeIn() togetherWith fadeOut()
                },
                label = "panel_view_shift"
            ) { targetTab ->
                when (targetTab) {
                    ActiveTab.INVENTORY -> InventoryDashboardScreen(
                        viewModel = viewModel,
                        onNavigateToScanner = { activeTab = ActiveTab.SCANNER }
                    )
                    ActiveTab.SCANNER -> ScannerViewScreen(
                        viewModel = viewModel,
                        onNavigateToInventory = { activeTab = ActiveTab.INVENTORY }
                    )
                    ActiveTab.ANALYTICS -> HistoricalGraphScreen(
                        items = items,
                        logs = logs
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
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) activeColor else inactiveColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontFamily = FontFamily.Serif,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 9.sp,
            color = if (isSelected) activeColor else inactiveColor,
            letterSpacing = 0.5.sp
        )
    }
}
