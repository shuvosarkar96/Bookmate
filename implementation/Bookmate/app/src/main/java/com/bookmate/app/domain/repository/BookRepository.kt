package com.bookmate.app.domain.repository

import com.bookmate.app.domain.model.Book
import com.bookmate.app.domain.model.NewBook

interface BookRepository {
    suspend fun getBooks(): Result<List<Book>>
    suspend fun getBook(bookId: String): Result<Book?>

    /** Admin only. Adds a description-only book and returns its new id. */
    suspend fun addBook(book: NewBook): Result<String>

    /** Admin only. Changes the editable fields of a book. Chapters are not touched. */
    suspend fun updateBook(bookId: String, book: NewBook): Result<Unit>

    /** Admin only. Removes the book together with its chapters and extras. */
    suspend fun deleteBook(bookId: String): Result<Unit>
}