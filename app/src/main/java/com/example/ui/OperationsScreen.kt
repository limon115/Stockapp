// Architected by Khalid Hasan Limon
package com.example.ui

import android.widget.Toast
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import java.util.Calendar
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OperationsScreen(
    viewModel: InventoryViewModel,
    items: List<InventoryItem>
) {
    val context = LocalContext.current
    var showStockInDialog by remember { mutableStateOf(false) }
    var showStockInChoiceDialog by remember { mutableStateOf(false) }
    var showRestockDialog by remember { mutableStateOf(false) }
    var showStockOutDialog by remember { mutableStateOf(false) }
    var showPdfPeriodDialog by remember { mutableStateOf(false) }

    var stockInDate by remember { mutableStateOf("") }
    var stockInTime by remember { mutableStateOf("") }
    
    var stockOutDate by remember { mutableStateOf("") }
    var stockOutTime by remember { mutableStateOf("") }

    var showCustomCategoryDialog by remember { mutableStateOf(false) }
    var customCategories by remember { mutableStateOf(listOf<String>()) }
    
    // Hoisted states for Stock In dialog to prevent data loss when switching to custom category dialog
    var stockInProductName by remember { mutableStateOf("") }
    var stockInCategory by remember { mutableStateOf("") }
    var stockInCategoryExpanded by remember { mutableStateOf(false) }
    var stockInDetails by remember { mutableStateOf("") }
    var stockInValueStr by remember { mutableStateOf("") }
    var stockInQuantityStr by remember { mutableStateOf("") }

    val exportMsg by viewModel.exportMessage.collectAsState()

    LaunchedEffect(exportMsg) {
        if (exportMsg != null) {
            Toast.makeText(context, exportMsg, Toast.LENGTH_LONG).show()
            viewModel.clearExportMessage()
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            .verticalScroll(scrollState),
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
            GlassCard(
                modifier = Modifier
                    .weight(1f)
                    .height(150.dp)
                    .clickable { 
                        showStockInChoiceDialog = true 
                    },
                cornerRadius = 16.dp,
                borderWidth = 0.5.dp,
                borderColor = AccentGreen.copy(alpha = 0.3f)
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
            GlassCard(
                modifier = Modifier
                    .weight(1f)
                    .height(150.dp)
                    .clickable { 
                        val now = Date()
                        stockOutDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now)
                        stockOutTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(now)
                        showStockOutDialog = true 
                    },
                cornerRadius = 16.dp,
                borderWidth = 0.5.dp,
                borderColor = AccentRed.copy(alpha = 0.3f)
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
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 16.dp,
            borderWidth = 0.5.dp,
            borderColor = NeonCyan.copy(alpha = 0.3f)
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
        Spacer(modifier = Modifier.height(32.dp))

        // 4. Mechanical Calculator
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 16.dp,
            borderWidth = 0.5.dp,
            borderColor = NeonCyan.copy(alpha = 0.3f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "HARDWARE CALCULATOR",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = NeonCyan,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                SkeuomorphicCalculator()
            }
        }
        
        Spacer(modifier = Modifier.height(100.dp))
    }

    if (showStockInDialog) {
        val availableCategories = remember(items, customCategories) {
            val all = mutableSetOf("Chemical", "Electronic", "General")
            all.addAll(customCategories)
            items.forEach { all.add(it.category) }
            all.toList().sorted()
        }

        val existingItem = remember(stockInProductName, items) { 
            items.find { it.name.equals(stockInProductName.trim(), ignoreCase = true) } 
        }

        LaunchedEffect(stockInQuantityStr, existingItem) {
            if (existingItem != null) {
                val qty = stockInQuantityStr.toIntOrNull() ?: 0
                if (qty > 0) {
                    val totalValue = qty * existingItem.cost
                    stockInValueStr = String.format(java.util.Locale.US, "%.2f", totalValue)
                }
            }
        }

        AlertDialog(
            onDismissRequest = { showStockInDialog = false },
            containerColor = DynamicCardBackground,
            title = { Text("Stock In", color = GlassTextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = stockInDate,
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Date (yyyy-MM-dd)") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
                            )
                            Box(modifier = Modifier.matchParentSize().background(Color.Transparent).clickable {
                                val calendar = Calendar.getInstance()
                                DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        stockInDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            })
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = stockInTime,
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Time (hh:mm a)") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
                            )
                            Box(modifier = Modifier.matchParentSize().background(Color.Transparent).clickable {
                                val calendar = Calendar.getInstance()
                                TimePickerDialog(
                                    context,
                                    { _, hourOfDay, minute ->
                                        val amPm = if (hourOfDay >= 12) "PM" else "AM"
                                        val hour = if (hourOfDay % 12 == 0) 12 else hourOfDay % 12
                                        stockInTime = String.format(Locale.getDefault(), "%02d:%02d %s", hour, minute, amPm)
                                    },
                                    calendar.get(Calendar.HOUR_OF_DAY),
                                    calendar.get(Calendar.MINUTE),
                                    false
                                ).show()
                            })
                        }
                    }
                    OutlinedTextField(
                        value = stockInProductName,
                        onValueChange = { stockInProductName = it },
                        label = { Text("Product Name") },
                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = stockInCategory,
                                onValueChange = {},
                                readOnly = true,
                                enabled = true,
                                label = { Text("Category") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                ),
                                trailingIcon = {
                                    Icon(
                                        imageVector = if (stockInCategoryExpanded) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                        contentDescription = "Dropdown"
                                    )
                                }
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(Color.Transparent)
                                    .clickable { stockInCategoryExpanded = !stockInCategoryExpanded }
                            )
                        }
                        IconButton(
                            onClick = { 
                                showStockInDialog = false
                                showCustomCategoryDialog = true 
                            },
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AccentGreen.copy(alpha = 0.2f))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add custom category", tint = AccentGreen)
                        }
                    }
                    if (stockInCategoryExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 150.dp)
                                .background(DynamicMenuBackground, RoundedCornerShape(8.dp))
                                .border(1.dp, GlassBorderColor, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            availableCategories.forEach { cat ->
                                Text(
                                    text = cat,
                                    color = GlassTextPrimary,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            stockInCategory = cat
                                            stockInCategoryExpanded = false
                                        }
                                        .padding(vertical = 12.dp, horizontal = 8.dp)
                                )
                                HorizontalDivider(color = GlassBorderColor.copy(alpha = 0.5f))
                            }
                        }
                    }
                    OutlinedTextField(
                        value = stockInDetails,
                        onValueChange = { stockInDetails = it },
                        label = { Text("Details (What is stock for)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
                    )
                    OutlinedTextField(
                        value = stockInValueStr,
                        onValueChange = { stockInValueStr = it },
                        label = { Text("Stock Value (Total)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
                    )
                    OutlinedTextField(
                        value = stockInQuantityStr,
                        onValueChange = { stockInQuantityStr = it },
                        label = { Text("Stock Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = stockInQuantityStr.toIntOrNull() ?: 0
                        val v = stockInValueStr.toDoubleOrNull() ?: 0.0
                        if (stockInProductName.isBlank() || qty <= 0) {
                            Toast.makeText(context, "Invalid input", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        
                        // Check if item exists by name
                        val timestamp = try {
                            val format = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
                            format.parse("$stockInDate $stockInTime")?.time ?: System.currentTimeMillis()
                        } catch (e: Exception) {
                            System.currentTimeMillis()
                        }

                        val existingItem = items.find { it.name.equals(stockInProductName.trim(), ignoreCase = true) }
                        if (existingItem != null) {
                            viewModel.adjustStock(
                                itemId = existingItem.itemId,
                                quantityChanged = qty,
                                transactionType = "IN",
                                details = stockInDetails,
                                overrideValue = v,
                                timestamp = timestamp
                            )
                            Toast.makeText(context, "Stock updated", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.addItem(
                                name = stockInProductName.trim(),
                                sku = "NAME-${System.currentTimeMillis().toString().takeLast(6)}",
                                initialStock = qty,
                                category = stockInCategory.trim().ifBlank { "General" },
                                cost = v / qty.toDouble(),
                                lowStockThreshold = 2,
                                timestamp = timestamp
                            ) {
                                Toast.makeText(context, "New stock added", Toast.LENGTH_SHORT).show()
                            }
                        }
                        showStockInDialog = false
                    },
                    modifier = Modifier.glassmorphic(cornerRadius = 24.dp, borderWidth = 0.5.dp, borderColor = AccentGreen.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) { Text("Confirm", color = AccentGreen, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showStockInDialog = false }) { Text("Cancel", color = GlassTextSecondary) }
            }
        )
    }

    if (showStockInChoiceDialog) {
        AlertDialog(
            onDismissRequest = { showStockInChoiceDialog = false },
            title = { Text("Stock In Options", color = GlassTextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you registering new stock for a new product or restocking an existing one?", color = GlassTextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        val now = Date()
                        val calendar = Calendar.getInstance()
                        calendar.time = now
                        stockInDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH))
                        
                        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
                        val amPm = if (hourOfDay >= 12) "PM" else "AM"
                        val hour = if (hourOfDay % 12 == 0) 12 else hourOfDay % 12
                        stockInTime = String.format(Locale.getDefault(), "%02d:%02d %s", hour, calendar.get(Calendar.MINUTE), amPm)
                        
                        showStockInChoiceDialog = false
                        showStockInDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                ) {
                    Text("New Stock")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        val now = Date()
                        val calendar = Calendar.getInstance()
                        calendar.time = now
                        stockInDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH))
                        
                        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
                        val amPm = if (hourOfDay >= 12) "PM" else "AM"
                        val hour = if (hourOfDay % 12 == 0) 12 else hourOfDay % 12
                        stockInTime = String.format(Locale.getDefault(), "%02d:%02d %s", hour, calendar.get(Calendar.MINUTE), amPm)
                        
                        showStockInChoiceDialog = false
                        showRestockDialog = true
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GlassTextPrimary)
                ) {
                    Text("Restock Old Entry")
                }
            },
            containerColor = DynamicCardBackground
        )
    }

    if (showRestockDialog) {
        var expanded by remember { mutableStateOf(false) }
        var selectedItem by remember { mutableStateOf<InventoryItem?>(null) }
        var details by remember { mutableStateOf("Restock") }
        var valueStr by remember { mutableStateOf("") }
        var quantityStr by remember { mutableStateOf("") }
        
        LaunchedEffect(quantityStr, selectedItem) {
            if (selectedItem != null) {
                val qty = quantityStr.toIntOrNull() ?: 0
                if (qty > 0) {
                    val totalValue = qty * selectedItem!!.cost
                    valueStr = String.format(java.util.Locale.US, "%.2f", totalValue)
                } else {
                    valueStr = ""
                }
            }
        }

        AlertDialog(
            onDismissRequest = { showRestockDialog = false },
            containerColor = DynamicCardBackground,
            title = { Text("Restock Entry", color = GlassTextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = stockInDate,
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Date (yyyy-MM-dd)") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
                            )
                            Box(modifier = Modifier.matchParentSize().background(Color.Transparent).clickable {
                                val calendar = Calendar.getInstance()
                                DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        stockInDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            })
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = stockInTime,
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Time (hh:mm a)") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
                            )
                            Box(modifier = Modifier.matchParentSize().background(Color.Transparent).clickable {
                                val calendar = Calendar.getInstance()
                                TimePickerDialog(
                                    context,
                                    { _, hourOfDay, minute ->
                                        val amPm = if (hourOfDay >= 12) "PM" else "AM"
                                        val hour = if (hourOfDay % 12 == 0) 12 else hourOfDay % 12
                                        stockInTime = String.format(Locale.getDefault(), "%02d:%02d %s", hour, minute, amPm)
                                    },
                                    calendar.get(Calendar.HOUR_OF_DAY),
                                    calendar.get(Calendar.MINUTE),
                                    false
                                ).show()
                            })
                        }
                    }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedItem?.name ?: "Select Product",
                            onValueChange = {},
                            readOnly = true,
                            enabled = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            trailingIcon = {
                                Icon(
                                    imageVector = if (expanded) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                    contentDescription = "Dropdown"
                                )
                            }
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(Color.Transparent)
                                .clickable { expanded = !expanded }
                        )
                    }
                    
                    if (expanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 150.dp)
                                .background(DynamicMenuBackground, RoundedCornerShape(8.dp))
                                .border(1.dp, GlassBorderColor, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            items.take(20).forEach { item ->
                                Text(
                                    text = "${item.name} (${item.currentStock} in stock)",
                                    color = GlassTextPrimary,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedItem = item
                                            expanded = false
                                        }
                                        .padding(vertical = 12.dp, horizontal = 8.dp)
                                )
                                HorizontalDivider(color = GlassBorderColor.copy(alpha = 0.5f))
                            }
                        }
                    }

                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = { quantityStr = it },
                        label = { Text("Stock In Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
                    )
                    OutlinedTextField(
                        value = details,
                        onValueChange = { details = it },
                        label = { Text("Details (What is stock for)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
                    )
                    OutlinedTextField(
                        value = valueStr,
                        onValueChange = { valueStr = it },
                        label = { Text("Added Stock Value") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = quantityStr.toIntOrNull() ?: 0
                        val v = valueStr.toDoubleOrNull() ?: 0.0
                        if (selectedItem == null || qty <= 0) {
                            Toast.makeText(context, "Invalid input", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        
                        val timestamp = try {
                            val format = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
                            format.parse("$stockInDate $stockInTime")?.time ?: System.currentTimeMillis()
                        } catch (e: Exception) {
                            System.currentTimeMillis()
                        }

                        viewModel.adjustStock(
                            itemId = selectedItem!!.itemId,
                            quantityChanged = qty,
                            transactionType = "IN",
                            details = details,
                            overrideValue = v,
                            timestamp = timestamp
                        )
                        Toast.makeText(context, "Restock successful", Toast.LENGTH_SHORT).show()
                        showRestockDialog = false
                    },
                    modifier = Modifier.glassmorphic(cornerRadius = 24.dp, borderWidth = 0.5.dp, borderColor = AccentGreen.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) { Text("Confirm", color = AccentGreen, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showRestockDialog = false }) { Text("Cancel", color = GlassTextSecondary) }
            }
        )
    }

    if (showStockOutDialog) {
        var expanded by remember { mutableStateOf(false) }
        var selectedItem by remember { mutableStateOf<InventoryItem?>(null) }
        var details by remember { mutableStateOf("") }
        var valueStr by remember { mutableStateOf("") }
        var quantityStr by remember { mutableStateOf("") }

        LaunchedEffect(quantityStr, selectedItem) {
            if (selectedItem != null) {
                val qty = quantityStr.toIntOrNull() ?: 0
                if (qty > 0) {
                    val totalValue = qty * selectedItem!!.cost
                    valueStr = String.format(java.util.Locale.US, "%.2f", totalValue)
                } else {
                    valueStr = ""
                }
            }
        }

        AlertDialog(
            onDismissRequest = { showStockOutDialog = false },
            containerColor = DynamicCardBackground,
            title = { Text("Deduct Stock", color = GlassTextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = stockOutDate,
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Date (yyyy-MM-dd)") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
                            )
                            Box(modifier = Modifier.matchParentSize().background(Color.Transparent).clickable {
                                val calendar = Calendar.getInstance()
                                DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        stockOutDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            })
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = stockOutTime,
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Time (hh:mm a)") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
                            )
                            Box(modifier = Modifier.matchParentSize().background(Color.Transparent).clickable {
                                val calendar = Calendar.getInstance()
                                TimePickerDialog(
                                    context,
                                    { _, hourOfDay, minute ->
                                        val amPm = if (hourOfDay >= 12) "PM" else "AM"
                                        val hour = if (hourOfDay % 12 == 0) 12 else hourOfDay % 12
                                        stockOutTime = String.format(Locale.getDefault(), "%02d:%02d %s", hour, minute, amPm)
                                    },
                                    calendar.get(Calendar.HOUR_OF_DAY),
                                    calendar.get(Calendar.MINUTE),
                                    false
                                ).show()
                            })
                        }
                    }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = selectedItem?.name ?: "Select Product",
                                onValueChange = { },
                                readOnly = true,
                                enabled = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                ),
                                label = { Text("Choose Product") }
                            )
                            // Transparent overlay to catch clicks
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(Color.Transparent)
                                    .clickable { expanded = !expanded }
                            )
                        }
                        
                        if (expanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 150.dp)
                                    .background(DynamicMenuBackground, RoundedCornerShape(8.dp))
                                    .border(1.dp, GlassBorderColor, RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                items.take(20).forEach { item ->
                                    Text(
                                        text = "${item.name} (${item.currentStock} in stock)",
                                        color = GlassTextPrimary,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedItem = item
                                                expanded = false
                                            }
                                            .padding(vertical = 12.dp, horizontal = 8.dp)
                                    )
                                    HorizontalDivider(color = GlassBorderColor.copy(alpha = 0.5f))
                                }
                            }
                        }
                    
                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = { quantityStr = it },
                        label = { Text("Quantity to use") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
                    )
                    OutlinedTextField(
                        value = details,
                        onValueChange = { details = it },
                        label = { Text("Details (What is stock used for)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
                    )
                    OutlinedTextField(
                        value = valueStr,
                        onValueChange = { valueStr = it },
                        label = { Text("Used Stock Value") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
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
                        
                        val timestamp = try {
                            val format = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
                            format.parse("$stockOutDate $stockOutTime")?.time ?: System.currentTimeMillis()
                        } catch (e: Exception) {
                            System.currentTimeMillis()
                        }

                        viewModel.adjustStock(
                            itemId = selectedItem!!.itemId,
                            quantityChanged = qty,
                            transactionType = "OUT",
                            details = details,
                            overrideValue = v,
                            timestamp = timestamp
                        )
                        Toast.makeText(context, "Stock deducted", Toast.LENGTH_SHORT).show()
                        showStockOutDialog = false
                    },
                    modifier = Modifier.glassmorphic(cornerRadius = 24.dp, borderWidth = 0.5.dp, borderColor = AccentRed.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) { Text("Confirm", color = AccentRed, fontWeight = FontWeight.Bold) }
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .glassmorphic(cornerRadius = 24.dp, borderWidth = 0.5.dp, borderColor = NeonCyan.copy(alpha = 0.3f)),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues()
                    ) { Text("Last 7 Days Activity", color = NeonCyan) }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { 
                            viewModel.triggerPdfExport(context, 30)
                            showPdfPeriodDialog = false 
                        }, 
                        modifier = Modifier
                            .fillMaxWidth()
                            .glassmorphic(cornerRadius = 24.dp, borderWidth = 0.5.dp, borderColor = NeonCyan.copy(alpha = 0.3f)),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues()
                    ) { Text("Last 30 Days Monthly", color = NeonCyan) }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { 
                            viewModel.triggerPdfExport(context, null)
                            showPdfPeriodDialog = false 
                        }, 
                        modifier = Modifier
                            .fillMaxWidth()
                            .glassmorphic(cornerRadius = 24.dp, borderWidth = 0.5.dp, borderColor = NeonCyan.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues()
                    ) { Text("Master Enterprise All-Time", color = GlassTextPrimary, fontWeight = FontWeight.Bold) }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPdfPeriodDialog = false }) { Text("Cancel", color = AccentGreen) }
            }
        )
    }

    if (showCustomCategoryDialog) {
        var newCategoryName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { 
                showCustomCategoryDialog = false 
                showStockInDialog = true
            },
            title = { Text("Add Custom Category", color = GlassTextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text("Category Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCategoryName.isNotBlank()) {
                            customCategories = customCategories + newCategoryName.trim()
                        }
                        showCustomCategoryDialog = false
                        showStockInDialog = true
                    },
                    modifier = Modifier.glassmorphic(cornerRadius = 24.dp, borderWidth = 0.5.dp, borderColor = AccentGreen.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Text("Add", color = AccentGreen, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showCustomCategoryDialog = false 
                    showStockInDialog = true
                }) { Text("Cancel", color = GlassTextSecondary) }
            },
            containerColor = DynamicCardBackground
        )
    }
}
