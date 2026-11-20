package com.bookmate.app.domain.ai

import com.bookmate.app.domain.model.AccessType
import com.bookmate.app.domain.model.Book
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class RecommendationParserTest {

    private fun parserBook(id: String) = Book(
        id = id,
        title = "Title $id",
        author = "Author",
        isbn = "",
        genre = "Classic",
        description = "A description.",
        coverUrl = null,
        accessType = AccessType.FREE,
        hasFullText = true,
        chapterCount = 1,
        searchKeywords = emptyList()
    )

    private val catalog = listOf(parserBook("a"), parserBook("b"), parserBook("c"))

    @Test
    fun parses_a_plain_array() {
        val result = RecommendationParser.parse(
            """[{"bookId":"a","reason":"Nice"},{"bookId":"b","reason":"Good"}]""", catalog, 5
        )
        assertNotNull(result)
        assertEquals(listOf("a", "b"), result!!.map { it.book.id })
        assertEquals("Nice", result[0].reason)
    }

    @Test
    fun accepts_json_inside_code_fences_and_extra_text() {
        val raw = "Here you go:\n```json\n[{\"bookId\":\"a\",\"reason\":\"Nice\"}]\n```"
        val result = RecommendationParser.parse(raw, catalog, 5)
        assertEquals(listOf("a"), result!!.map { it.book.id })
    }

    @Test
    fun drops_ids_that_are_not_in_the_catalog() {
        val result = RecommendationParser.parse(
            """[{"bookId":"zzz","reason":"x"},{"bookId":"a","reason":"y"}]""", catalog, 5
        )
        assertEquals(listOf("a"), result!!.map { it.book.id })
    }

    @Test
    fun removes_duplicate_ids() {
        val result = RecommendationParser.parse(
            """[{"bookId":"a","reason":"x"},{"bookId":"a","reason":"y"},{"bookId":"b","reason":"z"}]""", catalog, 5
        )
        assertEquals(listOf("a", "b"), result!!.map { it.book.id })
        assertEquals("x", result[0].reason)
    }

    @Test
    fun keeps_at_most_max_items() {
        val result = RecommendationParser.parse(
            """[{"bookId":"a"},{"bookId":"b"},{"bookId":"c"}]""", catalog, 2
        )
        assertEquals(2, result!!.size)
    }

    @Test
    fun broken_json_returns_null() {
        assertNull(RecommendationParser.parse("not json at all", catalog, 5))
        assertNull(RecommendationParser.parse("""[{"bookId": """, catalog, 5))
        assertNull(RecommendationParser.parse("[{bad}]", catalog, 5))
    }

    @Test
    fun an_answer_without_a_valid_id_returns_null() {
        assertNull(RecommendationParser.parse("""[{"bookId":"zzz","reason":"x"}]""", catalog, 5))
        assertNull(RecommendationParser.parse("[]", catalog, 5))
    }

    @Test
    fun handles_escapes_in_the_reason() {
        val result = RecommendationParser.parse(
            """[{"bookId":"a","reason":"Say \"hi\" \u0041"}]""", catalog, 5
        )
        assertEquals("Say \"hi\" A", result!!.first().reason)
    }

    @Test
    fun a_missing_reason_becomes_empty() {
        val result = RecommendationParser.parse("""[{"bookId":"a"}]""", catalog, 5)
        assertEquals("", result!!.first().reason)
    }
}