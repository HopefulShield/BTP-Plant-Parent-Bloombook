
package com.bloombook.screens.journal
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloombook.backend.JournalRepo
import com.bloombook.backend.RetrofitClient
import com.bloombook.models.Journals
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.internal.notify
class JournalsViewModel(
    private val repository: JournalRepo = JournalRepo()
): ViewModel() {
    private val _uiState = MutableStateFlow(JournalsUiState())
    val uiState: StateFlow<JournalsUiState> = _uiState.asStateFlow()
    private val plantSearchApi = RetrofitClient.plantSearchApi
    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()
    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()
    private val _plants = MutableStateFlow(listOf<String>())
    val plants: StateFlow<List<String>> = _plants.asStateFlow()
    private val hasUser: Boolean
        get() = repository.hasUser()
    private val user: FirebaseUser?
        get() = repository.user()
    fun storage() = repository.storageRef()
    fun isNamesNonEmpty(): Boolean {
        var isNonEmpty =
            (_uiState.value.commonName.isNotEmpty() && _uiState.value.nickName.isNotEmpty())
        return isNonEmpty
    }
    fun onCommonNameChange(newCommonName: String) {
        _uiState.update { currState ->
            currState.copy(commonName = newCommonName)
        }
    }
    fun onNickNameChange(newNickName: String) {
        _uiState.update { currState ->
            currState.copy(nickName = newNickName)
        }
    }
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
    fun resetDeleteStatus() {
        _uiState.update { currState ->
            currState.copy(deleteStatus = "")
        }
    }
    fun setSelectedJournal(journal:Journals){
        _uiState.update { currState ->
            currState.copy(selectedJournal = journal)
        }
    }
    fun updateEditModal() {
        val editCommonName = uiState.value.selectedJournal?.commonName
        val editNickName = uiState.value.selectedJournal?.nickName
        val displayImageUri = uiState.value.selectedJournal?.displayImageUri
        _uiState.update { currState ->
            currState.copy(
                commonName = editCommonName.orEmpty(),
                nickName = editNickName.orEmpty(),
                displayImageUri = displayImageUri.orEmpty()
            )
        }
    }
    fun addJournal() {
        if (hasUser) {
            repository.addJournal(
                commonName = _uiState.value.commonName,
                nickName = _uiState.value.nickName,
                displayImageUri = _uiState.value.displayImageUri,
                timestamp  = Timestamp.now()
            ) { isAdded ->
                resetModalInfo()
            }
        }
        getJournals()
    }
    fun updateJournal() {
        // call repo methods to update the document
        repository.updateJournal(
            _uiState.value.selectedJournal!!.documentId,
            _uiState.value.commonName,
            _uiState.value.nickName,
            _uiState.value.displayImageUri
        ) {result ->
            if (result) {
                // without this reset, then the selected journal info will populate
                // any modal including the addPlantModal
                resetModalInfo()
            }
            else {
                Log.d("Updated journal failed: ", result.toString())
            }
        }
        getJournals()
    }
    fun deleteJournal() = viewModelScope.launch {
        Log.d("selected journal", _uiState.value.selectedJournal!!.documentId)
        _uiState.value.selectedJournal?.let {
            repository.deleteJournal(it.documentId).addOnSuccessListener {
                _uiState.update { currState ->
                    currState.copy(deleteStatus = "Successfully Deleted")
                }
            }.addOnFailureListener { e ->
                _uiState.update { currState ->
                    currState.copy(deleteStatus = e.message.toString())
                }
            }
        }
    }
    fun getJournals() = viewModelScope.launch {
        repository.getUserJournals().collect{ resource ->
            // grab the actual list data from the Resource object
            val list = resource.data
            _uiState.update { currState ->
                currState.copy(journalsList = list.orEmpty())
            }
        }
    }
    // Resets the commonName, nickName, and image states for the modal
    fun resetModalInfo() {
        _uiState.update { currState ->
            currState.copy(
                commonName = "",
                nickName = "",
                displayImageUri = ""
            )
        }
    }
    fun onSearchTextChange(text:String) {
        Log.d("PlantInfoViewModel", "Search text changed: $text")
        _searchText.value = text
        if (text.isNotBlank()) {
            searchPlants(text)
        } else {
            _plants.value = emptyList()
            clearPlantSuggestions()
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
    fun clearPlantSuggestions() {
        viewModelScope.launch {
            _plants.emit(emptyList())
            _isSearching.emit(false)
        }
    }
}
data class JournalsUiState(
    val commonName: String = "",
    val nickName: String = "",
    val displayImageUri: String = "",
    val allNamesComplete: Boolean = false,
    val deleteStatus: String = "",
    val selectedJournal: Journals? = null,
    val journalsList: List<Journals> = emptyList()
)