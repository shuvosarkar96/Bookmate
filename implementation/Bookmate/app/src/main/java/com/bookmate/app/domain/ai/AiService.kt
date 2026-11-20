package com.bookmate.app.domain.ai

import kotlinx.coroutines.flow.Flow

/** The only way the app talks to an AI model. The provider behind it can be replaced. */
interface AiService {
    /** One complete answer. */
    suspend fun generate(prompt: String): Result<String>

    /** The answer as it arrives, in text chunks. Throws if the call fails. */
    fun generateStream(prompt: String): Flow<String>
}

/** The model answered with no text (for example, the answer was blocked). */
class EmptyAnswerException : Exception()