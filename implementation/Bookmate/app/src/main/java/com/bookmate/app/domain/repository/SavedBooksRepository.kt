package com.bookmate.app.domain.repository

import com.bookmate.app.domain.model.Book

interface SavedBooksRepository {
    suspend fun isSaved(bookId: String): Result<Boolean>
    suspend fun setSaved(book: Book, saved: Boolean): Result<Unit>
}