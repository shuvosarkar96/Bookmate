package com.bookmate.app.ui.home

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookmate.app.R
import com.bookmate.app.domain.model.Book
import com.bookmate.app.domain.repository.AuthRepository
import com.bookmate.app.domain.repository.BookRepository
import com.bookmate.app.util.pickRecommended
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val recommended: List<Book> = emptyList(),
    @StringRes val errorRes: Int? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, errorRes = null) }
        viewModelScope.launch {
            val favorites = authRepository.currentUser().getOrNull()?.favoriteGenres.orEmpty()
            bookRepository.getBooks()
                .onSuccess { books ->
                    _uiState.update {
                        it.copy(isLoading = false, recommended = pickRecommended(books, favorites))
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false, errorRes = R.string.error_unknown) }
                }
        }
    }
}