package com.bookmate.app.domain.model

enum class AccessType { FREE, PREMIUM }

data class Book(
    val id: String,
    val title: String,
    val author: String,
    val isbn: String,
    val genre: String,
    val description: String,
    val coverUrl: String?,
    val accessType: AccessType,
    val hasFullText: Boolean,
    val chapterCount: Int,
    val searchKeywords: List<String>
)