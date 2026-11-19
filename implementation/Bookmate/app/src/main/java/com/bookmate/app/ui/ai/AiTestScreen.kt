package com.bookmate.app.ui.ai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bookmate.app.R

/** Temporary test screen for the AI answer component. Replaced by the AI Chat tab in Step 4.6. */
@Composable
fun AiTestScreen(viewModel: AiTestViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val remaining by viewModel.remaining.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.ai_demo_title),
            style = MaterialTheme.typography.titleLarge
        )
        remaining?.let {
            Text(text = stringResource(R.string.ai_demo_remaining, it))
        }
        Button(onClick = viewModel::summarize, enabled = !state.isBusy) {
            Text(stringResource(R.string.ai_demo_summarize))
        }
        OutlinedButton(onClick = viewModel::useUpRequests) {
            Text(stringResource(R.string.ai_demo_use_up))
        }
        AiAnswerCard(state = state, onRetry = viewModel::retry)
    }
}