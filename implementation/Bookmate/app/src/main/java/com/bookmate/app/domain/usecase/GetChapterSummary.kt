package com.bookmate.app.domain.usecase

import com.bookmate.app.domain.ai.ChunkSummarizer
import com.bookmate.app.domain.model.Book
import com.bookmate.app.domain.repository.ChapterRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * T2: summary of one chapter. The stored, team-checked summary comes first (no AI request).
 * If the chapter has none, it is created live and is not saved.
 */
class GetChapterSummary @Inject constructor(
    private val chapterRepository: ChapterRepository,
    private val chunkSummarizer: ChunkSummarizer
) {
    operator fun invoke(book: Book, chapter: Int): Flow<String> = flow {
        val loaded = chapterRepository.getChapter(book.id, chapter).getOrThrow()
            ?: throw IllegalStateException("Chapter not found")
        val stored = loaded.summary
        if (stored != null) {
            emit(stored)
        } else {
            emit(chunkSummarizer.summarize(book.title, book.author, loaded.paragraphs).getOrThrow())
        }
    }
}