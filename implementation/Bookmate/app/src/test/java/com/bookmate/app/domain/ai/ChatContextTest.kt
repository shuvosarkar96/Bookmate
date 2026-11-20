package com.bookmate.app.domain.ai

import com.bookmate.app.domain.model.AccessType
import com.bookmate.app.domain.model.Book
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatContextTest {

    private fun contextTestBook(accessType: AccessType, hasFullText: Boolean) = Book(
        id = "b1",
        title = "Title",
        author = "Author",
        isbn = "",
        genre = "",
        description = "A description.",
        coverUrl = null,
        accessType = accessType,
        hasFullText = hasFullText,
        chapterCount = 3,
        searchKeywords = emptyList()
    )

    @Test
    fun a_readable_book_with_text_uses_the_whole_book_summary() {
        val context = ChatContext.forBook(contextTestBook(AccessType.FREE, hasFullText = true), canRead = true)
        assertTrue(context.isBookLevel)
        assertFalse(context.descriptionOnly)
    }

    @Test
    fun a_locked_premium_book_uses_the_description_only() {
        val context = ChatContext.forBook(contextTestBook(AccessType.PREMIUM, hasFullText = true), canRead = false)
        assertTrue(context.descriptionOnly)
    }

    @Test
    fun a_book_without_full_text_uses_the_description_only() {
        val context = ChatContext.forBook(contextTestBook(AccessType.FREE, hasFullText = false), canRead = true)
        assertTrue(context.descriptionOnly)
    }

    @Test
    fun opening_from_book_detail_queues_the_summary_without_a_position() {
        val session = AiChatSession()
        val book = contextTestBook(AccessType.FREE, hasFullText = true)
        session.open(ChatContext.forBook(book, canRead = true), ChatAction.Summary)
        assertTrue(session.context.value!!.isBookLevel)
        assertEquals(ChatAction.Summary, session.consumePending())
    }
}