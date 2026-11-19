package com.bookmate.app.domain.repository

import com.bookmate.app.domain.model.Subscription

interface SubscriptionRepository {
    /** The signed-in Member's subscription, or null if they never subscribed. */
    suspend fun getMySubscription(): Result<Subscription?>
}