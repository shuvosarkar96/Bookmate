package com.bookmate.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BookFormTest {

    private fun check(
        title: String = "Emma",
        author: String = "Jane Austen",
        genre: String = "Classic",
        isbn: String = "",
        cover: String = ""
    ) = checkBookForm(title, author, genre, isbn, cover)

    @Test
    fun a_complete_form_has_no_problem() {
        assertNull(check(isbn = "978-0-14-143951-8", cover = "https://example.com/c.jpg"))
    }

    @Test
    fun a_blank_title_is_rejected() {
        assertEquals(BookFormProblem.TITLE_EMPTY, check(title = "  "))
    }

    @Test
    fun a_blank_author_is_rejected() {
        assertEquals(BookFormProblem.AUTHOR_EMPTY, check(author = ""))
    }

    @Test
    fun a_blank_genre_is_rejected() {
        assertEquals(BookFormProblem.GENRE_EMPTY, check(genre = ""))
    }

    @Test
    fun an_isbn_with_hyphens_and_spaces_is_accepted() {
        assertNull(check(isbn = "978 0 14 143951 8"))
    }

    @Test
    fun a_short_isbn_is_rejected() {
        assertEquals(BookFormProblem.ISBN_INVALID, check(isbn = "12345"))
    }

    @Test
    fun an_isbn_10_may_end_with_x() {
        assertNull(check(isbn = "080442957x"))
    }

    @Test
    fun isbn_and_cover_are_optional() {
        assertNull(check(isbn = "", cover = ""))
    }

    @Test
    fun a_cover_that_is_not_an_http_address_is_rejected() {
        assertEquals(BookFormProblem.COVER_INVALID, check(cover = "ftp://example.com/c.jpg"))
        assertEquals(BookFormProblem.COVER_INVALID, check(cover = "not a url"))
    }

    @Test
    fun normalizeIsbn_removes_separators_and_uppercases() {
        assertEquals("080442957X", normalizeIsbn("0-8044-2957-x"))
    }
}