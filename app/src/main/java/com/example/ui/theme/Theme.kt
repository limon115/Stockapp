// Architected by Khalid Hasan Limon
package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalIsDarkTheme = staticCompositionLocalOf { false }

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0891B2), // Rich Cyan/Teal contrast
    onPrimary = Color.White,
    secondary = Color(0xFF2563EB), // Rich Electric Blue
    onSecondary = Color.White,
    background = Color(0xFFD2F7FF), // Luminous, soft icy slate white
    onBackground = Color(0xFF0F172A), // Crisp slate
    surface = Color(0xE6FFFFFF), // Translucent clean white glass card list item
    onSurface = Color(0xFF0F172A),
    error = Color(0xFFDC2626),
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF00E5FF), // Neon Cyan accent
    onPrimary = Color(0xFF000000),
    secondary = Color(0xFF2979FF), // Electric Blue
    onSecondary = Color.White,
    background = Color(0xFF071424), // Skyblueish dark blend canvas
    onBackground = Color(0xFFF5F5F5),
    surface = Color(0xFF111827), // Material Dark Surface
    onSurface = Color(0xFFF5F5F5),
    error = Color(0xFFFF1744),
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    useDarkTheme: Boolean = false, // Set default UI to LIGHT mode as requested!
    content: @Composable () -> Unit
) {
    val colorScheme = if (useDarkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(
        LocalIsDarkTheme provides useDarkTheme
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

