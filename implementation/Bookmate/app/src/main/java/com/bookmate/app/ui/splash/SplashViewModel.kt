package com.bookmate.app.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookmate.app.domain.repository.AuthRepository
import com.bookmate.app.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val MIN_SPLASH_MS = 700L

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _destination = MutableStateFlow<String?>(null)
    val destination: StateFlow<String?> = _destination.asStateFlow()

    init {
        viewModelScope.launch {
            val user = authRepository.currentUser().getOrNull()
            delay(MIN_SPLASH_MS)
            _destination.value = when {
                user == null -> Routes.LOGIN
                !user.onboardingDone -> Routes.ONBOARDING
                else -> Routes.HOME
            }
        }
    }
}