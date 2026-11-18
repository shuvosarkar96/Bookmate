package com.bookmate.app.util

private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

fun isValidEmail(email: String): Boolean = EMAIL_REGEX.matches(email.trim())