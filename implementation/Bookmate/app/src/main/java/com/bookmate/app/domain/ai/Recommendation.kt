package com.bookmate.app.domain.ai

import com.bookmate.app.domain.model.Book

/** One recommended book. [reason] is empty for rule-based recommendations. */
data class Recommendation(val book: Book, val reason: String)