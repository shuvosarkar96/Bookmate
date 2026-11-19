package com.bookmate.app.data.repository

import com.bookmate.app.domain.model.AccessType
import com.bookmate.app.domain.model.Book
import com.bookmate.app.domain.repository.BookRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

@Singleton
class FirebaseBookRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : BookRepository {

    override suspend fun getBooks(): Result<List<Book>> = safeCall {
        firestore.collection(BOOKS)
            .orderBy("title")
            .get()
            .await()
            .documents
            .mapNotNull { it.toBook() }
    }

    override suspend fun getBook(bookId: String): Result<Book?> = safeCall {
        firestore.collection(BOOKS)
            .document(bookId)
            .get()
            .await()
            .toBook()
    }

    private inline fun <T> safeCall(block: () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }

    private fun DocumentSnapshot.toBook(): Book? {
        if (!exists()) return null
        val title = getString("title") ?: return null
        return Book(
            id = id,
            title = title,
            author = getString("author").orEmpty(),
            isbn = getString("isbn").orEmpty(),
            genre = getString("genre").orEmpty(),
            description = getString("description").orEmpty(),
            coverUrl = getString("coverUrl")?.takeIf { it.isNotBlank() },
            accessType = if (getString("accessType") == "free") AccessType.FREE else AccessType.PREMIUM,
            hasFullText = getBoolean("hasFullText") ?: false,
            chapterCount = (getLong("chapterCount") ?: 0L).toInt(),
            searchKeywords = (get("searchKeywords") as? List<*>)
                ?.filterIsInstance<String>()
                .orEmpty()
        )
    }

    private companion object {
        const val BOOKS = "books"
    }
}