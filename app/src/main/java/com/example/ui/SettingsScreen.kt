// Architected by Khalid Hasan Limon
package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val isDark by viewModel.isDarkMode.collectAsState()
    val globalThreshold by viewModel.defaultLowStockThreshold.collectAsState()
    
    val scrollState = rememberScrollState()
    
    // Smooth vertical scroll layout
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Sleek Header Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassLogLogo(size = 54.dp)
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "Settings",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = NeonCyan,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "System parameters & environment properties",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    color = GlassTextSecondary
                )
            }
        }

        // 2. Section: Display & Environment Theme
        Text(
            text = "VISUAL ENVIRONMENT",
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = NeonCyan,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        
        GlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                        contentDescription = "Theme Icon",
                        tint = NeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Dark Mode Override",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = GlassTextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isDark) "Ambient cosmic interface active" else "Pristine ice-light interface active",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 12.sp,
                            color = GlassTextSecondary
                        )
                    }
                }
                
                Switch(
                    checked = isDark,
                    onCheckedChange = { viewModel.setDarkMode(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Black,
                        checkedTrackColor = NeonCyan,
                        uncheckedThumbColor = GlassTextSecondary,
                        uncheckedTrackColor = GlassWhite
                    )
                )
            }
        }

        // 3. Section: Operational Alert Limits
        Text(
            text = "OPERATIONAL ALERT LIMITS",
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = NeonCyan,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        GlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Limits Icon",
                        tint = Color(0xFFFFB74D),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Low Stock Threshold Mode",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = GlassTextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Triggers ledger alerts when stock dips below limit",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 12.sp,
                            color = GlassTextSecondary
                        )
                    }
                }
                
                Divider(color = GlassBorderColor, thickness = 0.5.dp)

                // Tactical interactive value adjustment row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Global default boundary:",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = GlassTextPrimary
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconButton(
                            onClick = {
                                if (globalThreshold > 1) {
                                    viewModel.setDefaultLowStockThreshold(globalThreshold - 1)
                                }
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = GlassWhite
                            ),
                            modifier = Modifier
                                .size(36.dp)
                                .border(1.dp, GlassBorderColor, RoundedCornerShape(18.dp))
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrement", tint = GlassTextPrimary)
                        }
                        
                        Text(
                            text = "$globalThreshold units",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = NeonCyan,
                            modifier = Modifier.widthIn(min = 60.dp),
                            textAlign = TextAlign.Center
                        )
                        
                        IconButton(
                            onClick = {
                                if (globalThreshold < 100) {
                                    viewModel.setDefaultLowStockThreshold(globalThreshold + 1)
                                }
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = GlassWhite
                            ),
                            modifier = Modifier
                                .size(36.dp)
                                .border(1.dp, GlassBorderColor, RoundedCornerShape(18.dp))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increment", tint = GlassTextPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                
                // Advanced tactical button to batch apply to current database
                Button(
                    onClick = {
                        viewModel.applyDefaultThresholdToAll()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GlassWhite
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .border(1.dp, GlassBorderColor, RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        Icons.Default.Sync,
                        contentDescription = "Sync",
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SYNC ALL SKUs TO THIS THRESHOLD",
                        fontFamily = FontFamily.SansSerif,
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // 4. Attribution / Footer
        Spacer(modifier = Modifier.weight(1f))
        
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Developed by Khalid Hasan Limon",
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = GlassTextSecondary
            )
            Text(
                text = "Polwel OS Enterprise Edition",
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal,
                fontSize = 9.sp,
                color = GlassTextSecondary.copy(alpha = 0.5f)
            )
        }
    }
}
