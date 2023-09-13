package com.bloombook.screens.launch.login

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Exception


class LoginViewModel(): ViewModel() {


    private val _uiState = MutableStateFlow(LoginState())
    val uiState: StateFlow<LoginState> = _uiState.asStateFlow()
    val auth = Firebase.auth


    fun onEmailChange(newEmail:String) {
        _uiState.update{ currState: LoginState ->
            currState.copy( email = newEmail)
        }
        checkAllFields()
    }

    fun onPasswordChange(newPassword:String) {
        _uiState.update{ currState: LoginState ->
            currState.copy( password = newPassword)
        }
        checkAllFields()
    }

    private fun checkAllFields() {
        if (_uiState.value.password.isNotEmpty() && _uiState.value.email.isNotEmpty()) {
            _uiState.update{ currState ->
                currState.copy(allFieldsCorrect = true)
            }
        }
        else {
            _uiState.update{ currState ->
                currState.copy(allFieldsCorrect = false)
            }
        }
    }

    private fun setLoading(condition: Boolean) {
        _uiState.update{ currState ->
            currState.copy( isLoading  = condition)
        }
    }


    fun resetErrorMessage() {
        _uiState.update{ currState ->
            currState.copy( errorMessage  = "")
        }
    }


    fun onLoginClick(email:String, password:String) = viewModelScope.launch {
        setLoading(true)

        try {
            auth.signInWithEmailAndPassword(email, password).await()
            _uiState.update { currState ->
                currState.copy(loginStatus = true)
            }
        }
        catch (e : Exception) {
            _uiState.update { currState ->
                currState.copy(errorMessage = e.message.toString())
            }
        }

        setLoading(false)
    }
}