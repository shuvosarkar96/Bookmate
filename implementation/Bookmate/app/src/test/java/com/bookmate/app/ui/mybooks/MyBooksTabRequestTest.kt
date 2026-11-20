package com.bookmate.app.ui.mybooks

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MyBooksTabRequestTest {
    @Test
    fun a_requested_tab_is_taken_once() {
        val request = MyBooksTabRequest()
        request.request(MyBooksTab.SAVED)
        assertEquals(MyBooksTab.SAVED, request.consume())
        assertNull(request.consume())
    }

    @Test
    fun without_a_request_nothing_is_taken() {
        assertNull(MyBooksTabRequest().consume())
    }

    @Test
    fun a_new_request_replaces_the_old_one() {
        val request = MyBooksTabRequest()
        request.request(MyBooksTab.SAVED)
        request.request(MyBooksTab.FINISHED)
        assertEquals(MyBooksTab.FINISHED, request.consume())
    }
}