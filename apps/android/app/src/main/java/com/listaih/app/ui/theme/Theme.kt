package com.listaih.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2E7D32),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB6F2C0),
    onPrimaryContainer = Color(0xFF00210B),
    secondary = Color(0xFF00696D),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF9CF1F1),
    onSecondaryContainer = Color(0xFF002020),
    tertiary = Color(0xFF0061A4),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFCFE5FF),
    onTertiaryContainer = Color(0xFF001D36),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFBFDF7),
    onBackground = Color(0xFF1A1C19),
    surface = Color(0xFFFBFDF7),
    onSurface = Color(0xFF1A1C19),
    surfaceVariant = Color(0xFFDDE5DB),
    onSurfaceVariant = Color(0xFF414942),
    outline = Color(0xFF717971),
    outlineVariant = Color(0xFFCBD3CB),
    surfaceBright = Color(0xFFFBFDF7),
    surfaceContainer = Color(0xFFEEF2EA),
    surfaceContainerLow = Color(0xFFF5F7F1),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFE9EDE5),
    surfaceContainerHighest = Color(0xFFE3E7DF),
    scrim = Color(0xFF000000)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF9CD9A4),
    onPrimary = Color(0xFF00391A),
    primaryContainer = Color(0xFF00642F),
    onPrimaryContainer = Color(0xFFB6F5C0),
    secondary = Color(0xFF70D4D5),
    onSecondary = Color(0xFF003739),
    secondaryContainer = Color(0xFF004F52),
    onSecondaryContainer = Color(0xFF9CF0F1),
    tertiary = Color(0xFF9DCCFF),
    onTertiary = Color(0xFF00325A),
    tertiaryContainer = Color(0xFF004A7C),
    onTertiaryContainer = Color(0xFFCFE5FF),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF111411),
    onBackground = Color(0xFFE2E4DC),
    surface = Color(0xFF111411),
    onSurface = Color(0xFFE2E4DC),
    surfaceVariant = Color(0xFF414942),
    onSurfaceVariant = Color(0xFFC1C9C0),
    outline = Color(0xFF8B938B),
    outlineVariant = Color(0xFF414942),
    surfaceBright = Color(0xFF373A35),
    surfaceContainer = Color(0xFF181B17),
    surfaceContainerLow = Color(0xFF111411),
    surfaceContainerLowest = Color(0xFF0C0F0C),
    surfaceContainerHigh = Color(0xFF222521),
    surfaceContainerHighest = Color(0xFF2C302B),
    scrim = Color(0xFF000000)
)

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun Theme(
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}