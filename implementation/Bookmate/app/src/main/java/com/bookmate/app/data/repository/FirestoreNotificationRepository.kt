package com.bookmate.app.data.repository

import com.bookmate.app.domain.model.AppNotification
import com.bookmate.app.domain.repository.NotificationRepository
import com.bookmate.app.util.NewNotification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.tasks.await

class FirestoreNotificationRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : NotificationRepository {

    private fun collection(): CollectionReference {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("Not signed in")
        return firestore.collection("users").document(uid).collection("notifications")
    }

    private inline fun <T> call(block: () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun getNotifications(): Result<List<AppNotification>> = call {
        collection().get().await().documents
            .map { doc ->
                AppNotification(
                    id = doc.id,
                    type = doc.getString("type").orEmpty(),
                    endDate = doc.getLong("endDate") ?: 0L,
                    createdAt = doc.getTimestamp("createdAt")?.toDate()?.time ?: 0L,
                    read = doc.getBoolean("read") ?: false
                )
            }
            .sortedByDescending { it.createdAt }
    }

    override suspend fun create(notification: NewNotification): Result<Unit> = call {
        collection().document(notification.id).set(
            mapOf(
                "type" to notification.type,
                "endDate" to notification.endDate,
                "createdAt" to FieldValue.serverTimestamp(),
                "read" to false
            )
        ).await()
        Unit
    }

    override suspend fun markRead(id: String): Result<Unit> = call {
        collection().document(id).update("read", true).await()
        Unit
    }

    override suspend fun delete(id: String): Result<Unit> = call {
        collection().document(id).delete().await()
        Unit
    }
}