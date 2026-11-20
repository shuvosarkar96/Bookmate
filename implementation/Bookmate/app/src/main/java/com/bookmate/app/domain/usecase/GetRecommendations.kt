package com.bookmate.app.domain.usecase

import com.bookmate.app.domain.ai.AiUsageRepository
import com.bookmate.app.domain.ai.RecommendationOutcome
import com.bookmate.app.domain.ai.Recommender
import com.bookmate.app.domain.repository.AuthRepository
import com.bookmate.app.domain.repository.BookRepository
import com.bookmate.app.domain.repository.ReadingProgressRepository
import javax.inject.Inject

/**
 * T8 recommendation for the signed-in Member. Needs the catalog; everything else is optional.
 * Uses one of the daily AI uses per run. [request] may be empty.
 */
class GetRecommendations @Inject constructor(
    private val bookRepository: BookRepository,
    private val authRepository: AuthRepository,
    private val readingProgressRepository: ReadingProgressRepository,
    private val aiUsageRepository: AiUsageRepository,
    private val recommender: Recommender
) {
    suspend operator fun invoke(userId: String?, request: String): Result<RecommendationOutcome> {
        val catalog = bookRepository.getBooks().getOrElse { return Result.failure(it) }
        val favorites = authRepository.currentUser().getOrNull()?.favoriteGenres.orEmpty()
        val latest = readingProgressRepository.getLatestInProgress().getOrNull()
        val recent = catalog.firstOrNull { it.id == latest?.bookId }
        val outcome = recommender.recommend(catalog, favorites, listOfNotNull(recent), request) {
            userId != null && aiUsageRepository.tryConsume(userId)
        }
        return Result.success(outcome)
    }
}