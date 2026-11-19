package com.bookmate.app.data.ai

import com.bookmate.app.domain.ai.AiErrorMapper
import com.bookmate.app.domain.ai.AiException
import com.bookmate.app.domain.ai.AiRepository
import com.bookmate.app.domain.ai.AiService
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.withTimeout

@OptIn(FlowPreview::class)
@Singleton
class DefaultAiRepository @Inject constructor(
    private val aiService: AiService
) : AiRepository {

    /** 30 seconds. A var only so that unit tests can make it short. */
    internal var timeoutMillis: Long = TIMEOUT_MILLIS

    override suspend fun generate(prompt: String): Result<String> {
        var attempt = 0
        while (true) {
            val result = try {
                withTimeout(timeoutMillis) { aiService.generate(prompt) }
            } catch (e: TimeoutCancellationException) {
                Result.failure(e)
            }
            val failure = result.exceptionOrNull() ?: return result
            val error = AiErrorMapper.map(failure)
            if (attempt >= MAX_RETRIES || !error.retryable) {
                return Result.failure(AiException(error, failure))
            }
            attempt++
        }
    }

    override fun generateStream(prompt: String): Flow<String> = flow {
        var emitted = false
        emitAll(
            flow { emitAll(aiService.generateStream(prompt)) }
                .timeout(timeoutMillis.milliseconds)
                .onEach { emitted = true }
                // Retry only if nothing was shown yet, so the text is never doubled.
                .retryWhen { cause, attempt ->
                    attempt < MAX_RETRIES && !emitted && AiErrorMapper.map(cause).retryable
                }
        )
    }.catch { error ->
        throw AiException(AiErrorMapper.map(error), error)
    }

    private companion object {
        const val TIMEOUT_MILLIS = 30_000L
        const val MAX_RETRIES = 1
    }
}