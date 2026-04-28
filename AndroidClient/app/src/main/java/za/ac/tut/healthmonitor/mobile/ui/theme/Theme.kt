package za.ac.tut.healthmonitor.mobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = AccentBlue,
    secondary = AccentCoral
)

private val DarkColors = darkColorScheme(
    primary = AccentBlue,
    secondary = AccentCoral
)

@Composable
fun HealthMonitorTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}
