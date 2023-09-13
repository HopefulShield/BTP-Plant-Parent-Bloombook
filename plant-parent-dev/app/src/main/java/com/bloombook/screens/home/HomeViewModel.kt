package com.bloombook.screens.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloombook.backend.JournalRepo
import com.bloombook.backend.RemindersRepo
import com.bloombook.models.Reminders
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class HomeViewModel(
    private val journalRepository: JournalRepo = JournalRepo(),
    private val reminderRepository: RemindersRepo = RemindersRepo()
): ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(HomeScreenState())
    val uiState: StateFlow<HomeScreenState> = _uiState.asStateFlow()

    fun storage() = journalRepository.storageRef()

    fun getJournals() = viewModelScope.launch {

        journalRepository.getUserJournals().collect{ resource ->

            val list = resource.data

            _uiState.update { currState ->
                currState.copy(journalsList = list.orEmpty())
            }
        }
    }

    fun getDailyReminders() = viewModelScope.launch {
        reminderRepository.getRemindersAtDate(LocalDate.now()).collect { resource ->
            val list = resource.data
            _uiState.update { currState ->
                currState.copy(dailyRemindersList = list.orEmpty())
            }
        }
    }

    fun onCompleteReminder(reminderId: String) {
        reminderRepository.markReminderAsDone(reminderId) {
            // do something
        }
    }


    fun deleteReminder() {
        reminderRepository.deleteReminder(_uiState.value.selectedReminder!!.documentId) {

            _uiState.update { currState ->
                currState.copy(deleteStatus = it)
            }
        }
    }
    fun setSelectedReminder(reminder: Reminders) {
        _uiState.update { currState ->
            currState.copy(selectedReminder = reminder)
        }
    }
    fun resetDeleteStatus (){
        _uiState.update { currState ->
            currState.copy(deleteStatus = false)
        }
    }





}