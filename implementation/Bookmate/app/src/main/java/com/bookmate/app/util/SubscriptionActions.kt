package com.bookmate.app.util

import com.bookmate.app.domain.model.Subscription

private const val ACTION_DAY_MILLIS = 24L * 60L * 60L * 1000L

/** Admin "Activate": a new period that starts now, whatever the old subscription was. */
fun activateSubscription(
    userId: String,
    periodDays: Int,
    nowMillis: Long = System.currentTimeMillis()
): Subscription = Subscription(
    userId = userId,
    startDate = nowMillis,
    endDate = nowMillis + periodDays * ACTION_DAY_MILLIS,
    status = Subscription.STATUS_ACTIVE,
    startedBy = "admin"
)

/** Admin "Extend": adds a period after the end date when active, otherwise starts now. */
fun extendSubscription(
    userId: String,
    current: Subscription?,
    periodDays: Int,
    nowMillis: Long = System.currentTimeMillis()
): Subscription {
    val active = current != null && current.isActive(nowMillis)
    return Subscription(
        userId = userId,
        startDate = if (active) current!!.startDate else nowMillis,
        endDate = nextEndDate(current, periodDays, nowMillis),
        status = Subscription.STATUS_ACTIVE,
        startedBy = "admin"
    )
}

/** Admin "End": the subscription stops now. An end date in the past is kept. */
fun endSubscription(
    current: Subscription,
    nowMillis: Long = System.currentTimeMillis()
): Subscription = current.copy(
    status = Subscription.STATUS_ENDED,
    endDate = minOf(current.endDate, nowMillis)
)