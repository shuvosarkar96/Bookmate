package com.bookmate.app.data.repository

import com.bookmate.app.domain.model.AccessType
import com.bookmate.app.domain.model.Book
import com.bookmate.app.domain.model.NewBook
import com.bookmate.app.domain.repository.BookRepository
import com.bookmate.app.util.makeSearchKeywords
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
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

    override suspend fun addBook(book: NewBook): Result<String> = safeCall {
        val ref = firestore.collection(BOOKS).document()
        val data = mutableMapOf<String, Any>(
            "title" to book.title,
            "author" to book.author,
            "isbn" to book.isbn,
            "genre" to book.genre,
            "description" to book.description,
            "accessType" to if (book.accessType == AccessType.FREE) "free" else "premium",
            "hasFullText" to false,
            "chapterCount" to 0,
            "searchKeywords" to makeSearchKeywords(book.title, book.author, book.genre),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        book.coverUrl?.let { data["coverUrl"] = it }
        ref.set(data).await()
        ref.id
    }

    override suspend fun updateBook(bookId: String, book: NewBook): Result<Unit> = safeCall {
        firestore.collection(BOOKS).document(bookId).update(
            mapOf(
                "title" to book.title,
                "author" to book.author,
                "isbn" to book.isbn,
                "genre" to book.genre,
                "description" to book.description,
                "accessType" to if (book.accessType == AccessType.FREE) "free" else "premium",
                "searchKeywords" to makeSearchKeywords(book.title, book.author, book.genre),
                "coverUrl" to (book.coverUrl ?: FieldValue.delete()),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
        Unit
    }

    override suspend fun deleteBook(bookId: String): Result<Unit> = safeCall {
        val bookRef = firestore.collection(BOOKS).document(bookId)
        for (sub in listOf("chapters", "extras")) {
            val docs = bookRef.collection(sub).get().await().documents
            docs.chunked(400).forEach { part ->
                val batch = firestore.batch()
                part.forEach { batch.delete(it.reference) }
                batch.commit().await()
            }
        }
        bookRef.delete().await()
        Unit
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