package com.bookmate.app.domain.repository

import com.bookmate.app.domain.model.ReadingProgress

interface ReadingProgressRepository {
    /** The signed-in Member's progress in this book, or null if they never opened it. */
    suspend fun getProgress(bookId: String): Result<ReadingProgress?>

    /** Saves the chapter and paragraph index where the Member is reading. */
    suspend fun saveProgress(bookId: String, chapter: Int, paragraph: Int): Result<Unit>
}