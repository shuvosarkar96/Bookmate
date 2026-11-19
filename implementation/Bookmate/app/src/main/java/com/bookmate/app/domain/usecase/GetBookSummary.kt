package com.bookmate.app.domain.usecase

import com.bookmate.app.domain.ai.ChunkSummarizer
import com.bookmate.app.domain.model.Book
import com.bookmate.app.domain.repository.ChapterRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * T1: summary of the whole book. The stored summary (extras/summary) comes first. If none is
 * stored, it is built live from the chapter summaries (stored ones first) and then merged.
 * It is not saved.
 */
class GetBookSummary @Inject constructor(
    private val chapterRepository: ChapterRepository,
    private val chunkSummarizer: ChunkSummarizer
) {
    operator fun invoke(book: Book): Flow<String> = flow {
        val stored = chapterRepository.getBookSummary(book.id).getOrThrow()
        if (stored != null) {
            emit(stored)
            return@flow
        }
        val summaries = chapterRepository.getChapterSummaries(book.id, book.chapterCount + 1).getOrThrow()
        val partials = mutableListOf<String>()
        for (number in 1..book.chapterCount) {
            val known = summaries[number]
            if (!known.isNullOrBlank()) {
                partials.add(known)
                continue
            }
            val chapter = chapterRepository.getChapter(book.id, number).getOrThrow() ?: continue
            partials.add(
                chunkSummarizer.summarize(book.title, book.author, chapter.paragraphs).getOrThrow()
            )
        }
        emit(
            chunkSummarizer.summarize(book.title, book.author, partials, WHOLE_BOOK_SENTENCES).getOrThrow()
        )
    }

    private companion object {
        const val WHOLE_BOOK_SENTENCES = 8
    }
}