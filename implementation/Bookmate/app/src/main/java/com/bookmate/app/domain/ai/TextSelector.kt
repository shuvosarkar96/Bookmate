package com.bookmate.app.domain.ai

/** The summary of one earlier chapter. */
data class ChapterSummary(val chapter: Int, val text: String)

/** The book text chosen for one AI request. */
data class SelectedText(
    val earlierSummaries: List<ChapterSummary>,
    val readSoFar: List<String>
)

/**
 * The ONLY place that decides how much book text is sent to the AI.
 * The reader position is the index of the paragraph where the reader stopped.
 * Everything up to and including that paragraph may be sent. Nothing after it, ever.
 */
object TextSelector {
    const val EXPLAIN_PARAGRAPH_COUNT = 5

    /** Paragraphs of the current chapter from the start up to and including the position. */
    fun readSoFar(paragraphs: List<String>, paragraphIndex: Int): List<String> {
        if (paragraphs.isEmpty()) return emptyList()
        val last = paragraphIndex.coerceIn(0, paragraphs.lastIndex)
        return paragraphs.subList(0, last + 1).toList()
    }

    /** The last paragraphs up to and including the position (for "Explain this part"). */
    fun lastParagraphs(
        paragraphs: List<String>,
        paragraphIndex: Int,
        count: Int = EXPLAIN_PARAGRAPH_COUNT
    ): List<String> = readSoFar(paragraphs, paragraphIndex).takeLast(count)

    /** Summaries of the chapters BEFORE the current one, in chapter order. Blank ones are skipped. */
    fun earlierSummaries(summaries: Map<Int, String>, currentChapter: Int): List<ChapterSummary> =
        summaries.entries
            .filter { it.key < currentChapter && it.value.isNotBlank() }
            .sortedBy { it.key }
            .map { ChapterSummary(it.key, it.value) }

    /** Input for the recap and the question: earlier summaries plus the current chapter so far. */
    fun forPosition(
        summaries: Map<Int, String>,
        currentChapter: Int,
        paragraphs: List<String>,
        paragraphIndex: Int
    ): SelectedText = SelectedText(
        earlierSummaries = earlierSummaries(summaries, currentChapter),
        readSoFar = readSoFar(paragraphs, paragraphIndex)
    )
}