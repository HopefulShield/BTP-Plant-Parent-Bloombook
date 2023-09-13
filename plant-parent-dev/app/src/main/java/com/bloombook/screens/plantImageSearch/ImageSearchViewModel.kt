package com.bloombook.screens.plantImageSearch

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

class ImageSearchViewModel(

): ViewModel() {

    private val retrofit = RetrofitClient.imageSearchApi


    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    private val _identifiedPlants = MutableLiveData<List<PlantMatch>>(emptyList())
    val identifiedPlants: LiveData<List<PlantMatch>> = _identifiedPlants

    private val _uiState = MutableStateFlow(ImageSearchUiState())
    val uiState: StateFlow<ImageSearchUiState> = _uiState.asStateFlow()

    fun addImage(image: String) {
        _uiState.update { currState ->
            currState.copy(displayImageUri = image )
        }
    }


    fun deleteImage() {
        _uiState.update { currState ->
            currState.copy(displayImageUri = "" )
        }
    }

    fun identifyPlant(images: List<MultipartBody.Part>) {
        viewModelScope.launch {
            try {
                _isSearching.value = true
                val response = retrofit.identifyPlant("all", "2b10UiS8TgA9qAkAEmDteJELO", images)
                if (response.isSuccessful) {
                    _identifiedPlants.postValue(response.body()?.results?.take(5) ?: emptyList())
                } else {
                    // handle error
                    Log.e("ImageSearchViewModel", "Identify plant request failed: ${response.code()}")
                }
            } catch (e: Exception) {
                //handle exception
                Log.e("ImageSearchViewModel", "Identify plant request exception: ${e.message}")
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun clearIdentifiedPlants() {
        _identifiedPlants.value = emptyList()
    }

}

data class ImageSearchUiState(

    val displayImageUri: String = "",

)