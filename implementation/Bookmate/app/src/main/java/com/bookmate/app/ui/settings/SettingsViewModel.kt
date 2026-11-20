package com.bookmate.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookmate.app.domain.ai.AiChatSession
import com.bookmate.app.domain.repository.AuthRepository
import com.bookmate.app.domain.repository.SettingsRepository
import com.bookmate.app.util.textSizeAt
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val textSize: Int = SettingsRepository.DEFAULT_TEXT_SIZE,
    val remindersEnabled: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository,
    private val chatSession: AiChatSession
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.readingTextSize,
        settingsRepository.remindersEnabled
    ) { size, reminders -> SettingsUiState(size, reminders) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    /** [position] is the slider position (0 to the last text size). */
    fun onTextSizePosition(position: Int) {
        viewModelScope.launch { settingsRepository.setReadingTextSize(textSizeAt(position)) }
    }

    fun onRemindersChange(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setRemindersEnabled(enabled) }
    }

    /** Signs out and forgets the AI chat. The caller then restarts the app. */
    fun logout() {
        authRepository.logout()
        chatSession.clear()
    }
}