package com.bookmate.app.ui.reader

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookmate.app.R
import com.bookmate.app.domain.model.AccessDeniedException
import com.bookmate.app.domain.model.Book
import com.bookmate.app.domain.model.Chapter
import com.bookmate.app.domain.repository.BookRepository
import com.bookmate.app.domain.repository.ChapterRepository
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

data class ReaderUiState(
    val isLoading: Boolean = true,
    val book: Book? = null,
    val chapterNo: Int = 1,
    val chapter: Chapter? = null,
    val chapterLoading: Boolean = false,
    val locked: Boolean = false,
    @StringRes val errorRes: Int? = null
)

@HiltViewModel
class ReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val bookRepository: BookRepository,
    private val chapterRepository: ChapterRepository,
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    private val bookId: String = savedStateHandle.get<String>(Routes.BOOK_ID_ARG).orEmpty()

    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, errorRes = null, locked = false) }
        viewModelScope.launch {
            val bookResult = bookRepository.getBook(bookId)
            val book = bookResult.getOrNull()
            if (bookResult.isFailure) {
                fail(R.string.error_unknown)
                return@launch
            }
            if (book == null) {
                fail(R.string.detail_not_found)
                return@launch
            }
            if (!book.hasFullText || book.chapterCount < 1) {
                fail(R.string.reader_no_text)
                return@launch
            }
            val hasActive = subscriptionRepository.getMySubscription().getOrNull()?.isActive() == true
            if (!canReadBook(book, hasActive)) {
                _uiState.update { it.copy(isLoading = false, book = book, locked = true) }
                return@launch
            }
            _uiState.update { it.copy(book = book) }
            openChapter(1)
        }
    }

    fun retry() {
        if (_uiState.value.book == null) {
            load()
        } else {
            viewModelScope.launch { openChapter(_uiState.value.chapterNo) }
        }
    }

    fun nextChapter() {
        val state = _uiState.value
        val book = state.book ?: return
        if (state.chapterLoading || state.chapterNo >= book.chapterCount) return
        viewModelScope.launch { openChapter(state.chapterNo + 1) }
    }

    fun previousChapter() {
        val state = _uiState.value
        if (state.chapterLoading || state.chapterNo <= 1) return
        viewModelScope.launch { openChapter(state.chapterNo - 1) }
    }

    private suspend fun openChapter(number: Int) {
        _uiState.update { it.copy(chapterNo = number, chapterLoading = true, errorRes = null) }
        chapterRepository.getChapter(bookId, number)
            .onSuccess { chapter ->
                if (chapter == null) {
                    fail(R.string.error_unknown)
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, chapterLoading = false, chapter = chapter)
                    }
                }
            }
            .onFailure { error ->
                if (error is AccessDeniedException) {
                    _uiState.update {
                        it.copy(isLoading = false, chapterLoading = false, locked = true)
                    }
                } else {
                    fail(R.string.error_unknown)
                }
            }
    }

    private fun fail(@StringRes errorRes: Int) {
        _uiState.update { it.copy(isLoading = false, chapterLoading = false, errorRes = errorRes) }
    }
}