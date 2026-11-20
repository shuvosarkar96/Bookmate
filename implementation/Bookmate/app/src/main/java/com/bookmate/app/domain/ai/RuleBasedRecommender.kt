package com.bookmate.app.domain.ai

import com.bookmate.app.domain.model.Book

/**
 * The fallback when the AI cannot answer: genre match plus words of the reader's request.
 * Books the reader has already read are left out. Books that match nothing are not returned.
 */
object RuleBasedRecommender {
    private const val GENRE_POINTS = 2

    private val stopWords = setOf(
        "the", "and", "for", "book", "books", "about", "with", "some", "want", "like",
        "please", "give", "read", "something", "recommend", "suggest", "any"
    )

    fun recommend(
        catalog: List<Book>,
        favoriteGenres: List<String>,
        recentBookIds: Set<String>,
        request: String,
        max: Int
    ): List<Recommendation> {
        val genres = favoriteGenres.map { it.trim().lowercase() }.filter { it.isNotEmpty() }.toSet()
        val words = request.lowercase()
            .split(Regex("[^\\p{L}\\p{N}]+"))
            .filter { it.length >= 3 && it !in stopWords }
            .toSet()
        return catalog.asSequence()
            .filter { it.id !in recentBookIds }
            .map { it to score(it, genres, words) }
            .filter { it.second > 0 }
            .sortedWith(compareByDescending<Pair<Book, Int>> { it.second }.thenBy { it.first.title })
            .take(max)
            .map { Recommendation(it.first, "") }
            .toList()
    }

    private fun score(book: Book, genres: Set<String>, words: Set<String>): Int {
        var points = 0
        if (book.genre.trim().lowercase() in genres) points += GENRE_POINTS
        val haystack = listOf(
            book.title, book.author, book.genre, book.description, book.searchKeywords.joinToString(" ")
        ).joinToString(" ").lowercase()
        points += words.count { haystack.contains(it) }
        return points
    }
}