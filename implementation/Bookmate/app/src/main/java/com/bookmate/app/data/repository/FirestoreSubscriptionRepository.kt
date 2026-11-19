package com.bookmate.app.data.repository

import com.bookmate.app.domain.model.Plan
import com.bookmate.app.domain.model.Subscription
import com.bookmate.app.domain.repository.SubscriptionRepository
import com.bookmate.app.util.nextEndDate
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

@Singleton
class FirestoreSubscriptionRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : SubscriptionRepository {

    override suspend fun getPlan(): Result<Plan?> = safeCall {
        val doc = firestore.collection("config").document("plan").get().await()
        if (!doc.exists()) return@safeCall null
        Plan(
            name = doc.getString("name").orEmpty(),
            price = doc.getDouble("price") ?: 0.0,
            priceLabel = doc.getString("priceLabel").orEmpty(),
            periodDays = doc.getLong("periodDays")?.toInt() ?: 30
        )
    }

    override suspend fun getMySubscription(): Result<Subscription?> = safeCall {
        val uid = auth.currentUser?.uid ?: return@safeCall null
        readSubscription(uid)
    }

    override suspend fun subscribe(periodDays: Int): Result<Subscription> = safeCall {
        val uid = auth.currentUser?.uid ?: error("Not signed in")
        val current = readSubscription(uid)
        val now = System.currentTimeMillis()
        val endMillis = nextEndDate(current, periodDays, now)
        val startMillis = if (current != null && current.isActive(now)) current.startDate else now
        firestore.collection("subscriptions").document(uid).set(
            mapOf(
                "userId" to uid,
                "startDate" to Timestamp(Date(startMillis)),
                "endDate" to Timestamp(Date(endMillis)),
                "status" to Subscription.STATUS_ACTIVE,
                "startedBy" to "member",
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
        Subscription(
            userId = uid,
            startDate = startMillis,
            endDate = endMillis,
            status = Subscription.STATUS_ACTIVE,
            startedBy = "member"
        )
    }

    private suspend fun readSubscription(uid: String): Subscription? {
        val doc = firestore.collection("subscriptions").document(uid).get().await()
        if (!doc.exists()) return null
        return Subscription(
            userId = uid,
            startDate = doc.getTimestamp("startDate").toMillis(),
            endDate = doc.getTimestamp("endDate").toMillis(),
            status = doc.getString("status").orEmpty(),
            startedBy = doc.getString("startedBy").orEmpty()
        )
    }

    private fun Timestamp?.toMillis(): Long = this?.toDate()?.time ?: 0L

    private inline fun <T> safeCall(block: () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
}