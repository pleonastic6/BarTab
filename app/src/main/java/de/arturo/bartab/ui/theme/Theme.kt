package de.arturo.bartab.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFF41D66A),
    onPrimary = Color(0xFF08110B),
    secondary = Color(0xFFFFB74D),
    onSecondary = Color(0xFF201100),
    tertiary = Color(0xFFFF6B6B),
    background = Color(0xFF17191C),
    onBackground = Color(0xFFF3F5F7),
    surface = Color(0xFF22262B),
    onSurface = Color(0xFFF3F5F7),
    surfaceVariant = Color(0xFF2B3137),
    onSurfaceVariant = Color(0xFFC8D0D8),
    outline = Color(0xFF4A525B),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF167C32),
    onPrimary = Color.White,
    secondary = Color(0xFFE38900),
    tertiary = Color(0xFFC62828),
    background = Color(0xFFF5F7F8),
    onBackground = Color(0xFF17191C),
    surface = Color.White,
    onSurface = Color(0xFF17191C),
    surfaceVariant = Color(0xFFE6EAEE),
    onSurfaceVariant = Color(0xFF47515B),
    outline = Color(0xFFB0B8C0),
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
