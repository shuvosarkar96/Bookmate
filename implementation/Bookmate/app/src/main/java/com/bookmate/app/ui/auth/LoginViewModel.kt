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

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    @StringRes val errorRes: Int? = null,
    val loggedInUser: AppUser? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, errorRes = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, errorRes = null) }
    }

    fun onLoginClick() {
        val current = _uiState.value
        if (current.isLoading) return
        if (!isValidEmail(current.email)) {
            _uiState.update { it.copy(errorRes = R.string.error_email_invalid) }
            return
        }
        if (current.password.isEmpty()) {
            _uiState.update { it.copy(errorRes = R.string.error_password_empty) }
            return
        }
        _uiState.update { it.copy(isLoading = true, errorRes = null) }
        viewModelScope.launch {
            authRepository.login(current.email, current.password)
                .onSuccess { user ->
                    _uiState.update { it.copy(isLoading = false, loggedInUser = user) }
                }
                .onFailure { error ->
                    val failure = (error as? AuthException)?.failure
                    _uiState.update { it.copy(isLoading = false, errorRes = failure.messageRes()) }
                }
        }
    }
}