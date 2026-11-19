package com.bookmate.app.domain.ai

/** Virtual pages for the AI sheet: about 250 words per page, counted from the start of the chapter. */
object VirtualPages {
    const val WORDS_PER_PAGE = 250

    /** The 1-based page of the paragraph at paragraphIndex. */
    fun pageOf(paragraphs: List<String>, paragraphIndex: Int): Int {
        if (paragraphs.isEmpty()) return 1
        val last = paragraphIndex.coerceIn(0, paragraphs.lastIndex)
        val wordsBefore = paragraphs.subList(0, last).sumOf { ChunkSummarizer.wordCount(it) }
        return wordsBefore / WORDS_PER_PAGE + 1
    }
}