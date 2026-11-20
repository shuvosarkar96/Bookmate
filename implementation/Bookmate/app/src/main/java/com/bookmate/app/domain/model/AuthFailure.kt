package com.bookmate.app.domain.model

enum class AuthFailure {
    INVALID_CREDENTIALS,
    EMAIL_IN_USE,
    WEAK_PASSWORD,
    WRONG_PASSWORD,
    NETWORK,
    UNKNOWN
}

class AuthException(
    val failure: AuthFailure,
    cause: Throwable? = null
) : Exception(failure.name, cause)