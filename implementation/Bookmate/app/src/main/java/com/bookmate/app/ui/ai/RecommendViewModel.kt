package com.bookmate.app.ui.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookmate.app.domain.ai.AiError
import com.bookmate.app.domain.ai.PromptBuilder
import com.bookmate.app.domain.ai.Recommendation
import com.bookmate.app.domain.usecase.GetRecommendations
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecommendUiState(
    val request: String = "",
    val isLoading: Boolean = false,
    /** True after the first run, so an empty list can be told apart from "not asked yet". */
    val hasRun: Boolean = false,
    val items: List<Recommendation> = emptyList(),
    val fromAi: Boolean = false,
    val aiError: AiError? = null,
    val loadFailed: Boolean = false
)

@HiltViewModel
class RecommendViewModel @Inject constructor(
    private val getRecommendations: GetRecommendations
) : ViewModel() {

    private val _state = MutableStateFlow(RecommendUiState())
    val state: StateFlow<RecommendUiState> = _state.asStateFlow()

    fun onRequestChange(text: String) {
        if (text.length <= PromptBuilder.MAX_QUESTION_LENGTH) {
            _state.update { it.copy(request = text) }
        }
    }

    /** One run uses one of the daily AI uses. An empty request means the default. */
    fun recommend() {
        val request = _state.value.request
        if (_state.value.isLoading) return
        _state.update { it.copy(isLoading = true, loadFailed = false) }
        viewModelScope.launch {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            getRecommendations(uid, request).fold(
                onSuccess = { outcome ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            hasRun = true,
                            items = outcome.items,
                            fromAi = outcome.fromAi,
                            aiError = outcome.aiError
                        )
                    }
                },
                onFailure = {
                    _state.update {
                        it.copy(isLoading = false, hasRun = true, items = emptyList(), aiError = null, loadFailed = true)
                    }
                }
            )
        }
    }
}