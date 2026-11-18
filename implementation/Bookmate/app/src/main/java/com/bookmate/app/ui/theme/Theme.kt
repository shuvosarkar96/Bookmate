package com.bookmate.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val BookmateColorScheme = lightColorScheme(
    primary = Green,
    onPrimary = Cream,
    primaryContainer = Sand,
    onPrimaryContainer = DarkGreen,
    secondary = Olive,
    onSecondary = Cream,
    background = Cream,
    onBackground = DarkGreen,
    surface = Cream,
    onSurface = DarkGreen,
    surfaceVariant = Sand,
    onSurfaceVariant = Green,
    outline = Moss
)

@Composable
fun BookmateTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BookmateColorScheme,
        typography = Typography,
        content = content
    )
}