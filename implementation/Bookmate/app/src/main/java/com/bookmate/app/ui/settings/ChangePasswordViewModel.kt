package com.bookmate.app.ui.settings

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookmate.app.R
import com.bookmate.app.domain.model.AuthException
import com.bookmate.app.domain.repository.AuthRepository
import com.bookmate.app.ui.auth.messageRes
import com.bookmate.app.util.PasswordProblem
import com.bookmate.app.util.checkNewPassword
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChangePasswordUiState(
    val current: String = "",
    val newPassword: String = "",
    val confirm: String = "",
    val isSaving: Boolean = false,
    @StringRes val errorRes: Int? = null,
    @StringRes val infoRes: Int? = null
)

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()

    fun onCurrentChange(value: String) {
        _uiState.update { it.copy(current = value, errorRes = null, infoRes = null) }
    }

    fun onNewChange(value: String) {
        _uiState.update { it.copy(newPassword = value, errorRes = null, infoRes = null) }
    }

    fun onConfirmChange(value: String) {
        _uiState.update { it.copy(confirm = value, errorRes = null, infoRes = null) }
    }

    fun onSaveClick() {
        val state = _uiState.value
        if (state.isSaving) return
        val problem = checkNewPassword(state.current, state.newPassword, state.confirm)
        if (problem != null) {
            _uiState.update { it.copy(errorRes = problem.messageRes(), infoRes = null) }
            return
        }
        _uiState.update { it.copy(isSaving = true, errorRes = null, infoRes = null) }
        viewModelScope.launch {
            authRepository.changePassword(state.current, state.newPassword)
                .onSuccess {
                    // Clear all fields so no password stays in memory.
                    _uiState.value = ChangePasswordUiState(infoRes = R.string.change_password_done)
                }
                .onFailure { error ->
                    val failure = (error as? AuthException)?.failure
                    _uiState.update { s -> s.copy(isSaving = false, errorRes = failure.messageRes()) }
                }
        }
    }
}

@StringRes
private fun PasswordProblem.messageRes(): Int = when (this) {
    PasswordProblem.CURRENT_EMPTY -> R.string.error_password_empty
    PasswordProblem.TOO_SHORT -> R.string.error_password_short
    PasswordProblem.MISMATCH -> R.string.error_password_mismatch
    PasswordProblem.SAME_AS_CURRENT -> R.string.error_password_same
}