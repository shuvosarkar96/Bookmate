package com.bookmate.app.util

import com.bookmate.app.domain.model.AppNotification
import com.bookmate.app.domain.model.Subscription
import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationRulesTest {

    private val day = 24L * 60 * 60 * 1000
    private val now = 1_000_000_000_000L

    private fun sub(endDate: Long, status: String = Subscription.STATUS_ACTIVE) =
        Subscription("u1", now - 30 * day, endDate, status, "member")

    @Test
    fun no_subscription_needs_no_notice() {
        assertEquals(emptyList<NewNotification>(), neededNotifications(null, emptySet(), now))
    }

    @Test
    fun an_active_subscription_far_from_the_end_needs_no_notice() {
        assertEquals(emptyList<NewNotification>(), neededNotifications(sub(now + 10 * day), emptySet(), now))
    }

    @Test
    fun an_active_subscription_ending_in_two_days_needs_an_ending_notice() {
        val end = now + 2 * day
        val result = neededNotifications(sub(end), emptySet(), now)
        assertEquals(listOf(NewNotification("ending_$end", AppNotification.TYPE_ENDING, end)), result)
    }

    @Test
    fun exactly_three_days_left_still_counts_as_ending_soon() {
        val end = now + 3 * day
        assertEquals(1, neededNotifications(sub(end), emptySet(), now).size)
    }

    @Test
    fun an_existing_ending_notice_is_not_created_again() {
        val end = now + 2 * day
        assertEquals(0, neededNotifications(sub(end), setOf("ending_$end"), now).size)
    }

    @Test
    fun an_expired_subscription_needs_an_ended_notice() {
        val end = now - 1000
        val result = neededNotifications(sub(end), emptySet(), now)
        assertEquals(listOf(NewNotification("ended_$end", AppNotification.TYPE_ENDED, end)), result)
    }

    @Test
    fun a_subscription_ended_by_the_admin_needs_an_ended_notice() {
        val result = neededNotifications(sub(now, Subscription.STATUS_ENDED), emptySet(), now)
        assertEquals(AppNotification.TYPE_ENDED, result.single().type)
    }

    @Test
    fun an_existing_ended_notice_is_not_created_again() {
        val end = now - 1000
        assertEquals(0, neededNotifications(sub(end), setOf("ended_$end"), now).size)
    }
}