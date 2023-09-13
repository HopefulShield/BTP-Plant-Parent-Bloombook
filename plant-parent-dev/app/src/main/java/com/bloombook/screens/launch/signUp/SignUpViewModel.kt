package com.bloombook.screens.launch.signUp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Job
import java.lang.Exception

class SignUpViewModel: ViewModel() {


    private val _uiState = MutableStateFlow(SignUpState())
    val uiState: StateFlow<SignUpState> = _uiState.asStateFlow()
    private val auth = Firebase.auth
    private val firestore = FirebaseFirestore.getInstance()




    fun onNameChange(newName:String) {
        _uiState.update{ currState ->
            currState.copy( name = newName)
        }

    }

    fun onEmailChange(newEmail:String) {
        _uiState.update{ currState ->
            currState.copy( email = newEmail)
        }
        checkEmailFormat(newEmail)
    }

    fun checkEmailFormat(email:String) {
        val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        _uiState.update{ currState ->
            currState.copy( isEmailFormatted = email.matches(Regex(emailRegex)))
        }
        checkAllFields()
    }

    fun onPasswordChange(newPassword:String) {
        _uiState.update{ currState ->
            currState.copy( password = newPassword)
        }
        checkPasswordMatch()
    }

    fun onConfirmPasswordChange(newConfirmPassword:String) {
        _uiState.update{ currState ->
            currState.copy( confirmPassword = newConfirmPassword)
        }
        checkPasswordMatch()
    }

    private fun checkPasswordMatch() {
        var nonEmpty = (_uiState.value.password.isNotEmpty() && _uiState.value.confirmPassword.isNotEmpty())
        var isEqual = _uiState.value.password == _uiState.value.confirmPassword

        _uiState.update{ currState ->
            currState.copy( passwordsMatch = (nonEmpty && isEqual))
        }
        checkAllFields()

    }

    private fun checkAllFields() {
        if (    _uiState.value.passwordsMatch
            &&  _uiState.value.isEmailFormatted
            &&  _uiState.value.name.isNotEmpty()
        ) {
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




    suspend fun onSignUpClick(email:String, password:String) {
        /*
        * preliminary checks:
        * no fields are null
        * email is valid: name@email.domain
        * password meets strength requirements: character length, contains special character, etc.
        * password and confirmPassword fields match*/
        setLoading(true)
        viewModelScope.launch {
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await().user

                if (result != null) {
                    Log.d("result", result.uid)
                }

                // If user uid exists, then add user to the firestore users collection
                result?.uid?.let { uid ->
                    val userData = hashMapOf(
                        "uid" to uid,
                        "name" to _uiState.value.name,
                        "email" to email,
                        "profilePicture" to "",
                        "imageList" to emptyList<String>()
                    )

                    result.updateProfile(userProfileChangeRequest {
                        displayName = _uiState.value.name
                    })

                    firestore.collection("users").document(uid).set(userData)
                        .addOnSuccessListener {
                            Log.d("Firestore creating user status","Added user")
                            _uiState.update { currState ->
                                currState.copy(userID = result.uid)
                            }
                        }
                        .addOnFailureListener {
                            Log.d("Firestore creating user status", it.message!!.toString())
                            _uiState.update { currState ->
                                currState.copy(userID = "", errorMessage = it.message!!)
                            }
                        }
                }

            }
            catch (e : Exception) {
                Log.d("result", e.message.toString())
                _uiState.update { currState ->
                    currState.copy(errorMessage = e.message.toString() )
                }
            }
            setLoading(false)
        }
    }

}
