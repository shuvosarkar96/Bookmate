package com.bookmate.app.ui.auth

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookmate.app.R
import com.bookmate.app.domain.model.AppUser
import com.bookmate.app.domain.model.AuthException
import com.bookmate.app.domain.repository.AuthRepository
import com.bookmate.app.util.isValidEmail
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    @StringRes val errorRes: Int? = null,
    val registeredUser: AppUser? = null
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value, errorRes = null) }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, errorRes = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, errorRes = null) }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { it.copy(confirmPassword = value, errorRes = null) }
    }

    fun onRegisterClick() {
        val current = _uiState.value
        if (current.isLoading) return
        val error = when {
            current.name.isBlank() -> R.string.error_name_empty
            !isValidEmail(current.email) -> R.string.error_email_invalid
            current.password.length < 8 -> R.string.error_password_short
            current.password != current.confirmPassword -> R.string.error_password_mismatch
            else -> null
        }
        if (error != null) {
            _uiState.update { it.copy(errorRes = error) }
            return
        }
        _uiState.update { it.copy(isLoading = true, errorRes = null) }
        viewModelScope.launch {
            authRepository.register(current.name.trim(), current.email.trim(), current.password)
                .onSuccess { user ->
                    _uiState.update { it.copy(isLoading = false, registeredUser = user) }
                }
                .onFailure { throwable ->
                    val failure = (throwable as? AuthException)?.failure
                    _uiState.update { it.copy(isLoading = false, errorRes = failure.messageRes()) }
                }
        }
    }
}