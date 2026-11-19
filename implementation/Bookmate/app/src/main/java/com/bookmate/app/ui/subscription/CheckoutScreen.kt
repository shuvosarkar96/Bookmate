package com.bookmate.app.ui.subscription

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bookmate.app.R
import com.bookmate.app.ui.components.BackChevronIcon
import com.bookmate.app.ui.components.PrimaryButton
import com.bookmate.app.ui.theme.CardSurface
import com.bookmate.app.ui.theme.DarkGreen
import com.bookmate.app.ui.theme.Dimens
import com.bookmate.app.ui.theme.Green
import com.bookmate.app.ui.theme.SecondaryContainerColor
import com.bookmate.app.ui.theme.TextPrimary
import com.bookmate.app.ui.theme.TextSecondary

@Composable
fun CheckoutScreen(
    onDone: () -> Unit,
    onCancel: () -> Unit,
    viewModel: CheckoutViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val backLabel = stringResource(R.string.action_back)
    val plan = state.plan

    LaunchedEffect(state.completed) {
        if (state.completed) onDone()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.SideMargin, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(CardSurface)
                    .semantics { contentDescription = backLabel }
                    .clickable(onClick = onCancel),
                contentAlignment = Alignment.Center
            ) {
                BackChevronIcon()
            }
            Text(
                text = stringResource(R.string.checkout_title),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        when {
            state.isLoading -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Green)
            }

            plan == null -> Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(state.errorRes ?: R.string.error_unknown),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = stringResource(R.string.home_retry),
                    style = MaterialTheme.typography.labelLarge,
                    color = DarkGreen,
                    modifier = Modifier
                        .clickable(onClick = viewModel::load)
                        .padding(16.dp)
                )
            }

            else -> Column(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = Dimens.SideMargin)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SecondaryContainerColor)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.checkout_demo_banner),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkGreen,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(CardSurface)
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.checkout_summary_title),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkGreen
                        )
                        SummaryRow(stringResource(R.string.checkout_plan), plan.name)
                        SummaryRow(stringResource(R.string.checkout_price), plan.priceLabel)
                        SummaryRow(
                            stringResource(R.string.checkout_period),
                            stringResource(R.string.sub_period, plan.periodDays)
                        )
                        SummaryRow(stringResource(R.string.checkout_new_end), state.newEndText)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.SideMargin)
                        .navigationBarsPadding()
                        .padding(top = 8.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    state.errorRes?.let {
                        Text(
                            text = stringResource(it),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    PrimaryButton(
                        text = stringResource(R.string.checkout_confirm),
                        onClick = viewModel::confirm,
                        enabled = !state.isSubmitting
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.ButtonHeight)
                            .clip(RoundedCornerShape(50))
                            .background(SecondaryContainerColor)
                            .clickable(enabled = !state.isSubmitting, onClick = onCancel),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.checkout_cancel),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 14.sp, color = TextSecondary)
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}