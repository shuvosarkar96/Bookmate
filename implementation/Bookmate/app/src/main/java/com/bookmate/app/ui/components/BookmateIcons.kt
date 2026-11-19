package com.bookmate.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bookmate.app.ui.theme.TextPrimary

@Composable
fun SearchIcon(modifier: Modifier = Modifier, color: Color = TextPrimary, size: Dp = 24.dp) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size.width / 24f
        drawCircle(
            color = color,
            radius = 7f * s,
            center = Offset(10f * s, 10f * s),
            style = Stroke(width = 2f * s, cap = StrokeCap.Round)
        )
        drawLine(
            color = color,
            start = Offset(15f * s, 15f * s),
            end = Offset(21f * s, 21f * s),
            strokeWidth = 2f * s,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun BarcodeIcon(modifier: Modifier = Modifier, color: Color = TextPrimary, size: Dp = 24.dp) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size.width / 24f
        listOf(3.5f, 8.5f, 13.5f, 18.5f).forEach { x ->
            drawLine(
                color = color,
                start = Offset(x * s, 2f * s),
                end = Offset(x * s, 22f * s),
                strokeWidth = 2f * s,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun SparkleIcon(modifier: Modifier = Modifier, color: Color = TextPrimary, size: Dp = 32.dp) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size.width / 32f
        val c = 16f
        val path = Path().apply {
            moveTo(c * s, (c - 16f) * s)
            lineTo((c + 4f) * s, (c - 4f) * s)
            lineTo((c + 16f) * s, c * s)
            lineTo((c + 4f) * s, (c + 4f) * s)
            lineTo(c * s, (c + 16f) * s)
            lineTo((c - 4f) * s, (c + 4f) * s)
            lineTo((c - 16f) * s, c * s)
            lineTo((c - 4f) * s, (c - 4f) * s)
            close()
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2f * s, join = StrokeJoin.Round)
        )
    }
}