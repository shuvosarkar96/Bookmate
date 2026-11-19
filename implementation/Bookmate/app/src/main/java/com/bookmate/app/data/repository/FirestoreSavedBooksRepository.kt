package com.bookmate.app.data.repository

import com.bookmate.app.domain.model.Book
import com.bookmate.app.domain.repository.SavedBooksRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

@Singleton
class FirestoreSavedBooksRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : SavedBooksRepository {

    override suspend fun isSaved(bookId: String): Result<Boolean> = safeCall {
        val ref = savedRef(bookId)
        ref != null && ref.get().await().exists()
    }

    override suspend fun setSaved(book: Book, saved: Boolean): Result<Unit> = safeCall {
        val ref = savedRef(book.id) ?: error("Not signed in")
        if (saved) {
            ref.set(
                mapOf(
                    "bookTitle" to book.title,
                    "savedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } else {
            ref.delete().await()
        }
        Unit
    }

    private fun savedRef(bookId: String): DocumentReference? {
        val uid = auth.currentUser?.uid ?: return null
        return firestore.collection("users").document(uid)
            .collection("savedBooks").document(bookId)
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