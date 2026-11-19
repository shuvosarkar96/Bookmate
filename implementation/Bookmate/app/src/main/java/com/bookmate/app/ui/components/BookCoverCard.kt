package com.bookmate.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bookmate.app.domain.model.Book
import com.bookmate.app.ui.theme.CoverBlock1
import com.bookmate.app.ui.theme.CoverBlock2
import com.bookmate.app.ui.theme.CoverBlock3
import com.bookmate.app.ui.theme.DarkGreen
import com.bookmate.app.ui.theme.TextPrimary

private fun coverColor(index: Int): Color = when (index % 3) {
    0 -> CoverBlock1
    1 -> CoverBlock2
    else -> CoverBlock3
}

@Composable
fun BookCoverCard(
    book: Book,
    index: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(94f / 140f)
                .clip(RoundedCornerShape(10.dp))
                .background(coverColor(index)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = book.title.take(1).uppercase(),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGreen.copy(alpha = 0.6f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = book.title,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            color = TextPrimary,
            maxLines = 2,
            minLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(6.dp))
        AccessChip(accessType = book.accessType)
    }
}