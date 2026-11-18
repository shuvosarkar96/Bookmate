package com.bookmate.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.dp
import com.bookmate.app.ui.theme.Cream
import com.bookmate.app.ui.theme.Green

@Composable
fun BookmateLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(80.dp)) {
        val factor = size.width / 80f
        scale(factor, factor, pivot = Offset.Zero) {
            drawRoundRect(
                color = Green,
                size = Size(80f, 80f),
                cornerRadius = CornerRadius(24f, 24f)
            )
            drawRoundRect(
                color = Cream,
                topLeft = Offset(15f, 26f),
                size = Size(22f, 34f),
                cornerRadius = CornerRadius(3f, 3f)
            )
            drawRoundRect(
                color = Cream.copy(alpha = 0.75f),
                topLeft = Offset(43f, 26f),
                size = Size(22f, 34f),
                cornerRadius = CornerRadius(3f, 3f)
            )
        }
    }
}