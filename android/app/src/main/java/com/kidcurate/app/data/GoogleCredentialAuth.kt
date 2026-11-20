package com.kidcurate.app.data

import android.app.Activity
import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

data class GoogleCredentialAccount(
    val email: String,
    val name: String,
    val idToken: String,
)

object GoogleCredentialAuth {
    suspend fun signIn(activity: Activity, serverClientId: String): GoogleCredentialAccount {
        require(serverClientId.isNotBlank()) { "Google OAuth client ID is not configured." }

        val option = GetSignInWithGoogleOption.Builder(serverClientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        val result = CredentialManager.create(activity).getCredential(
            context = activity,
            request = request,
        )
        val credential = result.credential
        if (
            credential !is CustomCredential ||
            credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            throw IllegalStateException("Google did not return a valid identity credential.")
        }

        val google = GoogleIdTokenCredential.createFrom(credential.data)
        val email = google.id.trim().lowercase()
        require(email.isNotBlank() && google.idToken.isNotBlank()) {
            "Google account identity was incomplete."
        }
        val name = google.displayName?.trim()?.ifBlank { null }
            ?: google.givenName?.trim()?.ifBlank { null }
            ?: email.substringBefore("@").replaceFirstChar { it.uppercase() }

        return GoogleCredentialAccount(
            email = email,
            name = name,
            idToken = google.idToken,
        )
    }

    suspend fun signOut(context: Context) {
        CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
    }

    fun userMessage(error: Throwable): String {
        android.util.Log.e("GoogleCredentialAuth", "Sign-in error: ${error.javaClass.name}: ${error.message}", error)
        return when (error) {
            is androidx.credentials.exceptions.GetCredentialCancellationException ->
                "Google sign-in was cancelled."
            is androidx.credentials.exceptions.NoCredentialException ->
                "No Google account available or account not authorized for this app signature."
            is GetCredentialException ->
                error.message ?: "Google sign-in failed. Please try again."
            else -> error.message ?: "Google sign-in failed. Please try again."
        }
    }
}
