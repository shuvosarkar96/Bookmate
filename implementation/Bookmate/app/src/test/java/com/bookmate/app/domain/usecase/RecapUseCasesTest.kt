package com.bookmate.app.domain.usecase

import com.bookmate.app.domain.ai.AiError
import com.bookmate.app.domain.ai.AiException
import com.bookmate.app.domain.ai.AiRepository
import com.bookmate.app.domain.ai.ChunkSummarizer
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

private class RecapAi : AiRepository {
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

/** Returns ALL stored summaries on purpose, even later ones: the use case must still drop them. */
private class RecapChapters(
    private val chapters: Map<Int, Chapter> = emptyMap(),
    private val summaries: Map<Int, String> = emptyMap(),
    private val bookSummary: String? = null
) : ChapterRepository {
    override suspend fun getChapter(bookId: String, chapterNo: Int): Result<Chapter?> =
        Result.success(chapters[chapterNo])

    override suspend fun getBookSummary(bookId: String): Result<String?> = Result.success(bookSummary)

    override suspend fun getChapterSummaries(bookId: String, beforeChapter: Int): Result<Map<Int, String>> =
        Result.success(summaries)
}

private fun sampleBook() = Book(
    id = "b1",
    title = "Dracula",
    author = "Bram Stoker",
    isbn = "",
    genre = "Horror",
    description = "A count in a castle.",
    coverUrl = null,
    accessType = AccessType.FREE,
    hasFullText = true,
    chapterCount = 3,
    searchKeywords = emptyList()
)

private val allSummaries = mapOf(
    1 to "SUM-ONE",
    2 to "SUM-TWO",
    3 to "SUM-THREE-SECRET",
    4 to "SUM-FOUR-SECRET"
)

private fun chapterThree() = Chapter(3, "Three", List(10) { "PARA-$it" })

class RecapUseCasesTest {

    @Test
    fun recap_sends_earlier_summaries_and_text_up_to_the_position_only() = runBlocking {
        val ai = RecapAi()
        val chapters = RecapChapters(mapOf(3 to chapterThree()), allSummaries)
        GetRecap(chapters, ai)(sampleBook(), 3, 4).toList()
        val prompt = ai.streamPrompts.single()
        assertTrue(prompt.contains("SUM-ONE"))
        assertTrue(prompt.contains("SUM-TWO"))
        assertTrue(prompt.contains("PARA-0"))
        assertTrue(prompt.contains("PARA-4"))
        assertFalse(prompt.contains("SUM-THREE-SECRET"))
        assertFalse(prompt.contains("SUM-FOUR-SECRET"))
        assertFalse(prompt.contains("PARA-5"))
        assertFalse(prompt.contains("PARA-9"))
    }

    @Test
    fun recap_in_the_first_chapter_uses_only_the_text_read_so_far() = runBlocking {
        val ai = RecapAi()
        val chapters = RecapChapters(mapOf(1 to Chapter(1, "One", List(5) { "PARA-$it" })), allSummaries)
        GetRecap(chapters, ai)(sampleBook(), 1, 0).toList()
        val prompt = ai.streamPrompts.single()
        assertTrue(prompt.contains("PARA-0"))
        assertFalse(prompt.contains("PARA-1"))
        assertFalse(prompt.contains("SUM-ONE"))
    }

    @Test
    fun ask_sends_the_question_and_the_same_safe_text() = runBlocking {
        val ai = RecapAi()
        val chapters = RecapChapters(mapOf(3 to chapterThree()), allSummaries)
        AskAboutBook(chapters, ai)(sampleBook(), 3, 4, "Who is the count?").toList()
        val prompt = ai.streamPrompts.single()
        assertTrue(prompt.contains("Who is the count?"))
        assertTrue(prompt.contains("SUM-TWO"))
        assertTrue(prompt.contains("PARA-4"))
        assertFalse(prompt.contains("SUM-THREE-SECRET"))
        assertFalse(prompt.contains("PARA-5"))
    }

    @Test
    fun ask_cuts_the_question_to_300_characters() = runBlocking {
        val ai = RecapAi()
        val chapters = RecapChapters(mapOf(3 to chapterThree()), allSummaries)
        AskAboutBook(chapters, ai)(sampleBook(), 3, 4, "x".repeat(400)).toList()
        val prompt = ai.streamPrompts.single()
        assertTrue(prompt.contains("x".repeat(300)))
        assertFalse(prompt.contains("x".repeat(301)))
    }

    @Test
    fun ask_with_a_blank_question_is_blocked_and_sends_nothing() = runBlocking {
        val ai = RecapAi()
        val chapters = RecapChapters(mapOf(3 to chapterThree()), allSummaries)
        try {
            AskAboutBook(chapters, ai)(sampleBook(), 3, 4, "   ").toList()
            fail("expected an AiException")
        } catch (e: AiException) {
            assertEquals(AiError.BLOCKED, e.error)
        }
        assertTrue(ai.streamPrompts.isEmpty())
    }

    @Test
    fun book_summary_uses_the_stored_summary_without_an_ai_request() = runBlocking {
        val ai = RecapAi()
        val chapters = RecapChapters(bookSummary = "STORED-BOOK")
        val result = GetBookSummary(chapters, ChunkSummarizer(ai))(sampleBook()).toList()
        assertEquals(listOf("STORED-BOOK"), result)
        assertTrue(ai.generatePrompts.isEmpty())
        assertTrue(ai.streamPrompts.isEmpty())
    }

    @Test
    fun book_summary_is_built_live_from_chapter_summaries_and_one_merge() = runBlocking {
        val ai = RecapAi()
        val chapters = RecapChapters(
            chapters = mapOf(3 to Chapter(3, "Three", listOf("body three"))),
            summaries = mapOf(1 to "S-ONE", 2 to "S-TWO")
        )
        val result = GetBookSummary(chapters, ChunkSummarizer(ai))(sampleBook()).toList()
        assertEquals(listOf("LIVE"), result)
        // chapter 3 had no stored summary (1 request), then the merge (1 request)
        assertEquals(2, ai.generatePrompts.size)
        assertTrue(ai.generatePrompts[0].contains("body three"))
        assertTrue(ai.generatePrompts[1].contains("S-ONE"))
        assertTrue(ai.generatePrompts[1].contains("S-TWO"))
    }
}