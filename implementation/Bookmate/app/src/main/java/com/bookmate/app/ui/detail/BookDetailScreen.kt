package com.bookmate.app.ui.detail

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bookmate.app.R
import com.bookmate.app.domain.model.AccessType
import com.bookmate.app.domain.model.Book
import com.bookmate.app.ui.components.AccessChip
import com.bookmate.app.ui.components.BackChevronIcon
import com.bookmate.app.ui.components.BookmarkIcon
import com.bookmate.app.ui.components.LockIcon
import com.bookmate.app.ui.components.PrimaryButton
import com.bookmate.app.ui.components.SparkleIcon
import com.bookmate.app.ui.theme.CardSurface
import com.bookmate.app.ui.theme.CoverBlock1
import com.bookmate.app.ui.theme.DarkGreen
import com.bookmate.app.ui.theme.Dimens
import com.bookmate.app.ui.theme.Green
import com.bookmate.app.ui.theme.SecondaryContainerColor
import com.bookmate.app.ui.theme.TextPrimary
import com.bookmate.app.ui.theme.TextSecondary

@Composable
fun BookDetailScreen(
    onBack: () -> Unit,
    onRead: (String) -> Unit,
    onSubscribe: () -> Unit,
    viewModel: BookDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val book = state.book
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refreshAccess() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        TopBar(
            onBack = onBack,
            showSave = book != null,
            isSaved = state.isSaved,
            onSaveToggle = viewModel::onSaveToggle
        )
        when {
            state.isLoading -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Green)
            }

            book == null -> Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(
                        if (state.notFound) R.string.detail_not_found else (state.errorRes ?: R.string.error_unknown)
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = stringResource(R.string.home_retry),
                    style = MaterialTheme.typography.labelLarge,
                    color = DarkGreen,
                    modifier = Modifier
                        .clickable(onClick = viewModel::load)
                        .padding(16.dp)
                )
            }

            else -> BookContent(
                book = book,
                canRead = state.canRead,
                saveFailed = state.saveFailed,
                onRead = onRead,
                onSubscribe = onSubscribe,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TopBar(
    onBack: () -> Unit,
    showSave: Boolean,
    isSaved: Boolean,
    onSaveToggle: () -> Unit
) {
    val backLabel = stringResource(R.string.action_back)
    val saveLabel = stringResource(if (isSaved) R.string.detail_unsave else R.string.detail_save)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.SideMargin, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(CardSurface)
                .semantics { contentDescription = backLabel }
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            BackChevronIcon()
        }
        if (showSave) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(CardSurface)
                    .semantics { contentDescription = saveLabel }
                    .clickable(onClick = onSaveToggle),
                contentAlignment = Alignment.Center
            ) {
                BookmarkIcon(filled = isSaved)
            }
        } else {
            Spacer(modifier = Modifier.size(44.dp))
        }
    }
}

@Composable
private fun BookContent(
    book: Book,
    canRead: Boolean,
    saveFailed: Boolean,
    onRead: (String) -> Unit,
    onSubscribe: () -> Unit,
    modifier: Modifier = Modifier
) {
    var aiSoon by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.SideMargin),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .size(width = 170.dp, height = 250.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(CoverBlock1),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = book.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkGreen,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = book.author,
                        fontSize = 14.sp,
                        color = DarkGreen,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = book.title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = book.author,
                fontSize = 15.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                if (book.genre.isNotBlank()) {
                    InfoChip(text = book.genre)
                }
                AccessChip(accessType = book.accessType)
                if (book.hasFullText) {
                    InfoChip(text = stringResource(R.string.detail_chip_full_text))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            AccessCard(book = book, canRead = canRead)
            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(CardSurface)
                    .padding(20.dp)
            ) {
                Text(
                    text = stringResource(R.string.detail_about),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkGreen
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = book.description,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
            }
            if (book.hasFullText) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.detail_source),
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.ButtonHeight)
                    .clip(RoundedCornerShape(50))
                    .background(SecondaryContainerColor)
                    .clickable { aiSoon = true },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SparkleIcon(color = TextPrimary, size = 24.dp)
                Spacer(modifier = Modifier.size(10.dp))
                Text(
                    text = stringResource(
                        if (canRead && book.hasFullText) R.string.detail_summarize
                        else R.string.detail_summarize_description
                    ),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }
            if (aiSoon) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.coming_soon),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Green
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.SideMargin)
                .navigationBarsPadding()
                .padding(top = 8.dp, bottom = 16.dp)
        ) {
            if (saveFailed) {
                Text(
                    text = stringResource(R.string.detail_save_failed),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }
            when {
                !canRead -> PrimaryButton(
                    text = stringResource(R.string.detail_subscribe),
                    onClick = onSubscribe,
                    enabled = true
                )

                book.hasFullText -> PrimaryButton(
                    text = stringResource(R.string.detail_read),
                    onClick = { onRead(book.id) },
                    enabled = true
                )
            }
        }
    }
}

@Composable
private fun AccessCard(book: Book, canRead: Boolean) {
    val title = when {
        !canRead -> R.string.detail_premium_title
        book.accessType == AccessType.FREE -> R.string.detail_free_title
        else -> R.string.detail_included_title
    }
    val subtitle = if (canRead) R.string.detail_free_subtitle else R.string.detail_premium_subtitle
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardSurface)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (!canRead) {
            LockIcon()
        }
        Column {
            Text(
                text = stringResource(title),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = stringResource(subtitle),
                fontSize = 13.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun InfoChip(text: String) {
    Box(
        modifier = Modifier
            .height(24.dp)
            .clip(RoundedCornerShape(50))
            .background(CardSurface)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, fontSize = 12.sp, color = TextPrimary)
    }
}