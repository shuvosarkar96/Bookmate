package com.bookmate.app.domain.usecase

import com.bookmate.app.domain.ai.AiError
import com.bookmate.app.domain.ai.AiException
import com.bookmate.app.domain.ai.AiRepository
import com.bookmate.app.domain.ai.PromptBuilder
import com.bookmate.app.domain.ai.TextSelector
import com.bookmate.app.domain.model.Book
import com.bookmate.app.domain.repository.ChapterRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

/**
 * T5: explain the part the reader just read. Input = the last paragraphs up to the reader's
 * position (TextSelector decides). Nothing after the position is ever sent.
 */
class ExplainPart @Inject constructor(
    private val chapterRepository: ChapterRepository,
    private val aiRepository: AiRepository
) {
    operator fun invoke(book: Book, chapter: Int, paragraphIndex: Int): Flow<String> = flow {
        val loaded = chapterRepository.getChapter(book.id, chapter).getOrThrow()
            ?: throw IllegalStateException("Chapter not found")
        val paragraphs = TextSelector.lastParagraphs(loaded.paragraphs, paragraphIndex)
        if (paragraphs.isEmpty()) throw AiException(AiError.BLOCKED)
        emitAll(aiRepository.generateStream(PromptBuilder.explain(book.title, paragraphs)))
    }
}