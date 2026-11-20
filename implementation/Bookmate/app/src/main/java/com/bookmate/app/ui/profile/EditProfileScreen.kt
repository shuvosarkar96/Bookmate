package com.bookmate.app.ui.profile

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bookmate.app.R
import com.bookmate.app.ui.components.BookmateCard
import com.bookmate.app.ui.components.BookmateTextField
import com.bookmate.app.ui.components.PrimaryButton
import com.bookmate.app.ui.onboarding.Genre
import com.bookmate.app.ui.onboarding.GenreChip
import com.bookmate.app.ui.theme.Dimens

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val backLabel = stringResource(R.string.action_back)

    LaunchedEffect(state.saved) {
        if (state.saved) onBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
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
                text = stringResource(R.string.settings_edit_profile),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        when {
            state.isLoading -> Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            !state.loaded -> state.errorRes?.let { errorRes ->
                Text(
                    text = stringResource(errorRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            else -> BookmateCard {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    BookmateTextField(
                        value = state.name,
                        onValueChange = viewModel::onNameChange,
                        placeholder = stringResource(R.string.register_name),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            keyboardType = KeyboardType.Text
                        )
                    )
                    Text(
                        text = stringResource(R.string.profile_genres_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
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
                    state.errorRes?.let { errorRes ->
                        Text(
                            text = stringResource(errorRes),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    PrimaryButton(
                        text = stringResource(R.string.edit_profile_save),
                        onClick = viewModel::onSaveClick,
                        enabled = !state.isSaving
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}