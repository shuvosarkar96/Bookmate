package com.bookmate.app.ui.admin

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookmate.app.R
import com.bookmate.app.domain.model.Book
import com.bookmate.app.domain.repository.BookRepository
import com.bookmate.app.util.filterBooks
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ManageBooksUiState(
    val isLoading: Boolean = true,
    /** Books that match the search text. */
    val books: List<Book> = emptyList(),
    /** Number of books in the catalog, before searching. */
    val total: Int = 0,
    val query: String = "",
    @StringRes val errorRes: Int? = null,
    /** The book waiting for the Admin to confirm its removal. */
    val confirmBook: Book? = null,
    val isRemoving: Boolean = false,
    val removedTitle: String? = null
)

@HiltViewModel
class ManageBooksViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {

    private var allBooks: List<Book> = emptyList()

    private val _uiState = MutableStateFlow(ManageBooksUiState())
    val uiState: StateFlow<ManageBooksUiState> = _uiState.asStateFlow()

    /** Called every time the screen is shown, so the list follows adds and edits. */
    fun load() {
        _uiState.update { it.copy(isLoading = allBooks.isEmpty(), errorRes = null) }
        viewModelScope.launch {
            bookRepository.getBooks()
                .onSuccess { books ->
                    allBooks = books
                    _uiState.update { refreshed(it).copy(isLoading = false) }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false, errorRes = R.string.error_unknown) }
                }
        }
    }

    fun onQueryChange(value: String) {
        _uiState.update { refreshed(it.copy(query = value)) }
    }

    fun onRemoveClick(book: Book) {
        _uiState.update { it.copy(confirmBook = book, removedTitle = null, errorRes = null) }
    }

    fun onDismissConfirm() {
        if (_uiState.value.isRemoving) return
        _uiState.update { it.copy(confirmBook = null) }
    }

    fun onConfirmRemove() {
        val book = _uiState.value.confirmBook ?: return
        if (_uiState.value.isRemoving) return
        _uiState.update { it.copy(isRemoving = true) }
        viewModelScope.launch {
            bookRepository.deleteBook(book.id)
                .onSuccess {
                    allBooks = allBooks.filter { it.id != book.id }
                    _uiState.update {
                        refreshed(it).copy(confirmBook = null, isRemoving = false, removedTitle = book.title)
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(confirmBook = null, isRemoving = false, errorRes = R.string.error_unknown)
                    }
                }
        }
    }

    private fun refreshed(state: ManageBooksUiState): ManageBooksUiState =
        state.copy(books = filterBooks(allBooks, state.query, null, null), total = allBooks.size)
}