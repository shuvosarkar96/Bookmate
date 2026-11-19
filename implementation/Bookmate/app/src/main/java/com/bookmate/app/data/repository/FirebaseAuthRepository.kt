package com.bookmate.app.data.repository

import com.bookmate.app.domain.model.AppUser
import com.bookmate.app.domain.model.AuthException
import com.bookmate.app.domain.model.AuthFailure
import com.bookmate.app.domain.model.Role
import com.bookmate.app.domain.repository.AuthRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.tasks.await

@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override suspend fun register(name: String, email: String, password: String): Result<AppUser> =
        safeCall {
            val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val firebaseUser = result.user ?: throw AuthException(AuthFailure.UNKNOWN)
            val appUser = AppUser(
                uid = firebaseUser.uid,
                name = name.trim(),
                email = email.trim(),
                role = Role.MEMBER,
                favoriteGenres = emptyList(),
                onboardingDone = false
            )
            usersRef(appUser.uid).set(newUserData(appUser)).await()
            appUser
        }

    override suspend fun login(email: String, password: String): Result<AppUser> =
        safeCall {
            auth.signInWithEmailAndPassword(email.trim(), password).await()
            loadCurrentUser() ?: throw AuthException(AuthFailure.UNKNOWN)
        }

    override suspend fun currentUser(): Result<AppUser?> =
        safeCall { loadCurrentUser() }

    override suspend fun saveOnboarding(genres: List<String>): Result<Unit> =
        safeCall {
            val uid = auth.currentUser?.uid ?: throw AuthException(AuthFailure.UNKNOWN)
            usersRef(uid).update(
                mapOf(
                    "favoriteGenres" to genres,
                    "onboardingDone" to true
                )
            ).await()
            Unit
        }

    override fun logout() {
        auth.signOut()
    }

    private fun usersRef(uid: String): DocumentReference =
        firestore.collection("users").document(uid)

    private fun newUserData(user: AppUser): Map<String, Any> = mapOf(
        "name" to user.name,
        "email" to user.email,
        "role" to "member",
        "favoriteGenres" to user.favoriteGenres,
        "onboardingDone" to user.onboardingDone,
        "createdAt" to FieldValue.serverTimestamp()
    )

    private suspend fun loadCurrentUser(): AppUser? {
        val firebaseUser = auth.currentUser ?: return null
        val snapshot = usersRef(firebaseUser.uid).get().await()
        if (snapshot.exists()) return snapshot.toAppUser(firebaseUser.uid)

        // The account exists but its profile document is missing: create it as a Member.
        val appUser = AppUser(
            uid = firebaseUser.uid,
            name = firebaseUser.displayName.orEmpty(),
            email = firebaseUser.email.orEmpty(),
            role = Role.MEMBER,
            favoriteGenres = emptyList(),
            onboardingDone = false
        )
        usersRef(appUser.uid).set(newUserData(appUser)).await()
        return appUser
    }

    private fun DocumentSnapshot.toAppUser(uid: String): AppUser = AppUser(
        uid = uid,
        name = getString("name").orEmpty(),
        email = getString("email").orEmpty(),
        role = if (getString("role") == "admin") Role.ADMIN else Role.MEMBER,
        favoriteGenres = (get("favoriteGenres") as? List<*>)?.filterIsInstance<String>().orEmpty(),
        onboardingDone = getBoolean("onboardingDone") ?: false
    )

    private suspend fun <T> safeCall(block: suspend () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: AuthException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(AuthException(mapFailure(e), e))
        }

    private fun mapFailure(e: Exception): AuthFailure = when {
        e is FirebaseAuthWeakPasswordException -> AuthFailure.WEAK_PASSWORD
        e is FirebaseAuthUserCollisionException -> AuthFailure.EMAIL_IN_USE
        e is FirebaseAuthInvalidCredentialsException -> AuthFailure.INVALID_CREDENTIALS
        e is FirebaseAuthInvalidUserException -> AuthFailure.INVALID_CREDENTIALS
        e is FirebaseNetworkException -> AuthFailure.NETWORK
        e is FirebaseFirestoreException &&
            e.code == FirebaseFirestoreException.Code.UNAVAILABLE -> AuthFailure.NETWORK
        else -> AuthFailure.UNKNOWN
    }
}