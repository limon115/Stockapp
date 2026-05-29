// Architected by Khalid Hasan Limon
package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.InventoryItem
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun OperationsScreen(
    viewModel: InventoryViewModel,
    items: List<InventoryItem>
) {
    val context = LocalContext.current
    var showStockInDialog by remember { mutableStateOf(false) }
    var showStockOutDialog by remember { mutableStateOf(false) }
    var showPdfPeriodDialog by remember { mutableStateOf(false) }

    val exportMsg by viewModel.exportMessage.collectAsState()

    LaunchedEffect(exportMsg) {
        if (exportMsg != null) {
            Toast.makeText(context, exportMsg, Toast.LENGTH_LONG).show()
            viewModel.clearExportMessage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GlassHeader(
            title = "Operations",
            subtitle = "Log stock ins and outs"
        )
        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // STOCK IN BUTTON
            ElevatedCard(
                modifier = Modifier
                    .weight(1f)
                    .height(150.dp)
                    .clickable { showStockInDialog = true },
                colors = CardDefaults.elevatedCardColors(containerColor = DynamicCardSecondary),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x3300E676))
                            .border(1.dp, Color(0xFF00E676).copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Stock In", tint = AccentGreen, modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Stock In", fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, color = GlassTextPrimary)
                }
            }

            // STOCK OUT BUTTON
            ElevatedCard(
                modifier = Modifier
                    .weight(1f)
                    .height(150.dp)
                    .clickable { showStockOutDialog = true },
                colors = CardDefaults.elevatedCardColors(containerColor = DynamicCardSecondary),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x33FF1744))
                            .border(1.dp, AccentRed.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Deduct Stock", tint = AccentRed, modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Deduct Stock", fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, color = GlassTextPrimary)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 3. Document Extraction Suite (Enterprise PDF / CSV exports)
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = DynamicCardSecondary
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "ENTERPRISE DOCUMENT EXPORT ENGINE",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = NeonCyan,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // CSV Button
                    Button(
                        onClick = { viewModel.triggerCsvExport(context) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GlassWhite
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Export CSV",
                                tint = NeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Export CSV",
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 12.sp,
                                color = NeonCyan,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // PDF Button
                    Button(
                        onClick = { showPdfPeriodDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "Render PDF",
                                tint = OnNeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Render PDF",
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 12.sp,
                                color = OnNeonCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    if (showStockInDialog) {
        var productName by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("") }
        var details by remember { mutableStateOf("") }
        var valueStr by remember { mutableStateOf("") }
        var quantityStr by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showStockInDialog = false },
            containerColor = DynamicCardBackground,
            title = { Text("Stock In", color = GlassTextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = productName,
                        onValueChange = { productName = it },
                        label = { Text("Product Name") },
                        colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
                    )
                    OutlinedTextField(
                        value = details,
                        onValueChange = { details = it },
                        label = { Text("Details (What is stock for)") },
                        colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
                    )
                    OutlinedTextField(
                        value = valueStr,
                        onValueChange = { valueStr = it },
                        label = { Text("Stock Value (Total)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
                    )
                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = { quantityStr = it },
                        label = { Text("Stock Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = quantityStr.toIntOrNull() ?: 0
                        val v = valueStr.toDoubleOrNull() ?: 0.0
                        if (productName.isBlank() || qty <= 0) {
                            Toast.makeText(context, "Invalid input", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        
                        // Check if item exists by name
                        val existingItem = items.find { it.name.equals(productName.trim(), ignoreCase = true) }
                        if (existingItem != null) {
                            viewModel.adjustStock(
                                itemId = existingItem.itemId,
                                quantityChanged = qty,
                                transactionType = "IN",
                                details = details,
                                overrideValue = v
                            )
                            Toast.makeText(context, "Stock updated", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.addItem(
                                name = productName.trim(),
                                sku = "NAME-${System.currentTimeMillis().toString().takeLast(6)}",
                                initialStock = qty,
                                category = category.trim().ifBlank { "General" },
                                cost = v / qty.toDouble(),
                                lowStockThreshold = 2
                            ) {
                                Toast.makeText(context, "New stock added", Toast.LENGTH_SHORT).show()
                            }
                        }
                        showStockInDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                ) { Text("Confirm", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showStockInDialog = false }) { Text("Cancel", color = GlassTextSecondary) }
            }
        )
    }

    if (showStockOutDialog) {
        var expanded by remember { mutableStateOf(false) }
        var selectedItem by remember { mutableStateOf<InventoryItem?>(null) }
        var details by remember { mutableStateOf("") }
        var valueStr by remember { mutableStateOf("") }
        var quantityStr by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showStockOutDialog = false },
            containerColor = DynamicCardBackground,
            title = { Text("Deduct Stock", color = GlassTextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box {
                        OutlinedTextField(
                            value = selectedItem?.name ?: "Select Product",
                            onValueChange = { },
                            readOnly = true,
                            enabled = false,
                            modifier = Modifier.clickable { expanded = true }.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                disabledTextColor = GlassTextPrimary,
                                disabledContainerColor = Color.Transparent,
                                disabledIndicatorColor = GlassBorderColor
                            ),
                            label = { Text("Choose Product") }
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(DynamicMenuBackground)
                        ) {
                            items.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text("${item.name} (${item.currentStock} in stock)", color = GlassTextPrimary) },
                                    onClick = {
                                        selectedItem = item
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = { quantityStr = it },
                        label = { Text("Quantity to use") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
                    )
                    OutlinedTextField(
                        value = details,
                        onValueChange = { details = it },
                        label = { Text("Details (What is stock used for)") },
                        colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
                    )
                    OutlinedTextField(
                        value = valueStr,
                        onValueChange = { valueStr = it },
                        label = { Text("Used Stock Value") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = quantityStr.toIntOrNull() ?: 0
                        val v = valueStr.toDoubleOrNull() ?: 0.0
                        if (selectedItem == null || qty <= 0 || qty > selectedItem!!.currentStock) {
                            Toast.makeText(context, "Invalid input or insufficient stock", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        
                        viewModel.adjustStock(
                            itemId = selectedItem!!.itemId,
                            quantityChanged = qty,
                            transactionType = "OUT",
                            details = details,
                            overrideValue = v
                        )
                        Toast.makeText(context, "Stock deducted", Toast.LENGTH_SHORT).show()
                        showStockOutDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                ) { Text("Confirm", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showStockOutDialog = false }) { Text("Cancel", color = GlassTextSecondary) }
            }
        )
    }

    if (showPdfPeriodDialog) {
        AlertDialog(
            onDismissRequest = { showPdfPeriodDialog = false },
            containerColor = DynamicCardBackground,
            title = {
                Text("PDF Report Extraction", color = GlassTextPrimary, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text("Select a specialized period to construct a targeted report inclusive of Audit Transaction logs within that timeframe.", color = GlassTextSecondary, fontSize = 13.sp)
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { 
                            viewModel.triggerPdfExport(context, 7)
                            showPdfPeriodDialog = false 
                        }, 
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = DynamicCardSecondary)
                    ) { Text("Last 7 Days Activity", color = NeonCyan) }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { 
                            viewModel.triggerPdfExport(context, 30)
                            showPdfPeriodDialog = false 
                        }, 
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = DynamicCardSecondary)
                    ) { Text("Last 30 Days Monthly", color = NeonCyan) }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { 
                            viewModel.triggerPdfExport(context, null)
                            showPdfPeriodDialog = false 
                        }, 
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                    ) { Text("Master Enterprise All-Time", color = Color.White, fontWeight = FontWeight.Bold) }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPdfPeriodDialog = false }) { Text("Cancel", color = AccentGreen) }
            }
        )
    }
}
