package com.bloombook.screens.info.plantCard


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PlantCardViewModel(
    private val savedStateHandle: SavedStateHandle,
): ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(PlantCardState())
    val uiState: StateFlow<PlantCardState> = _uiState.asStateFlow()


    private val searchedPlant: String = checkNotNull(savedStateHandle["searched_plant"])

    fun getName(): String? {
        return searchedPlant
    }


    fun fetchInfo() {
        // this code is to test initializing the screen with firebase data
        db.collection("plants").document(searchedPlant).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document: DocumentSnapshot? = task.result
                if (document != null && document.exists()) {
                    val plantCardState = document.toObject(PlantCardState::class.java)
                    _uiState.value = plantCardState ?: PlantCardState()
                }
            }
        }
    }


}




