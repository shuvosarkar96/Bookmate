package com.bookmate.app.domain.model

enum class Role { MEMBER, ADMIN }

data class AppUser(
    val uid: String,
    val name: String,
    val email: String,
    val role: Role,
    val favoriteGenres: List<String>,
    val onboardingDone: Boolean
)