package com.bloombook.screens.journal.entries

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloombook.backend.EntriesRepo
import com.bloombook.backend.JournalRepo
import com.bloombook.backend.Resources
import com.bloombook.models.Entries
import com.bloombook.models.Journals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EntriesViewModel(
    private val repository: EntriesRepo = EntriesRepo(),
    private val journalRepository: JournalRepo = JournalRepo()
): ViewModel() {

    private val _entriesUiState = MutableStateFlow(EntriesUiState())
    val entriesUiState: StateFlow<EntriesUiState> = _entriesUiState.asStateFlow()

    val user = repository.user()

    fun storage() = repository.storageRef()




    fun getJournalEntries(journalId: String) = viewModelScope.launch {
        repository.getJournalEntries(journalId).collect { resource ->

            val entries = resource.data
            // Get a reference to the cloud storage where we store image files
            _entriesUiState.update { currState ->
                currState.copy(entriesList = entries.orEmpty())
            }
        }
    }

    fun deleteEntry(journalId: String) {
        repository.deleteEntry(journalId, _entriesUiState.value.selectedEntry!!.documentId) {
            _entriesUiState.update { currState ->
                currState.copy(entryDeletedStatus = it)
            }
        }
    }

    fun resetDeleteStatus() {
        _entriesUiState.update { currState ->
            currState.copy(entryDeletedStatus = false)
        }

    }

    fun setSelectedEntry(entry:Entries) {
        _entriesUiState.update { currState ->
            currState.copy(selectedEntry = entry)
        }
    }


}

data class EntriesUiState(
    val entriesList: List<Entries> = emptyList(),
    val entryDeletedStatus:Boolean = false,

    // For the top bar icon image
    val displayImageUri: String = "",
    val displayCommonName: String = "",
    val displayNickName: String = "",
    val selectedEntry: Entries? = null
)