package com.bookmate.app.ui.auth

import androidx.annotation.StringRes
import com.bookmate.app.R
import com.bookmate.app.domain.model.AuthFailure

@StringRes
fun AuthFailure?.messageRes(): Int = when (this) {
    AuthFailure.INVALID_CREDENTIALS -> R.string.error_invalid_credentials
    AuthFailure.EMAIL_IN_USE -> R.string.error_email_in_use
    AuthFailure.WEAK_PASSWORD -> R.string.error_weak_password
    AuthFailure.NETWORK -> R.string.error_network
    AuthFailure.UNKNOWN, null -> R.string.error_unknown
}