package com.bookmate.app.util

import org.junit.Assert.assertEquals
import org.junit.Test

class InitialsTest {
    @Test
    fun two_words_give_two_capital_letters() {
        assertEquals("SK", initialsOf("sudipta karmaker", "a@b.com"))
    }

    @Test
    fun one_word_gives_one_letter() {
        assertEquals("S", initialsOf("Sudipta", "a@b.com"))
    }

    @Test
    fun only_the_first_two_words_count() {
        assertEquals("AB", initialsOf("  Ann   Beth Carol ", "a@b.com"))
    }

    @Test
    fun a_blank_name_uses_the_email() {
        assertEquals("X", initialsOf("   ", "x@y.com"))
    }

    @Test
    fun nothing_at_all_gives_a_question_mark() {
        assertEquals("?", initialsOf("", ""))
    }
}