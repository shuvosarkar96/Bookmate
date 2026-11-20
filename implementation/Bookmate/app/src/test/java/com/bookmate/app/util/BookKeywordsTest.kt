package com.bookmate.app.util

import org.junit.Assert.assertEquals
import org.junit.Test

class BookKeywordsTest {

    @Test
    fun words_are_lowercase_and_sorted() {
        assertEquals(
            listOf("and", "austen", "classic", "jane", "prejudice", "pride"),
            makeSearchKeywords("Pride and Prejudice", "Jane Austen", "Classic")
        )
    }

    @Test
    fun one_letter_words_are_dropped() {
        assertEquals(
            listOf("cities", "dickens", "of", "tale", "two"),
            makeSearchKeywords("A Tale of Two Cities", "Dickens", "")
        )
    }

    @Test
    fun punctuation_splits_words() {
        assertEquals(listOf("help", "self"), makeSearchKeywords("Self-help", "", ""))
    }

    @Test
    fun duplicates_are_removed() {
        assertEquals(listOf("emma"), makeSearchKeywords("Emma", "Emma", ""))
    }
}