package com.bookmate.app.domain.model

/**
 * A Member's Bookmate Premium subscription (document subscriptions/{uid}).
 * Dates are epoch milliseconds.
 */
data class Subscription(
    val userId: String,
    val startDate: Long,
    val endDate: Long,
    val status: String,
    val startedBy: String
) {
    /** Active when status is "active" and the end date is still in the future. */
    fun isActive(nowMillis: Long = System.currentTimeMillis()): Boolean =
        status == STATUS_ACTIVE && endDate > nowMillis

    companion object {
        const val STATUS_ACTIVE = "active"
        const val STATUS_ENDED = "ended"
    }
}