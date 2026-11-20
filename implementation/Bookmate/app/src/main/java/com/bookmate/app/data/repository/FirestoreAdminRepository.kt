package com.bookmate.app.data.repository

import com.bookmate.app.domain.model.MemberSubscription
import com.bookmate.app.domain.model.Subscription
import com.bookmate.app.domain.repository.AdminRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

@Singleton
class FirestoreAdminRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : AdminRepository {

    override suspend fun getMembers(): Result<List<MemberSubscription>> = safeCall {
        val users = firestore.collection("users").get().await().documents
        val subscriptions = firestore.collection("subscriptions").get().await().documents
            .associate { it.id to it.toSubscription(it.id) }
        users
            .filter { it.getString("role") != "admin" }
            .map {
                MemberSubscription(
                    userId = it.id,
                    name = it.getString("name").orEmpty(),
                    email = it.getString("email").orEmpty(),
                    subscription = subscriptions[it.id]
                )
            }
            .sortedBy { it.name.lowercase() }
    }

    override suspend fun saveSubscription(subscription: Subscription): Result<Unit> = safeCall {
        firestore.collection("subscriptions").document(subscription.userId).set(
            mapOf(
                "userId" to subscription.userId,
                "startDate" to Timestamp(Date(subscription.startDate)),
                "endDate" to Timestamp(Date(subscription.endDate)),
                "status" to subscription.status,
                "startedBy" to subscription.startedBy,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
        Unit
    }

    override suspend fun updatePlanPrice(price: Double, priceLabel: String): Result<Unit> = safeCall {
        firestore.collection("config").document("plan").update(
            mapOf(
                "price" to price,
                "priceLabel" to priceLabel
            )
        ).await()
        Unit
    }

    private fun DocumentSnapshot.toSubscription(uid: String): Subscription = Subscription(
        userId = uid,
        startDate = getTimestamp("startDate")?.toDate()?.time ?: 0L,
        endDate = getTimestamp("endDate")?.toDate()?.time ?: 0L,
        status = getString("status").orEmpty(),
        startedBy = getString("startedBy").orEmpty()
    )

    private inline fun <T> safeCall(block: () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
}