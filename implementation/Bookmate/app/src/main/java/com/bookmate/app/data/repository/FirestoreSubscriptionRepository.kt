package com.bookmate.app.data.repository

import com.bookmate.app.domain.model.Subscription
import com.bookmate.app.domain.repository.SubscriptionRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

@Singleton
class FirestoreSubscriptionRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : SubscriptionRepository {

    override suspend fun getMySubscription(): Result<Subscription?> = safeCall {
        val uid = auth.currentUser?.uid ?: return@safeCall null
        val doc = firestore.collection("subscriptions").document(uid).get().await()
        if (!doc.exists()) return@safeCall null
        Subscription(
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