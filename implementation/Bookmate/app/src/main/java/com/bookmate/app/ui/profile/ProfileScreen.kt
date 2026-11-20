package com.bookmate.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bookmate.app.R
import com.bookmate.app.domain.model.AppUser
import com.bookmate.app.domain.model.Role
import com.bookmate.app.ui.components.PrimaryButton
import com.bookmate.app.ui.subscription.SubscriptionStatus
import com.bookmate.app.ui.theme.CardSurface
import com.bookmate.app.ui.theme.Cream
import com.bookmate.app.ui.theme.DarkGreen
import com.bookmate.app.ui.theme.Dimens
import com.bookmate.app.ui.theme.Green
import com.bookmate.app.ui.theme.TextPrimary
import com.bookmate.app.ui.theme.TextSecondary
import com.bookmate.app.util.initialsOf

/** SCR Profile: account, reading, subscription row, links, and Admin tools for Admin. */
@Composable
fun ProfileScreen(
    onSubscribe: () -> Unit,
    onContinue: (String) -> Unit,
    onOpenMyBooks: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAdmin: (String) -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.load() }
    val user = state.user

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.SideMargin)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.nav_profile),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(20.dp))

        when {
            state.isLoading -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Green)
            }

            user == null -> Column(
                modifier = Modifier.fillMaxWidth(),
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

            else -> {
                HeaderCard(user)
                Spacer(modifier = Modifier.height(12.dp))
                GenresCard(user.favoriteGenres)
                Spacer(modifier = Modifier.height(12.dp))
                CurrentBookCard(
                    title = state.currentBook?.title,
                    author = state.currentBook?.author.orEmpty(),
                    chapter = state.currentChapter,
                    chapterCount = state.currentBook?.chapterCount ?: 0,
                    onClick = { state.currentBook?.let { onContinue(it.id) } }
                )
                Spacer(modifier = Modifier.height(12.dp))
                SubscriptionCard(
                    status = state.subscription,
                    endDateText = state.endDateText,
                    onSubscribe = onSubscribe
                )
                Spacer(modifier = Modifier.height(12.dp))
                CardBox {
                    ListRow(stringResource(R.string.profile_saved), onOpenMyBooks)
                    ListRow(stringResource(R.string.profile_finished), onOpenMyBooks)
                    ListRow(stringResource(R.string.profile_notifications), onOpenNotifications)
                    ListRow(stringResource(R.string.screen_settings), onOpenSettings)
                    ListRow(
                        text = stringResource(R.string.profile_logout),
                        onClick = {
                            viewModel.logout()
                            onLoggedOut()
                        },
                        color = MaterialTheme.colorScheme.error
                    )
                }
                if (user.role == Role.ADMIN) {
                    Spacer(modifier = Modifier.height(12.dp))
                    CardBox {
                        Text(
                            text = stringResource(R.string.profile_admin_title),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkGreen
                        )
                        ListRow(stringResource(R.string.screen_admin_add_book), onClick = { onOpenAdmin("admin/add_book") })
                        ListRow(stringResource(R.string.screen_admin_manage_books), onClick = { onOpenAdmin("admin/manage_books") })
                        ListRow(stringResource(R.string.screen_admin_subscribers), onClick = { onOpenAdmin("admin/subscribers") })
                        ListRow(stringResource(R.string.screen_admin_plan_price), onClick = { onOpenAdmin("admin/plan_price") })
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(Dimens.NavHeight + 48.dp))
    }
}

@Composable
private fun CardBox(onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardSurface)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        content = content
    )
}

@Composable
private fun HeaderCard(user: AppUser) {
    CardBox {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Green),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initialsOf(user.name, user.email),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Cream
                )
            }
            Column {
                Text(
                    text = user.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(text = user.email, fontSize = 14.sp, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun GenresCard(genres: List<String>) {
    CardBox {
        Text(
            text = stringResource(R.string.profile_genres_title),
            fontSize = 12.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (genres.isEmpty()) stringResource(R.string.profile_genres_none) else genres.joinToString(", "),
            fontSize = 15.sp,
            color = TextPrimary
        )
    }
}

@Composable
private fun CurrentBookCard(
    title: String?,
    author: String,
    chapter: Int,
    chapterCount: Int,
    onClick: () -> Unit
) {
    CardBox(onClick = if (title != null) onClick else null) {
        Text(
            text = stringResource(R.string.profile_current_title),
            fontSize = 12.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        if (title == null) {
            Text(
                text = stringResource(R.string.profile_current_none),
                fontSize = 15.sp,
                color = TextPrimary
            )
        } else {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkGreen)
            Text(text = author, fontSize = 13.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.profile_current_chapter, chapter, chapterCount),
                fontSize = 14.sp,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun SubscriptionCard(
    status: SubscriptionStatus,
    endDateText: String,
    onSubscribe: () -> Unit
) {
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
    CardBox {
        Text(
            text = stringResource(R.string.profile_sub_title),
            fontSize = 12.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
        if (status != SubscriptionStatus.ACTIVE) {
            Spacer(modifier = Modifier.height(12.dp))
            PrimaryButton(
                text = stringResource(
                    if (status == SubscriptionStatus.EXPIRED) R.string.sub_renew else R.string.sub_subscribe
                ),
                onClick = onSubscribe,
                enabled = true
            )
        }
    }
}

@Composable
private fun ListRow(text: String, onClick: () -> Unit, color: Color = TextPrimary) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = text, fontSize = 16.sp, color = color)
        Text(text = "›", fontSize = 20.sp, color = TextSecondary)
    }
}