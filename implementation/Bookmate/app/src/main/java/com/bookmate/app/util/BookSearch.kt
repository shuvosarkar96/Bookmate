package com.bookmate.app.util

import com.bookmate.app.domain.model.AccessType
import com.bookmate.app.domain.model.Book

private val TOKEN_REGEX = Regex("[\\p{L}\\p{N}]+")

private fun tokenize(text: String): List<String> =
    TOKEN_REGEX.findAll(text.lowercase()).map { it.value }.toList()

/**
 * Device-side search. Every word typed must start one of the book's keywords
 * or appear inside its ISBN. [access] and [genre] narrow the list further.
 */
fun filterBooks(
    books: List<Book>,
    query: String,
    access: AccessType?,
    genre: String?
): List<Book> {
    val tokens = tokenize(query)
    return books.filter { book ->
        val accessOk = access == null || book.accessType == access
        val genreOk = genre == null || book.genre.equals(genre, ignoreCase = true)
        if (!accessOk || !genreOk) return@filter false
        if (tokens.isEmpty()) return@filter true
        val words = book.searchKeywords.ifEmpty {
            tokenize("${book.title} ${book.author} ${book.genre}")
        }
        tokens.all { token ->
            words.any { it.startsWith(token) } || book.isbn.contains(token)
        }
    }
}