package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PlayverseDarkScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = Color(0xFF1A1100),
    primaryContainer = Color(0xFF3B2806),
    onPrimaryContainer = GoldLight,
    secondary = CyanAccent,
    onSecondary = Color(0xFF002029),
    secondaryContainer = Color(0xFF0D3B4C),
    onSecondaryContainer = CyanGlow,
    tertiary = AmethystPurple,
    background = RoyalNavyDeep,
    onBackground = Color(0xFFF8FAFC),
    surface = RoyalIndigoBg,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = RoyalBlueSurface,
    onSurfaceVariant = Color(0xFFCBD5E1),
    error = RubyDanger,
    outline = RoyalGlassBorder
)

private val PlayverseLightScheme = lightColorScheme(
    primary = Color(0xFFD97706),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFEF3C7),
    onPrimaryContainer = Color(0xFF78350F),
    secondary = Color(0xFF0284C7),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0C4A6E),
    tertiary = Color(0xFF6D28D9),
    background = LightRoyalBg,
    onBackground = LightRoyalText,
    surface = LightRoyalSurface,
    onSurface = LightRoyalText,
    surfaceVariant = Color(0xFFE0E7FF),
    onSurfaceVariant = Color(0xFF334155),
    error = RubyDanger,
    outline = Color(0xFF94A3B8)
)

@Composable
fun PlayverseTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) PlayverseDarkScheme else PlayverseLightScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
