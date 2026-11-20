package com.bookmate.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bookmate.app.domain.model.Book
import com.bookmate.app.ui.theme.CardSurface
import com.bookmate.app.ui.theme.CoverBlock1
import com.bookmate.app.ui.theme.CoverBlock2
import com.bookmate.app.ui.theme.CoverBlock3
import com.bookmate.app.ui.theme.DarkGreen
import com.bookmate.app.ui.theme.TextPrimary
import com.bookmate.app.ui.theme.TextSecondary

/** A book as a card: cover block, title, author, optional [subtitle] and the Free/Premium chip. */
@Composable
fun BookListCard(
    book: Book,
    index: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    val coverColor = when (index % 3) {
        0 -> CoverBlock1
        1 -> CoverBlock2
        else -> CoverBlock3
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CardSurface)
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 52.dp, height = 68.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(coverColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = book.title.take(1).uppercase(),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGreen.copy(alpha = 0.6f)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = book.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = book.author,
                fontSize = 13.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Text(text = subtitle, fontSize = 13.sp, color = TextPrimary)
            }
            Spacer(modifier = Modifier.height(8.dp))
            AccessChip(accessType = book.accessType)
        }
    }
}