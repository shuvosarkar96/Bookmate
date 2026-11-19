package com.bookmate.app.data.repository

import com.bookmate.app.domain.model.AccessDeniedException
import com.bookmate.app.domain.model.Chapter
import com.bookmate.app.domain.repository.ChapterRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

@Singleton
class FirestoreChapterRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : ChapterRepository {

    override suspend fun getChapter(bookId: String, chapterNo: Int): Result<Chapter?> =
        try {
            val doc = firestore.collection("books").document(bookId)
                .collection("chapters").document(chapterNo.toString())
                .get().await()
            if (!doc.exists()) {
                Result.success(null)
            } else {
                val paragraphs = (doc.get("paragraphs") as? List<*>)
                    ?.filterIsInstance<String>()
                    .orEmpty()
                Result.success(
                    Chapter(
                        number = doc.getLong("number")?.toInt() ?: chapterNo,
                        title = doc.getString("title").orEmpty(),
                        paragraphs = paragraphs
                    )
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                Result.failure(AccessDeniedException())
            } else {
                Result.failure(e)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
}