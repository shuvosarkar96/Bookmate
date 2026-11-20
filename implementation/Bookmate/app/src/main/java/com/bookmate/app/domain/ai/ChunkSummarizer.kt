package com.bookmate.app.domain.ai

import javax.inject.Inject

/**
 * Summarizes text that may be too long for one request.
 * Up to MAX_WORDS words: one request. Longer: split at paragraph boundaries, summarize each
 * chunk, then merge the partial summaries (again in chunks if they are still too long).
 */
class ChunkSummarizer @Inject constructor(
    private val aiRepository: AiRepository
) {

    suspend fun summarize(
        title: String,
        author: String,
        paragraphs: List<String>,
        sentences: Int = DEFAULT_SENTENCES
    ): Result<String> {
        val clean = paragraphs.filter { it.isNotBlank() }
        if (clean.isEmpty()) {
            return Result.failure(IllegalArgumentException("No text to summarize"))
        }
        return summarizeLevel(title, author, clean, sentences, 0)
    }

    private suspend fun summarizeLevel(
        title: String,
        author: String,
        paragraphs: List<String>,
        sentences: Int,
        depth: Int
    ): Result<String> {
        val chunks = splitIntoChunks(paragraphs, MAX_WORDS)
        if (chunks.size == 1 || depth >= MAX_DEPTH) {
            return aiRepository.generate(
                PromptBuilder.summarizePart(title, author, paragraphs.joinToString("\n\n"), sentences)
            )
        }
        val partials = mutableListOf<String>()
        for (chunk in chunks) {
            val result = aiRepository.generate(
                PromptBuilder.summarizePart(title, author, chunk.joinToString("\n\n"), sentences)
            )
            partials.add(result.getOrElse { return Result.failure(it) })
        }
        return summarizeLevel(title, author, partials, sentences, depth + 1)
    }

    companion object {
        const val MAX_WORDS = 3000
        const val DEFAULT_SENTENCES = 5
        private const val MAX_DEPTH = 3
        private val WHITESPACE = Regex("\\s+")

        fun wordCount(text: String): Int = text.split(WHITESPACE).count { it.isNotEmpty() }

        /**
         * Groups paragraphs into chunks of at most maxWords words. A paragraph is never split.
         * A paragraph longer than maxWords gets a chunk of its own.
         */
        fun splitIntoChunks(paragraphs: List<String>, maxWords: Int = MAX_WORDS): List<List<String>> {
            val chunks = mutableListOf<List<String>>()
            var current = mutableListOf<String>()
            var words = 0
            for (paragraph in paragraphs) {
                val count = wordCount(paragraph)
                if (current.isNotEmpty() && words + count > maxWords) {
                    chunks.add(current)
                    current = mutableListOf()
                    words = 0
                }
                current.add(paragraph)
                words += count
            }
            if (current.isNotEmpty()) chunks.add(current)
            return chunks
        }
    }
}