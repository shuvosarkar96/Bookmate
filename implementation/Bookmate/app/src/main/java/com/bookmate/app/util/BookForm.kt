package com.bookmate.app.util

enum class BookFormProblem { TITLE_EMPTY, AUTHOR_EMPTY, GENRE_EMPTY, ISBN_INVALID, COVER_INVALID }

/** Removes hyphens and spaces and makes a final x capital. */
fun normalizeIsbn(raw: String): String = raw.filter { it.isLetterOrDigit() }.uppercase()

private fun isIsbnShape(isbn: String): Boolean = when (isbn.length) {
    13 -> isbn.all { it.isDigit() }
    10 -> isbn.take(9).all { it.isDigit() } && (isbn.last().isDigit() || isbn.last() == 'X')
    else -> false
}

private fun isHttpUrl(url: String): Boolean {
    val lower = url.lowercase()
    return (lower.startsWith("http://") || lower.startsWith("https://")) && !lower.contains(' ')
}

/** Checks the Add a book form. ISBN and cover are optional. Returns the first problem, or null. */
fun checkBookForm(
    title: String,
    author: String,
    genre: String,
    isbn: String,
    coverUrl: String
): BookFormProblem? = when {
    title.isBlank() -> BookFormProblem.TITLE_EMPTY
    author.isBlank() -> BookFormProblem.AUTHOR_EMPTY
    genre.isBlank() -> BookFormProblem.GENRE_EMPTY
    isbn.isNotBlank() && !isIsbnShape(normalizeIsbn(isbn)) -> BookFormProblem.ISBN_INVALID
    coverUrl.isNotBlank() && !isHttpUrl(coverUrl.trim()) -> BookFormProblem.COVER_INVALID
    else -> null
}