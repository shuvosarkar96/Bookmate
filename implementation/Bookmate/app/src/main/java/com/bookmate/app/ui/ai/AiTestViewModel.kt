package com.bookmate.app.ui.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookmate.app.domain.ai.AiRepository
import com.bookmate.app.domain.ai.PromptBuilder
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
    private val aiService: AiRepository
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
        val PROMPT = PromptBuilder.descriptionSummary(
            title = "Dracula",
            author = "Bram Stoker",
            description = "A young solicitor travels to Transylvania to help a nobleman buy a house in England " +
                "and discovers that his host is a dangerous vampire."
        )
    }
}