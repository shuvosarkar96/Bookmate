package com.bookmate.app.domain.ai

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** No prompt may ever contain text that comes after the reader's position. */
class SpoilerSafetyTest {

    private val paragraphs = List(30) { "PARA-$it-END" }
    private val summaries = (1..8).associateWith { "SUMMARY-$it-END" }
    private val currentChapter = 5

    private fun assertNoSpoilers(prompt: String, position: Int, withSummaries: Boolean) {
        assertTrue("position $position missing", prompt.contains("PARA-$position-END"))
        for (later in position + 1 until paragraphs.size) {
            assertFalse("paragraph $later leaked at position $position", prompt.contains("PARA-$later-END"))
        }
        for (chapter in currentChapter..8) {
            assertFalse("summary $chapter leaked", prompt.contains("SUMMARY-$chapter-END"))
        }
        if (withSummaries) {
            for (chapter in 1 until currentChapter) {
                assertTrue("summary $chapter missing", prompt.contains("SUMMARY-$chapter-END"))
            }
        }
    }

    @Test
    fun recap_never_contains_text_after_the_position() {
        for (position in paragraphs.indices) {
            val selected = TextSelector.forPosition(summaries, currentChapter, paragraphs, position)
            assertNoSpoilers(PromptBuilder.recap("Book", currentChapter, selected), position, true)
        }
    }

    @Test
    fun question_never_contains_text_after_the_position() {
        for (position in paragraphs.indices) {
            val selected = TextSelector.forPosition(summaries, currentChapter, paragraphs, position)
            val prompt = PromptBuilder.question("Book", "Who is speaking?", selected)
            assertNoSpoilers(prompt, position, true)
        }
    }

    @Test
    fun explain_never_contains_text_after_the_position() {
        for (position in paragraphs.indices) {
            val prompt = PromptBuilder.explain("Book", TextSelector.lastParagraphs(paragraphs, position))
            assertNoSpoilers(prompt, position, false)
            assertFalse(prompt.contains("SUMMARY-"))
        }
    }

    @Test
    fun position_beyond_the_end_is_safe() {
        val selected = TextSelector.forPosition(summaries, currentChapter, paragraphs, 10_000)
        val prompt = PromptBuilder.recap("Book", currentChapter, selected)
        assertTrue(prompt.contains("PARA-29-END"))
        assertFalse(prompt.contains("SUMMARY-5-END"))
    }
}