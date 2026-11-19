package com.bookmate.app.ui.onboarding

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookmate.app.domain.model.AuthException
import com.bookmate.app.domain.repository.AuthRepository
import com.bookmate.app.ui.auth.messageRes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val selected: Set<Genre> = emptySet(),
    val isSaving: Boolean = false,
    @StringRes val errorRes: Int? = null,
    val finished: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onToggle(genre: Genre) {
        _uiState.update {
            val next = if (genre in it.selected) it.selected - genre else it.selected + genre
            it.copy(selected = next, errorRes = null)
        }
    }

    fun onContinue() {
        val ids = Genre.entries.filter { it in _uiState.value.selected }.map { it.id }
        save(ids)
    }

    fun onSkip() {
        save(emptyList())
    }

    private fun save(genreIds: List<String>) {
        if (_uiState.value.isSaving) return
        _uiState.update { it.copy(isSaving = true, errorRes = null) }
        viewModelScope.launch {
            authRepository.saveOnboarding(genreIds)
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false, finished = true) }
                }
                .onFailure { throwable ->
                    val failure = (throwable as? AuthException)?.failure
                    _uiState.update { it.copy(isSaving = false, errorRes = failure.messageRes()) }
                }
        }
    }
}