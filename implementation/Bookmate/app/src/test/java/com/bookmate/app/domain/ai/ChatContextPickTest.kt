package com.bookmate.app.domain.ai

import com.bookmate.app.domain.model.AccessType
import com.bookmate.app.domain.model.Book
import com.bookmate.app.domain.model.ReadingProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatContextPickTest {

    private fun pickTestBook(hasFullText: Boolean = true) = Book(
        id = "b1",
        title = "Title",
        author = "Author",
        isbn = "",
        genre = "",
        description = "A description.",
        coverUrl = null,
        accessType = AccessType.FREE,
        hasFullText = hasFullText,
        chapterCount = 10,
        searchKeywords = emptyList()
    )

    private val progress = ReadingProgress(chapter = 3, paragraph = 7, finished = false)

    @Test
    fun a_reading_book_keeps_its_saved_position() {
        val context = ChatContext.forPick(pickTestBook(), canRead = true, progress = progress)
        assertEquals(3, context.chapter)
        assertEquals(7, context.paragraph)
        assertFalse(context.isBookLevel)
        assertFalse(context.descriptionOnly)
    }

    @Test
    fun a_locked_book_uses_the_description_even_with_progress() {
        val context = ChatContext.forPick(pickTestBook(), canRead = false, progress = progress)
        assertTrue(context.isBookLevel)
        assertTrue(context.descriptionOnly)
    }

    @Test
    fun a_saved_book_without_progress_starts_at_book_level() {
        val context = ChatContext.forPick(pickTestBook(), canRead = true, progress = null)
        assertTrue(context.isBookLevel)
        assertFalse(context.descriptionOnly)
    }

    @Test
    fun a_book_without_full_text_ignores_the_progress() {
        val context = ChatContext.forPick(pickTestBook(hasFullText = false), canRead = true, progress = progress)
        assertTrue(context.isBookLevel)
        assertTrue(context.descriptionOnly)
    }
}