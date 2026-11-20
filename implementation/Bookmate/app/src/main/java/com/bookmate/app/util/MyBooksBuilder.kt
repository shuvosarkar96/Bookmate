package com.bookmate.app.util

import com.bookmate.app.domain.model.Book
import com.bookmate.app.domain.model.BookProgress
import com.bookmate.app.domain.model.FinishedMonth
import com.bookmate.app.domain.model.MyBooks
import com.bookmate.app.domain.model.ReadingEntry
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

/**
 * Builds the My Books lists. Entries whose book is not in the catalog are skipped.
 * Finished books are grouped by month, newest first. A finished book without a date counts as [nowMillis].
 */
fun buildMyBooks(
    catalog: List<Book>,
    progress: List<BookProgress>,
    savedIds: List<String>,
    zone: ZoneId = ZoneId.systemDefault(),
    nowMillis: Long = System.currentTimeMillis()
): MyBooks {
    val byId = catalog.associateBy { it.id }
    val reading = progress
        .filter { !it.progress.finished }
        .mapNotNull { entry -> byId[entry.bookId]?.let { ReadingEntry(it, entry.progress) } }
    val saved = savedIds.mapNotNull { byId[it] }
    val finished = progress
        .filter { it.progress.finished }
        .mapNotNull { entry -> byId[entry.bookId]?.let { it to (entry.progress.finishedAt ?: nowMillis) } }
        .sortedByDescending { it.second }
        .groupBy(
            keySelector = { YearMonth.from(Instant.ofEpochMilli(it.second).atZone(zone)) },
            valueTransform = { it.first }
        )
        .map { (month, books) -> FinishedMonth(month.year, month.monthValue, books) }
    return MyBooks(reading = reading, saved = saved, finished = finished)
}