package com.bookmate.app.data.ai

import android.util.Log
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
        val startedAt = System.currentTimeMillis()
        log("generate: start")
        var attempt = 0
        while (true) {
            val result = try {
                withTimeout(timeoutMillis) { aiService.generate(prompt) }
            } catch (e: TimeoutCancellationException) {
                Result.failure(e)
            }
            val failure = result.exceptionOrNull()
            if (failure == null) {
                log("generate: answer after ${elapsed(startedAt)} ms")
                return result
            }
            val error = AiErrorMapper.map(failure)
            val giveUp = attempt >= MAX_RETRIES || !error.retryable
            log("generate: attempt $attempt failed after ${elapsed(startedAt)} ms: ${describe(failure)} -> $error, giveUp=$giveUp")
            if (giveUp) return Result.failure(AiException(error, failure))
            attempt++
        }
    }

    override fun generateStream(prompt: String): Flow<String> = flow {
        var emitted = false
        val startedAt = System.currentTimeMillis()
        log("stream: start")
        emitAll(
            // A fresh request for every attempt, so a retry really asks again.
            flow { emitAll(aiService.generateStream(prompt)) }
                .timeout(timeoutMillis.milliseconds)
                .onEach {
                    if (!emitted) log("stream: first chunk after ${elapsed(startedAt)} ms")
                    emitted = true
                }
                // Retry only if nothing was shown yet, so the text is never doubled.
                .retryWhen { cause, attempt ->
                    val retry = attempt < MAX_RETRIES && !emitted && AiErrorMapper.map(cause).retryable
                    log("stream: attempt $attempt failed after ${elapsed(startedAt)} ms: ${describe(cause)}, retry=$retry")
                    retry
                }
        )
    }.catch { error ->
        val mapped = AiErrorMapper.map(error)
        log("stream: gave up -> $mapped")
        throw AiException(mapped, error)
    }

    private fun elapsed(startedAt: Long): Long = System.currentTimeMillis() - startedAt

    /** Class name and a short message only. The prompt and the answer are never logged. */
    private fun describe(error: Throwable): String =
        error.javaClass.simpleName + ": " + error.message.orEmpty().take(200)

    private fun log(message: String) {
        try {
            Log.d(TAG, message)
        } catch (_: Throwable) {
            // Not available in plain JVM unit tests.
        }
    }

    private companion object {
        const val TAG = "BookmateAi"
        const val TIMEOUT_MILLIS = 30_000L
        const val MAX_RETRIES = 1
    }
}