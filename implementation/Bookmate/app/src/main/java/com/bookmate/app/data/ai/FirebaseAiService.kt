package com.bookmate.app.data.ai

import com.bookmate.app.domain.ai.AiService
import com.bookmate.app.domain.ai.EmptyAnswerException
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

@Singleton
class FirebaseAiService @Inject constructor() : AiService {

    // Gemini Developer API through Firebase AI Logic. No API key in the app.
    private val model by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel(AiConfig.MODEL_NAME)
    }

    override suspend fun generate(prompt: String): Result<String> =
        try {
            val text = model.generateContent(prompt).text
            if (text.isNullOrBlank()) {
                Result.failure(EmptyAnswerException())
            } else {
                Result.success(text)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }

    override fun generateStream(prompt: String): Flow<String> =
        model.generateContentStream(prompt).mapNotNull { chunk ->
            chunk.text?.takeIf { it.isNotEmpty() }
        }
}