// Architected by Khalid Hasan Limon
package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GlassColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Black,
    secondary = ElectricBlue,
    onSecondary = Color.White,
    background = Black,
    onBackground = GlassTextPrimary,
    surface = Color(0x1BFFFFFF),
    onSurface = GlassTextPrimary,
    error = AccentRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GlassColorScheme,
        typography = Typography,
        content = content
    )
}
