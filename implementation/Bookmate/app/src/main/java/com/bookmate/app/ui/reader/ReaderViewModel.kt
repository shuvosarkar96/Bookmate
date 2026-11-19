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
import com.bookmate.app.domain.repository.ReadingProgressRepository
import com.bookmate.app.domain.repository.SubscriptionRepository
import com.bookmate.app.ui.navigation.Routes
import com.bookmate.app.util.canReadBook
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.NonCancellable
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
    /** Paragraph index to scroll to when the chapter opens (saved position, otherwise 0). */
    val resumeParagraph: Int = 0,
    val locked: Boolean = false,
    @StringRes val errorRes: Int? = null
)

@HiltViewModel
class ReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val bookRepository: BookRepository,
    private val chapterRepository: ChapterRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val progressRepository: ReadingProgressRepository
) : ViewModel() {

    private val bookId: String = savedStateHandle.get<String>(Routes.BOOK_ID_ARG).orEmpty()

    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    /** Index of the paragraph at the top of the screen. Kept out of the UI state on purpose. */
    private var currentParagraph = 0

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
            val progress = progressRepository.getProgress(bookId).getOrNull()
            val startChapter = (progress?.chapter ?: 1).coerceIn(1, book.chapterCount)
            val startParagraph =
                if (progress != null && progress.chapter == startChapter) progress.paragraph.coerceAtLeast(0) else 0
            _uiState.update { it.copy(book = book) }
            openChapter(startChapter, startParagraph)
        }
    }

    fun retry() {
        if (_uiState.value.book == null) {
            load()
        } else {
            viewModelScope.launch { openChapter(_uiState.value.chapterNo, 0) }
        }
    }

    fun nextChapter() {
        val state = _uiState.value
        val book = state.book ?: return
        if (state.chapterLoading || state.chapterNo >= book.chapterCount) return
        viewModelScope.launch { openChapter(state.chapterNo + 1, 0) }
    }

    fun previousChapter() {
        val state = _uiState.value
        if (state.chapterLoading || state.chapterNo <= 1) return
        viewModelScope.launch { openChapter(state.chapterNo - 1, 0) }
    }

    /** Called by the screen while the Member scrolls. */
    fun onParagraphChanged(index: Int) {
        currentParagraph = index
    }

    /** Saves the current chapter and paragraph. Called when leaving the Reader or the app. */
    fun savePosition() {
        val state = _uiState.value
        if (state.book == null || state.locked || state.chapter == null || state.chapterLoading) return
        saveInBackground(state.chapterNo, currentParagraph)
    }

    private suspend fun openChapter(number: Int, paragraph: Int) {
        _uiState.update {
            it.copy(
                chapterNo = number,
                resumeParagraph = paragraph,
                chapterLoading = true,
                errorRes = null
            )
        }
        chapterRepository.getChapter(bookId, number)
            .onSuccess { chapter ->
                if (chapter == null) {
                    fail(R.string.error_unknown)
                } else {
                    currentParagraph = paragraph
                    _uiState.update {
                        it.copy(isLoading = false, chapterLoading = false, chapter = chapter)
                    }
                    saveInBackground(number, paragraph)
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

    /** NonCancellable: the write must finish even if this ViewModel is cleared right after. */
    private fun saveInBackground(chapter: Int, paragraph: Int) {
        viewModelScope.launch(NonCancellable) {
            progressRepository.saveProgress(bookId, chapter, paragraph)
        }
    }

    private fun fail(@StringRes errorRes: Int) {
        _uiState.update { it.copy(isLoading = false, chapterLoading = false, errorRes = errorRes) }
    }
}