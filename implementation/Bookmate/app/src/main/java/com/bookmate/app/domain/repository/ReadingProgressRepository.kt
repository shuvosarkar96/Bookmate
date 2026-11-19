package com.bookmate.app.domain.repository

import com.bookmate.app.domain.model.BookProgress
import com.bookmate.app.domain.model.ReadingProgress

interface ReadingProgressRepository {
    /** The signed-in Member's progress in this book, or null if they never opened it. */
    suspend fun getProgress(bookId: String): Result<ReadingProgress?>

    /** Saves the chapter and paragraph index where the Member is reading. */
    suspend fun saveProgress(bookId: String, chapter: Int, paragraph: Int): Result<Unit>

    /** The most recently read book that is not finished, or null. */
    suspend fun getLatestInProgress(): Result<BookProgress?>

    /** Marks the book as finished (finished = true, finishedAt = now). */
    suspend fun markFinished(bookId: String, chapter: Int): Result<Unit>
}