// Architected by Khalid Hasan Limon
package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.data.AuditLogEntry
import com.example.data.InventoryItem
import com.example.ui.theme.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InventoryDashboardScreen(
    viewModel: InventoryViewModel
) {
    val context = LocalContext.current
    
    val items by viewModel.inventoryState.collectAsState()
    val logs by viewModel.auditLogsState.collectAsState()
    val exportMsg by viewModel.exportMessage.collectAsState()

    // Clear alerts on triggers
    LaunchedEffect(exportMsg) {
        if (exportMsg != null) {
            Toast.makeText(context, exportMsg, Toast.LENGTH_LONG).show()
            viewModel.clearExportMessage()
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    
    // Manual registration visibility
    var showManualRegisterForm by remember { mutableStateOf(false) }
    var showPdfPeriodDialog by remember { mutableStateOf(false) }
    
    // New Stock Registration Variables
    var newName by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("General") }
    var newStockString by remember { mutableStateOf("5") }
    var newCostString by remember { mutableStateOf("10.00") }
    var newThresholdString by remember { mutableStateOf("2") }

    val coroutineScope = rememberCoroutineScope()
    var isSuggestionsLoading by remember { mutableStateOf(false) }
    var suggestionExplanation by remember { mutableStateOf<String?>(null) }
    var suggestionHasAiIntelligence by remember { mutableStateOf(true) }

    fun fetchAiSuggestions(nameCode: String = "") {
        val trimmedName = nameCode.trim()
        if (trimmedName.isEmpty()) return
        isSuggestionsLoading = true
        suggestionExplanation = null
        coroutineScope.launch {
            try {
                val result = GeminiClient.suggestProductForName(trimmedName)
                
                newCategory = result.category
                newCostString = String.format("%.2f", result.cost)
                suggestionExplanation = result.explanation
                suggestionHasAiIntelligence = result.hasAiIntelligence
            } catch (e: Exception) {
                suggestionExplanation = "Could not fetch suggestions: ${e.message}"
            } finally {
                isSuggestionsLoading = false
            }
        }
    }

    // Categories list dynamically calculated
    val categories = remember(items) {
        val list = items.map { it.category }.distinct().toMutableList()
        list.add(0, "All")
        list
    }

    // Filtered inventory list
    val filteredLogs = remember(logs, searchQuery) {
        logs.filter { log ->
            val matchSearch = log.productName.contains(searchQuery, ignoreCase = true) || log.details.contains(searchQuery, ignoreCase = true)
            matchSearch
        }.sortedByDescending { it.timestamp }
    }

    // Calculations
    val totalItemsCount = items.size
    val totalStockUnits = items.sumOf { it.currentStock }
    val totalValuation = items.sumOf { it.currentStock * it.cost }
    
    // Critical stock items indicator list based on per-item custom threshold
    val lowStockItems = remember(items) {
        items.filter { it.currentStock <= it.lowStockThreshold }
    }
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Sleek Frosted Title Banner
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        GlassHeader(
                            title = "Polwel",
                            subtitle = "Enterprise Tactical Inventory Ledger"
                        )
                    }
                    
                    // Manual Registration toggler button
                    TextButton(
                        onClick = { showManualRegisterForm = !showManualRegisterForm },
                        colors = ButtonDefaults.textButtonColors(contentColor = NeonCyan)
                    ) {
                        Icon(
                            imageVector = if (showManualRegisterForm) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = "Manuel Input Override"
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (showManualRegisterForm) "Close" else "Manual Override",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 2. Statistics Grid Redesigned as modern Material 3 Elevated Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Global valuation Elevated Card
                    ElevatedCard(
                        modifier = Modifier
                            .weight(1.2f)
                            .height(84.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = DynamicCardBackground
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "TOTAL VALUATION",
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = GlassTextSecondary,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "৳${DecimalFormat("#,##0.00").format(totalValuation)}",
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = NeonCyan
                            )
                        }
                    }
                    
                    // Total Unique SKUs Elevated Card
                    ElevatedCard(
                        modifier = Modifier
                            .weight(0.9f)
                            .height(84.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = DynamicCardBackground
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "UNIQUE SKUS",
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = GlassTextSecondary,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "$totalItemsCount entries",
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = GlassTextPrimary
                            )
                        }
                    }
                    
                    // Total Stock Units Elevated Card
                    ElevatedCard(
                        modifier = Modifier
                            .weight(0.9f)
                            .height(84.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = DynamicCardBackground
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "TOTAL UNITS",
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = GlassTextSecondary,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "$totalStockUnits items",
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = ElectricBlue
                            )
                        }
                    }
                }
            }

            // 2b. Critical Stock Alert Panel (Threshold: <= 5 units)
            if (lowStockItems.isNotEmpty()) {
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFFFB74D).copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                            borderColor = Color(0xFFFFB74D).copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Warning",
                                        tint = Color(0xFFFFB74D),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "CRITICAL STOCK ALERT",
                                        fontFamily = FontFamily.SansSerif,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFFFFB74D),
                                        letterSpacing = 1.sp
                                    )
                                }
                                
                                // Count indicator
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0x33FFB74D))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${lowStockItems.size} SKUs Low",
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFB74D)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                lowStockItems.forEach { lowItem ->
                                    val isOutOfStock = lowItem.currentStock == 0
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(GlassWhiteSubtle)
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = lowItem.name,
                                                fontFamily = FontFamily.SansSerif,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = GlassTextPrimary
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row {
                                                Text(
                                                    text = "SKU: ${lowItem.sku}",
                                                    fontFamily = FontFamily.SansSerif,
                                                    fontSize = 10.sp,
                                                    color = GlassTextSecondary
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = if (isOutOfStock) "OUT OF STOCK" else "${lowItem.currentStock} units remaining",
                                                    fontFamily = FontFamily.SansSerif,
                                                    fontSize = 10.sp,
                                                    color = if (isOutOfStock) AccentRed else Color(0xFFFFB74D),
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                        
                                        // Quick Restock Button (+10 items)
                                        Button(
                                            onClick = {
                                                viewModel.adjustStock(lowItem.itemId, 10, "IN")
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x1B00E5FF)),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier
                                                .height(28.dp)
                                                .border(0.5.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                        ) {
                                            Text(
                                                text = "RESTOCK +10",
                                                fontFamily = FontFamily.SansSerif,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = NeonCyan
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Document Extraction Suite (Enterprise PDF / CSV exports)
            item {
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
                            // CSV Button (Distinct Outlined/Glass component with crisp vector icons)
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

                            // PDF Button (Distinct high contrast component with down-arrow)
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

            // 4. Manual Add Form Overlay Drawer
            item {
                AnimatedVisibility(
                    visible = showManualRegisterForm,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0x3300E5FF), RoundedCornerShape(16.dp))
                    ) {
                        Text(
                            text = "MANUAL REGISTRATION OVERRIDE",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = NeonCyan
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Smart suggestion details or loading status
                            AnimatedVisibility(
                                visible = isSuggestionsLoading || !suggestionExplanation.isNullOrBlank(),
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSuggestionsLoading) Color(0x3300E5FF) else if (suggestionHasAiIntelligence) Color(0x1F00E5FF) else Color(0x1F2979FF))
                                        .border(
                                            1.dp,
                                            if (isSuggestionsLoading) NeonCyan.copy(alpha = 0.5f) else if (suggestionHasAiIntelligence) NeonCyan.copy(alpha = 0.3f) else ElectricBlue.copy(alpha = 0.3f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(8.dp)
                                ) {
                                    if (isSuggestionsLoading) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(14.dp),
                                                strokeWidth = 2.dp,
                                                color = NeonCyan
                                            )
                                            Text(
                                                text = "AI analyzing product features...",
                                                fontFamily = FontFamily.SansSerif,
                                                fontSize = 11.sp,
                                                color = GlassTextPrimary
                                            )
                                        }
                                    } else if (!suggestionExplanation.isNullOrBlank()) {
                                        Column {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (suggestionHasAiIntelligence) Icons.Default.AutoAwesome else Icons.Default.Info,
                                                    contentDescription = "Context",
                                                    tint = if (suggestionHasAiIntelligence) NeonCyan else ElectricBlue,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Text(
                                                    text = if (suggestionHasAiIntelligence) "AI Suggestion loaded" else "Dynamic Template applied",
                                                    fontFamily = FontFamily.SansSerif,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp,
                                                    color = if (suggestionHasAiIntelligence) NeonCyan else ElectricBlue
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = suggestionExplanation!!,
                                                fontFamily = FontFamily.SansSerif,
                                                fontSize = 11.sp,
                                                color = GlassTextSecondary
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    GlassTextField(
                                        value = newName,
                                        onValueChange = { newName = it },
                                        label = "Item/Product Name",
                                        placeholder = "e.g. Cobalt Lens Glass"
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { fetchAiSuggestions(newName) },
                                    enabled = !isSuggestionsLoading && newName.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                                    contentPadding = PaddingValues(horizontal = 12.dp)
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = "AI Suggest Detail", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("AI Suggest", fontSize = 11.sp, fontFamily = FontFamily.SansSerif)
                                }
                            }

                            GlassTextField(
                                value = newCategory,
                                onValueChange = { newCategory = it },
                                label = "Category",
                                placeholder = "General"
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                GlassTextField(
                                    value = newStockString,
                                    onValueChange = { newStockString = it },
                                    label = "Initial stock",
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )

                                GlassTextField(
                                    value = newCostString,
                                    onValueChange = { newCostString = it },
                                    label = "Unit Cost (TK)",
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f)
                                )

                                GlassTextField(
                                    value = newThresholdString,
                                    onValueChange = { newThresholdString = it },
                                    label = "Alert Limit",
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Button(
                                onClick = {
                                    if (newName.isBlank()) {
                                        Toast.makeText(context, "Requires valid Name", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    
                                    val finalSku = "NAME-${System.currentTimeMillis().toString().takeLast(6)}"
                                    
                                    val stockVal = newStockString.toIntOrNull() ?: 0
                                    val costVal = newCostString.toDoubleOrNull() ?: 0.00
                                    val thresholdVal = newThresholdString.toIntOrNull() ?: 2
                                    
                                    viewModel.addItem(
                                        name = newName,
                                        sku = finalSku,
                                        initialStock = stockVal,
                                        category = newCategory.trim(),
                                        cost = costVal,
                                        lowStockThreshold = thresholdVal
                                    ) { id ->
                                        Toast.makeText(context, "Item registered successfully!", Toast.LENGTH_SHORT).show()
                                        // Reset fields
                                        newName = ""
                                        newStockString = "5"
                                        newCostString = "10.00"
                                        newThresholdString = "2"
                                        showManualRegisterForm = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("REGISTER PRODUCT", fontFamily = FontFamily.SansSerif, color = OnNeonCyan, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 5. Sleek Material 3 Search Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { 
                            Text(
                                "Search by item name...", 
                                fontFamily = FontFamily.SansSerif, 
                                color = GlassTextSecondary,
                                fontSize = 14.sp
                            ) 
                        },
                        leadingIcon = { 
                            Icon(
                                imageVector = Icons.Default.Search, 
                                contentDescription = "Search", 
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            ) 
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear, 
                                        contentDescription = "Clear", 
                                        tint = GlassTextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = DynamicCardSecondary,
                            unfocusedContainerColor = DynamicMenuBackground,
                            disabledContainerColor = DynamicMenuBackground,
                            focusedIndicatorColor = NeonCyan,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = GlassTextPrimary,
                            unfocusedTextColor = GlassTextPrimary,
                            cursorColor = NeonCyan
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .border(1.dp, NeonCyan.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                    )
                }
            }

            // 6. Dynamic Inventory Ledger List
            if (filteredLogs.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Info, contentDescription = "Empty Ledger", tint = GlassBorderColor, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No stock transactions found", fontFamily = FontFamily.SansSerif, color = GlassTextSecondary, fontSize = 12.sp)
                    }
                }
            } else {
                items(
                    items = filteredLogs,
                    key = { it.logId }
                ) { log ->
                    AuditLogRow(
                        log = log,
                        modifier = Modifier
                    )
                }
            }

            // Developer attribution footer
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
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
                        color = GlassBorderColor
                    )
                }
            }
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
}

@Composable
fun InventoryItemRow(
    item: InventoryItem,
    onAdjustStock: (Int, String) -> Unit,
    onDelete: () -> Unit,
    onUpdateThreshold: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedItemMenu by remember { mutableStateOf(false) }
    var showThresholdDialog by remember { mutableStateOf(false) }
    var inputThreshold by remember { mutableStateOf(item.lowStockThreshold.toString()) }
    
    // Warn if out of stock (falls below threshold, e.g., lowStockThreshold units)
    val isOut = item.currentStock == 0
    val isLow = !isOut && item.currentStock <= item.lowStockThreshold
    
    val badgeColor = when {
        isOut -> AccentRed
        isLow -> Color(0xFFFFB74D) // Warm Amber
        else -> AccentGreen
    }

    val badgeText = when {
        isOut -> "CRITICAL OUT"
        isLow -> "LOW STOCK"
        else -> "SECURE STOCK"
    }

    val cardBorderColor = when {
        isOut -> AccentRed.copy(alpha = 0.4f)
        isLow -> Color(0xFFFFB74D).copy(alpha = 0.4f)
        else -> null
    }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        borderColor = cardBorderColor
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Item details
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.name,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = GlassTextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(badgeColor.copy(alpha = 0.15f))
                            .border(0.5.dp, badgeColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "SKU: ${item.sku}",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 11.sp,
                        color = GlassTextSecondary
                    )
                    Text(
                        text = "Category: ${item.category}",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 11.sp,
                        color = GlassTextSecondary
                    )
                    // Added visual indicator of the alert threshold
                    Text(
                        text = "Alert: ≤${item.lowStockThreshold}",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 11.sp,
                        color = if (isLow) Color(0xFFFFB74D) else GlassTextSecondary
                    )
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Cost: ৳${String.format("%.2f", item.cost)}",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 11.sp,
                        color = GlassTextSecondary
                    )
                    Text(
                        text = "Value: ৳${String.format("%.2f", item.currentStock * item.cost)}",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 11.sp,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Real-time frictionless adjustments panel
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                // Red -
                IconButton(
                    onClick = { if (item.currentStock > 0) onAdjustStock(1, "OUT") },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0x16FF1744)),
                    modifier = Modifier
                        .size(34.dp)
                        .border(0.5.dp, AccentRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Reduce Stock",
                        tint = AccentRed,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Physical value
                Text(
                    text = item.currentStock.toString(),
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = GlassTextPrimary,
                    modifier = Modifier.widthIn(min = 24.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Green +
                IconButton(
                    onClick = { onAdjustStock(1, "IN") },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0x1600E676)),
                    modifier = Modifier
                        .size(34.dp)
                        .border(0.5.dp, AccentGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Stock",
                        tint = AccentGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(4.dp))

                // Options dialog dropdown
                Box {
                    IconButton(
                        onClick = { expandedItemMenu = true }
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Actions", tint = GlassTextSecondary)
                    }
                    
                    DropdownMenu(
                        expanded = expandedItemMenu,
                        onDismissRequest = { expandedItemMenu = false },
                        modifier = Modifier.background(DynamicMenuBackground)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Set Alert Limit", fontFamily = FontFamily.SansSerif, color = GlassTextPrimary) },
                            leadingIcon = { Icon(Icons.Default.Warning, contentDescription = "Threshold", tint = Color(0xFFFFB74D)) },
                            onClick = {
                                inputThreshold = item.lowStockThreshold.toString()
                                showThresholdDialog = true
                                expandedItemMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete SKU Entry", fontFamily = FontFamily.SansSerif, color = AccentRed) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AccentRed) },
                            onClick = {
                                onDelete()
                                expandedItemMenu = false
                            }
                        )
                    }
                }
            }
        }
    }

    if (showThresholdDialog) {
        AlertDialog(
            onDismissRequest = { showThresholdDialog = false },
            title = {
                Text(
                    text = "Custom Alert limit",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    color = GlassTextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Set custom low-stock threshold for ${item.name}. Alert is active when current stock is equal to or below this value.",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 12.sp,
                        color = GlassTextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassTextField(
                        value = inputThreshold,
                        onValueChange = { inputThreshold = it },
                        label = "Alert Threshold",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val thresh = inputThreshold.toIntOrNull()
                        if (thresh != null && thresh >= 0) {
                            onUpdateThreshold(thresh)
                            showThresholdDialog = false
                        }
                    }
                ) {
                    Text("SAVE", fontFamily = FontFamily.SansSerif, color = NeonCyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showThresholdDialog = false }) {
                    Text("CANCEL", fontFamily = FontFamily.SansSerif, color = GlassTextSecondary)
                }
            },
            containerColor = DynamicCardSecondary
        )
    }
}

@Composable
fun AuditLogRow(
    log: AuditLogEntry,
    modifier: Modifier = Modifier
) {
    val isOut = log.transactionType == "OUT"
    val color = if (isOut) AccentRed else AccentGreen
    val qtySign = if (isOut) "-" else "+"

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .border(0.5.dp, color.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = log.productName,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = GlassTextPrimary
                )
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(color.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$qtySign${log.quantityChanged}",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        fontSize = 14.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = log.details.ifBlank { "No details provided" },
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 12.sp,
                    color = GlassTextSecondary
                )
                
                Text(
                    text = "Val: ৳${String.format(Locale.US, "%.2f", log.stockValue)}",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 12.sp,
                    color = GlassTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            
            val df = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            Text(
                text = df.format(Date(log.timestamp)),
                fontFamily = FontFamily.SansSerif,
                fontSize = 10.sp,
                color = GlassTextSecondary.copy(alpha = 0.7f)
            )
        }
    }
}
