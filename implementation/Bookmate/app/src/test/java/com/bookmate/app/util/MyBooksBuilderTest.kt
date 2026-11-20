package com.bookmate.app.util

import com.bookmate.app.domain.model.AccessType
import com.bookmate.app.domain.model.Book
import com.bookmate.app.domain.model.BookProgress
import com.bookmate.app.domain.model.ReadingProgress
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

class MyBooksBuilderTest {

    private fun myBook(id: String) = Book(
        id = id,
        title = "Title $id",
        author = "Author",
        isbn = "",
        genre = "Classic",
        description = "A description.",
        coverUrl = null,
        accessType = AccessType.FREE,
        hasFullText = true,
        chapterCount = 10,
        searchKeywords = emptyList()
    )

    private val catalog = listOf(myBook("a"), myBook("b"), myBook("c"), myBook("d"))

    private fun day(year: Int, month: Int, dayOfMonth: Int): Long =
        LocalDate.of(year, month, dayOfMonth).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    private fun reading(id: String) = BookProgress(id, ReadingProgress(2, 5, false))

    private fun finished(id: String, at: Long?) = BookProgress(id, ReadingProgress(10, 0, true, at))

    private fun build(
        progress: List<BookProgress> = emptyList(),
        saved: List<String> = emptyList(),
        now: Long = day(2026, 12, 1)
    ) = buildMyBooks(catalog, progress, saved, ZoneOffset.UTC, now)

    @Test
    fun reading_lists_the_unfinished_books_in_order() {
        val result = build(progress = listOf(reading("b"), finished("a", day(2026, 10, 1)), reading("c")))
        assertEquals(listOf("b", "c"), result.reading.map { it.book.id })
    }

    @Test
    fun saved_keeps_the_saved_order_and_skips_missing_books() {
        val result = build(saved = listOf("c", "zzz", "a"))
        assertEquals(listOf("c", "a"), result.saved.map { it.id })
    }

    @Test
    fun finished_books_are_grouped_by_month_newest_first() {
        val result = build(
            progress = listOf(
                finished("a", day(2026, 10, 15)),
                finished("b", day(2026, 11, 2)),
                finished("c", day(2026, 11, 20))
            )
        )
        assertEquals(listOf(2026 to 11, 2026 to 10), result.finished.map { it.year to it.month })
        assertEquals(listOf("c", "b"), result.finished[0].books.map { it.id })
        assertEquals(listOf("a"), result.finished[1].books.map { it.id })
    }

    @Test
    fun progress_for_a_book_that_is_not_in_the_catalog_is_skipped() {
        val result = build(progress = listOf(reading("zzz"), finished("zzz", day(2026, 10, 1))))
        assertEquals(0, result.reading.size)
        assertEquals(0, result.finishedCount)
    }

    @Test
    fun a_finished_book_without_a_date_counts_as_now() {
        val result = build(progress = listOf(finished("a", null)), now = day(2026, 12, 1))
        assertEquals(12, result.finished.single().month)
    }

    @Test
    fun the_finished_count_adds_all_months() {
        val result = build(
            progress = listOf(
                finished("a", day(2026, 9, 1)),
                finished("b", day(2026, 10, 1)),
                finished("c", day(2026, 10, 2))
            )
        )
        assertEquals(3, result.finishedCount)
    }
}