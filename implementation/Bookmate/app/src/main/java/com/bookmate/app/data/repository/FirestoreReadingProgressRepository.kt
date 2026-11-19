package com.bookmate.app.data.repository

import com.bookmate.app.domain.model.BookProgress
import com.bookmate.app.domain.model.ReadingProgress
import com.bookmate.app.domain.repository.ReadingProgressRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

@Singleton
class FirestoreReadingProgressRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ReadingProgressRepository {

    override suspend fun getProgress(bookId: String): Result<ReadingProgress?> = safeCall {
        val ref = progressRef(bookId) ?: return@safeCall null
        val doc = ref.get().await()
        if (!doc.exists()) null else doc.toProgress()
    }

    override suspend fun saveProgress(bookId: String, chapter: Int, paragraph: Int): Result<Unit> =
        safeCall {
            val ref = progressRef(bookId) ?: error("Not signed in")
            val exists = ref.get().await().exists()
            val data = mutableMapOf<String, Any>(
                "chapter" to chapter,
                "paragraph" to paragraph,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (!exists) data["finished"] = false
            ref.set(data, SetOptions.merge()).await()
            Unit
        }

    override suspend fun getLatestInProgress(): Result<BookProgress?> = safeCall {
        val uid = auth.currentUser?.uid ?: return@safeCall null
        val snapshot = firestore.collection("users").document(uid)
            .collection("readingProgress")
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .await()
        snapshot.documents
            .firstOrNull { it.getBoolean("finished") != true }
            ?.let { BookProgress(bookId = it.id, progress = it.toProgress()) }
    }

    private fun DocumentSnapshot.toProgress() = ReadingProgress(
        chapter = getLong("chapter")?.toInt() ?: 1,
        paragraph = getLong("paragraph")?.toInt() ?: 0,
        finished = getBoolean("finished") ?: false
    )

    private fun progressRef(bookId: String): DocumentReference? {
        val uid = auth.currentUser?.uid ?: return null
        return firestore.collection("users").document(uid)
            .collection("readingProgress").document(bookId)
    }

    private inline fun <T> safeCall(block: () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
}