package com.bookmate.app.ui.search

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bookmate.app.R
import com.bookmate.app.domain.model.AccessType
import com.bookmate.app.domain.model.Book
import com.bookmate.app.ui.components.AccessChip
import com.bookmate.app.ui.components.SearchIcon
import com.bookmate.app.ui.theme.CardSurface
import com.bookmate.app.ui.theme.CoverBlock1
import com.bookmate.app.ui.theme.CoverBlock2
import com.bookmate.app.ui.theme.CoverBlock3
import com.bookmate.app.ui.theme.Cream
import com.bookmate.app.ui.theme.DarkGreen
import com.bookmate.app.ui.theme.Dimens
import com.bookmate.app.ui.theme.Green
import com.bookmate.app.ui.theme.TextPrimary
import com.bookmate.app.ui.theme.TextSecondary

@Composable
fun SearchScreen(
    onBookClick: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    var genreMenuOpen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = Dimens.SideMargin)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.nav_search),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(20.dp))

        SearchField(
            query = state.query,
            onChange = viewModel::onQueryChange,
            onSearch = { focusManager.clearFocus() }
        )
        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SearchFilterChip(
                label = stringResource(R.string.search_filter_all),
                selected = state.access == null && state.genre == null,
                onClick = viewModel::onAllClick
            )
            Box {
                SearchFilterChip(
                    label = state.genre ?: stringResource(R.string.search_filter_genre),
                    selected = state.genre != null,
                    onClick = { genreMenuOpen = true }
                )
                DropdownMenu(
                    expanded = genreMenuOpen,
                    onDismissRequest = { genreMenuOpen = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.search_all_genres)) },
                        onClick = {
                            viewModel.onGenreChange(null)
                            genreMenuOpen = false
                        }
                    )
                    state.genres.forEach { genre ->
                        DropdownMenuItem(
                            text = { Text(genre) },
                            onClick = {
                                viewModel.onGenreChange(genre)
                                genreMenuOpen = false
                            }
                        )
                    }
                }
            }
            SearchFilterChip(
                label = stringResource(R.string.access_free),
                selected = state.access == AccessType.FREE,
                onClick = { viewModel.onAccessToggle(AccessType.FREE) }
            )
            SearchFilterChip(
                label = stringResource(R.string.access_premium),
                selected = state.access == AccessType.PREMIUM,
                onClick = { viewModel.onAccessToggle(AccessType.PREMIUM) }
            )
        }
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

            state.errorRes != null -> Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(state.errorRes!!),
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

            state.results.isEmpty() -> Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.search_no_results),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkGreen
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.search_no_results_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            else -> {
                val count = state.results.size
                Text(
                    text = context.resources.getQuantityString(R.plurals.search_results, count, count),
                    fontSize = 13.sp,
                    color = TextPrimary
                )
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = Dimens.NavHeight + 48.dp)
                ) {
                    itemsIndexed(state.results, key = { _, book -> book.id }) { index, book ->
                        ResultCard(book = book, index = index, onClick = { onBookClick(book.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    val clearLabel = stringResource(R.string.search_clear)
    val hint = stringResource(R.string.home_search_hint)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimens.FieldHeight)
            .clip(RoundedCornerShape(26.dp))
            .background(CardSurface)
            .padding(start = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SearchIcon()
        BasicTextField(
            value = query,
            onValueChange = onChange,
            singleLine = true,
            textStyle = TextStyle(fontSize = 16.sp, color = TextPrimary),
            cursorBrush = SolidColor(Green),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            decorationBox = { inner ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (query.isEmpty()) {
                        Text(text = hint, fontSize = 16.sp, color = TextSecondary)
                    }
                    inner()
                }
            }
        )
        if (query.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .semantics { contentDescription = clearLabel }
                    .clickable { onChange("") },
                contentAlignment = Alignment.Center
            ) {
                ClearIcon(color = TextSecondary)
            }
        }
    }
}

@Composable
private fun ClearIcon(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val s = size.width / 24f
        drawLine(color, Offset(6f * s, 6f * s), Offset(18f * s, 18f * s), strokeWidth = 2f * s, cap = StrokeCap.Round)
        drawLine(color, Offset(18f * s, 6f * s), Offset(6f * s, 18f * s), strokeWidth = 2f * s, cap = StrokeCap.Round)
    }
}

@Composable
private fun SearchFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) Green else CardSurface)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) Cream else TextPrimary
        )
    }
}

@Composable
private fun ResultCard(book: Book, index: Int, onClick: () -> Unit) {
    val coverColor = when (index % 3) {
        0 -> CoverBlock1
        1 -> CoverBlock2
        else -> CoverBlock3
    }
    Row(
        modifier = Modifier
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
            Spacer(modifier = Modifier.height(8.dp))
            AccessChip(accessType = book.accessType)
        }
    }
}