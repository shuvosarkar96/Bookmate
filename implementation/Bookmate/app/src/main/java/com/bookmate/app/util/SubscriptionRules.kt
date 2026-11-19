package com.bookmate.app.util

import com.bookmate.app.domain.model.Subscription

private const val DAY_MILLIS = 24L * 60L * 60L * 1000L

/**
 * End date after subscribing or renewing. If the current subscription is still
 * active, the new period is added after its end date. Otherwise it starts now.
 */
fun nextEndDate(
    current: Subscription?,
    periodDays: Int,
    nowMillis: Long = System.currentTimeMillis()
): Long {
    val base = if (current != null && current.isActive(nowMillis)) current.endDate else nowMillis
    return base + periodDays * DAY_MILLIS
}