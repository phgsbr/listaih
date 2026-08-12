package com.listaih.wear.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF5BE08F),
    onPrimary = Color(0xFF00371B),
    primaryContainer = Color(0xFF00713A),
    onPrimaryContainer = Color(0xFF9CFFC2),
    secondary = Color(0xFF4AD8F2),
    onSecondary = Color(0xFF00343E),
    secondaryContainer = Color(0xFF00505E),
    onSecondaryContainer = Color(0xFFB8EBFF),
    tertiary = Color(0xFFB79DFF),
    onTertiary = Color(0xFF2F1866),
    tertiaryContainer = Color(0xFF46307F),
    onTertiaryContainer = Color(0xFFE8DCFF),
    error = Color(0xFFFF6B6B),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0B1510),
    onBackground = Color(0xFFE9F2EA),
    surface = Color(0xFF0B1510),
    onSurface = Color(0xFFE9F2EA),
    surfaceVariant = Color(0xFF3A473D),
    onSurfaceVariant = Color(0xFFCBD8CC),
    outline = Color(0xFF94A196),
    outlineVariant = Color(0xFF3A473D),
    surfaceContainerLowest = Color(0xFF06100B),
    surfaceContainerLow = Color(0xFF0F1B15),
    surfaceContainer = Color(0xFF15221B),
    surfaceContainerHigh = Color(0xFF1F2E26),
    surfaceContainerHighest = Color(0xFF2A3B31)
)

@Composable
fun WearTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = WearTypography,
        content = content
    )
}