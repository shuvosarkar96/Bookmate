package com.bookmate.app.ui.mybooks

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookmate.app.R
import com.bookmate.app.domain.model.MyBooks
import com.bookmate.app.domain.usecase.GetMyBooks
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class MyBooksTab { READING, SAVED, FINISHED }

data class MyBooksUiState(
    val isLoading: Boolean = true,
    val books: MyBooks? = null,
    @StringRes val errorRes: Int? = null,
    val tab: MyBooksTab = MyBooksTab.READING
)

@HiltViewModel
class MyBooksViewModel @Inject constructor(
    private val getMyBooks: GetMyBooks,
    private val tabRequest: MyBooksTabRequest
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyBooksUiState())
    val uiState: StateFlow<MyBooksUiState> = _uiState.asStateFlow()

    /** Called every time the screen is shown, so the lists are always fresh. */
    fun load() {
        tabRequest.consume()?.let { wanted -> _uiState.update { it.copy(tab = wanted) } }
        _uiState.update { it.copy(isLoading = it.books == null, errorRes = null) }
        viewModelScope.launch {
            getMyBooks().fold(
                onSuccess = { books -> _uiState.update { it.copy(isLoading = false, books = books) } },
                onFailure = {
                    // Keep the lists that are already shown; only an empty screen shows the error.
                    _uiState.update {
                        it.copy(isLoading = false, errorRes = if (it.books == null) R.string.error_unknown else null)
                    }
                }
            )
        }
    }

    fun selectTab(tab: MyBooksTab) {
        _uiState.update { it.copy(tab = tab) }
    }
}