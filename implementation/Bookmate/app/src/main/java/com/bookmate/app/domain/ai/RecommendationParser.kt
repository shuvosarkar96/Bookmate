package com.bookmate.app.domain.ai

import com.bookmate.app.domain.model.Book

/**
 * Checks the AI recommendation answer (T8). Keeps only book ids that exist in the catalog,
 * removes duplicates and keeps at most [max] items. Returns null when the answer is not usable:
 * the caller then retries once and falls back to RuleBasedRecommender.
 */
object RecommendationParser {
    const val MAX_REASON_LENGTH = 160

    fun parse(raw: String, catalog: List<Book>, max: Int): List<Recommendation>? {
        val start = raw.indexOf('[')
        val end = raw.lastIndexOf(']')
        if (start < 0 || end <= start) return null
        val items = try {
            MiniJson.parse(raw.substring(start, end + 1)) as? List<*>
        } catch (e: IllegalArgumentException) {
            null
        } ?: return null

        val byId = catalog.associateBy { it.id }
        val seen = mutableSetOf<String>()
        val result = mutableListOf<Recommendation>()
        for (item in items) {
            if (result.size >= max) break
            val map = item as? Map<*, *> ?: continue
            val id = (map["bookId"] as? String)?.trim() ?: continue
            val book = byId[id] ?: continue
            if (!seen.add(id)) continue
            val reason = (map["reason"] as? String).orEmpty().trim().take(MAX_REASON_LENGTH)
            result.add(Recommendation(book, reason))
        }
        return result.ifEmpty { null }
    }
}