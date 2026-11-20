package com.bookmate.app.ui.mybooks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bookmate.app.R
import com.bookmate.app.domain.model.MyBooks
import com.bookmate.app.ui.components.BookListCard
import com.bookmate.app.ui.theme.Cream
import com.bookmate.app.ui.theme.DarkGreen
import com.bookmate.app.ui.theme.Dimens
import com.bookmate.app.ui.theme.Green
import com.bookmate.app.ui.theme.SecondaryContainerColor
import com.bookmate.app.ui.theme.TextPrimary
import com.bookmate.app.ui.theme.TextSecondary
import java.time.Month
import java.time.format.TextStyle as MonthTextStyle
import java.util.Locale

/** SCR My Books: Reading, Saved and Finished (grouped by month). */
@Composable
fun MyBooksScreen(
    onBookClick: (String) -> Unit,
    viewModel: MyBooksViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.load() }
    val books = state.books

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = Dimens.SideMargin)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.nav_my_books),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        TabToggle(selected = state.tab, onSelect = viewModel::selectTab)
        Spacer(modifier = Modifier.height(12.dp))

        when {
            state.isLoading -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Green)
            }

            books == null -> Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(state.errorRes ?: R.string.error_unknown),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = stringResource(R.string.home_retry),
                    style = MaterialTheme.typography.labelLarge,
                    color = DarkGreen,
                    modifier = Modifier
                        .clickable(onClick = viewModel::load)
                        .padding(12.dp)
                )
            }

            else -> MyBooksList(tab = state.tab, books = books, onBookClick = onBookClick)
        }
    }
}

@Composable
private fun TabToggle(selected: MyBooksTab, onSelect: (MyBooksTab) -> Unit) {
    val labels = listOf(
        MyBooksTab.READING to stringResource(R.string.my_books_tab_reading),
        MyBooksTab.SAVED to stringResource(R.string.my_books_tab_saved),
        MyBooksTab.FINISHED to stringResource(R.string.my_books_tab_finished)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(SecondaryContainerColor)
            .padding(4.dp)
    ) {
        labels.forEach { (tab, label) ->
            val isSelected = tab == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) Green else Color.Transparent)
                    .clickable { onSelect(tab) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 15.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Cream else TextPrimary
                )
            }
        }
    }
}

@Composable
private fun MyBooksList(tab: MyBooksTab, books: MyBooks, onBookClick: (String) -> Unit) {
    when (tab) {
        MyBooksTab.READING -> if (books.reading.isEmpty()) {
            EmptyText(R.string.my_books_empty_reading)
        } else {
            BookColumn {
                itemsIndexed(books.reading, key = { _, entry -> entry.book.id }) { index, entry ->
                    BookListCard(
                        book = entry.book,
                        index = index,
                        subtitle = stringResource(
                            R.string.profile_current_chapter,
                            entry.progress.chapter,
                            entry.book.chapterCount
                        ),
                        onClick = { onBookClick(entry.book.id) }
                    )
                }
            }
        }

        MyBooksTab.SAVED -> if (books.saved.isEmpty()) {
            EmptyText(R.string.my_books_empty_saved)
        } else {
            BookColumn {
                itemsIndexed(books.saved, key = { _, book -> book.id }) { index, book ->
                    BookListCard(book = book, index = index, onClick = { onBookClick(book.id) })
                }
            }
        }

        MyBooksTab.FINISHED -> if (books.finished.isEmpty()) {
            EmptyText(R.string.my_books_empty_finished)
        } else {
            BookColumn {
                item(key = "total") {
                    Text(
                        text = stringResource(R.string.my_books_finished_total, books.finishedCount),
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                }
                books.finished.forEach { group ->
                    item(key = "header-${group.year}-${group.month}") {
                        val monthName = Month.of(group.month)
                            .getDisplayName(MonthTextStyle.FULL, Locale.getDefault())
                        Text(
                            text = stringResource(R.string.my_books_month, monthName, group.year, group.books.size),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkGreen,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                    itemsIndexed(group.books, key = { _, book -> "${group.year}-${group.month}-${book.id}" }) { index, book ->
                        BookListCard(book = book, index = index, onClick = { onBookClick(book.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun BookColumn(content: LazyListScope.() -> Unit) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = Dimens.NavHeight + 48.dp),
        content = content
    )
}

@Composable
private fun EmptyText(textRes: Int) {
    Text(
        text = stringResource(textRes),
        style = MaterialTheme.typography.bodyMedium,
        color = TextSecondary,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp)
    )
}