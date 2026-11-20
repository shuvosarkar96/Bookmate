package com.bookmate.app.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bookmate.app.R
import com.bookmate.app.ui.components.PrimaryButton
import com.bookmate.app.ui.theme.Cream
import com.bookmate.app.ui.theme.DarkGreen
import com.bookmate.app.ui.theme.Dimens
import com.bookmate.app.ui.theme.Green
import com.bookmate.app.ui.theme.InputFill
import com.bookmate.app.ui.theme.OutlineColor
import com.bookmate.app.ui.theme.TextPrimary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    onBack: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val backLabel = stringResource(R.string.action_back)

    LaunchedEffect(state.finished) {
        if (state.finished) onFinished()
    }

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
                text = stringResource(R.string.onboarding_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.onboarding_subtitle),
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Genre.entries.forEach { genre ->
                    GenreChip(
                        label = stringResource(genre.labelRes),
                        selected = genre in state.selected,
                        onToggle = { viewModel.onToggle(genre) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.onboarding_selected, state.selected.size),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        state.errorRes?.let { errorRes ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(errorRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        PrimaryButton(
            text = stringResource(R.string.onboarding_continue),
            onClick = viewModel::onContinue,
            enabled = state.selected.isNotEmpty() && !state.isSaving
        )
        Text(
            text = stringResource(R.string.onboarding_skip),
            style = MaterialTheme.typography.labelLarge,
            color = DarkGreen,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable(enabled = !state.isSaving, onClick = viewModel::onSkip)
                .padding(vertical = 16.dp, horizontal = 24.dp)
        )
    }
}

@Composable
internal fun GenreChip(
    label: String,
    selected: Boolean,
    onToggle: () -> Unit
) {
    val shape = RoundedCornerShape(50)
    Box(
        modifier = Modifier
            .height(44.dp)
            .clip(shape)
            .background(if (selected) Green else InputFill)
            .border(BorderStroke(1.dp, if (selected) Green else OutlineColor), shape)
            .toggleable(value = selected, role = Role.Checkbox, onValueChange = { onToggle() })
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
            fontWeight = FontWeight.Medium,
            color = if (selected) Cream else TextPrimary
        )
    }
}