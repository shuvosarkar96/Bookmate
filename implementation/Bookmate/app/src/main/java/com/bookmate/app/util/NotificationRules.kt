package com.bookmate.app.util

import com.bookmate.app.domain.model.AppNotification
import com.bookmate.app.domain.model.Subscription

/** A subscription notice that still has to be created. The id is fixed, so it is created only once. */
data class NewNotification(val id: String, val type: String, val endDate: Long)

/** An "ending soon" notice is made when the subscription ends within this many days. */
const val ENDING_SOON_DAYS = 3

private const val DAY_MILLIS = 24L * 60 * 60 * 1000

/**
 * The subscription notices this Member should have but does not have yet.
 * Ended or expired: one "ended" notice. Active and ending soon: one "ending" notice.
 * A notice is skipped when its id is in [existingIds].
 */
fun neededNotifications(
    subscription: Subscription?,
    existingIds: Set<String>,
    nowMillis: Long
): List<NewNotification> {
    if (subscription == null) return emptyList()
    val endDate = subscription.endDate
    val notice = if (!subscription.isActive(nowMillis)) {
        NewNotification("ended_$endDate", AppNotification.TYPE_ENDED, endDate)
    } else if (endDate - nowMillis <= ENDING_SOON_DAYS * DAY_MILLIS) {
        NewNotification("ending_$endDate", AppNotification.TYPE_ENDING, endDate)
    } else {
        return emptyList()
    }
    return if (notice.id in existingIds) emptyList() else listOf(notice)
}