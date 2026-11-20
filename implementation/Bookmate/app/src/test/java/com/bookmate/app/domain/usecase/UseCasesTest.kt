package com.bookmate.app.domain.usecase

import com.bookmate.app.domain.ai.AiError
import com.bookmate.app.domain.ai.AiException
import com.bookmate.app.domain.ai.AiRepository
import com.bookmate.app.domain.ai.ChunkSummarizer
import com.bookmate.app.domain.model.AccessDeniedException
import com.bookmate.app.domain.model.AccessType
import com.bookmate.app.domain.model.Book
import com.bookmate.app.domain.model.Chapter
import com.bookmate.app.domain.repository.ChapterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

private class FakeAi : AiRepository {
    val streamPrompts = mutableListOf<String>()
    val generatePrompts = mutableListOf<String>()

    override suspend fun generate(prompt: String): Result<String> {
        generatePrompts.add(prompt)
        return Result.success("LIVE")
    }

    override fun generateStream(prompt: String): Flow<String> {
        streamPrompts.add(prompt)
        return flowOf("a", "b")
    }
}

private class FakeChapters(
    private val chapters: Map<Int, Chapter> = emptyMap(),
    private val denied: Boolean = false
) : ChapterRepository {
    override suspend fun getChapter(bookId: String, chapterNo: Int): Result<Chapter?> =
        if (denied) Result.failure(AccessDeniedException()) else Result.success(chapters[chapterNo])

    override suspend fun getBookSummary(bookId: String): Result<String?> = Result.success(null)

    override suspend fun getChapterSummaries(bookId: String, beforeChapter: Int): Result<Map<Int, String>> =
        Result.success(emptyMap())
}

private fun testBook(description: String = "A count in a castle.") = Book(
    id = "b1",
    title = "Dracula",
    author = "Bram Stoker",
    isbn = "",
    genre = "Horror",
    description = description,
    coverUrl = null,
    accessType = AccessType.FREE,
    hasFullText = true,
    chapterCount = 3,
    searchKeywords = emptyList()
)

class UseCasesTest {

    @Test
    fun description_summary_sends_title_author_and_description_only() = runBlocking {
        val ai = FakeAi()
        val result = GetDescriptionSummary(ai)(testBook()).toList()
        assertEquals(listOf("a", "b"), result)
        val prompt = ai.streamPrompts.single()
        assertTrue(prompt.contains("Dracula"))
        assertTrue(prompt.contains("Bram Stoker"))
        assertTrue(prompt.contains("A count in a castle."))
    }

    @Test
    fun description_summary_with_blank_description_is_blocked_and_sends_nothing() = runBlocking {
        val ai = FakeAi()
        try {
            GetDescriptionSummary(ai)(testBook(description = "  ")).toList()
            fail("expected an AiException")
        } catch (e: AiException) {
            assertEquals(AiError.BLOCKED, e.error)
        }
        assertTrue(ai.streamPrompts.isEmpty())
    }

    @Test
    fun explain_sends_only_the_last_five_paragraphs_up_to_the_position() = runBlocking {
        val ai = FakeAi()
        val paragraphs = List(10) { "PARA-$it text" }
        val chapters = FakeChapters(mapOf(1 to Chapter(1, "One", paragraphs)))
        ExplainPart(chapters, ai)(testBook(), 1, 6).toList()
        val prompt = ai.streamPrompts.single()
        assertTrue(prompt.contains("PARA-6"))
        assertTrue(prompt.contains("PARA-2"))
        assertFalse(prompt.contains("PARA-1"))
        assertFalse(prompt.contains("PARA-7"))
        assertFalse(prompt.contains("PARA-9"))
    }

    @Test
    fun explain_passes_on_access_denied() = runBlocking {
        val ai = FakeAi()
        try {
            ExplainPart(FakeChapters(denied = true), ai)(testBook(), 1, 0).toList()
            fail("expected AccessDeniedException")
        } catch (e: AccessDeniedException) {
            // expected
        }
        assertTrue(ai.streamPrompts.isEmpty())
    }

    @Test
    fun explain_fails_when_the_chapter_is_missing() = runBlocking {
        val ai = FakeAi()
        try {
            ExplainPart(FakeChapters(), ai)(testBook(), 5, 0).toList()
            fail("expected IllegalStateException")
        } catch (e: IllegalStateException) {
            // expected
        }
        assertTrue(ai.streamPrompts.isEmpty())
    }

    @Test
    fun chapter_summary_uses_the_stored_summary_without_an_ai_request() = runBlocking {
        val ai = FakeAi()
        val chapters = FakeChapters(mapOf(2 to Chapter(2, "Two", listOf("text"), summary = "STORED")))
        val result = GetChapterSummary(chapters, ChunkSummarizer(ai))(testBook(), 2).toList()
        assertEquals(listOf("STORED"), result)
        assertTrue(ai.generatePrompts.isEmpty())
        assertTrue(ai.streamPrompts.isEmpty())
    }

    @Test
    fun chapter_summary_is_made_live_when_none_is_stored() = runBlocking {
        val ai = FakeAi()
        val chapters = FakeChapters(mapOf(2 to Chapter(2, "Two", listOf("chapter body text"))))
        val result = GetChapterSummary(chapters, ChunkSummarizer(ai))(testBook(), 2).toList()
        assertEquals(listOf("LIVE"), result)
        assertEquals(1, ai.generatePrompts.size)
        assertTrue(ai.generatePrompts[0].contains("chapter body text"))
    }
}