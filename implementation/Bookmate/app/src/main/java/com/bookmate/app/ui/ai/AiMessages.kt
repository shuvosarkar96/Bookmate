package com.bookmate.app.ui.ai

import androidx.annotation.StringRes
import com.bookmate.app.R
import com.bookmate.app.domain.ai.AiError

@StringRes
fun AiError.messageRes(): Int = when (this) {
    AiError.NO_INTERNET -> R.string.ai_error_no_internet
    AiError.TIMEOUT -> R.string.ai_error_timeout
    AiError.LIMIT -> R.string.ai_error_limit
    AiError.BLOCKED -> R.string.ai_error_blocked
    AiError.OTHER -> R.string.ai_error_other
}