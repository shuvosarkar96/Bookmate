package com.bookmate.app.domain.ai

import org.junit.Assert.assertEquals
import org.junit.Test

class AiOutputTest {

    @Test
    fun removes_the_text_tags() {
        assertEquals("Hello", AiOutput.clean("<text>Hello</text>"))
    }

    @Test
    fun trims_the_whitespace_the_tags_leave_behind() {
        assertEquals("Hi there", AiOutput.clean("<text>\nHi there\n</text>"))
    }

    @Test
    fun is_not_case_sensitive() {
        assertEquals("x", AiOutput.clean("<TEXT>x</TEXT>"))
    }

    @Test
    fun removes_a_tag_that_is_still_streaming() {
        assertEquals("Hello", AiOutput.clean("Hello <te"))
        assertEquals("Hello", AiOutput.clean("Hello </tex"))
    }

    @Test
    fun leaves_normal_text_alone() {
        assertEquals("1 < 2 and 3 > 2", AiOutput.clean("1 < 2 and 3 > 2"))
    }
}