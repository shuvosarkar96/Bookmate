package com.bookmate.app.ui.subscription

import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bookmate.app.R
import com.bookmate.app.domain.model.Plan
import com.bookmate.app.ui.components.BackChevronIcon
import com.bookmate.app.ui.components.PrimaryButton
import com.bookmate.app.ui.theme.CardSurface
import com.bookmate.app.ui.theme.DarkGreen
import com.bookmate.app.ui.theme.Dimens
import com.bookmate.app.ui.theme.Green
import com.bookmate.app.ui.theme.TextPrimary
import com.bookmate.app.ui.theme.TextSecondary

@Composable
fun SubscriptionScreen(
    onBack: () -> Unit,
    onSubscribe: () -> Unit,
    viewModel: SubscriptionViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.load() }

    val backLabel = stringResource(R.string.action_back)
    val plan = state.plan

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
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                BackChevronIcon()
            }
            Text(
                text = stringResource(R.string.sub_title),
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
                    PlanCard(plan = plan)
                    Spacer(modifier = Modifier.height(12.dp))
                    StatusCard(status = state.status, endDateText = state.endDateText)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.SideMargin)
                        .navigationBarsPadding()
                        .padding(top = 8.dp, bottom = 16.dp)
                ) {
                    PrimaryButton(
                        text = stringResource(
                            if (state.status == SubscriptionStatus.ACTIVE) R.string.sub_renew
                            else R.string.sub_subscribe
                        ),
                        onClick = onSubscribe,
                        enabled = true
                    )
                }
            }
        }
    }
}

@Composable
private fun PlanCard(plan: Plan) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardSurface)
            .padding(20.dp)
    ) {
        Text(
            text = plan.name,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = DarkGreen
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = plan.priceLabel,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = stringResource(R.string.sub_period, plan.periodDays),
            fontSize = 13.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(16.dp))
        BenefitRow(text = stringResource(R.string.sub_benefit_read))
        Spacer(modifier = Modifier.height(10.dp))
        BenefitRow(text = stringResource(R.string.sub_benefit_ai))
    }
}

@Composable
private fun StatusCard(status: SubscriptionStatus, endDateText: String) {
    val text = when (status) {
        SubscriptionStatus.NONE -> stringResource(R.string.sub_status_none)
        SubscriptionStatus.ACTIVE -> stringResource(R.string.sub_status_active, endDateText)
        SubscriptionStatus.EXPIRED -> stringResource(R.string.sub_status_expired, endDateText)
    }
    val color = when (status) {
        SubscriptionStatus.ACTIVE -> DarkGreen
        SubscriptionStatus.EXPIRED -> MaterialTheme.colorScheme.error
        SubscriptionStatus.NONE -> TextPrimary
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardSurface)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.sub_status_label),
            fontSize = 12.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun BenefitRow(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Canvas(modifier = Modifier.size(18.dp)) {
            val s = size.width / 18f
            val check = Path().apply {
                moveTo(3f * s, 9.5f * s)
                lineTo(7.5f * s, 14f * s)
                lineTo(15f * s, 4.5f * s)
            }
            drawPath(
                path = check,
                color = Green,
                style = Stroke(width = 2.2f * s, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
        Text(text = text, fontSize = 14.sp, color = TextPrimary)
    }
}