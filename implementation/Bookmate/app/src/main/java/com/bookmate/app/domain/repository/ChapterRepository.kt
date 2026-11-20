package com.bookmate.app.domain.repository

import com.bookmate.app.domain.model.Chapter

interface ChapterRepository {
    /** The chapter, or null if it does not exist. Fails with AccessDeniedException if the Member may not read it. */
    suspend fun getChapter(bookId: String, chapterNo: Int): Result<Chapter?>

    /** The stored whole-book summary (extras/summary), or null when there is none. */
    suspend fun getBookSummary(bookId: String): Result<String?>

    /** Stored summaries (chapter number to text) of the chapters BEFORE beforeChapter. */
    suspend fun getChapterSummaries(bookId: String, beforeChapter: Int): Result<Map<Int, String>>
}