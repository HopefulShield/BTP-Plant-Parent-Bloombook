package com.bloombook.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloombook.models.Users
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    val auth = Firebase.auth
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    fun getUserId(): String = Firebase.auth.currentUser?.uid.orEmpty()

    private val _authStatus: MutableStateFlow<Boolean> = MutableStateFlow(auth.currentUser != null)
    val authStatus: StateFlow<Boolean> = _authStatus

    private val _userName: MutableStateFlow<String> = MutableStateFlow("")
    val userName: StateFlow<String> = _userName


    private val authStateListener = auth.addAuthStateListener { FirebaseAuth ->
        _authStatus.value = (FirebaseAuth.currentUser != null)
    }


    fun getUserInfo(): Task<DocumentSnapshot>  {
        val userId = getUserId()
        val userDocRef = firestore.collection("users").document(userId)
        return userDocRef.get()
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener { authStateListener }
    }


    fun signOut() {
        auth.signOut()
        _authStatus.value = false
    }

    val getAuthStatus: Boolean
        get() = _authStatus.value

}