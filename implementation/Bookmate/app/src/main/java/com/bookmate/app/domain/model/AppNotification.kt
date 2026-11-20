package com.bookmate.app.domain.model

/**
 * A message for the signed-in Member (document users/{uid}/notifications/{id}).
 * Dates are epoch milliseconds. [endDate] is the end date of the subscription the message is about.
 */
data class AppNotification(
    val id: String,
    val type: String,
    val endDate: Long,
    val createdAt: Long,
    val read: Boolean
) {
    companion object {
        const val TYPE_ENDING = "subscription_ending"
        const val TYPE_ENDED = "subscription_ended"
    }
}