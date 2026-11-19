package com.bookmate.app.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bookmate.app.R
import com.bookmate.app.domain.model.Book
import com.bookmate.app.ui.components.BackChevronIcon
import com.bookmate.app.ui.components.LockIcon
import com.bookmate.app.ui.components.PrimaryButton
import com.bookmate.app.ui.components.SparkleIcon
import com.bookmate.app.ui.theme.CardSurface
import com.bookmate.app.ui.theme.DarkGreen
import com.bookmate.app.ui.theme.Dimens
import com.bookmate.app.ui.theme.Green
import com.bookmate.app.ui.theme.SecondaryContainerColor
import com.bookmate.app.ui.theme.TextPrimary
import com.bookmate.app.ui.theme.TextSecondary

@Composable
fun ReaderScreen(
    onBack: () -> Unit,
    onSubscribe: () -> Unit,
    viewModel: ReaderViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val book = state.book
    val textSize by viewModel.textSize.collectAsStateWithLifecycle()
    DisposableEffect(Unit) { onDispose { viewModel.savePosition() } }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { viewModel.savePosition() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        ReaderHeader(
            title = book?.title.orEmpty(),
            subtitle = when {
                book == null || state.locked -> ""
                !state.chapter?.title.isNullOrBlank() -> state.chapter?.title.orEmpty()
                else -> stringResource(R.string.reader_chapter, state.chapterNo)
            },
            onBack = onBack,
            showTextSize = book != null && !state.isLoading && !state.locked && state.errorRes == null,
            onTextSize = viewModel::cycleTextSize
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

            state.locked -> Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = Dimens.SideMargin),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LockIcon()
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.detail_premium_title),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = stringResource(R.string.reader_locked_message),
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                PrimaryButton(
                    text = stringResource(R.string.detail_subscribe),
                    onClick = onSubscribe,
                    enabled = true
                )
            }

            state.errorRes != null || book == null -> Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
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
                        .clickable(onClick = viewModel::retry)
                        .padding(16.dp)
                )
            }

            else -> ReaderBody(
                state = state,
                book = book,
                onPrevious = viewModel::previousChapter,
                onNext = viewModel::nextChapter,
                onFinish = viewModel::markFinished,
                onParagraphChanged = viewModel::onParagraphChanged,
                textSize = textSize,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ReaderHeader(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    showTextSize: Boolean,
    onTextSize: () -> Unit
) {
    val textSizeLabel = stringResource(R.string.reader_text_size)
    val backLabel = stringResource(R.string.action_back)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.SideMargin, vertical = 8.dp),
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
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (showTextSize) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(CardSurface)
                    .semantics { contentDescription = textSizeLabel }
                    .clickable(onClick = onTextSize),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.reader_text_aa),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        } else {
            Spacer(modifier = Modifier.size(44.dp))
        }
    }
}

@Composable
private fun ReaderBody(
    state: ReaderUiState,
    book: Book,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onFinish: () -> Unit,
    onParagraphChanged: (Int) -> Unit,
    textSize: Int,
    modifier: Modifier = Modifier
) {
    var aiSoon by remember { mutableStateOf(false) }
    val aiLabel = stringResource(R.string.reader_ai)
    val chapter = state.chapter
    val fraction = (state.chapterNo.toFloat() / book.chapterCount.toFloat()).coerceIn(0.03f, 1f)

    Box(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.SideMargin)
                    .clip(RoundedCornerShape(28.dp))
                    .background(CardSurface),
                contentAlignment = Alignment.Center
            ) {
                when {
                    chapter == null -> CircularProgressIndicator(color = Green)
                    state.chapterLoading -> CircularProgressIndicator(color = Green)
                    chapter.paragraphs.isEmpty() -> Text(
                        text = stringResource(R.string.reader_empty),
                        fontSize = 14.sp,
                        color = TextSecondary
                    )

                    else -> key(state.chapterNo) {
                        val listState = rememberLazyListState(
                            initialFirstVisibleItemIndex =
                                state.resumeParagraph.coerceIn(0, chapter.paragraphs.lastIndex)
                        )
                        LaunchedEffect(listState) {
                            snapshotFlow { listState.firstVisibleItemIndex }
                                .collect { onParagraphChanged(it) }
                        }
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(chapter.paragraphs) { paragraph ->
                                Text(
                                    text = paragraph,
                                    fontSize = textSize.sp,
                                    lineHeight = (textSize * 1.65f).sp,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.SideMargin)
                    .navigationBarsPadding()
                    .padding(top = 16.dp, bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(SecondaryContainerColor)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(Green)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(
                            R.string.reader_chapter_of,
                            state.chapterNo,
                            book.chapterCount
                        ),
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                    if (state.finishFailed) {
                        Text(
                            text = stringResource(R.string.reader_finish_failed),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    if (aiSoon) {
                        Text(
                            text = stringResource(R.string.coming_soon),
                            fontSize = 13.sp,
                            color = Green
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.padding(end = 88.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    NavPill(
                        text = stringResource(R.string.reader_previous),
                        enabled = state.chapterNo > 1 && !state.chapterLoading,
                        onClick = onPrevious,
                        modifier = Modifier.weight(1f)
                    )
                    NavPill(
                        text = stringResource(
                            when {
                                state.chapterNo < book.chapterCount -> R.string.reader_next
                                state.finished -> R.string.reader_finished
                                else -> R.string.reader_finish
                            }
                        ),
                        enabled = !state.chapterLoading && (state.chapterNo < book.chapterCount || !state.finished),
                        onClick = if (state.chapterNo < book.chapterCount) onNext else onFinish,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = Dimens.SideMargin, bottom = 24.dp)
                .size(80.dp)
                .clip(CircleShape)
                .background(Green.copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Green)
                    .semantics { contentDescription = aiLabel }
                    .clickable { aiSoon = true },
                contentAlignment = Alignment.Center
            ) {
                SparkleIcon(color = Color(0xFFFAF2DE), size = 28.dp)
            }
        }
    }
}

@Composable
private fun NavPill(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(50))
            .background(
                if (enabled) SecondaryContainerColor else SecondaryContainerColor.copy(alpha = 0.5f)
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (enabled) TextPrimary else TextSecondary
        )
    }
}