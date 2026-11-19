package com.bookmate.app.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    /** Reading text size in sp. */
    val readingTextSize: Flow<Int>

    suspend fun setReadingTextSize(sizeSp: Int)

    companion object {
        const val DEFAULT_TEXT_SIZE = 17
        val TEXT_SIZES = listOf(15, 17, 20, 24)
    }
}