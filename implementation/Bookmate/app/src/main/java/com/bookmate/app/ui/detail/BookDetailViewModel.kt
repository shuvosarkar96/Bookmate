package com.bookmate.app.ui.detail

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookmate.app.R
import com.bookmate.app.domain.model.Book
import com.bookmate.app.domain.repository.BookRepository
import com.bookmate.app.domain.repository.SavedBooksRepository
import com.bookmate.app.domain.repository.SubscriptionRepository
import com.bookmate.app.ui.navigation.Routes
import com.bookmate.app.util.canReadBook
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BookDetailUiState(
    val isLoading: Boolean = true,
    val book: Book? = null,
    val notFound: Boolean = false,
    @StringRes val errorRes: Int? = null,
    val canRead: Boolean = false,
    val isSaved: Boolean = false,
    val saveFailed: Boolean = false
)

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val bookRepository: BookRepository,
    private val savedBooksRepository: SavedBooksRepository,
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    private val bookId: String = savedStateHandle.get<String>(Routes.BOOK_ID_ARG).orEmpty()

    private val _uiState = MutableStateFlow(BookDetailUiState())
    val uiState: StateFlow<BookDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, errorRes = null, notFound = false) }
        viewModelScope.launch {
            bookRepository.getBook(bookId)
                .onSuccess { book ->
                    if (book == null) {
                        _uiState.update { it.copy(isLoading = false, notFound = true) }
                    } else {
                        val saved = savedBooksRepository.isSaved(book.id).getOrDefault(false)
                        val hasActive = subscriptionRepository.getMySubscription().getOrNull()?.isActive() == true
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                book = book,
                                canRead = canReadBook(book, hasActive),
                                isSaved = saved
                            )
                        }
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false, errorRes = R.string.error_unknown) }
                }
        }
    }

    /** Re-checks the subscription, for example after coming back from Checkout. */
    fun refreshAccess() {
        val book = _uiState.value.book ?: return
        viewModelScope.launch {
            val hasActive = subscriptionRepository.getMySubscription().getOrNull()?.isActive() == true
            _uiState.update { it.copy(canRead = canReadBook(book, hasActive)) }
        }
    }

    fun onSaveToggle() {
        val book = _uiState.value.book ?: return
        val target = !_uiState.value.isSaved
        _uiState.update { it.copy(isSaved = target, saveFailed = false) }
        viewModelScope.launch {
            savedBooksRepository.setSaved(book, target).onFailure {
                _uiState.update { state -> state.copy(isSaved = !target, saveFailed = true) }
            }
        }
    }
}