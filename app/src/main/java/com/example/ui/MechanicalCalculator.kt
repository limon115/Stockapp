package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonCyan

@Composable
fun SkeuomorphicCalculator() {
    var display by remember { mutableStateOf("0") }
    var operand by remember { mutableStateOf(0.0) }
    var operator by remember { mutableStateOf("") }
    var startNew by remember { mutableStateOf(true) }

    val handleInput = { input: String ->
        when (input) {
            "C" -> {
                display = "0"
                operand = 0.0
                operator = ""
                startNew = true
            }
            "+", "-", "*", "/" -> {
                operand = display.toDoubleOrNull() ?: 0.0
                operator = input
                startNew = true
            }
            "=" -> {
                val current = display.toDoubleOrNull() ?: 0.0
                val result = when (operator) {
                    "+" -> operand + current
                    "-" -> operand - current
                    "*" -> operand * current
                    "/" -> if (current != 0.0) operand / current else Double.NaN
                    else -> current
                }
                display = if (result % 1.0 == 0.0) result.toInt().toString() else result.toString()
                operator = ""
                startNew = true
            }
            else -> {
                if (startNew) {
                    display = input
                    startNew = false
                } else {
                    if (input == "." && display.contains(".")) {
                        // ignore
                    } else {
                        display += input
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF2B2B2B), Color(0xFF1E1E1E))
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .border(2.dp, Color(0xFF3B3B3B), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Display screen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF9EAC90), Color(0xFFB0BFA2))
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                .border(2.dp, Color(0xFF1A1A1A), RoundedCornerShape(8.dp))
                .padding(12.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Text(
                text = display,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                color = Color(0xFF1C2114),
                maxLines = 1
            )
            // Inner shadow representation
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0x33000000), Color.Transparent),
                            start = Offset(0f, 0f),
                            end = Offset(0f, 40f)
                        )
                    )
            )
        }

        val buttons = listOf(
            listOf("7", "8", "9", "/"),
            listOf("4", "5", "6", "*"),
            listOf("1", "2", "3", "-"),
            listOf("C", "0", "=", "+")
        )

        buttons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { btn ->
                    MechanicalButton(
                        text = btn,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        onClick = { handleInput(btn) },
                        isAccent = btn == "=" || btn == "C",
                        isOperator = btn == "+" || btn == "-" || btn == "*" || btn == "/"
                    )
                }
            }
        }
    }
}

@Composable
fun MechanicalButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    isAccent: Boolean = false,
    isOperator: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val baseColor = when {
        isAccent -> Color(0xFFD64A38) // Red mechanical key
        isOperator -> Color(0xFF424242) // Darker gray for ops
        else -> Color(0xFFE0E0E0) // Beige/Off-white for numbers
    }

    val topColor = when {
        isAccent -> Color(0xFFF16553)
        isOperator -> Color(0xFF5A5A5A)
        else -> Color(0xFFF2F2F2)
    }

    val bottomColor = when {
        isAccent -> Color(0xFFA13324)
        isOperator -> Color(0xFF272727)
        else -> Color(0xFFB0B0B0)
    }

    val textColor = when {
        isAccent || isOperator -> Color.White
        else -> Color(0xFF222222)
    }
    
    val paddingY = if (isPressed) 2.dp else 6.dp

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF111111)) // Deep shadow under the key
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Key cap chassis
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (isPressed) 0.dp else 4.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(topColor, bottomColor)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                .border(1.dp, Color(0x33ffffff), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Key top surface
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp)
                    .padding(top = 4.dp, bottom = 10.dp)
                    .background(
                        baseColor,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .border(1.dp, Color(0x33000000), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    color = textColor,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
