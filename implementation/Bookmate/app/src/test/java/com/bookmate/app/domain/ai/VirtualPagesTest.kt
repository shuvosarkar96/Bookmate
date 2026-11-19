package com.bookmate.app.domain.ai

import org.junit.Assert.assertEquals
import org.junit.Test

class VirtualPagesTest {

    private fun words(n: Int): String = List(n) { "w" }.joinToString(" ")

    private val chapter = List(6) { words(100) }

    @Test
    fun empty_chapter_is_page_one() {
        assertEquals(1, VirtualPages.pageOf(emptyList(), 0))
    }

    @Test
    fun first_paragraph_is_page_one() {
        assertEquals(1, VirtualPages.pageOf(chapter, 0))
    }

    @Test
    fun page_changes_after_250_words() {
        assertEquals(1, VirtualPages.pageOf(chapter, 2))
        assertEquals(2, VirtualPages.pageOf(chapter, 3))
        assertEquals(3, VirtualPages.pageOf(chapter, 5))
    }

    @Test
    fun index_past_the_end_uses_the_last_paragraph() {
        assertEquals(3, VirtualPages.pageOf(chapter, 99))
    }

    @Test
    fun negative_index_is_page_one() {
        assertEquals(1, VirtualPages.pageOf(chapter, -4))
    }
}