package com.bookmate.app.ui.home

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bookmate.app.R
import com.bookmate.app.ui.components.BarcodeIcon
import com.bookmate.app.ui.components.BookCoverCard
import com.bookmate.app.ui.components.BookmateCard
import com.bookmate.app.ui.components.SearchIcon
import com.bookmate.app.ui.components.SparkleIcon
import com.bookmate.app.ui.theme.Cream
import com.bookmate.app.ui.theme.CardSurface
import com.bookmate.app.ui.theme.DarkGreen
import com.bookmate.app.ui.theme.Dimens
import com.bookmate.app.ui.theme.Green
import com.bookmate.app.ui.theme.SecondaryContainerColor
import com.bookmate.app.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    onSearchClick: () -> Unit,
    onScanClick: () -> Unit,
    onAskClick: () -> Unit,
    onBookClick: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.SideMargin)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.nav_home),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(20.dp))

        SearchBar(onSearchClick = onSearchClick, onScanClick = onScanClick)
        Spacer(modifier = Modifier.height(16.dp))

        BookmateCard {
            Column(modifier = Modifier.padding(4.dp)) {
                Text(
                    text = stringResource(R.string.home_recommended_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkGreen
                )
                Text(
                    text = stringResource(R.string.home_recommended_subtitle),
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                when {
                    state.isLoading -> Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
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
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.home_retry),
                            style = MaterialTheme.typography.labelLarge,
                            color = DarkGreen,
                            modifier = Modifier
                                .clickable(onClick = viewModel::load)
                                .padding(8.dp)
                        )
                    }

                    state.recommended.isEmpty() -> Text(
                        text = stringResource(R.string.home_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    else -> Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        state.recommended.forEachIndexed { index, book ->
                            BookCoverCard(
                                book = book,
                                index = index,
                                onClick = { onBookClick(book.id) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        repeat(3 - state.recommended.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        AskBookmateCard(onClick = onAskClick)
        Spacer(modifier = Modifier.height(Dimens.NavHeight + 48.dp))
    }
}

@Composable
private fun SearchBar(onSearchClick: () -> Unit, onScanClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimens.FieldHeight)
            .clip(RoundedCornerShape(26.dp))
            .background(CardSurface)
            .clickable(onClick = onSearchClick)
            .padding(start = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SearchIcon()
        Text(
            text = stringResource(R.string.home_search_hint),
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
            color = TextSecondary,
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        )
        Box(
            modifier = Modifier
                .size(48.dp)
                .clickable(onClick = onScanClick),
            contentAlignment = Alignment.Center
        ) {
            BarcodeIcon()
        }
    }
}

@Composable
private fun AskBookmateCard(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Green)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.home_ask_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Cream
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.home_ask_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = SecondaryContainerColor
            )
        }
        SparkleIcon(color = Cream)
    }
}