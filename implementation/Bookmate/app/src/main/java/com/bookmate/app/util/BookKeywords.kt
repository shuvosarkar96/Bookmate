package com.bookmate.app.util

private val NON_WORD = Regex("[^\\p{L}\\p{N}]+")

/**
 * Search keywords of a book: lowercase words of at least 2 characters, no duplicates, sorted.
 * Same rule as the seed tool, so added and seeded books are found the same way.
 */
fun makeSearchKeywords(vararg parts: String): List<String> =
    parts.flatMap { it.lowercase().split(NON_WORD) }
        .filter { it.length >= 2 }
        .toSortedSet()
        .toList()