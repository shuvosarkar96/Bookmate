package com.bookmate.app.ui.profile

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookmate.app.R
import com.bookmate.app.domain.ai.AiChatSession
import com.bookmate.app.domain.model.AppUser
import com.bookmate.app.domain.model.Book
import com.bookmate.app.domain.repository.AuthRepository
import com.bookmate.app.domain.repository.SubscriptionRepository
import com.bookmate.app.domain.usecase.GetMyBooks
import com.bookmate.app.ui.mybooks.MyBooksTab
import com.bookmate.app.ui.mybooks.MyBooksTabRequest
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
    val currentChapter: Int = 0,
    /** Counts for the stats row. Null when the lists could not be loaded. */
    val readingCount: Int? = null,
    val finishedCount: Int? = null,
    val savedCount: Int? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val getMyBooks: GetMyBooks,
    private val tabRequest: MyBooksTabRequest,
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
            val myBooks = getMyBooks().getOrNull()
            val current = myBooks?.reading?.firstOrNull()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    user = user,
                    subscription = status,
                    endDateText = subscription?.let { s -> formatDate(s.endDate) }.orEmpty(),
                    currentBook = current?.book,
                    currentChapter = current?.progress?.chapter ?: 0,
                    readingCount = myBooks?.reading?.size,
                    finishedCount = myBooks?.finishedCount,
                    savedCount = myBooks?.saved?.size
                )
            }
        }
    }

    /** Asks the My Books screen to open on this tab the next time it is shown. */
    fun requestMyBooksTab(tab: MyBooksTab) {
        tabRequest.request(tab)
    }

    /** Signs out and forgets the AI chat. The caller then restarts the app. */
    fun logout() {
        authRepository.logout()
        chatSession.clear()
    }
}