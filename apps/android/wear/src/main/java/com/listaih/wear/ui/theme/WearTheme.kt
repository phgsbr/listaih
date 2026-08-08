package com.listaih.wear.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006B3C),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFA5F5C9),
    onPrimaryContainer = Color(0xFF002111),
    secondary = Color(0xFF00696D),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF99F0F2),
    onSecondaryContainer = Color(0xFF002020),
    tertiary = Color(0xFF0061A4),
    error = Color(0xFFBA1A1A),
    background = Color(0xFF1A1C19),
    onBackground = Color(0xFFE3E3E0),
    surface = Color(0xFF1A1C19),
    onSurface = Color(0xFFE3E3E0),
    surfaceVariant = Color(0xFF3F4239),
    outline = Color(0xFF8B8F84),
    outlineVariant = Color(0xFF3F4239),
    shadow = Color(0x80000000)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF7DD9A4),
    onPrimary = Color(0xFF003D22),
    primaryContainer = Color(0xFF00502A),
    onPrimaryContainer = Color(0xFFA5F5C9),
    secondary = Color(0xFF4FD8DC),
    onSecondary = Color(0xFF003739),
    secondaryContainer = Color(0xFF004F52),
    onSecondaryContainer = Color(0xFF99F0F2),
    tertiary = Color(0xFF6C9FFF),
    error = Color(0xFFFFB4AB),
    background = Color(0xFF1A1C19),
    onBackground = Color(0xFFE3E3E0),
    surface = Color(0xFF1A1C19),
    onSurface = Color(0xFFE3E3E0),
    surfaceVariant = Color(0xFF3F4239),
    outline = Color(0xFF8B8F84),
    outlineVariant = Color(0xFF3F4239),
    shadow = Color(0x80000000)
)

@Composable
fun WearTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = WearTypography,
        content = content
    )
}