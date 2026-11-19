package com.bookmate.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

object Dimens {
    val SideMargin = 24.dp
    val ButtonHeight = 52.dp
    val FieldHeight = 52.dp
    val NavHeight = 72.dp
    val NavSideMargin = 16.dp
    val AiButtonSize = 64.dp
}

private val BookmateColorScheme = lightColorScheme(
    primary = Green,
    onPrimary = Cream,
    primaryContainer = NavContainer,
    onPrimaryContainer = DarkGreen,
    secondary = Olive,
    onSecondary = Cream,
    secondaryContainer = SecondaryContainerColor,
    onSecondaryContainer = DarkGreen,
    background = Cream,
    onBackground = TextPrimary,
    surface = CardSurface,
    onSurface = TextPrimary,
    surfaceVariant = SecondaryContainerColor,
    onSurfaceVariant = TextSecondary,
    outline = OutlineColor
)

private val BookmateShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(44.dp)
)

@Composable
fun BookmateTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BookmateColorScheme,
        typography = Typography,
        shapes = BookmateShapes,
        content = content
    )
}