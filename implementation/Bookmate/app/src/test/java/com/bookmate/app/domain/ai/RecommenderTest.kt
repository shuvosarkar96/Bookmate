package com.bookmate.app.domain.ai

import com.bookmate.app.domain.model.AccessType
import com.bookmate.app.domain.model.Book
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private class RecommenderFakeAi(private val answers: List<Result<String>>) : AiRepository {
    var calls = 0

    override suspend fun generate(prompt: String): Result<String> =
        answers[minOf(calls++, answers.size - 1)]

    override fun generateStream(prompt: String): Flow<String> = emptyFlow()
}

class RecommenderTest {

    private fun recommenderBook(id: String, genre: String) = Book(
        id = id,
        title = "Title $id",
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
        recommenderBook("a", "Classic"),
        recommenderBook("b", "Mystery"),
        recommenderBook("c", "Classic")
    )
    private val valid = """[{"bookId":"b","reason":"Fits you"}]"""

    private fun run(ai: RecommenderFakeAi, canUse: Boolean = true, books: List<Book> = catalog) = runBlocking {
        Recommender(ai).recommend(books, listOf("Classic"), emptyList(), "") { canUse }
    }

    @Test
    fun uses_the_ai_answer_when_it_is_valid() {
        val ai = RecommenderFakeAi(listOf(Result.success(valid)))
        val outcome = run(ai)
        assertTrue(outcome.fromAi)
        assertEquals(listOf("b"), outcome.items.map { it.book.id })
        assertEquals(1, ai.calls)
    }

    @Test
    fun asks_once_more_when_the_first_answer_is_bad() {
        val ai = RecommenderFakeAi(listOf(Result.success("oops"), Result.success(valid)))
        val outcome = run(ai)
        assertTrue(outcome.fromAi)
        assertEquals(2, ai.calls)
    }

    @Test
    fun falls_back_to_the_rules_after_two_bad_answers() {
        val ai = RecommenderFakeAi(listOf(Result.success("oops")))
        val outcome = run(ai)
        assertFalse(outcome.fromAi)
        assertEquals(listOf("a", "c"), outcome.items.map { it.book.id })
        assertEquals(AiError.BLOCKED, outcome.aiError)
        assertEquals(2, ai.calls)
    }

    @Test
    fun falls_back_without_a_second_call_when_the_call_fails() {
        val ai = RecommenderFakeAi(listOf(Result.failure(AiException(AiError.TIMEOUT))))
        val outcome = run(ai)
        assertFalse(outcome.fromAi)
        assertEquals(AiError.TIMEOUT, outcome.aiError)
        assertEquals(1, ai.calls)
        assertTrue(outcome.items.isNotEmpty())
    }

    @Test
    fun does_not_call_the_ai_when_the_daily_limit_is_reached() {
        val ai = RecommenderFakeAi(listOf(Result.success(valid)))
        val outcome = run(ai, canUse = false)
        assertFalse(outcome.fromAi)
        assertEquals(AiError.LIMIT, outcome.aiError)
        assertEquals(0, ai.calls)
    }

    @Test
    fun an_empty_catalog_gives_an_empty_result() {
        val ai = RecommenderFakeAi(listOf(Result.success(valid)))
        val outcome = run(ai, books = emptyList())
        assertTrue(outcome.items.isEmpty())
        assertNull(outcome.aiError)
        assertEquals(0, ai.calls)
    }
}