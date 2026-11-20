package com.bookmate.app.domain.ai

import com.bookmate.app.domain.model.AccessType
import com.bookmate.app.domain.model.Book
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleBasedRecommenderTest {

    private fun ruleBook(id: String, title: String, genre: String) = Book(
        id = id,
        title = title,
        author = "Author",
        isbn = "",
        genre = genre,
        description = "A description.",
        coverUrl = null,
        accessType = AccessType.FREE,
        hasFullText = true,
        chapterCount = 1,
        searchKeywords = emptyList()
    )

    private val catalog = listOf(
        ruleBook("a", "Pride", "Classic"),
        ruleBook("b", "Dark Night", "Mystery"),
        ruleBook("c", "Emma", "Classic")
    )

    @Test
    fun books_of_a_favorite_genre_come_first() {
        val result = RuleBasedRecommender.recommend(catalog, listOf("Mystery"), emptySet(), "", 5)
        assertEquals("b", result.first().book.id)
    }

    @Test
    fun words_of_the_request_add_to_the_score() {
        val result = RuleBasedRecommender.recommend(catalog, emptyList(), emptySet(), "something dark", 5)
        assertEquals(listOf("b"), result.map { it.book.id })
    }

    @Test
    fun books_already_read_are_left_out() {
        val result = RuleBasedRecommender.recommend(catalog, listOf("Classic"), setOf("a"), "", 5)
        assertEquals(listOf("c"), result.map { it.book.id })
    }

    @Test
    fun no_match_gives_an_empty_list() {
        assertTrue(RuleBasedRecommender.recommend(catalog, listOf("Poetry"), emptySet(), "", 5).isEmpty())
    }

    @Test
    fun respects_the_maximum() {
        val result = RuleBasedRecommender.recommend(catalog, listOf("Classic"), emptySet(), "", 1)
        assertEquals(1, result.size)
    }
}