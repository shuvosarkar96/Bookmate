package com.bookmate.app.ui.ai

import com.bookmate.app.domain.ai.AiError
import com.bookmate.app.domain.ai.AiErrorMapper
import com.bookmate.app.domain.ai.AiOutput
import com.bookmate.app.domain.ai.AiUsageRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AiAnswerState {
    data object Idle : AiAnswerState
    data object Loading : AiAnswerState
    data class Streaming(val text: String) : AiAnswerState
    data class Done(val text: String) : AiAnswerState
    /** [partial] is the text that arrived before the failure, if any. */
    data class Error(val error: AiError, val partial: String = "") : AiAnswerState
}

val AiAnswerState.isBusy: Boolean
    get() = this is AiAnswerState.Loading || this is AiAnswerState.Streaming

/**
 * Runs one AI request at a time for a screen. Counts the daily cap, streams the text into
 * [state] and maps every failure to an AiError. Create it inside a ViewModel.
 */
class AiAnswerController(
    private val scope: CoroutineScope,
    private val usage: AiUsageRepository,
    private val userId: () -> String?
) {
    private val _state = MutableStateFlow<AiAnswerState>(AiAnswerState.Idle)
    val state: StateFlow<AiAnswerState> = _state.asStateFlow()

    private var job: Job? = null
    private var lastRequest: (() -> Flow<String>)? = null

    /** Starts a request. Ignored while another one is running. */
    fun run(request: () -> Flow<String>) {
        if (_state.value.isBusy) return
        lastRequest = request
        start(request)
    }

    /** Repeats the last request. */
    fun retry() {
        val request = lastRequest ?: return
        if (_state.value.isBusy) return
        start(request)
    }

    /** Stops any request and clears the answer. */
    fun reset() {
        job?.cancel()
        lastRequest = null
        _state.value = AiAnswerState.Idle
    }

    private fun start(request: () -> Flow<String>) {
        _state.value = AiAnswerState.Loading
        job = scope.launch {
            val uid = userId()
            if (uid == null) {
                _state.value = AiAnswerState.Error(AiError.OTHER)
                return@launch
            }
            if (!usage.tryConsume(uid)) {
                _state.value = AiAnswerState.Error(AiError.LIMIT)
                return@launch
            }
            val text = StringBuilder()
            try {
                request().collect { chunk ->
                    text.append(chunk)
                    _state.value = AiAnswerState.Streaming(AiOutput.clean(text.toString()))
                }
                _state.value = if (AiOutput.clean(text.toString()).isBlank()) {
                    AiAnswerState.Error(AiError.BLOCKED)
                } else {
                    AiAnswerState.Done(AiOutput.clean(text.toString()))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                _state.value = AiAnswerState.Error(AiErrorMapper.map(e), AiOutput.clean(text.toString()))
            }
        }
    }
}