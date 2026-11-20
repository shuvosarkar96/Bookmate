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
 * T4: recap of the story so far. Input = stored summaries of EARLIER chapters + the current
 * chapter up to the reader's position. TextSelector decides; nothing after the position is sent.
 */
class GetRecap @Inject constructor(
    private val chapterRepository: ChapterRepository,
    private val aiRepository: AiRepository
) {
    operator fun invoke(book: Book, chapter: Int, paragraphIndex: Int): Flow<String> = flow {
        val current = chapterRepository.getChapter(book.id, chapter).getOrThrow()
            ?: throw IllegalStateException("Chapter not found")
        val earlier = chapterRepository.getChapterSummaries(book.id, chapter).getOrThrow()
        val selected = TextSelector.forPosition(earlier, chapter, current.paragraphs, paragraphIndex)
        if (selected.earlierSummaries.isEmpty() && selected.readSoFar.isEmpty()) {
            throw AiException(AiError.BLOCKED)
        }
        emitAll(aiRepository.generateStream(PromptBuilder.recap(book.title, chapter, selected)))
    }
}