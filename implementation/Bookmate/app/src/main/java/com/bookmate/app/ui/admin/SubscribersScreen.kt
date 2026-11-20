package com.bookmate.app.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bookmate.app.R
import com.bookmate.app.domain.model.MemberSubscription
import com.bookmate.app.ui.components.BookmateTextField
import com.bookmate.app.ui.subscription.SubscriptionStatus
import com.bookmate.app.ui.theme.CardSurface
import com.bookmate.app.ui.theme.Cream
import com.bookmate.app.ui.theme.DarkGreen
import com.bookmate.app.ui.theme.Dimens
import com.bookmate.app.ui.theme.Green
import com.bookmate.app.ui.theme.SecondaryContainerColor
import com.bookmate.app.ui.theme.TextPrimary
import com.bookmate.app.ui.theme.TextSecondary
import com.bookmate.app.util.formatDate

/** Admin: Subscribers. Members with their subscription and the actions Activate, Extend, End. */
@Composable
fun SubscribersScreen(
    onBack: () -> Unit,
    viewModel: SubscribersViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val backLabel = stringResource(R.string.action_back)
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.load() }

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
                text = stringResource(R.string.screen_admin_subscribers),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        BookmateTextField(
            value = state.query,
            onValueChange = viewModel::onQueryChange,
            placeholder = stringResource(R.string.subscribers_search)
        )
        Spacer(modifier = Modifier.height(12.dp))

        state.noticeRes?.let { noticeRes ->
            Text(
                text = stringResource(noticeRes, state.noticeName),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        state.errorRes?.let { errorRes ->
            Text(
                text = stringResource(errorRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            if (state.total == 0) {
                Text(
                    text = stringResource(R.string.home_retry),
                    style = MaterialTheme.typography.labelLarge,
                    color = DarkGreen,
                    modifier = Modifier
                        .clickable(onClick = viewModel::load)
                        .padding(vertical = 12.dp)
                )
            }
        }

        when {
            state.isLoading -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Green)
            }

            state.members.isEmpty() && state.errorRes == null -> Text(
                text = stringResource(R.string.subscribers_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            else -> LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(state.members, key = { it.userId }) { member ->
                    MemberCard(
                        member = member,
                        periodDays = state.periodDays,
                        enabled = state.busyUserId == null,
                        onActivate = { viewModel.onActivate(member) },
                        onExtend = { viewModel.onExtend(member) },
                        onEnd = { viewModel.onEndClick(member) }
                    )
                }
            }
        }
    }

    state.confirmEnd?.let { member ->
        AlertDialog(
            onDismissRequest = viewModel::onDismissEnd,
            title = { Text(stringResource(R.string.subscribers_end_title)) },
            text = {
                Text(stringResource(R.string.subscribers_end_text, member.name.ifBlank { member.email }))
            },
            confirmButton = {
                TextButton(onClick = viewModel::onConfirmEnd) {
                    Text(
                        text = stringResource(R.string.subscribers_end),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissEnd) {
                    Text(stringResource(R.string.add_book_cancel))
                }
            }
        )
    }
}

@Composable
private fun MemberCard(
    member: MemberSubscription,
    periodDays: Int,
    enabled: Boolean,
    onActivate: () -> Unit,
    onExtend: () -> Unit,
    onEnd: () -> Unit
) {
    val subscription = member.subscription
    val status = when {
        subscription == null -> SubscriptionStatus.NONE
        subscription.isActive() -> SubscriptionStatus.ACTIVE
        else -> SubscriptionStatus.EXPIRED
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardSurface)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.name.ifBlank { member.email },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkGreen,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (member.name.isNotBlank()) {
                    Text(
                        text = member.email,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            StatusChip(status)
        }
        if (subscription != null) {
            Text(
                text = stringResource(
                    if (status == SubscriptionStatus.ACTIVE) R.string.subscribers_ends else R.string.subscribers_ended,
                    formatDate(subscription.endDate)
                ),
                fontSize = 13.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            if (status == SubscriptionStatus.ACTIVE) {
                ActionText(stringResource(R.string.subscribers_extend, periodDays), DarkGreen, enabled, onExtend)
                ActionText(stringResource(R.string.subscribers_end), MaterialTheme.colorScheme.error, enabled, onEnd)
            } else {
                ActionText(stringResource(R.string.subscribers_activate, periodDays), DarkGreen, enabled, onActivate)
            }
        }
    }
}

@Composable
private fun ActionText(text: String, color: Color, enabled: Boolean, onClick: () -> Unit) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = if (enabled) color else TextSecondary,
        modifier = Modifier
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 8.dp)
    )
}

@Composable
private fun StatusChip(status: SubscriptionStatus) {
    val active = status == SubscriptionStatus.ACTIVE
    Box(
        modifier = Modifier
            .height(24.dp)
            .clip(RoundedCornerShape(50))
            .background(if (active) Green else SecondaryContainerColor)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(
                when (status) {
                    SubscriptionStatus.ACTIVE -> R.string.subscribers_status_active
                    SubscriptionStatus.EXPIRED -> R.string.subscribers_status_expired
                    SubscriptionStatus.NONE -> R.string.subscribers_status_none
                }
            ),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (active) Cream else TextPrimary
        )
    }
}