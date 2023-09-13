package com.bloombook.screens.launch.login

data class LoginState(
    val email: String = "",
    val password: String = "",
    val loginStatus: Boolean = false,
    val errorMessage: String = "",
    val allFieldsCorrect: Boolean = false,
    val isLoading: Boolean = false
)

