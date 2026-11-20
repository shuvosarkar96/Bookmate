package com.bookmate.app.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
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
import com.bookmate.app.ui.theme.DarkGreen
import com.bookmate.app.ui.theme.Dimens
import com.bookmate.app.ui.theme.Green
import com.bookmate.app.ui.theme.InputFill
import com.bookmate.app.ui.theme.OutlineColor
import com.bookmate.app.ui.theme.TextPrimary
import com.bookmate.app.ui.theme.TextSecondary

/** SCR-15 Admin: Add a book. Reached from the Admin tools card on Profile. */
@Composable
fun AddBookScreen(
    onBack: () -> Unit,
    viewModel: AddBookViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val backLabel = stringResource(R.string.action_back)

    LaunchedEffect(state.done) {
        if (state.done) onBack()
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
                text = stringResource(if (state.isEdit) R.string.edit_book_title else R.string.screen_admin_add_book),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        BookmateCard {
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BookmateTextField(
                    value = state.isbn,
                    onValueChange = viewModel::onIsbnChange,
                    placeholder = stringResource(R.string.add_book_isbn)
                )
                BookmateTextField(
                    value = state.title,
                    onValueChange = viewModel::onTitleChange,
                    placeholder = stringResource(R.string.add_book_title),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
                BookmateTextField(
                    value = state.author,
                    onValueChange = viewModel::onAuthorChange,
                    placeholder = stringResource(R.string.add_book_author),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GenreField(
                        selected = Genre.entries.firstOrNull { it.id.equals(state.genre, ignoreCase = true) },
                        fallbackText = state.genre,
                        onSelect = viewModel::onGenreChange,
                        modifier = Modifier.weight(1f)
                    )
                    AccessField(
                        premium = state.premium,
                        onChange = viewModel::onPremiumChange,
                        modifier = Modifier.weight(1f)
                    )
                }
                DescriptionField(
                    value = state.description,
                    onValueChange = viewModel::onDescriptionChange,
                    placeholder = stringResource(R.string.add_book_description)
                )
                BookmateTextField(
                    value = state.coverUrl,
                    onValueChange = viewModel::onCoverChange,
                    placeholder = stringResource(R.string.add_book_cover),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                )
                state.errorRes?.let { errorRes ->
                    Text(
                        text = stringResource(errorRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                state.savedTitle?.let { title ->
                    Text(
                        text = stringResource(R.string.add_book_saved, title),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                PrimaryButton(
                    text = stringResource(if (state.isEdit) R.string.edit_profile_save else R.string.add_book_save),
                    onClick = viewModel::onSaveClick,
                    enabled = !state.isSaving && !state.isLoading
                )
            }
        }

        Text(
            text = stringResource(R.string.add_book_cancel),
            style = MaterialTheme.typography.labelLarge,
            color = DarkGreen,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable(onClick = onBack)
                .padding(vertical = 20.dp, horizontal = 24.dp)
        )
    }
}

@Composable
private fun GenreField(
    selected: Genre?,
    fallbackText: String,
    onSelect: (Genre) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val shape = MaterialTheme.shapes.small
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.FieldHeight)
                .background(InputFill, shape)
                .border(1.dp, OutlineColor, shape)
                .clickable { expanded = true }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = when {
                    selected != null -> stringResource(selected.labelRes)
                    fallbackText.isNotBlank() -> fallbackText
                    else -> stringResource(R.string.add_book_genre)
                },
                style = MaterialTheme.typography.bodyLarge,
                color = if (selected != null || fallbackText.isNotBlank()) TextPrimary else TextSecondary,
                maxLines = 1
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Genre.entries.forEach { genre ->
                DropdownMenuItem(
                    text = { Text(stringResource(genre.labelRes)) },
                    onClick = {
                        onSelect(genre)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun AccessField(
    premium: Boolean,
    onChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = MaterialTheme.shapes.small
    Row(
        modifier = modifier
            .height(Dimens.FieldHeight)
            .background(InputFill, shape)
            .border(1.dp, OutlineColor, shape)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = stringResource(R.string.add_book_access), fontSize = 11.sp, color = TextSecondary)
            Text(
                text = stringResource(if (premium) R.string.add_book_premium else R.string.add_book_free),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
        Switch(checked = premium, onCheckedChange = onChange)
    }
}

@Composable
private fun DescriptionField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    val shape = MaterialTheme.shapes.small
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary),
        cursorBrush = SolidColor(Green),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 110.dp)
                    .background(InputFill, shape)
                    .border(1.dp, OutlineColor, shape)
                    .padding(16.dp)
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                }
                innerTextField()
            }
        }
    )
}