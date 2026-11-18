package com.bookmate.app.domain.repository

import com.bookmate.app.domain.model.AppUser

interface AuthRepository {
    suspend fun register(name: String, email: String, password: String): Result<AppUser>
    suspend fun login(email: String, password: String): Result<AppUser>

    /** Returns the signed-in user, or null when nobody is signed in. */
    suspend fun currentUser(): Result<AppUser?>

    /** Saves the picked genres and marks onboarding as done. An empty list means Skip. */
    suspend fun saveOnboarding(genres: List<String>): Result<Unit>

    fun logout()
}