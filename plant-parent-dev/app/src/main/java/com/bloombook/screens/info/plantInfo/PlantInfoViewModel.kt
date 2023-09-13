package com.bloombook.screens.info.plantInfo

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloombook.backend.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlantInfoViewModel: ViewModel() {

    private val plantSearchApi = RetrofitClient.plantSearchApi

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()


    private val _plants = MutableStateFlow(listOf<String>())


    val plants: StateFlow<List<String>> = _plants.asStateFlow()


    fun onSearchTextChange(text:String) {
        Log.d("PlantInfoViewModel", "Search text changed: $text")
        _searchText.value = text
        if (text.isNotBlank()) {
            searchPlants(text)
        } else {
            _plants.value = emptyList()
        }
    }

    private fun searchPlants(query:String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isSearching.value = true
            try {
                val response = plantSearchApi.searchPlants(query)
                val plantNames = response.map {it.plant}
                _plants.value = plantNames
            } catch (e: Exception) {
                _plants.value = emptyList()
            }
            _isSearching.value = false
        }
    }



}









