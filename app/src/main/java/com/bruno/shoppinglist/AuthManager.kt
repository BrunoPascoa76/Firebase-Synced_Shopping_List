package com.bruno.shoppinglist

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class AuthManager(private val context: Context) {
    private val auth = FirebaseAuth.getInstance()
    private val credentialManager = CredentialManager.create(context)

    suspend fun googleSignIn(): FirebaseUser? {
        return try {
            // 1. Configure the Google request
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false) // Set to true to ONLY show accounts previously used
                .setServerClientId(context.getString(R.string.default_web_client_id))
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            // 2. Open the "Bottom Sheet" for the user to pick an account
            val result = credentialManager.getCredential(context, request)

            // 3. Extract the ID Token
            val googleIdToken = GoogleIdTokenCredential.createFrom(result.credential.data).idToken

            // 4. Give the token to Firebase
            val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
            auth.signInWithCredential(credential).await().user

        } catch (e: Exception) {
            // Handle cancellation or errors here
            null
        }
    }
}