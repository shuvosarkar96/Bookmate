package com.bookmate.app.ui.profile

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookmate.app.R
import com.bookmate.app.domain.ai.AiChatSession
import com.bookmate.app.domain.model.AppUser
import com.bookmate.app.domain.model.Book
import com.bookmate.app.domain.repository.AuthRepository
import com.bookmate.app.domain.repository.BookRepository
import com.bookmate.app.domain.repository.ReadingProgressRepository
import com.bookmate.app.domain.repository.SubscriptionRepository
import com.bookmate.app.ui.subscription.SubscriptionStatus
import com.bookmate.app.util.formatDate
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: AppUser? = null,
    @StringRes val errorRes: Int? = null,
    val subscription: SubscriptionStatus = SubscriptionStatus.NONE,
    val endDateText: String = "",
    val currentBook: Book? = null,
    val currentChapter: Int = 0
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val readingProgressRepository: ReadingProgressRepository,
    private val bookRepository: BookRepository,
    private val chatSession: AiChatSession
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    /** Called every time the screen is shown, so subscription and reading are always fresh. */
    fun load() {
        _uiState.update { it.copy(isLoading = it.user == null, errorRes = null) }
        viewModelScope.launch {
            val user = authRepository.currentUser().getOrNull()
            if (user == null) {
                _uiState.update { it.copy(isLoading = false, errorRes = R.string.error_unknown) }
                return@launch
            }
            val subscription = subscriptionRepository.getMySubscription().getOrNull()
            val status = when {
                subscription == null -> SubscriptionStatus.NONE
                subscription.isActive() -> SubscriptionStatus.ACTIVE
                else -> SubscriptionStatus.EXPIRED
            }
            val latest = readingProgressRepository.getLatestInProgress().getOrNull()
            val book = latest?.let { bookRepository.getBook(it.bookId).getOrNull() }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    user = user,
                    subscription = status,
                    endDateText = subscription?.let { s -> formatDate(s.endDate) }.orEmpty(),
                    currentBook = book,
                    currentChapter = latest?.progress?.chapter ?: 0
                )
            }
        }
    }

    /** Signs out and forgets the AI chat. The caller then restarts the app. */
    fun logout() {
        authRepository.logout()
        chatSession.clear()
    }
}