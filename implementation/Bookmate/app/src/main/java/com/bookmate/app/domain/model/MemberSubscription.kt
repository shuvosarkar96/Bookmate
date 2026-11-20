package com.bookmate.app.domain.model

/** A Member as the Admin sees them: name, email and their subscription (null if none). */
data class MemberSubscription(
    val userId: String,
    val name: String,
    val email: String,
    val subscription: Subscription?
)