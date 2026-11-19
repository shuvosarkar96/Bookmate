package com.bookmate.app.ui.search

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookmate.app.R
import com.bookmate.app.domain.model.AccessType
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

data class SearchUiState(
    val query: String = "",
    val access: AccessType? = null,
    val genre: String? = null,
    val genres: List<String> = emptyList(),
    val results: List<Book> = emptyList(),
    val isLoading: Boolean = true,
    @StringRes val errorRes: Int? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {

    private var allBooks: List<Book> = emptyList()

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, errorRes = null) }
        viewModelScope.launch {
            bookRepository.getBooks()
                .onSuccess { books ->
                    allBooks = books
                    val genres = books.map { it.genre }.filter { it.isNotBlank() }.distinct().sorted()
                    updateFiltered { it.copy(isLoading = false, genres = genres) }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false, errorRes = R.string.error_unknown) }
                }
        }
    }

    fun onQueryChange(value: String) = updateFiltered { it.copy(query = value) }

    fun onAccessToggle(access: AccessType) =
        updateFiltered { it.copy(access = if (it.access == access) null else access) }

    fun onGenreChange(genre: String?) = updateFiltered { it.copy(genre = genre) }

    fun onAllClick() = updateFiltered { it.copy(access = null, genre = null) }

    private fun updateFiltered(transform: (SearchUiState) -> SearchUiState) {
        _uiState.update { current ->
            val next = transform(current)
            next.copy(results = filterBooks(allBooks, next.query, next.access, next.genre))
        }
    }
}