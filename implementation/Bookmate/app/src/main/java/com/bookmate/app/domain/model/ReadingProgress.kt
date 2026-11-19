package com.bookmate.app.domain.model

/** Where a Member stopped reading a book (document users/{uid}/readingProgress/{bookId}). */
data class ReadingProgress(
    val chapter: Int,
    val paragraph: Int,
    val finished: Boolean
)

/** A reading progress together with the id of its book. */
data class BookProgress(
    val bookId: String,
    val progress: ReadingProgress
)