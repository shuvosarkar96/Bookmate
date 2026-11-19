package com.bookmate.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookmate.app.domain.model.Book
import com.bookmate.app.domain.repository.BookRepository
import com.bookmate.app.domain.repository.ReadingProgressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ContinueReadingItem(
    val book: Book,
    val chapter: Int,
    val chapterCount: Int
)

data class ContinueReadingUiState(
    val item: ContinueReadingItem? = null
)

@HiltViewModel
class ContinueReadingViewModel @Inject constructor(
    private val progressRepository: ReadingProgressRepository,
    private val bookRepository: BookRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContinueReadingUiState())
    val uiState: StateFlow<ContinueReadingUiState> = _uiState.asStateFlow()

    /** Called every time Home is shown, so the card follows the latest reading. */
    fun load() {
        viewModelScope.launch {
            val latestResult = progressRepository.getLatestInProgress()
            if (latestResult.isFailure) return@launch
            val latest = latestResult.getOrNull()
            val book = latest?.let { bookRepository.getBook(it.bookId).getOrNull() }
            val item = if (latest != null && book != null && book.hasFullText && book.chapterCount > 0) {
                ContinueReadingItem(
                    book = book,
                    chapter = latest.progress.chapter.coerceIn(1, book.chapterCount),
                    chapterCount = book.chapterCount
                )
            } else {
                null
            }
            _uiState.update { it.copy(item = item) }
        }
    }
}