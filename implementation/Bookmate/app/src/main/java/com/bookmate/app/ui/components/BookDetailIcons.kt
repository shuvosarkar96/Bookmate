package com.bookmate.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.bookmate.app.ui.theme.Green
import com.bookmate.app.ui.theme.TextPrimary

@Composable
fun BackChevronIcon(modifier: Modifier = Modifier, color: Color = TextPrimary) {
    Canvas(modifier = modifier.size(24.dp)) {
        val s = size.width / 24f
        val path = Path().apply {
            moveTo(15f * s, 5f * s)
            lineTo(7f * s, 12f * s)
            lineTo(15f * s, 19f * s)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.5f * s, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun BookmarkIcon(
    filled: Boolean,
    modifier: Modifier = Modifier,
    color: Color = TextPrimary,
    fillColor: Color = Green
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val s = size.width / 24f
        val path = Path().apply {
            moveTo(5f * s, 3f * s)
            lineTo(19f * s, 3f * s)
            lineTo(19f * s, 22f * s)
            lineTo(12f * s, 16.5f * s)
            lineTo(5f * s, 22f * s)
            close()
        }
        if (filled) {
            drawPath(path = path, color = fillColor)
        }
        drawPath(
            path = path,
            color = if (filled) fillColor else color,
            style = Stroke(width = 2f * s, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun LockIcon(modifier: Modifier = Modifier, color: Color = Green) {
    Canvas(modifier = modifier.size(width = 24.dp, height = 28.dp)) {
        val s = size.width / 24f
        drawRoundRect(
            color = color,
            topLeft = Offset(3f * s, 12f * s),
            size = Size(18f * s, 14f * s),
            cornerRadius = CornerRadius(3f * s, 3f * s)
        )
        val shackle = Path().apply {
            moveTo(6f * s, 12f * s)
            lineTo(6f * s, 7f * s)
            cubicTo(6f * s, 0f, 18f * s, 0f, 18f * s, 7f * s)
            lineTo(18f * s, 12f * s)
        }
        drawPath(
            path = shackle,
            color = color,
            style = Stroke(width = 2f * s, cap = StrokeCap.Round)
        )
    }
}