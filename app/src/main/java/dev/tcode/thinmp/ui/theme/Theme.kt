package dev.tcode.thinmp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColorScheme(
    primary = LightGray,
    onPrimary = DarkGray,
    secondary = LightGray2,
    onSecondary = DarkGray2,
    background = Color.Black,
    onBackground = DarkGray
)

private val LightColorPalette = lightColorScheme(
    primary = DarkGray,
    onPrimary = LightGray,
    secondary = DarkGray2,
    onSecondary = LightGray2,
    background = Color.White,
    onBackground = LightGray
)

@Composable
fun ThinMPTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colorScheme = colors,
        typography = typography,
        shapes = shapes,
        content = content
    )
}