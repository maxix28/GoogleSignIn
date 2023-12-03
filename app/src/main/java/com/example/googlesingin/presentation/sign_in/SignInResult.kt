package com.example.googlesingin.presentation.sign_in

class SignInResult(
    val data: com.example.googlesingin.presentation.sign_in.UserData?,
    val errorMessage: String?,

    ) {
}
data class UserData(
    val userId : String?,
    val userName: String?,
    val profilePictureUrl: String?
)