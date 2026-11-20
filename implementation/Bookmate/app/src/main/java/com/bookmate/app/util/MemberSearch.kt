package com.bookmate.app.util

import com.bookmate.app.domain.model.MemberSubscription

/** Keeps the Members whose name or email contains the search text (any case). */
fun filterMembers(members: List<MemberSubscription>, query: String): List<MemberSubscription> {
    val text = query.trim().lowercase()
    if (text.isEmpty()) return members
    return members.filter { it.name.lowercase().contains(text) || it.email.lowercase().contains(text) }
}