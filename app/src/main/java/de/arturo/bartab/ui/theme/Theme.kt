package de.arturo.bartab.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFF37D15C),
    secondary = Color(0xFFFFA726),
    tertiary = Color(0xFFEF5350),
    background = Color(0xFF1E1F23),
    surface = Color(0xFF26282D),
    onPrimary = Color(0xFF0D0F12),
    onBackground = Color(0xFFF4F5F7),
    onSurface = Color(0xFFF4F5F7),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF1E8E3E),
    secondary = Color(0xFFF59E0B),
    tertiary = Color(0xFFDC2626),
)

@Composable
fun BarTabTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content,
    )
}
