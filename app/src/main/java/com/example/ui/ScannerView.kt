// Architected by Khalid Hasan Limon
package com.example.ui

import android.Manifest
import kotlin.OptIn
import android.content.Context
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.data.InventoryItem
import com.example.ui.theme.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScannerViewScreen(
    viewModel: InventoryViewModel,
    onNavigateToInventory: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Permission Management
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    
    var scannedSku by remember { mutableStateOf("") }
    var activeScannedItem by remember { mutableStateOf<InventoryItem?>(null) }
    var showScanDetailsDialog by remember { mutableStateOf(false) }
    
    // Creation fields for new SKU
    var showCreateSkuDialog by remember { mutableStateOf(false) }
    var newSkuName by remember { mutableStateOf("") }
    var newSkuCategory by remember { mutableStateOf("Electronics") }
    var newSkuStock by remember { mutableStateOf("10") }
    var newSkuCost by remember { mutableStateOf("25.00") }
    var newSkuThreshold by remember { mutableStateOf("2") }

    // Quick quantity manipulation
    var adjustQtyString by remember { mutableStateOf("1") }

    // Handle barcode matches
    fun handleBarcodeScanned(barcode: String) {
        val trimmed = barcode.trim()
        if (trimmed.isEmpty() || trimmed == scannedSku) return
        scannedSku = trimmed
        
        viewModel.findItemBySku(trimmed) { item ->
            if (item != null) {
                activeScannedItem = item
                showScanDetailsDialog = true
                showCreateSkuDialog = false
            } else {
                newSkuName = ""
                newSkuCost = "15.00"
                newSkuStock = "5"
                newSkuCategory = "General"
                newSkuThreshold = "2"
                showScanDetailsDialog = false
                showCreateSkuDialog = true
            }
        }
    }

    Scaffold(
        containerColor = Black
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            GlassHeader(
                title = "Frictionless Scanner",
                subtitle = "Subsecond offline ML Kit resolution mapping camera inputs to Room"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action banner checking permission
            if (!cameraPermissionState.status.isGranted) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Text(
                        text = "Camera access is highly recommended for laser scanner resolve.",
                        fontFamily = FontFamily.SansSerif,
                        color = GlassTextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { cameraPermissionState.launchPermissionRequest() },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    ) {
                        Text("Grant Camera Permission", color = Black, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Real Camera Feed Frame inside beautiful rounded glass
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .glassmorphic(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (cameraPermissionState.status.isGranted) {
                    CameraPreviewContainer(
                        context = context,
                        onBarcodeDetected = { code ->
                            handleBarcodeScanned(code)
                        }
                    )
                    
                    // Transparent scanning viewfinder box
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .border(2.dp, NeonCyan, RoundedCornerShape(12.dp))
                            .background(Color(0x1100E5FF))
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VideocamOff,
                            contentDescription = "Camera Disabled",
                            tint = GlassTextSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Live preview unavailable",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 12.sp,
                            color = GlassTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Enterprise Manual Simulator Section (MANDATORY fallback to allow testing scanned results inside browser emulator)
            Text(
                text = "EMULATOR TEST BARCODE PANEL",
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = ElectricBlue
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                var manualInputSku by remember { mutableStateOf("") }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlassTextField(
                        value = manualInputSku,
                        onValueChange = { manualInputSku = it },
                        label = "Scan SKU Simulator",
                        placeholder = "e.g. SKU-1002, 978054",
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (manualInputSku.isNotBlank()) {
                                handleBarcodeScanned(manualInputSku)
                            } else {
                                Toast.makeText(context, "Insert simulated SKU code", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = NeonCyan)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Simulate Scan", tint = Black)
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Demo scan templates
                Text(
                    text = "Quick Mock Templates:",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 11.sp,
                    color = GlassTextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("SKU-PRO-MAX", "SKU-GLASS-X1", "SKU-LASER-9").forEach { code ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x1BFFFFFF))
                                .clickable { handleBarcodeScanned(code) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = code,
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 10.sp,
                                color = NeonCyan
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            if (scannedSku.isNotBlank()) {
                Text(
                    text = "Last Detected SKU code: $scannedSku",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 12.sp,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }

        // Dialogs for scanner flow
        // 1. Stock Adjustments Dialog (Restock / Deduct)
        if (showScanDetailsDialog && activeScannedItem != null) {
            val item = activeScannedItem!!
            AlertDialog(
                onDismissRequest = { showScanDetailsDialog = false; scannedSku = "" },
                containerColor = Color(0xFF0F172A),
                title = {
                    Text(
                        text = "SKU MATCH: ${item.name}",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "SKU: ${item.sku}",
                            fontFamily = FontFamily.SansSerif,
                            color = GlassTextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Category: ${item.category}",
                            fontFamily = FontFamily.SansSerif,
                            color = GlassTextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Standard Cost: ৳${String.format("%.2f", item.cost)}",
                            fontFamily = FontFamily.SansSerif,
                            color = GlassTextSecondary,
                            fontSize = 12.sp
                        )
                        
                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = GlassBorderColor)
                        
                        Text(
                            text = "Current Stock Level: ${item.currentStock} units",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        GlassTextField(
                            value = adjustQtyString,
                            onValueChange = { adjustQtyString = it },
                            label = "Stock Transaction Value",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                },
                confirmButton = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Deduct Stock
                        Button(
                            onClick = {
                                val units = adjustQtyString.toIntOrNull() ?: 1
                                viewModel.adjustStock(item.itemId, units, "OUT")
                                Toast.makeText(context, "Deducted $units units of ${item.name}", Toast.LENGTH_SHORT).show()
                                showScanDetailsDialog = false
                                scannedSku = ""
                                onNavigateToInventory()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Deduct (-)", color = Color.White, fontFamily = FontFamily.SansSerif)
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))

                        // Restock Item
                        Button(
                            onClick = {
                                val units = adjustQtyString.toIntOrNull() ?: 1
                                viewModel.adjustStock(item.itemId, units, "IN")
                                Toast.makeText(context, "Restocked $units units of ${item.name}", Toast.LENGTH_SHORT).show()
                                showScanDetailsDialog = false
                                scannedSku = ""
                                onNavigateToInventory()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Restock (+)", color = Black, fontFamily = FontFamily.SansSerif)
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showScanDetailsDialog = false; scannedSku = "" }) {
                        Text("Cancel", fontFamily = FontFamily.SansSerif, color = GlassTextSecondary)
                    }
                }
            )
        }

        // 2. Add New SKU Dialog (Manual Override Form)
        if (showCreateSkuDialog) {
            AlertDialog(
                onDismissRequest = { showCreateSkuDialog = false; scannedSku = "" },
                containerColor = Color(0xFF0F172A),
                title = {
                    Text(
                        text = "NEW SKU DETECTED",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlue
                    )
                },
                text = {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Text(
                                text = "Code: $scannedSku",
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan,
                                fontSize = 13.sp
                            )
                        }
                        item {
                            GlassTextField(
                                value = newSkuName,
                                onValueChange = { newSkuName = it },
                                label = "Item Name",
                                placeholder = "e.g. Liquid Glass Panel"
                            )
                        }
                        item {
                            GlassTextField(
                                value = newSkuCategory,
                                onValueChange = { newSkuCategory = it },
                                label = "Category",
                                placeholder = "e.g. Raw Materials"
                            )
                        }
                        item {
                            Row {
                                GlassTextField(
                                    value = newSkuStock,
                                    onValueChange = { newSkuStock = it },
                                    label = "Initial Stock",
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                GlassTextField(
                                    value = newSkuCost,
                                    onValueChange = { newSkuCost = it },
                                    label = "Cost (TK)",
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        item {
                            GlassTextField(
                                value = newSkuThreshold,
                                onValueChange = { newSkuThreshold = it },
                                label = "Low-Stock Alert Threshold",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newSkuName.isBlank()) {
                                Toast.makeText(context, "Enter a valid Item Name", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val stock = newSkuStock.toIntOrNull() ?: 0
                            val cost = newSkuCost.toDoubleOrNull() ?: 10.00
                            val threshold = newSkuThreshold.toIntOrNull() ?: 2
                            
                            viewModel.addItem(
                                name = newSkuName,
                                sku = scannedSku,
                                initialStock = stock,
                                category = newSkuCategory,
                                cost = cost,
                                lowStockThreshold = threshold
                            ) {
                                Toast.makeText(context, "Added new SKU: $newSkuName", Toast.LENGTH_SHORT).show()
                                showCreateSkuDialog = false
                                scannedSku = ""
                                onNavigateToInventory()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    ) {
                        Text("Register Item", color = Black, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateSkuDialog = false; scannedSku = "" }) {
                        Text("Cancel", fontFamily = FontFamily.SansSerif, color = GlassTextSecondary)
                    }
                }
            )
        }
    }
}

@Composable
fun CameraPreviewContainer(
    context: Context,
    onBarcodeDetected: (String) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderEmpty = remember { mutableStateOf<ProcessCameraProvider?>(null) }
    
    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                cameraProviderEmpty.value = cameraProvider
                
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                
                imageAnalysis.setAnalyzer(
                    Executors.newSingleThreadExecutor(),
                    BarcodeAnalyzer { barcode ->
                        // Return discovered barcode on dynamic UI thread
                        previewView.post {
                            onBarcodeDetected(barcode)
                        }
                    }
                )
                
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                } catch (exc: Exception) {
                    exc.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(ctx))
            
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
    
    // Unbind on dispose
    DisposableEffect(Unit) {
        onDispose {
            try {
                cameraProviderEmpty.value?.unbindAll()
            } catch (e: Exception) {
                // Safely ignored
            }
        }
    }
}
