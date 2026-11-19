package com.bookmate.app.ui.ai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bookmate.app.R

/** Shows the state of an AI answer: loading, streaming text with the notice, or an error. */
@Composable
fun AiAnswerCard(
    state: AiAnswerState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (state is AiAnswerState.Idle) return
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when (state) {
                AiAnswerState.Idle -> Unit
                AiAnswerState.Loading -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Text(
                        text = stringResource(R.string.ai_answer_loading),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                is AiAnswerState.Streaming -> AnswerBody(state.text)
                is AiAnswerState.Done -> AnswerBody(state.text)
                is AiAnswerState.Error -> {
                    Text(
                        text = stringResource(state.error.messageRes()),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    if (state.error.retryable) {
                        TextButton(onClick = onRetry) {
                            Text(stringResource(R.string.ai_answer_retry))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnswerBody(text: String) {
    SelectionContainer {
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
    Text(
        text = stringResource(R.string.ai_answer_notice),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}