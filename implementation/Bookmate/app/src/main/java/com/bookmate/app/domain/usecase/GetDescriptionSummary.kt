package com.bookmate.app.domain.usecase

import com.bookmate.app.domain.ai.AiError
import com.bookmate.app.domain.ai.AiException
import com.bookmate.app.domain.ai.AiRepository
import com.bookmate.app.domain.ai.PromptBuilder
import com.bookmate.app.domain.model.Book
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * T7: summary from the book description only. Used for books with no full text and for
 * Premium books that are locked for this Member. No book text is sent, only title, author
 * and description.
 */
class GetDescriptionSummary @Inject constructor(
    private val aiRepository: AiRepository
) {
    operator fun invoke(book: Book): Flow<String> {
        if (book.description.isBlank()) {
            return flow { throw AiException(AiError.BLOCKED) }
        }
        return aiRepository.generateStream(
            PromptBuilder.descriptionSummary(book.title, book.author, book.description)
        )
    }
}