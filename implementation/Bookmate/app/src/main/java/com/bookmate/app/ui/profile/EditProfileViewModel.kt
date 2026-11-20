package com.bookmate.app.ui.profile

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookmate.app.R
import com.bookmate.app.domain.model.AuthException
import com.bookmate.app.domain.repository.AuthRepository
import com.bookmate.app.ui.auth.messageRes
import com.bookmate.app.ui.onboarding.Genre
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditProfileUiState(
    val isLoading: Boolean = true,
    /** True once the current profile was read. Saving is only possible after that. */
    val loaded: Boolean = false,
    val name: String = "",
    val selected: Set<Genre> = emptySet(),
    val isSaving: Boolean = false,
    @StringRes val errorRes: Int? = null,
    val saved: Boolean = false
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    /** Saved genre ids that are not in the current genre list. They are kept on save. */
    private var otherGenreIds: List<String> = emptyList()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val user = authRepository.currentUser().getOrNull()
            if (user == null) {
                _uiState.update { it.copy(isLoading = false, errorRes = R.string.error_unknown) }
                return@launch
            }
            otherGenreIds = user.favoriteGenres.filter { id -> Genre.entries.none { it.id == id } }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    loaded = true,
                    name = user.name,
                    selected = Genre.entries.filter { g -> g.id in user.favoriteGenres }.toSet()
                )
            }
        }
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value, errorRes = null) }
    }

    fun onToggle(genre: Genre) {
        _uiState.update {
            val next = if (genre in it.selected) it.selected - genre else it.selected + genre
            it.copy(selected = next, errorRes = null)
        }
    }

    fun onSaveClick() {
        val current = _uiState.value
        if (!current.loaded || current.isSaving) return
        if (current.name.isBlank()) {
            _uiState.update { it.copy(errorRes = R.string.error_name_empty) }
            return
        }
        val ids = Genre.entries.filter { it in current.selected }.map { it.id } + otherGenreIds
        _uiState.update { it.copy(isSaving = true, errorRes = null) }
        viewModelScope.launch {
            authRepository.updateProfile(current.name.trim(), ids)
                .onSuccess {
                    _uiState.update { s -> s.copy(isSaving = false, saved = true) }
                }
                .onFailure { error ->
                    val failure = (error as? AuthException)?.failure
                    _uiState.update { s -> s.copy(isSaving = false, errorRes = failure.messageRes()) }
                }
        }
    }
}