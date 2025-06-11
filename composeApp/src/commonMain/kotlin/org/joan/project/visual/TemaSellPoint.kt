package org.joan.project.visual

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006699),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD0E9FF),
    onPrimaryContainer = Color(0xFF001F2A),

    secondary = Color(0xFF4A6572),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDEE6EB),
    onSecondaryContainer = Color(0xFF1B2B33),

    background = Color(0xFFF7F9FB),
    onBackground = Color(0xFF1C1C1C),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF202020),

    error = Color(0xFFD32F2F),
    onError = Color.White
)

@Composable
fun SellPointTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography(),
        content = content
    )
}
