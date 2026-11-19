package com.bookmate.app.domain.ai

import kotlinx.coroutines.flow.Flow

/**
 * The AI access the rest of the app uses. Adds a 30 second timeout, one retry and error mapping
 * on top of AiService. Failures are AiException.
 */
interface AiRepository {
    suspend fun generate(prompt: String): Result<String>

    /** Streams the answer. Throws AiException when it fails. */
    fun generateStream(prompt: String): Flow<String>
}