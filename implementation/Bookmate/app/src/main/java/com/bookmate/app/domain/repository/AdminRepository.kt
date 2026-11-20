package com.bookmate.app.domain.repository

import com.bookmate.app.domain.model.MemberSubscription
import com.bookmate.app.domain.model.Subscription

/** Admin-only reads and writes. Firestore rules allow them only for an Admin. */
interface AdminRepository {
    /** All Members (not Admins) with their subscription, sorted by name. */
    suspend fun getMembers(): Result<List<MemberSubscription>>

    /** Writes the whole subscription document of [Subscription.userId]. */
    suspend fun saveSubscription(subscription: Subscription): Result<Unit>

    /** Changes the price and its label in config/plan. */
    suspend fun updatePlanPrice(price: Double, priceLabel: String): Result<Unit>
}