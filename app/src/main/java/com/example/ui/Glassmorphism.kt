// Architected by Khalid Hasan Limon
package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GlassBorderColor
import com.example.ui.theme.GlassWhite
import com.example.ui.theme.GlassWhiteSubtle
import com.example.ui.theme.NeonCyan

fun Modifier.glassmorphic(
    cornerRadius: Dp = 16.dp,
    borderWidth: Dp = 1.dp,
    borderColor: Color? = null
): Modifier = this
    .clip(RoundedCornerShape(cornerRadius))
    .background(
        Brush.verticalGradient(
            colors = listOf(
                GlassWhite,
                GlassWhiteSubtle
            )
        )
    )
    .border(
        width = borderWidth,
        brush = if (borderColor != null) {
            androidx.compose.ui.graphics.SolidColor(borderColor)
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    GlassBorderColor,
                    Color(0x0AFFFFFF)
                )
            )
        },
        shape = RoundedCornerShape(cornerRadius)
    )

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    borderWidth: Dp = 1.dp,
    borderColor: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .glassmorphic(cornerRadius, borderWidth, borderColor)
            .padding(16.dp),
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent),
        label = {
            Text(
                text = label,
                fontFamily = FontFamily.Serif,
                color = com.example.ui.theme.GlassTextSecondary,
                fontSize = 13.sp
            )
        },
        placeholder = {
            Text(
                text = placeholder,
                fontFamily = FontFamily.Serif,
                color = Color(0x66FFFFFF),
                fontSize = 14.sp
            )
        },
        keyboardOptions = keyboardOptions,
        singleLine = singleLine,
        textStyle = LocalTextStyle.current.copy(
            fontFamily = FontFamily.Serif,
            color = com.example.ui.theme.GlassTextPrimary,
            fontSize = 15.sp
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = GlassBorderColor,
            focusedContainerColor = Color(0x1BFFFFFF),
            unfocusedContainerColor = Color(0x05FFFFFF),
            cursorColor = NeonCyan,
            errorContainerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun GlassLogLogo(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer aesthetic neon glow back-canvas
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val scaleR = size.toPx()
            drawCircle(
                color = NeonCyan.copy(alpha = 0.18f),
                radius = scaleR * 0.45f,
                center = center
            )
        }
        
        // Fluid glassy isometric shard body
        Box(
            modifier = Modifier
                .fillMaxSize(0.85f)
                .glassmorphic(
                    cornerRadius = (size.value * 0.25f).dp,
                    borderWidth = 1.dp,
                    borderColor = NeonCyan.copy(alpha = 0.4f)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Isometric design layers and abstract glowing monogram elements
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.toPx()
                
                // Draw a beautiful geometric isometric box representing the Ledger / Stock volume
                val cubePath = Path().apply {
                    // Start rendering isogrid structure lines
                    moveTo(w * 0.15f, w * 0.35f)
                    lineTo(w * 0.5f, w * 0.15f)
                    lineTo(w * 0.85f, w * 0.35f)
                    lineTo(w * 0.85f, w * 0.65f)
                    lineTo(w * 0.5f, w * 0.85f)
                    lineTo(w * 0.15f, w * 0.65f)
                    close()
                }
                
                drawPath(
                    path = cubePath,
                    color = Color.White.copy(alpha = 0.15f),
                    style = Stroke(width = 1.dp.toPx())
                )
                
                // Monogram paths representing "G" style light beam
                val beamG = Path().apply {
                    moveTo(w * 0.85f, w * 0.35f)
                    lineTo(w * 0.5f, w * 0.55f)
                    lineTo(w * 0.5f, w * 0.85f)
                    
                    // Connected lower structure base
                    moveTo(w * 0.15f, w * 0.65f)
                    lineTo(w * 0.5f, w * 0.55f)
                }
                
                drawPath(
                    path = beamG,
                    color = Color.White.copy(alpha = 0.25f),
                    style = Stroke(width = 1.dp.toPx())
                )
                
                // Intense glowing lasers (Monogram GL)
                val laserPath = Path().apply {
                    moveTo(w * 0.5f, w * 0.25f)
                    lineTo(w * 0.32f, w * 0.35f)
                    lineTo(w * 0.32f, w * 0.61f)
                    lineTo(w * 0.5f, w * 0.72f)
                    lineTo(w * 0.68f, w * 0.61f)
                    lineTo(w * 0.68f, w * 0.52f)
                    lineTo(w * 0.5f, w * 0.42f)
                }
                
                drawPath(
                    path = laserPath,
                    color = NeonCyan,
                    style = Stroke(
                        width = 1.8.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // High-point specular glint
                drawCircle(
                    color = Color.White,
                    radius = 2.dp.toPx(),
                    center = Offset(w * 0.5f, w * 0.25f)
                )
            }
        }
    }
}

@Composable
fun GlassHeader(
    title: String,
    subtitle: String? = null
) {
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
                text = title,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = NeonCyan,
                letterSpacing = 1.sp
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    color = com.example.ui.theme.GlassTextSecondary
                )
            }
        }
    }
}
