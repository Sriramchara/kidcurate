package com.kidcurate.app.data

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
    suspend fun signIn(context: Context, serverClientId: String): GoogleCredentialAccount {
        require(serverClientId.isNotBlank()) { "Google OAuth client ID is not configured." }

        val option = GetSignInWithGoogleOption.Builder(serverClientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        val result = CredentialManager.create(context).getCredential(
            context = context,
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

    fun userMessage(error: Throwable): String = when (error) {
        is GetCredentialException -> "Google sign-in was cancelled or no account is available."
        else -> error.message ?: "Google sign-in failed. Please try again."
    }
}
