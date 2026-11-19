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
 * T6: answer a question about the book. Same input as the recap (earlier chapter summaries +
 * the current chapter up to the position). The question is at most 300 characters.
 */
class AskAboutBook @Inject constructor(
    private val chapterRepository: ChapterRepository,
    private val aiRepository: AiRepository
) {
    operator fun invoke(book: Book, chapter: Int, paragraphIndex: Int, question: String): Flow<String> = flow {
        val trimmed = question.trim().take(PromptBuilder.MAX_QUESTION_LENGTH)
        if (trimmed.isEmpty()) throw AiException(AiError.BLOCKED)
        val current = chapterRepository.getChapter(book.id, chapter).getOrThrow()
            ?: throw IllegalStateException("Chapter not found")
        val earlier = chapterRepository.getChapterSummaries(book.id, chapter).getOrThrow()
        val selected = TextSelector.forPosition(earlier, chapter, current.paragraphs, paragraphIndex)
        emitAll(aiRepository.generateStream(PromptBuilder.question(book.title, trimmed, selected)))
    }
}