package pe.com.zzynan.euzin.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = CobaltBlue,
    onPrimary = White,
    primaryContainer = SoftBlue,
    onPrimaryContainer = DarkBlue,
    secondary = DarkBlue,
    onSecondary = White,
    background = White,
    onBackground = DarkGray,
    surface = LightGray,
    onSurface = DarkGray,
    surfaceVariant = SoftBlue.copy(alpha = 0.2f)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF0070FF),
    onPrimary = White,
    primaryContainer = DarkBlue,
    onPrimaryContainer = White,
    secondary = SoftBlue,
    onSecondary = DarkGray,
    background = Color(0xFF0F141B),
    onBackground = White,
    surface = Color(0xFF1A212D),
    onSurface = White,
    surfaceVariant = Color(0xFF243044)
)

@Composable
fun EUZINTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors: ColorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
