package com.bookmate.app.domain.repository

import com.bookmate.app.domain.model.Plan
import com.bookmate.app.domain.model.Subscription

interface SubscriptionRepository {
    /** The plan from config/plan, or null if it does not exist. */
    suspend fun getPlan(): Result<Plan?>

    /** The signed-in Member's subscription, or null if they never subscribed. */
    suspend fun getMySubscription(): Result<Subscription?>

    /** Demo payment: creates or extends the signed-in Member's subscription. */
    suspend fun subscribe(periodDays: Int): Result<Subscription>
}