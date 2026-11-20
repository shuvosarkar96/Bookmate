package com.bookmate.app.domain.ai

import java.io.IOException
import java.net.SocketTimeoutException
import kotlinx.coroutines.TimeoutCancellationException

/** Turns any exception from the AI call into an AiError. */
object AiErrorMapper {

    fun map(error: Throwable): AiError {
        if (error is AiException) return error.error

        val chain = generateSequence(error) { it.cause }.take(6).toList()

        if (chain.any { it is EmptyAnswerException }) return AiError.BLOCKED
        if (chain.any { it is TimeoutCancellationException || it is SocketTimeoutException }) {
            return AiError.TIMEOUT
        }
        if (chain.any { it is IOException }) return AiError.NO_INTERNET

        val text = chain
            .joinToString(" ") { it.javaClass.simpleName + " " + it.message.orEmpty() }
            .lowercase()
        return when {
            LIMIT_WORDS.any { it in text } -> AiError.LIMIT
            BLOCKED_WORDS.any { it in text } -> AiError.BLOCKED
            else -> AiError.OTHER
        }
    }

    // Quota used up, App Check rejected, API disabled or not allowed: "not available right now".
    private val LIMIT_WORDS = listOf(
        "quota", "429", "resource_exhausted", "app check", "permission", "api key", "disabled"
    )
    private val BLOCKED_WORDS = listOf("blocked", "safety", "stopped")
}