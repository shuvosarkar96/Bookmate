package com.bookmate.app.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bookmate.app.R
import com.bookmate.app.ui.components.BookmateTextField
import com.bookmate.app.ui.theme.CardSurface
import com.bookmate.app.ui.theme.Cream
import com.bookmate.app.ui.theme.DarkGreen
import com.bookmate.app.ui.theme.Dimens
import com.bookmate.app.ui.theme.Green
import com.bookmate.app.ui.theme.TextSecondary

/** Admin: Plan price. Shows the current price and saves a new one. */
@Composable
fun PlanPriceScreen(
    onBack: () -> Unit,
    viewModel: PlanPriceViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val backLabel = stringResource(R.string.action_back)

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
                text = stringResource(R.string.planprice_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Green)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(CardSurface)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.planprice_current),
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                if (state.planName.isNotBlank()) {
                    Text(
                        text = state.planName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkGreen,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Text(
                    text = state.currentLabel,
                    fontSize = 16.sp,
                    color = DarkGreen,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.planprice_input),
                style = MaterialTheme.typography.labelLarge,
                color = DarkGreen
            )
            Spacer(modifier = Modifier.height(8.dp))
            BookmateTextField(
                value = state.input,
                onValueChange = viewModel::onInputChange,
                placeholder = stringResource(R.string.planprice_hint)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.planprice_note),
                fontSize = 13.sp,
                color = TextSecondary
            )
            state.errorRes?.let { errorRes ->
                Text(
                    text = stringResource(errorRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
            state.savedLabel?.let { label ->
                Text(
                    text = stringResource(R.string.planprice_saved, label),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = viewModel::onSave,
                enabled = !state.isSaving && state.input.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkGreen, contentColor = Cream)
            ) {
                Text(text = stringResource(R.string.planprice_save))
            }
        }
    }
}