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
import com.bookmate.app.domain.repository.SettingsRepository
import com.bookmate.app.domain.repository.SubscriptionRepository
import com.bookmate.app.domain.ai.AiChatSession
import com.bookmate.app.domain.ai.ChatAction
import com.bookmate.app.domain.ai.ChatContext
import com.bookmate.app.domain.ai.VirtualPages
import com.bookmate.app.ui.navigation.Routes
import com.bookmate.app.util.canReadBook
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
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
    val finished: Boolean = false,
    val finishFailed: Boolean = false,
    val aiSheetOpen: Boolean = false,
    /** Virtual page (about 250 words) shown in the AI sheet. */
    val aiPage: Int = 1,
    @StringRes val errorRes: Int? = null
)

@HiltViewModel
class ReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val bookRepository: BookRepository,
    private val chapterRepository: ChapterRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val progressRepository: ReadingProgressRepository,
    private val settingsRepository: SettingsRepository,
    private val chatSession: AiChatSession
) : ViewModel() {

    private val bookId: String = savedStateHandle.get<String>(Routes.BOOK_ID_ARG).orEmpty()

    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    /** Reading text size in sp, from DataStore. */
    val textSize: StateFlow<Int> = settingsRepository.readingTextSize
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsRepository.DEFAULT_TEXT_SIZE)

    /** The Aa button: next size in the list, then back to the first. */
    fun cycleTextSize() {
        val sizes = SettingsRepository.TEXT_SIZES
        val next = sizes[(sizes.indexOf(textSize.value) + 1) % sizes.size]
        viewModelScope.launch { settingsRepository.setReadingTextSize(next) }
    }

    /** Index of the paragraph at the top of the screen. Kept out of the UI state on purpose. */
    private var currentParagraph = 0

    // ---- AI help: the sheet only chooses; the answer streams in the Book Chat ----

    fun openAiSheet() {
        val chapter = _uiState.value.chapter ?: return
        val page = VirtualPages.pageOf(chapter.paragraphs, currentParagraph)
        _uiState.update { it.copy(aiSheetOpen = true, aiPage = page) }
    }

    fun closeAiSheet() {
        _uiState.update { it.copy(aiSheetOpen = false) }
    }

    /** Hands the choice to the Book Chat and closes the sheet. The screen then opens the AI tab. */
    fun startChat(action: ChatAction?) {
        val state = _uiState.value
        val book = state.book ?: return
        chatSession.open(ChatContext(book, state.chapterNo, currentParagraph), action)
        _uiState.update { it.copy(aiSheetOpen = false) }
    }

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
            _uiState.update { it.copy(book = book, finished = progress?.finished == true) }
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

    /** The Finish button on the last chapter. */
    fun markFinished() {
        val state = _uiState.value
        if (state.book == null || state.finished) return
        _uiState.update { it.copy(finishFailed = false) }
        viewModelScope.launch {
            progressRepository.markFinished(bookId, state.chapterNo)
                .onSuccess { _uiState.update { s -> s.copy(finished = true) } }
                .onFailure { _uiState.update { s -> s.copy(finishFailed = true) } }
        }
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