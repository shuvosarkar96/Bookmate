package com.bookmate.app.domain.model

/** One chapter of a full-text book (document books/{bookId}/chapters/{chapterNo}). */
data class Chapter(
    val number: Int,
    val title: String,
    val paragraphs: List<String>
)

/** The server refused to give the text, for example a Premium book without an active subscription. */
class AccessDeniedException : Exception()