package com.bookmate.app.util

/** Up to two capital letters for the avatar: the first letters of the first two words of the name. */
fun initialsOf(name: String, email: String): String {
    val fromName = name.trim()
        .split(Regex("\\s+"))
        .filter { it.isNotEmpty() }
        .take(2)
        .map { it.first().uppercaseChar() }
    if (fromName.isNotEmpty()) return fromName.joinToString("")
    val first = email.trim().firstOrNull() ?: return "?"
    return first.uppercaseChar().toString()
}