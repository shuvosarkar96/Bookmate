package com.bookmate.app.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    /** Reading text size in sp. */
    val readingTextSize: Flow<Int>

    suspend fun setReadingTextSize(sizeSp: Int)

    /** Whether subscription reminders are on. On by default. */
    val remindersEnabled: Flow<Boolean>

    suspend fun setRemindersEnabled(enabled: Boolean)

    companion object {
        const val DEFAULT_TEXT_SIZE = 17
        val TEXT_SIZES = listOf(15, 17, 20, 24)
    }
}