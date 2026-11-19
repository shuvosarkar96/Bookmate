package com.bookmate.app.ui.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookmate.app.domain.ai.AiService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AiTestUiState(
    val isLoading: Boolean = false,
    val answer: String = "",
    val errorDetail: String? = null
)

/** Temporary: proves that the AI connection works. Replaced by the AI Chat in Step 4.6. */
@HiltViewModel
class AiTestViewModel @Inject constructor(
    private val aiService: AiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiTestUiState())
    val uiState: StateFlow<AiTestUiState> = _uiState.asStateFlow()

    fun askOnce() {
        if (_uiState.value.isLoading) return
        _uiState.value = AiTestUiState(isLoading = true)
        viewModelScope.launch {
            aiService.generate(PROMPT)
                .onSuccess { text -> _uiState.value = AiTestUiState(answer = text) }
                .onFailure { e -> _uiState.value = AiTestUiState(errorDetail = describe(e)) }
        }
    }

    fun askStreaming() {
        if (_uiState.value.isLoading) return
        _uiState.value = AiTestUiState(isLoading = true)
        viewModelScope.launch {
            try {
                aiService.generateStream(PROMPT).collect { chunk ->
                    _uiState.update { it.copy(answer = it.answer + chunk) }
                }
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorDetail = describe(e)) }
            }
        }
    }

    private fun describe(e: Throwable): String = e.javaClass.simpleName + ": " + e.message

    private companion object {
        const val PROMPT = "Say hello to a reader of a library app in one short, friendly sentence."
    }
}