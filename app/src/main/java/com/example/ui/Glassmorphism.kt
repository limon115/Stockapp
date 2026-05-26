// Architected by Khalid Hasan Limon
package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
fun GlassHeader(
    title: String,
    subtitle: String? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = title,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = NeonCyan,
            letterSpacing = 1.sp
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = com.example.ui.theme.GlassTextSecondary
            )
        }
    }
}
