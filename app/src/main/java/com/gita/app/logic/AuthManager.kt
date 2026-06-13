package com.gita.app.logic

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

object AuthManager {
    private const val TAG = "AuthManager"
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

    val isLoggedIn: Boolean get() = getCurrentUser() != null

    suspend fun signInWithGoogle(context: Context, webClientId: String): Result<FirebaseUser> {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credentialManager = CredentialManager.create(context)
            val result = credentialManager.getCredential(context = context, request = request)

            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)

            val authResult = firebaseAuth.signInWithCredential(firebaseCredential).await()
            val user = authResult.user
                ?: return Result.failure(Exception("Sign-in succeeded but user is null"))

            Log.i(TAG, "Signed in: ${user.displayName}")
            Result.success(user)
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Credential error", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Sign-in failed", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
        Log.i(TAG, "Signed out")
    }
}
