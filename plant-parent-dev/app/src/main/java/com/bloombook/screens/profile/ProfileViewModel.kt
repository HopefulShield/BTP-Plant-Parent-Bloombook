package com.bloombook.screens.profile

import android.content.ContentValues.TAG
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloombook.models.Users
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ProfileViewModel(): ViewModel() {

    private val _uiState = MutableStateFlow(UserProfileState())
    val uiState: StateFlow<UserProfileState> = _uiState.asStateFlow()
    private val auth = Firebase.auth
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    
    val authEmail = getEmail()
    fun getUserId(): String = Firebase.auth.currentUser?.uid.orEmpty()

    fun storage() = FirebaseStorage.getInstance().reference
    private val storage = FirebaseStorage.getInstance().reference
    private val db = FirebaseFirestore.getInstance()


    fun getUsername(): String {
        val user = auth.currentUser
        val defaultUsername = "John Smith"
        var toReturn = ""

        user?.let { currentUser ->
            val uid = currentUser.uid
            firestore.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val username = document.getString("name") ?: defaultUsername
                        Log.d("user management", "getUsername(): $username")
                        Log.d("user management", "toReturn before assign: $toReturn")
                        toReturn = username
                        Log.d("user management", "toReturn after assign: $toReturn")
                    } else {
                        Log.d("user management", "User document does not exist")
                        toReturn = defaultUsername
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("user management", "Error getting user document: ${e.message}")
                    _uiState.update { currState ->
                        currState.copy(username = defaultUsername)
                    }
                }
        }
        Log.d("user management", "toReturn END: $toReturn")
        return toReturn
    }


    fun getEmail(): String {
        val user = auth.currentUser
        val defaultEmail = "default@email.com"
        user?.let {
            for (profile in it.providerData) {
                val email = profile.email
                Log.d("user management", "getEmail(): $email")
                return email ?: defaultEmail
            }
        }
        return defaultEmail
    }
    fun onUsernameChange(newUsername: String) {
        _uiState.update { currState ->
            currState.copy(username = newUsername)
        }
    }

    fun onEmailChange(newEmail: String) {
        _uiState.update { currState ->
            currState.copy(email = newEmail)
        }
    }

    fun removeCurrentPhoto() {
        _uiState.update { currState ->
            currState.copy(profilePicture = "")
        }

    }

    private fun setFields(user: Users) {
        _uiState.update { currState ->
            currState.copy(
                username = user.name,
                email = user.email,
                profilePicture = user.profilePicture,
                savedUsername = user.name
            )
        }
    }

    private fun repoGetUserInfo(
        onError:(Throwable?) -> Unit,
        onSuccess:(Users?) -> Unit
    ) {
        val userId = getUserId()
        val userDocRef = firestore.collection("users").document(userId)
        userDocRef
            .get()
            .addOnSuccessListener {
                onSuccess.invoke(it?.toObject(Users::class.java))
            }
            .addOnFailureListener{result ->
                onError.invoke(result.cause)
            }
    }

    fun getUserInfo() {
        repoGetUserInfo(
            onError = {}
        ) {user ->
            user?.let {setFields(it)}
        }
    }

    fun updateUserProfile(
        username: String,
        email: String,
        pfpUri: String
    ) {
        val user = Firebase.auth.currentUser
        val updateData = hashMapOf<String,Any>(
            "name" to username,
            "email" to email,
            "profilePicture" to pfpUri
        )

        if (pfpUri.isNotEmpty()) {

            val imagesRef = storage.child("user_photos/${pfpUri}")
            imagesRef.putFile(pfpUri.toUri())

            saveImageForUser(pfpUri) {
                Log.d("saving user's image", "Success")
            }
        }

        // update displayName for auth user
        user!!.updateProfile(userProfileChangeRequest {
            displayName = _uiState.value.savedUsername
        })


        // upate email only if it differs from original
        if (authEmail != email) {
            user!!.updateEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("user management", "User email address updated in auth")
                    } else {
                        Log.e("user management", "Failed to update email in auth")
                    }
                }
        } else {
            Log.d("user management", "same email. no updates made")
        }

        // firestore update
        firestore.collection("users").document(user!!.uid)
            .update(updateData)
            .addOnSuccessListener {
                _uiState.value = uiState.value.copy(
                    updatedSuccessfully = true
                )
            }
            .addOnFailureListener {
                _uiState.value = uiState.value.copy(
                    errorMessage = it.message ?: "Profile update failed"
                )
            }
    }

    private fun repoDeleteUser(): Task<HttpsCallableResult> {

        return FirebaseFunctions.getInstance().getHttpsCallable("deleteUser").call()
    }

    fun deleteUser() = viewModelScope.launch {
        repoDeleteUser().addOnSuccessListener {
            auth.currentUser!!.delete()
                .addOnSuccessListener {
                    _uiState.update { currState ->
                        currState.copy(deleteStatus = true)
                    }
                }
        }.addOnFailureListener { e ->
            _uiState.update { currState ->
                currState.copy(deleteStatus = false)
            }
        }

    }


    fun resetUpdateStatus() {
        _uiState.update { currState ->
            currState.copy(updatedSuccessfully = false)
        }
    }

    fun addImage(image: String) {
        _uiState.update { currState ->
            currState.copy(profilePicture = image )
        }
    }

    private fun saveImageForUser(uri: String, onComplete: (Boolean) -> Unit) {
        val userRef = firestore.collection("users").document("${getUserId()}")
        userRef.update("imageList", FieldValue.arrayUnion(uri))
            .addOnCompleteListener {result ->
                onComplete.invoke(result.isSuccessful)
            }
    }





}

data class UserProfileState(
    val username: String = "",
    val email: String = "",
    val profilePicture: String = "",

    val updatedSuccessfully: Boolean = false,
    val errorMessage: String = "",

    val deleteStatus: Boolean = false,
    val savedUsername: String = "",
    )