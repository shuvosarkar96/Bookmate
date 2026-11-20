package com.bookmate.app.domain.ai

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private class RecordingAi(private val failOnCall: Int? = null) : AiRepository {
    val prompts = mutableListOf<String>()

    override suspend fun generate(prompt: String): Result<String> {
        prompts.add(prompt)
        val n = prompts.size
        return if (n == failOnCall) {
            Result.failure(AiException(AiError.TIMEOUT))
        } else {
            Result.success("SUM$n")
        }
    }

    override fun generateStream(prompt: String): Flow<String> = emptyFlow()
}

class ChunkSummarizerTest {

    private fun words(n: Int): String = List(n) { "lorem" }.joinToString(" ")

    @Test
    fun short_text_needs_one_request() = runBlocking {
        val ai = RecordingAi()
        val result = ChunkSummarizer(ai).summarize("Dracula", "Bram Stoker", listOf("hello world", "second part"))
        assertEquals("SUM1", result.getOrNull())
        assertEquals(1, ai.prompts.size)
        assertTrue(ai.prompts[0].contains("hello world"))
        assertTrue(ai.prompts[0].contains("second part"))
    }

    @Test
    fun long_text_is_split_and_merged() = runBlocking {
        val ai = RecordingAi()
        val paragraphs = List(10) { words(1000) }
        val result = ChunkSummarizer(ai).summarize("T", "A", paragraphs)
        // 4 chunks (3 + 3 + 3 + 1 paragraphs) + 1 merge request
        assertEquals(5, ai.prompts.size)
        assertEquals("SUM5", result.getOrNull())
    }

    @Test
    fun merge_request_has_only_the_partial_summaries() = runBlocking {
        val ai = RecordingAi()
        ChunkSummarizer(ai).summarize("T", "A", List(10) { words(1000) })
        val merge = ai.prompts.last()
        listOf("SUM1", "SUM2", "SUM3", "SUM4").forEach { assertTrue(merge.contains(it)) }
        assertFalse(merge.contains("lorem"))
    }

    @Test
    fun split_keeps_paragraphs_whole_and_gives_a_big_one_its_own_chunk() {
        val paragraphs = listOf(words(4), words(4), words(4), words(12), words(2))
        val chunks = ChunkSummarizer.splitIntoChunks(paragraphs, 10)
        assertEquals(listOf(2, 1, 1, 1), chunks.map { it.size })
        assertEquals(paragraphs, chunks.flatten())
    }

    @Test
    fun split_of_short_text_is_one_chunk() {
        val chunks = ChunkSummarizer.splitIntoChunks(listOf(words(100), words(100)))
        assertEquals(1, chunks.size)
    }

    @Test
    fun failure_stops_further_requests() = runBlocking {
        val ai = RecordingAi(failOnCall = 2)
        val result = ChunkSummarizer(ai).summarize("T", "A", List(10) { words(1000) })
        assertTrue(result.isFailure)
        assertEquals(AiError.TIMEOUT, (result.exceptionOrNull() as AiException).error)
        assertEquals(2, ai.prompts.size)
    }

    @Test
    fun empty_text_fails_without_a_request() = runBlocking {
        val ai = RecordingAi()
        val result = ChunkSummarizer(ai).summarize("T", "A", listOf("", "   "))
        assertTrue(result.isFailure)
        assertEquals(0, ai.prompts.size)
    }
}