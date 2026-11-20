package com.bookmate.app.util

const val NEW_PASSWORD_MIN_LENGTH = 8

enum class PasswordProblem { CURRENT_EMPTY, TOO_SHORT, MISMATCH, SAME_AS_CURRENT }

/** Checks the Change password form. Returns the first problem, or null when it is fine. */
fun checkNewPassword(current: String, new: String, confirm: String): PasswordProblem? = when {
    current.isEmpty() -> PasswordProblem.CURRENT_EMPTY
    new.length < NEW_PASSWORD_MIN_LENGTH -> PasswordProblem.TOO_SHORT
    new != confirm -> PasswordProblem.MISMATCH
    new == current -> PasswordProblem.SAME_AS_CURRENT
    else -> null
}