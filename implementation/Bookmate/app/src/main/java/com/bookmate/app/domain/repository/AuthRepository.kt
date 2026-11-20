package com.bookmate.app.domain.repository

import com.bookmate.app.domain.model.AppUser

interface AuthRepository {
    suspend fun register(name: String, email: String, password: String): Result<AppUser>
    suspend fun login(email: String, password: String): Result<AppUser>

    /** Returns the signed-in user, or null when nobody is signed in. */
    suspend fun currentUser(): Result<AppUser?>

    /** Saves the picked genres and marks onboarding as done. An empty list means Skip. */
    suspend fun saveOnboarding(genres: List<String>): Result<Unit>

    /** Saves the new name and favorite genres of the signed-in user. */
    suspend fun updateProfile(name: String, favoriteGenres: List<String>): Result<Unit>

    /** Re-checks the current password, then sets the new one. Fails with WRONG_PASSWORD. */
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit>

    /** Sends a password reset email. Succeeds even when the email is not registered. */
    suspend fun sendPasswordReset(email: String): Result<Unit>

    fun logout()
}