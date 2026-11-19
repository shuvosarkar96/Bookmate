package com.bookmate.app.domain.ai

/**
 * Daily cap on AI requests, counted on the device. One count = one user action
 * (one tap on Summarize, Recap, Explain or one question).
 */
interface AiUsageRepository {
    /** Counts one request for today. Returns false, and counts nothing, when the cap is used up. */
    suspend fun tryConsume(userId: String): Boolean

    suspend fun remainingToday(userId: String): Int

    companion object {
        const val DAILY_LIMIT = 30
    }
}