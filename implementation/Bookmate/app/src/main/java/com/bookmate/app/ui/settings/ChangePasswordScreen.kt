package com.bookmate.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bookmate.app.R
import com.bookmate.app.ui.components.BookmateCard
import com.bookmate.app.ui.components.BookmateTextField
import com.bookmate.app.ui.components.PrimaryButton
import com.bookmate.app.ui.theme.Dimens

@Composable
fun ChangePasswordScreen(
    onBack: () -> Unit,
    viewModel: ChangePasswordViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val backLabel = stringResource(R.string.action_back)

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
                text = stringResource(R.string.settings_change_password),
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
                    value = state.current,
                    onValueChange = viewModel::onCurrentChange,
                    placeholder = stringResource(R.string.change_password_current),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = PasswordVisualTransformation()
                )
                BookmateTextField(
                    value = state.newPassword,
                    onValueChange = viewModel::onNewChange,
                    placeholder = stringResource(R.string.change_password_new),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = PasswordVisualTransformation()
                )
                BookmateTextField(
                    value = state.confirm,
                    onValueChange = viewModel::onConfirmChange,
                    placeholder = stringResource(R.string.change_password_confirm),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = PasswordVisualTransformation()
                )
                state.errorRes?.let { errorRes ->
                    Text(
                        text = stringResource(errorRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                state.infoRes?.let { infoRes ->
                    Text(
                        text = stringResource(infoRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                PrimaryButton(
                    text = stringResource(R.string.change_password_save),
                    onClick = viewModel::onSaveClick,
                    enabled = !state.isSaving
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}