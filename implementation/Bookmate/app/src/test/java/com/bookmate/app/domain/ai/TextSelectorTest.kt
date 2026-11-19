package com.bookmate.app.domain.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TextSelectorTest {

    private fun paragraphs(count: Int) = List(count) { "PARA-$it-END" }

    @Test
    fun readSoFar_includes_up_to_position() {
        val p = paragraphs(10)
        assertEquals(p.subList(0, 4), TextSelector.readSoFar(p, 3))
    }

    @Test
    fun readSoFar_clamps_the_position() {
        val p = paragraphs(5)
        assertEquals(listOf(p[0]), TextSelector.readSoFar(p, -7))
        assertEquals(p, TextSelector.readSoFar(p, 99))
    }

    @Test
    fun readSoFar_of_empty_chapter_is_empty() {
        assertTrue(TextSelector.readSoFar(emptyList(), 3).isEmpty())
    }

    @Test
    fun lastParagraphs_returns_five_ending_at_position() {
        val p = paragraphs(20)
        assertEquals(p.subList(5, 10), TextSelector.lastParagraphs(p, 9))
    }

    @Test
    fun lastParagraphs_is_shorter_at_chapter_start() {
        val p = paragraphs(20)
        assertEquals(p.subList(0, 3), TextSelector.lastParagraphs(p, 2))
    }

    @Test
    fun earlierSummaries_only_before_current_sorted() {
        val summaries = mapOf(3 to "S3", 1 to "S1", 4 to "S4", 2 to "S2")
        val result = TextSelector.earlierSummaries(summaries, currentChapter = 3)
        assertEquals(listOf(ChapterSummary(1, "S1"), ChapterSummary(2, "S2")), result)
    }

    @Test
    fun earlierSummaries_skips_blank() {
        val summaries = mapOf(1 to "S1", 2 to "  ", 3 to "S3")
        val result = TextSelector.earlierSummaries(summaries, currentChapter = 4)
        assertEquals(listOf(ChapterSummary(1, "S1"), ChapterSummary(3, "S3")), result)
    }
}