package com.bookmate.app.util

import com.bookmate.app.domain.model.Book

/** Books from the favorite genres first, then the rest, up to [limit]. */
fun pickRecommended(books: List<Book>, favoriteGenres: List<String>, limit: Int = 3): List<Book> {
    val matching = books.filter { it.genre in favoriteGenres }
    val others = books.filter { it.genre !in favoriteGenres }
    return (matching + others).take(limit)
}