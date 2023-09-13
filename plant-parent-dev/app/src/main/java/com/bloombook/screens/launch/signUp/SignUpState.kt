package com.bloombook.screens.launch.signUp

data class SignUpState(

    val name: String = "",
    val email: String = "",
    val isEmailFormatted: Boolean = true,
    val password: String = "",
    val confirmPassword: String = "",

    val userID: String = "",
    val errorMessage: String = "",

    val passwordsEmpty: String = "",
    val passwordsMatch: Boolean = true,

    val isLoading: Boolean = false,

    val allFieldsCorrect: Boolean = false
)

