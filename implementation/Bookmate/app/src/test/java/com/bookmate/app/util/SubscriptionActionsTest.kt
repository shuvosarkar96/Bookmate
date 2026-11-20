package com.bookmate.app.util

import com.bookmate.app.domain.model.Subscription
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SubscriptionActionsTest {

    private val day = 24L * 60L * 60L * 1000L
    private val now = 1_700_000_000_000L

    /** A subscription that started 5 days ago and ends [endInDays] from now. */
    private fun sub(endInDays: Long, status: String = Subscription.STATUS_ACTIVE) =
        Subscription("u1", now - 5 * day, now + endInDays * day, status, "member")

    @Test
    fun activate_starts_now_and_lasts_the_period() {
        val result = activateSubscription("u1", 30, now)
        assertEquals(now, result.startDate)
        assertEquals(now + 30 * day, result.endDate)
        assertTrue(result.isActive(now))
        assertEquals("admin", result.startedBy)
    }

    @Test
    fun extend_adds_to_the_end_of_an_active_subscription() {
        val current = sub(endInDays = 10)
        val result = extendSubscription("u1", current, 30, now)
        assertEquals(now + 40 * day, result.endDate)
        assertEquals(current.startDate, result.startDate)
    }

    @Test
    fun extend_an_expired_subscription_starts_now() {
        val result = extendSubscription("u1", sub(endInDays = -3), 30, now)
        assertEquals(now, result.startDate)
        assertEquals(now + 30 * day, result.endDate)
    }

    @Test
    fun extend_without_a_subscription_starts_now() {
        val result = extendSubscription("u1", null, 30, now)
        assertEquals(now, result.startDate)
        assertEquals(now + 30 * day, result.endDate)
    }

    @Test
    fun ending_makes_the_subscription_inactive_now() {
        val result = endSubscription(sub(endInDays = 10), now)
        assertFalse(result.isActive(now))
        assertEquals(Subscription.STATUS_ENDED, result.status)
        assertEquals(now, result.endDate)
    }

    @Test
    fun ending_keeps_an_end_date_in_the_past() {
        val result = endSubscription(sub(endInDays = -3), now)
        assertEquals(now - 3 * day, result.endDate)
    }
}