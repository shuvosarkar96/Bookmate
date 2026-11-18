package com.bookmate.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val Cream = Color(0xFFFAF2DE)
val Sand = Color(0xFFEADCBB)
val Sage = Color(0xFFC0C0A7)
val Moss = Color(0xFF9CA793)
val Olive = Color(0xFF78866E)

val Green = Color(0xFF576B48)
val DarkGreen = Color(0xFF324B21)

val CardSurface = Color(0xFFFBF9F4).copy(alpha = 0.9f)
val InputFill = Color(0xFFFFFFFF)
val OutlineColor = Color(0xFFDAD8CE)
val SecondaryContainerColor = Color(0xFFE3E3D9)
val NavContainer = Color(0xFFC3D3BA)
val NavActive = Color(0xFFACBDA1)
val TextPrimary = Color(0xFF0F1909)
val TextSecondary = Color(0xFF615E64)

val CoverBlock1 = Color(0xFFD8CFB4)
val CoverBlock2 = Color(0xFFB9C6AC)
val CoverBlock3 = Color(0xFFA9B39B)

val BookmateGradient = Brush.verticalGradient(
    0f to Cream,
    0.3f to Sand,
    0.58f to Sage,
    0.8f to Moss,
    1f to Olive
)