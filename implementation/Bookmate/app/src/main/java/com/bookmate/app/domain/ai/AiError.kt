package com.bookmate.app.domain.ai

/** What went wrong with an AI request, in terms the screen can show. */
enum class AiError(val retryable: Boolean) {
    NO_INTERNET(false),
    TIMEOUT(true),
    LIMIT(false),
    BLOCKED(false),
    OTHER(true)
}

/** The failure that AiRepository returns or throws. */
class AiException(val error: AiError, cause: Throwable? = null) : Exception(error.name, cause)