package com.bookmate.app.util

import com.bookmate.app.domain.model.MemberSubscription
import org.junit.Assert.assertEquals
import org.junit.Test

class MemberSearchTest {

    private val members = listOf(
        MemberSubscription("1", "Anna Lee", "anna@example.com", null),
        MemberSubscription("2", "Bob Stone", "bob@mail.org", null)
    )

    @Test
    fun an_empty_query_returns_everyone() {
        assertEquals(2, filterMembers(members, "  ").size)
    }

    @Test
    fun the_name_is_matched_in_any_case() {
        assertEquals(listOf("1"), filterMembers(members, "ANNA").map { it.userId })
    }

    @Test
    fun the_email_is_matched() {
        assertEquals(listOf("2"), filterMembers(members, "mail.org").map { it.userId })
    }

    @Test
    fun no_match_gives_an_empty_list() {
        assertEquals(0, filterMembers(members, "zzz").size)
    }
}