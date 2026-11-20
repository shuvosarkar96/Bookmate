package com.bookmate.app.ui.ai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bookmate.app.R
import com.bookmate.app.domain.model.Book
import com.bookmate.app.domain.model.ReadingProgress
import com.bookmate.app.ui.components.BookListCard
import com.bookmate.app.ui.components.PrimaryButton
import com.bookmate.app.ui.theme.DarkGreen
import com.bookmate.app.ui.theme.Green
import com.bookmate.app.ui.theme.TextSecondary

/** Shown in the AI tab when no book is selected: pick from Reading or Saved, or find one in Search. */
@Composable
fun PickBook(
    picker: PickerState,
    onPick: (Book, ReadingProgress?) -> Unit,
    onFindBook: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.ai_chat_empty),
                fontSize = 15.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (picker.isLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Green)
                }
            }
        }
        if (picker.reading.isNotEmpty()) {
            item(key = "reading-header") { SectionTitle(stringResource(R.string.my_books_tab_reading)) }
            itemsIndexed(picker.reading, key = { _, entry -> "reading-${entry.book.id}" }) { index, entry ->
                BookListCard(
                    book = entry.book,
                    index = index,
                    subtitle = stringResource(
                        R.string.profile_current_chapter,
                        entry.progress.chapter,
                        entry.book.chapterCount
                    ),
                    onClick = { onPick(entry.book, entry.progress) }
                )
            }
        }
        if (picker.saved.isNotEmpty()) {
            item(key = "saved-header") { SectionTitle(stringResource(R.string.my_books_tab_saved)) }
            itemsIndexed(picker.saved, key = { _, book -> "saved-${book.id}" }) { index, book ->
                BookListCard(book = book, index = index, onClick = { onPick(book, null) })
            }
        }
        item(key = "find") {
            PrimaryButton(
                text = stringResource(R.string.ai_chat_find_book),
                onClick = onFindBook,
                enabled = true
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DarkGreen)
}