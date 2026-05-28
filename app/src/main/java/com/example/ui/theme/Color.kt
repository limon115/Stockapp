// Architected by Khalid Hasan Limon
package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Black = Color(0xFF000000)

val GlassWhite: Color
    @Composable
    get() = if (LocalIsDarkTheme.current) Color(0x1BFFFFFF) else Color(0x1B000000)

val GlassWhiteSubtle: Color
    @Composable
    get() = if (LocalIsDarkTheme.current) Color(0x0DFFFFFF) else Color(0x0A000000)

val GlassBorderColor: Color
    @Composable
    get() = if (LocalIsDarkTheme.current) Color(0x33FFFFFF) else Color(0x1F0F172A)

val NeonCyan: Color
    @Composable
    get() = if (LocalIsDarkTheme.current) Color(0xFF00E5FF) else Color(0xFF0891B2)

val ElectricBlue: Color
    @Composable
    get() = if (LocalIsDarkTheme.current) Color(0xFF2979FF) else Color(0xFF2563EB)

val GlassTextPrimary: Color
    @Composable
    get() = if (LocalIsDarkTheme.current) Color(0xFFF5F5F5) else Color(0xFF0F172A)

val GlassTextSecondary: Color
    @Composable
    get() = if (LocalIsDarkTheme.current) Color(0xFF94A3B8) else Color(0xFF475569)

val AccentRed = Color(0xFFFF1744)
val AccentGreen = Color(0xFF00E676)

val OnNeonCyan: Color
    @Composable
    get() = if (LocalIsDarkTheme.current) Color.Black else Color.White

val DynamicCardBackground: Color
    @Composable
    get() = if (LocalIsDarkTheme.current) Color(0xFF111827) else Color(0xFFFFFFFF)

val DynamicCardSecondary: Color
    @Composable
    get() = if (LocalIsDarkTheme.current) Color(0xFF1E293B) else Color(0xFFE2E8F0)

val DynamicMenuBackground: Color
    @Composable
    get() = if (LocalIsDarkTheme.current) Color(0xFF0F172A) else Color(0xFFFFFFFF)


