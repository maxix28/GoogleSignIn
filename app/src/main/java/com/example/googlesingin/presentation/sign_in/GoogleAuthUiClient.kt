package com.example.googlesingin.presentation.sign_in

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInRequest.GoogleIdTokenRequestOptions
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class GoogleAuthUiClient (private val context : Context, private  val oneTapClient : SignInClient) {

    private val auth = Firebase.auth

    suspend fun signIn(): IntentSender? {
        val result = try {
            oneTapClient.beginSignIn(buildSignInRequest()).await()
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            null
        }

        return result?.pendingIntent?.intentSender
    }
    suspend fun signInWithIntent(intent: Intent): SignInResult {
        val credential = oneTapClient.getSignInCredentialFromIntent(intent)
        val googleIdToken = credential.googleIdToken
        val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken, null)
        return try {
            val user = auth.signInWithCredential(googleCredentials).await().user
            SignInResult(
                data = user?.run {
                    UserData(
                        userId = uid,
                      displayName,
                        profilePictureUrl = photoUrl?.toString()
                    )
                },
                errorMessage = null
            )
        } catch(e: Exception) {
            e.printStackTrace()
            if(e is CancellationException) throw e
            SignInResult(
                data = null,
                errorMessage = e.message
            )
        }
    }

 suspend fun getSignInResultFromIntent( intent: Intent): SignInResult{
val credential = oneTapClient.getSignInCredentialFromIntent(intent)
     val googleToken = credential.googleIdToken
     val googleCredentials = GoogleAuthProvider.getCredential(googleToken,null)
     return try{
         val user = auth.signInWithCredential(googleCredentials).await().user

SignInResult(
    data = user?.run {
        UserData(uid, displayName, photoUrl?.toString())
    },
    errorMessage = null

)
     }
     catch (e: Exception) {
         e.printStackTrace()
         if (e is CancellationException) throw e
     SignInResult(null,e.message,)
     }
 }


    suspend fun signOutWithIntent() {
        val result = try {
            oneTapClient.signOut().await()
            auth.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            null
        }


    }


    fun getSingedInUser(): UserData? = auth.currentUser?.run{
        UserData(
            uid,
            displayName,
            photoUrl?.toString()
        )
    }


    private  fun buildSignInRequest(): BeginSignInRequest{
        return BeginSignInRequest.Builder()
            .setGoogleIdTokenRequestOptions(
                GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId("599425892448-5vtrk74nu6haf7ipm8fu43jvnq873inf.apps.googleusercontent.com")
                    .build())
            .setAutoSelectEnabled(true)
            .build()

    }
}