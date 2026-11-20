package com.bookmate.app.ui.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bookmate.app.R
import com.bookmate.app.domain.ai.PromptBuilder
import com.bookmate.app.domain.ai.Recommendation
import com.bookmate.app.ui.components.AccessChip
import com.bookmate.app.ui.components.PrimaryButton
import com.bookmate.app.ui.theme.CardSurface
import com.bookmate.app.ui.theme.DarkGreen
import com.bookmate.app.ui.theme.Green
import com.bookmate.app.ui.theme.TextPrimary
import com.bookmate.app.ui.theme.TextSecondary

/** The Recommend part of the AI tab (SCR-09). */
@Composable
fun RecommendTab(
    onBookClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RecommendViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.recommend_intro),
            fontSize = 14.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = state.request,
            onValueChange = viewModel::onRequestChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.recommend_hint)) },
            maxLines = 3,
            enabled = !state.isLoading,
            shape = RoundedCornerShape(26.dp)
        )
        Text(
            text = stringResource(
                R.string.reader_ai_counter,
                state.request.length,
                PromptBuilder.MAX_QUESTION_LENGTH
            ),
            fontSize = 11.sp,
            color = TextSecondary,
            modifier = Modifier.padding(start = 16.dp, top = 2.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        PrimaryButton(
            text = stringResource(R.string.recommend_button),
            onClick = viewModel::recommend,
            enabled = !state.isLoading
        )
        Spacer(modifier = Modifier.height(12.dp))

        when {
            state.isLoading -> Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularProgressIndicator(color = Green, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                Text(
                    text = stringResource(R.string.recommend_loading),
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }

            state.loadFailed -> Text(
                text = stringResource(R.string.error_unknown),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )

            state.hasRun && state.items.isEmpty() -> Text(
                text = state.aiError?.let { stringResource(it.messageRes()) }
                    ?: stringResource(R.string.recommend_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = if (state.aiError != null) MaterialTheme.colorScheme.error else TextSecondary
            )

            state.items.isNotEmpty() -> LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!state.fromAi) {
                    item {
                        Text(
                            text = stringResource(R.string.recommend_fallback_notice),
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
                items(state.items, key = { it.book.id }) { item ->
                    RecommendationCard(item = item, onClick = { onBookClick(item.book.id) })
                }
                if (state.fromAi) {
                    item {
                        Text(
                            text = stringResource(R.string.ai_answer_notice),
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecommendationCard(item: Recommendation, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardSurface)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.book.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkGreen
                )
                Text(text = item.book.author, fontSize = 13.sp, color = TextSecondary)
            }
            AccessChip(accessType = item.book.accessType)
        }
        Text(
            text = item.reason.ifBlank { stringResource(R.string.recommend_reason_fallback) },
            fontSize = 14.sp,
            color = TextPrimary
        )
    }
}