package com.bookmate.app.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bookmate.app.R
import com.bookmate.app.domain.model.Book
import com.bookmate.app.ui.components.AccessChip
import com.bookmate.app.ui.components.BookmateTextField
import com.bookmate.app.ui.theme.CardSurface
import com.bookmate.app.ui.theme.DarkGreen
import com.bookmate.app.ui.theme.Dimens
import com.bookmate.app.ui.theme.Green
import com.bookmate.app.ui.theme.TextSecondary

/** Admin: Manage books. A list of all books with search and Remove. Reached from Profile. */
@Composable
fun ManageBooksScreen(
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    viewModel: ManageBooksViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val backLabel = stringResource(R.string.action_back)
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.load() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = Dimens.SideMargin)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .semantics { contentDescription = backLabel }
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "\u2190",
                    fontSize = 28.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = stringResource(R.string.screen_admin_manage_books),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        BookmateTextField(
            value = state.query,
            onValueChange = viewModel::onQueryChange,
            placeholder = stringResource(R.string.manage_books_search)
        )
        Spacer(modifier = Modifier.height(12.dp))

        state.removedTitle?.let { title ->
            Text(
                text = stringResource(R.string.manage_books_removed, title),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        state.errorRes?.let { errorRes ->
            Text(
                text = stringResource(errorRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            if (state.total == 0) {
                Text(
                    text = stringResource(R.string.home_retry),
                    style = MaterialTheme.typography.labelLarge,
                    color = DarkGreen,
                    modifier = Modifier
                        .clickable(onClick = viewModel::load)
                        .padding(vertical = 12.dp)
                )
            }
        }

        when {
            state.isLoading -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Green)
            }

            state.books.isEmpty() && state.errorRes == null -> Text(
                text = stringResource(R.string.manage_books_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            else -> LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)
            ) {
                items(state.books, key = { it.id }) { book ->
                    ManageBookCard(book = book, onEdit = { onEdit(book.id) }, onRemove = { viewModel.onRemoveClick(book) })
                }
            }
        }
    }

    state.confirmBook?.let { book ->
        AlertDialog(
            onDismissRequest = viewModel::onDismissConfirm,
            title = { Text(stringResource(R.string.manage_books_confirm_title)) },
            text = { Text(stringResource(R.string.manage_books_confirm_text, book.title)) },
            confirmButton = {
                TextButton(onClick = viewModel::onConfirmRemove, enabled = !state.isRemoving) {
                    Text(
                        text = stringResource(R.string.manage_books_remove),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissConfirm, enabled = !state.isRemoving) {
                    Text(stringResource(R.string.add_book_cancel))
                }
            }
        )
    }
}

@Composable
private fun ManageBookCard(book: Book, onEdit: () -> Unit, onRemove: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardSurface)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkGreen,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(text = book.author, fontSize = 13.sp, color = TextSecondary, maxLines = 1)
            }
            AccessChip(book.accessType)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = book.genre, fontSize = 13.sp, color = TextSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.manage_books_edit),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = DarkGreen,
                    modifier = Modifier
                        .clickable(onClick = onEdit)
                        .padding(vertical = 10.dp, horizontal = 8.dp)
                )
                Text(
                    text = stringResource(R.string.manage_books_remove),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .clickable(onClick = onRemove)
                        .padding(vertical = 10.dp, horizontal = 8.dp)
                )
            }
        }
    }
}