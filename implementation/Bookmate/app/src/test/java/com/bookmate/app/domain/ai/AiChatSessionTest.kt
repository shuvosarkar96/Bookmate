package com.bookmate.app.domain.ai

import com.bookmate.app.domain.model.AccessType
import com.bookmate.app.domain.model.Book
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

private fun sessionBook(id: String) = Book(
    id = id,
    title = "Title $id",
    author = "Author",
    isbn = "",
    genre = "Genre",
    description = "Description",
    coverUrl = null,
    accessType = AccessType.FREE,
    hasFullText = true,
    chapterCount = 3,
    searchKeywords = emptyList()
)

class AiChatSessionTest {

    @Test
    fun open_sets_the_context_and_the_pending_action() {
        val session = AiChatSession()
        session.open(ChatContext(sessionBook("a"), 2, 5), ChatAction.Recap)
        assertEquals(2, session.context.value?.chapter)
        assertEquals(ChatAction.Recap, session.pending.value)
    }

    @Test
    fun the_pending_action_can_be_taken_only_once() {
        val session = AiChatSession()
        session.open(ChatContext(sessionBook("a"), 1, 0), ChatAction.Summary)
        assertEquals(ChatAction.Summary, session.consumePending())
        assertNull(session.consumePending())
    }

    @Test
    fun messages_stay_for_the_same_book() {
        val session = AiChatSession()
        session.open(ChatContext(sessionBook("a"), 1, 0), null)
        session.addMessage(ChatMessage(ChatRole.USER, "hi"))
        session.open(ChatContext(sessionBook("a"), 2, 4), null)
        assertEquals(1, session.messages.value.size)
        assertEquals(2, session.context.value?.chapter)
    }

    @Test
    fun messages_are_cleared_for_another_book() {
        val session = AiChatSession()
        session.open(ChatContext(sessionBook("a"), 1, 0), null)
        session.addMessage(ChatMessage(ChatRole.USER, "hi"))
        session.open(ChatContext(sessionBook("b"), 1, 0), null)
        assertEquals(0, session.messages.value.size)
    }
}