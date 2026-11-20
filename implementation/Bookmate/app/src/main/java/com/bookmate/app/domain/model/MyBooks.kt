package com.bookmate.app.domain.model

/** A book the Member is reading, with where they stopped. */
data class ReadingEntry(val book: Book, val progress: ReadingProgress)

/** The books finished in one month, newest first. [month] is 1 to 12. */
data class FinishedMonth(val year: Int, val month: Int, val books: List<Book>)

/** The three lists of the My Books screen. */
data class MyBooks(
    val reading: List<ReadingEntry>,
    val saved: List<Book>,
    val finished: List<FinishedMonth>
) {
    val finishedCount: Int get() = finished.sumOf { it.books.size }
}