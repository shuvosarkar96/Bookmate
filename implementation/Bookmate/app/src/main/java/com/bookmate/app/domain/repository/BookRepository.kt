package com.bookmate.app.domain.repository

import com.bookmate.app.domain.model.Book

interface BookRepository {
    suspend fun getBooks(): Result<List<Book>>
    suspend fun getBook(bookId: String): Result<Book?>
}