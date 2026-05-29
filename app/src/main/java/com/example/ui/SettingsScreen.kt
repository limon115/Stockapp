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
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

        // --- NEW SECTION: BACKUP & RESTORE ---
        Text(
            text = "DATABASE BACKUP & RESTORE",
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = NeonCyan,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        val localContext = androidx.compose.ui.platform.LocalContext.current
        var showRestoreDialog by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()

        if (showRestoreDialog) {
            val backups = BackupRestoreManager.getAvailableBackups()
            AlertDialog(
                onDismissRequest = { showRestoreDialog = false },
                containerColor = DynamicCardBackground,
                title = { Text("Restore Backup", color = GlassTextPrimary) },
                text = {
                    if (backups.isEmpty()) {
                        Text("No backups found in ${BackupRestoreManager.backupsDir.absolutePath}", color = GlassTextSecondary)
                    } else {
                        Column {
                            backups.forEach { backupFile ->
                                TextButton(
                                    onClick = {
                                        coroutineScope.launch(Dispatchers.IO) {
                                            val success = BackupRestoreManager.restoreJsonBackup(localContext, backupFile)
                                            withContext(Dispatchers.Main) {
                                                if (success) {
                                                    android.widget.Toast.makeText(localContext, "Restored successfully!", android.widget.Toast.LENGTH_LONG).show()
                                                } else {
                                                    android.widget.Toast.makeText(localContext, "Restore failed", android.widget.Toast.LENGTH_SHORT).show()
                                                }
                                                showRestoreDialog = false
                                            }
                                        }
                                    }
                                ) {
                                    Text(backupFile.name, color = NeonCyan)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showRestoreDialog = false }) { Text("Close", color = GlassTextSecondary) }
                }
            )
        }

        GlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        if (!BackupRestoreManager.hasStoragePermission(localContext)) {
                            BackupRestoreManager.requestStoragePermission(localContext)
                        } else {
                            coroutineScope.launch(Dispatchers.IO) {
                                val path = BackupRestoreManager.createJsonBackup(localContext)
                                withContext(Dispatchers.Main) {
                                    if (path != null) {
                                        android.widget.Toast.makeText(localContext, "Backup saved to: $path", android.widget.Toast.LENGTH_LONG).show()
                                    } else {
                                        android.widget.Toast.makeText(localContext, "Backup failed", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GlassWhite),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp).border(1.dp, GlassBorderColor, RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.Backup, contentDescription = "Backup", tint = AccentGreen, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CREATE SYSTEM BACKUP", color = AccentGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        if (!BackupRestoreManager.hasStoragePermission(localContext)) {
                            BackupRestoreManager.requestStoragePermission(localContext)
                        } else {
                            showRestoreDialog = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GlassWhite),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp).border(1.dp, GlassBorderColor, RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.Restore, contentDescription = "Restore", tint = AccentRed, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("RESTORE FROM BACKUP", color = AccentRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                
                Text(
                    text = "Location: ${BackupRestoreManager.polwelBaseDir.absolutePath}",
                    fontSize = 10.sp,
                    color = GlassTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 4. Attribution / Footer
        Spacer(modifier = Modifier.height(32.dp))
        
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
        Spacer(modifier = Modifier.height(100.dp))
    }
}
