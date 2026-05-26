// Architected by Khalid Hasan Limon
package com.example.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AuditLogEntry
import com.example.data.InventoryItem
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoricalGraphScreen(
    items: List<InventoryItem>,
    logs: List<AuditLogEntry>
) {
    var selectedTimeframe by remember { mutableStateOf("7 Days") }
    var selectedItemIndex by remember { mutableIntStateOf(-1) } // -1 for global, or item index
    var chartType by remember { mutableStateOf("Line") } // "Line" or "Bar"
    
    val timeframes = listOf("7 Days", "30 Days", "All Time")
    
    // Choose selected dataset
    val activeItem = if (selectedItemIndex >= 0 && selectedItemIndex < items.size) items[selectedItemIndex] else null
    
    // Filter active logs based on selection
    val filteredLogs = remember(logs, activeItem, selectedTimeframe) {
        val now = System.currentTimeMillis()
        val limitMs = when (selectedTimeframe) {
            "7 Days" -> 7 * 24 * 60 * 60 * 1000L
            "30 Days" -> 30 * 24 * 60 * 60 * 1000L
            else -> Long.MAX_VALUE
        }
        
        logs.filter { log ->
            val matchItem = activeItem == null || log.itemId == activeItem.itemId
            val matchTime = (now - log.timestamp) <= limitMs
            matchItem && matchTime
        }.sortedBy { it.timestamp }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Dropdown Selector for items
        var expandedDropdown by remember { mutableStateOf(false) }
        
        GlassHeader(
            title = "Stock Velocity",
            subtitle = "Fluid tracking of historical transaction vectors and SKU trends"
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        // Timeframe selector row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selection dropdown label
            Box(
                modifier = Modifier
                    .weight(0.6f)
                    .glassmorphic(8.dp)
                    .clickable { expandedDropdown = true }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = activeItem?.name ?: "All Items (Global Logs)",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 13.sp,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold
                )
                
                DropdownMenu(
                    expanded = expandedDropdown,
                    onDismissRequest = { expandedDropdown = false },
                    modifier = Modifier.background(Color(0xFF0F172A))
                ) {
                    DropdownMenuItem(
                        text = { Text("All Items (Global)", fontFamily = FontFamily.SansSerif, color = Color.White) },
                        onClick = {
                            selectedItemIndex = -1
                            expandedDropdown = false
                        }
                    )
                    items.forEachIndexed { idx, item ->
                        DropdownMenuItem(
                            text = { Text("${item.name} (${item.sku})", fontFamily = FontFamily.SansSerif, color = Color.White) },
                            onClick = {
                                selectedItemIndex = idx
                                expandedDropdown = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))

            // Timeframe Segmented Control
            Row(
                modifier = Modifier
                    .weight(0.4f)
                    .glassmorphic(8.dp)
                    .padding(2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                timeframes.forEach { tf ->
                    val isSelected = selectedTimeframe == tf
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) Color(0x3300E5FF) else Color.Transparent)
                            .clickable { selectedTimeframe = tf }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tf.split(" ")[0],
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 11.sp,
                            color = if (isSelected) NeonCyan else GlassTextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Stats Highlight Box
        GlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            val totalIn = filteredLogs.filter { it.transactionType == "IN" }.sumOf { it.quantityChanged }
            val totalOut = filteredLogs.filter { it.transactionType == "OUT" }.sumOf { it.quantityChanged }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "RESTOCKED (IN)",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 10.sp,
                        color = GlassTextSecondary
                    )
                    Text(
                        text = "+$totalIn units",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 18.sp,
                        color = AccentGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(30.dp)
                        .background(GlassBorderColor)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "DEDUCTED (OUT)",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 10.sp,
                        color = GlassTextSecondary
                    )
                    Text(
                        text = "-$totalOut units",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 18.sp,
                        color = AccentRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chart Type Selector
        Row(
            modifier = Modifier.align(Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Chart Style: ",
                fontFamily = FontFamily.SansSerif,
                fontSize = 11.sp,
                color = GlassTextSecondary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Row(
                modifier = Modifier
                    .glassmorphic(6.dp)
                    .padding(2.dp)
            ) {
                listOf("Line", "Bar").forEach { type ->
                    val isSelected = chartType == type
                    Text(
                        text = type,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 11.sp,
                        color = if (isSelected) NeonCyan else GlassTextSecondary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) Color(0x22FFFFFF) else Color.Transparent)
                            .clickable { chartType = type }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Custom Paint Glassmorphic Canvas Chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .glassmorphic(16.dp)
                .padding(16.dp)
        ) {
            if (filteredLogs.isEmpty()) {
                // Return descriptive empty state
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No stock transactions recorded for this interval",
                        fontFamily = FontFamily.SansSerif,
                        color = GlassTextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Adjust item stock to trigger velocity graphs",
                        fontFamily = FontFamily.SansSerif,
                        color = Color(0x33FFFFFF),
                        fontSize = 11.sp
                    )
                }
            } else {
                // Generate chronological points representing stock levels over time
                val startingStock = activeItem?.currentStock ?: 100 // Estimate standard global
                val timelinePoints = remember(filteredLogs, startingStock) {
                    var current = startingStock
                    val points = mutableListOf<Pair<Long, Int>>()
                    
                    // Add current baseline
                    points.add(Pair(System.currentTimeMillis(), current))
                    
                    // Traverse logs backward to reconstruct historical levels
                    for (log in filteredLogs.reversed()) {
                        val change = log.quantityChanged
                        if (log.transactionType == "IN") {
                            current -= change // Prior to addition, stock was lower
                        } else {
                            current += change // Prior to deduction, stock was higher
                        }
                        points.add(Pair(log.timestamp, current))
                    }
                    points.reverse()
                    points
                }

                if (chartType == "Line") {
                    LineChartCanvas(
                        points = timelinePoints,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    BarChartCanvas(
                        logs = filteredLogs,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Developed by Khalid Hasan Limon",
            fontFamily = FontFamily.SansSerif,
            fontSize = 10.sp,
            color = Color(0x44FFFFFF),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun LineChartCanvas(
    points: List<Pair<Long, Int>>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val animatedProgress = animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 800),
        label = "line_chart_reveal"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        
        val paddingLeft = 45.dp.toPx()
        val paddingBottom = 25.dp.toPx()
        val paddingTop = 15.dp.toPx()
        val paddingRight = 15.dp.toPx()
        
        val chartWidth = width - paddingLeft - paddingRight
        val chartHeight = height - paddingTop - paddingBottom
        
        // Find boundaries
        val minX = points.minOf { it.first }
        val maxX = points.maxOf { it.first }
        val minY = 0f
        val maxY = (points.maxOf { it.second }.toFloat() * 1.25f).coerceAtLeast(10f)
        
        val timeSpan = (maxX - minX).coerceAtLeast(1L)
        val valueSpan = maxY - minY
        
        // Draw grid lines
        val axisPaint = Stroke(width = 1.dp.toPx())
        val gridPaint = Stroke(
            width = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )
        
        // 4 grid rows
        for (i in 0..4) {
            val ratio = i / 4f
            val yVal = minY + ratio * valueSpan
            val yPx = height - paddingBottom - (ratio * chartHeight)
            
            // Draw horizontal coordinate line
            drawLine(
                color = Color(0x33FFFFFF),
                start = Offset(paddingLeft, yPx),
                end = Offset(width - paddingRight, yPx),
                strokeWidth = if (i == 0) 1.5.dp.toPx() else 0.5.dp.toPx()
            )
            
            // Render label
            val textLayoutResult = textMeasurer.measure(
                text = AnnotatedString(yVal.toInt().toString()),
                style = TextStyle(
                    color = GlassTextSecondary,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.SansSerif
                )
            )
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(paddingLeft - textLayoutResult.size.width - 6.dp.toPx(), yPx - textLayoutResult.size.height / 2f)
            )
        }

        // Project coordinate points to Canvas space
        val pixelPoints = points.map { pt ->
            val xRatio = if (timeSpan == 0L) 0.5f else (pt.first - minX).toFloat() / timeSpan
            val yRatio = (pt.second - minY) / valueSpan
            
            val xPx = paddingLeft + xRatio * chartWidth
            val yPx = height - paddingBottom - yRatio * chartHeight
            Offset(xPx, yPx)
        }
        
        // Draw continuous line path
        if (pixelPoints.size > 1) {
            val path = Path().apply {
                moveTo(pixelPoints[0].x, pixelPoints[0].y)
                for (i in 1 until pixelPoints.size) {
                    val currentPoint = pixelPoints[i]
                    val prevPoint = pixelPoints[i-1]
                    // Bezier smoothing cubic control calculation
                    val controlX = (prevPoint.x + currentPoint.x) / 2
                    cubicTo(controlX, prevPoint.y, controlX, currentPoint.y, currentPoint.x, currentPoint.y)
                }
            }
            
            // Draw glowing translucent shadow under the curve
            val areaPath = Path().apply {
                addPath(path)
                lineTo(pixelPoints.last().x, height - paddingBottom)
                lineTo(pixelPoints.first().x, height - paddingBottom)
                close()
            }
            
            drawPath(
                path = areaPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x3B00E5FF),
                        Color.Transparent
                    )
                )
            )

            // Draw line curve
            drawPath(
                path = path,
                color = NeonCyan,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )
            
            // Draw points
            pixelPoints.forEachIndexed { index, offset ->
                if (index == 0 || index == pixelPoints.size - 1 || points[index].second == points.maxOf { it.second }) {
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = offset
                    )
                    
                    drawCircle(
                        color = ElectricBlue,
                        radius = 2.dp.toPx(),
                        center = offset
                    )
                }
            }
        }
        
        // Date labeling underneath
        val dateSdf = SimpleDateFormat("MM-dd", Locale.getDefault())
        val firstDateStr = dateSdf.format(Date(minX))
        val lastDateStr = dateSdf.format(Date(maxX))
        
        val dLabel1 = textMeasurer.measure(
            text = AnnotatedString(firstDateStr),
            style = TextStyle(color = GlassTextSecondary, fontSize = 9.sp, fontFamily = FontFamily.SansSerif)
        )
        drawText(dLabel1, topLeft = Offset(paddingLeft, height - paddingBottom + 4.dp.toPx()))
        
        val dLabel2 = textMeasurer.measure(
            text = AnnotatedString(lastDateStr),
            style = TextStyle(color = GlassTextSecondary, fontSize = 9.sp, fontFamily = FontFamily.SansSerif)
        )
        drawText(dLabel2, topLeft = Offset(width - paddingRight - dLabel2.size.width, height - paddingBottom + 4.dp.toPx()))
    }
}

@Composable
fun BarChartCanvas(
    logs: List<AuditLogEntry>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        
        val paddingLeft = 45.dp.toPx()
        val paddingBottom = 25.dp.toPx()
        val paddingTop = 15.dp.toPx()
        val paddingRight = 15.dp.toPx()
        
        val chartWidth = width - paddingLeft - paddingRight
        val chartHeight = height - paddingTop - paddingBottom
        
        // Render 4 lines boundary
        val maxY = (logs.maxOf { it.quantityChanged }.toFloat() * 1.25f).coerceAtLeast(10f)
        
        // Grid lines
        for (i in 0..4) {
            val ratio = i / 4f
            val yPx = height - paddingBottom - (ratio * chartHeight)
            drawLine(
                color = Color(0x22FFFFFF),
                start = Offset(paddingLeft, yPx),
                end = Offset(width - paddingRight, yPx),
                strokeWidth = 0.5.dp.toPx()
            )
            val textLayoutResult = textMeasurer.measure(
                text = AnnotatedString((ratio * maxY).toInt().toString()),
                style = TextStyle(color = GlassTextSecondary, fontSize = 9.sp, fontFamily = FontFamily.SansSerif)
            )
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(paddingLeft - textLayoutResult.size.width - 6.dp.toPx(), yPx - textLayoutResult.size.height / 2f)
            )
        }

        // Draw Bars
        val barCount = logs.size
        val totalSpacing = chartWidth * 0.3f
        val barWidth = (chartWidth - totalSpacing) / barCount.coerceAtLeast(1)
        val spacing = totalSpacing / (barCount + 1).coerceAtLeast(1)

        logs.forEachIndexed { index, log ->
            val ratio = log.quantityChanged.toFloat() / maxY
            val barHeight = ratio * chartHeight
            val color = if (log.transactionType == "IN") AccentGreen else AccentRed
            
            val xPx = paddingLeft + spacing + index * (barWidth + spacing)
            val yPx = height - paddingBottom - barHeight
            
            drawRect(
                color = color.copy(alpha = 0.85f),
                topLeft = Offset(xPx, yPx),
                size = Size(barWidth, barHeight)
            )
            
            // Draw a subtle highlighted top line on the bar
            drawLine(
                color = Color.White,
                start = Offset(xPx, yPx),
                end = Offset(xPx + barWidth, yPx),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw time labels for the first and last bars
        val dateSdf = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
        val firstLabel = dateSdf.format(Date(logs.first().timestamp))
        val lastLabel = dateSdf.format(Date(logs.last().timestamp))
        
        val dl1 = textMeasurer.measure(
            text = AnnotatedString(firstLabel),
            style = TextStyle(color = GlassTextSecondary, fontSize = 8.sp, fontFamily = FontFamily.SansSerif)
        )
        drawText(dl1, topLeft = Offset(paddingLeft, height - paddingBottom + 4.dp.toPx()))

        val dl2 = textMeasurer.measure(
            text = AnnotatedString(lastLabel),
            style = TextStyle(color = GlassTextSecondary, fontSize = 8.sp, fontFamily = FontFamily.SansSerif)
        )
        drawText(dl2, topLeft = Offset(width - paddingRight - dl2.size.width, height - paddingBottom + 4.dp.toPx()))
    }
}
