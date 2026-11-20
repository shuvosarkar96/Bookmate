package com.bookmate.app.domain.model

/** A book the Admin adds in the app. Values are already trimmed and checked. */
data class NewBook(
    val title: String,
    val author: String,
    val isbn: String,
    val genre: String,
    val description: String,
    val coverUrl: String?,
    val accessType: AccessType
)