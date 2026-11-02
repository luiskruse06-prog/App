package de.luis.bulktracker.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Fallback-Palette für Android 9–11 (dort gibt es kein Dynamic Color): kräftiges Grün.
private val DarkFallback = darkColorScheme(
    primary = Color(0xFF7DDC8A),
    onPrimary = Color(0xFF00391A),
    primaryContainer = Color(0xFF005228),
    onPrimaryContainer = Color(0xFF98F9A4),
    secondary = Color(0xFFB7CCB6),
    tertiary = Color(0xFFA1CED8),
)

private val LightFallback = lightColorScheme(
    primary = Color(0xFF1B6D36),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF98F9A4),
    onPrimaryContainer = Color(0xFF00210B),
    secondary = Color(0xFF506350),
    tertiary = Color(0xFF39656D),
)

/** Folgt dem System-Dunkelmodus; Dynamic Color ab Android 12. */
@Composable
fun BulkTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkFallback
        else -> LightFallback
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
