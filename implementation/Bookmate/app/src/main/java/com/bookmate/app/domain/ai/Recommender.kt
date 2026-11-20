package com.bookmate.app.domain.ai

import com.bookmate.app.domain.model.Book
import javax.inject.Inject

/**
 * The result of a recommendation run. [fromAi] is false for the rule-based fallback;
 * [aiError] then says why the AI was not used.
 */
data class RecommendationOutcome(
    val items: List<Recommendation>,
    val fromAi: Boolean,
    val aiError: AiError? = null
)

/**
 * T8: asks the AI for recommendations and checks the answer. A bad answer is asked for once more.
 * If the AI cannot be used or still fails, the rule-based recommendation is returned.
 * [canUseAi] is asked once per run (it counts one use of the daily limit).
 */
class Recommender @Inject constructor(
    private val aiRepository: AiRepository
) {
    suspend fun recommend(
        catalog: List<Book>,
        favoriteGenres: List<String>,
        recent: List<Book>,
        request: String,
        canUseAi: suspend () -> Boolean
    ): RecommendationOutcome {
        if (catalog.isEmpty()) return RecommendationOutcome(emptyList(), fromAi = false)

        fun fallback() = RuleBasedRecommender.recommend(
            catalog, favoriteGenres, recent.map { it.id }.toSet(), request, MAX_ITEMS
        )

        if (!canUseAi()) return RecommendationOutcome(fallback(), fromAi = false, aiError = AiError.LIMIT)

        val prompt = PromptBuilder.recommendation(
            max = MAX_ITEMS,
            favoriteGenres = favoriteGenres,
            recentTitles = recent.map { it.title },
            request = request,
            books = catalog
        )
        for (attempt in 1..MAX_ATTEMPTS) {
            val result = aiRepository.generate(prompt)
            val failure = result.exceptionOrNull()
            if (failure != null) {
                // The repository already retried the call itself.
                return RecommendationOutcome(fallback(), fromAi = false, aiError = AiErrorMapper.map(failure))
            }
            val parsed = RecommendationParser.parse(AiOutput.clean(result.getOrThrow()), catalog, MAX_ITEMS)
            if (parsed != null) return RecommendationOutcome(parsed, fromAi = true)
        }
        return RecommendationOutcome(fallback(), fromAi = false, aiError = AiError.BLOCKED)
    }

    companion object {
        const val MAX_ITEMS = 5

        /** The first answer plus one retry. */
        const val MAX_ATTEMPTS = 2
    }
}