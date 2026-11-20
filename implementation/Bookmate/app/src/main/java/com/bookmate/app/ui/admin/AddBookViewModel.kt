package com.bookmate.app.ui.admin

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookmate.app.R
import com.bookmate.app.domain.model.AccessType
import com.bookmate.app.domain.model.NewBook
import com.bookmate.app.domain.repository.BookRepository
import com.bookmate.app.ui.navigation.Routes
import com.bookmate.app.ui.onboarding.Genre
import com.bookmate.app.util.BookFormProblem
import com.bookmate.app.util.checkBookForm
import com.bookmate.app.util.normalizeIsbn
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddBookUiState(
    val isbn: String = "",
    val title: String = "",
    val author: String = "",
    val genre: String = "",
    val premium: Boolean = false,
    val description: String = "",
    val coverUrl: String = "",
    val isSaving: Boolean = false,
    val isEdit: Boolean = false,
    val isLoading: Boolean = false,
    val done: Boolean = false,
    @StringRes val errorRes: Int? = null,
    /** Title of the book that was just added. Shown once, until the next change. */
    val savedTitle: String? = null
)

@HiltViewModel
class AddBookViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val bookRepository: BookRepository
) : ViewModel() {

    /** Set when the screen edits an existing book. Null when it adds a new one. */
    private val bookId: String? = savedStateHandle.get<String>(Routes.BOOK_ID_ARG)
    private var loaded = false

    private val _uiState = MutableStateFlow(AddBookUiState(isEdit = bookId != null, isLoading = bookId != null))
    val uiState: StateFlow<AddBookUiState> = _uiState.asStateFlow()

    init {
        if (bookId != null) load(bookId)
    }

    private fun load(id: String) {
        viewModelScope.launch {
            bookRepository.getBook(id)
                .onSuccess { book ->
                    if (book == null) {
                        _uiState.update { it.copy(isLoading = false, errorRes = R.string.error_unknown) }
                    } else {
                        loaded = true
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isbn = book.isbn,
                                title = book.title,
                                author = book.author,
                                genre = book.genre,
                                premium = book.accessType == AccessType.PREMIUM,
                                description = book.description,
                                coverUrl = book.coverUrl.orEmpty()
                            )
                        }
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false, errorRes = R.string.error_unknown) }
                }
        }
    }

    private fun change(transform: (AddBookUiState) -> AddBookUiState) {
        _uiState.update { transform(it).copy(errorRes = null, savedTitle = null) }
    }

    fun onIsbnChange(value: String) = change { it.copy(isbn = value) }
    fun onTitleChange(value: String) = change { it.copy(title = value) }
    fun onAuthorChange(value: String) = change { it.copy(author = value) }
    fun onGenreChange(value: Genre) = change { it.copy(genre = value.id) }
    fun onPremiumChange(value: Boolean) = change { it.copy(premium = value) }
    fun onDescriptionChange(value: String) = change { it.copy(description = value) }
    fun onCoverChange(value: String) = change { it.copy(coverUrl = value) }

    fun onSaveClick() {
        val state = _uiState.value
        if (state.isSaving) return
        if (bookId != null && !loaded) return
        val problem = checkBookForm(state.title, state.author, state.genre, state.isbn, state.coverUrl)
        if (problem != null) {
            _uiState.update { it.copy(errorRes = problem.messageRes()) }
            return
        }
        val book = NewBook(
            title = state.title.trim(),
            author = state.author.trim(),
            isbn = normalizeIsbn(state.isbn),
            genre = state.genre,
            description = state.description.trim(),
            coverUrl = state.coverUrl.trim().ifBlank { null },
            accessType = if (state.premium) AccessType.PREMIUM else AccessType.FREE
        )
        _uiState.update { it.copy(isSaving = true, errorRes = null, savedTitle = null) }
        viewModelScope.launch {
            val editId = bookId
            if (editId == null) {
                bookRepository.addBook(book)
                    .onSuccess { _uiState.value = AddBookUiState(savedTitle = book.title) }
                    .onFailure {
                        _uiState.update { s -> s.copy(isSaving = false, errorRes = R.string.error_unknown) }
                    }
            } else {
                bookRepository.updateBook(editId, book)
                    .onSuccess { _uiState.update { s -> s.copy(isSaving = false, done = true) } }
                    .onFailure {
                        _uiState.update { s -> s.copy(isSaving = false, errorRes = R.string.error_unknown) }
                    }
            }
        }
    }
}

@StringRes
private fun BookFormProblem.messageRes(): Int = when (this) {
    BookFormProblem.TITLE_EMPTY -> R.string.error_book_title_empty
    BookFormProblem.AUTHOR_EMPTY -> R.string.error_book_author_empty
    BookFormProblem.GENRE_EMPTY -> R.string.error_book_genre_empty
    BookFormProblem.ISBN_INVALID -> R.string.error_book_isbn_invalid
    BookFormProblem.COVER_INVALID -> R.string.error_book_cover_invalid
}