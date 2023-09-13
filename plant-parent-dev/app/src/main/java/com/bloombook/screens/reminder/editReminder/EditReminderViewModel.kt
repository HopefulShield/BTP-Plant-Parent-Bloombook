package com.bloombook.screens.reminder.editReminder

import com.bloombook.backend.EntriesRepo


import android.content.Context
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloombook.backend.JournalRepo
import com.bloombook.backend.RemindersRepo
import com.bloombook.backend.Resources
import com.bloombook.models.Entries
import com.bloombook.models.Journals
import com.bloombook.models.Reminders
import com.google.firebase.Timestamp
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class EditReminderViewModel(
    private val repository: RemindersRepo = RemindersRepo(),
    private val journalRepository: JournalRepo = JournalRepo()
): ViewModel() {

    private val _uiState = MutableStateFlow(EditReminderState())
    val uiState: StateFlow<EditReminderState> = _uiState.asStateFlow()

    private val date
        get() = uiState.value.date
    private val time
        get() = uiState.value.time

    private val message
        get() = uiState.value.message

    private val interval
        get() = uiState.value.interval
    fun storage() = repository.storageRef()


    fun onDateChange(newDate: LocalDate) {
        _uiState.update { currState ->
            currState.copy(date = newDate)
        }
    }

    fun onTimeChange(newTime: LocalTime) {
        _uiState.update { currState ->
            currState.copy(time = newTime)
        }
    }

    fun onMessageChange(newMessage: String) {
        _uiState.update { currState ->
            currState.copy(message = newMessage)
        }
    }

    fun onIntervalChange(newInterval: String) {
        _uiState.update { currState ->
            currState.copy(interval = newInterval)
        }
    }

    fun addTagToScreen(journal: Journals): Boolean {

        val newTagList = _uiState.value.tagList.toMutableList()

        if (!newTagList.contains(journal)) {
            newTagList.add(0, journal)
            _uiState.update { currState ->
                currState.copy(tagList = newTagList)
            }
            return true
        }
        else {
            return false
        }
    }

    fun deleteTag(index: Int) {

        val newTagList = _uiState.value.tagList.toMutableList()
        newTagList.removeAt(index)

        _uiState.update { currState ->
            currState.copy(tagList = newTagList)
        }
    }

    private fun setEditFields(reminder: Reminders) {
         _uiState.update { currState ->
            currState.copy (
                tagList = reminder.tagList,
                message = reminder.message,
                interval = reminder.interval,
                selectedReminder = reminder,
                date = LocalDate.parse(formatDate(reminder.scheduledDate!!), DateTimeFormatter.ofPattern("M d yyyy")),
                time = LocalTime.parse(formatTime(reminder.scheduledDateTime!!), DateTimeFormatter.ofPattern("h mm a"))
            )
        }
    }

    fun getJournals() = viewModelScope.launch {

        journalRepository.getUserJournals().collect { resource ->

            journalRepository.getUserJournals().collect { resource ->

                // grab the actual list data from the Resource object
                val list = resource.data

                _uiState.update { currState ->
                    currState.copy(journalsList = list.orEmpty())
                }
            }
        }
    }

    fun getReminder(
        reminderId: String
    ) {
        repository.getReminder(
            reminderId = reminderId,
            onError = { error ->
                    Log.e("getEntry error", error?.message.toString() )
            }
        ) { reminder ->

            reminder?.let { setEditFields(it)}
        }
    }

    fun addReminder(callback: (String) -> Unit){
        var retrieveReminder = ""
        repository.addReminder(
            tagList = _uiState.value.tagList,
            scheduledDate = _uiState.value.date,
            scheduledTime = _uiState.value.time,
            message = _uiState.value.message,
            interval = _uiState.value.interval,
            timestamp = Timestamp.now(),
            onError = {}
        ) {

            Log.d("reminder VM added: ", it.toString())
            _uiState.update { currState ->
                currState.copy (
                    reminderAddedStatus = true,
                    selectedReminder = it
                )
            }
            retrieveReminder = _uiState.value.selectedReminder!!.documentId
            callback(retrieveReminder)
            Log.d("new reminder VM state: ",  _uiState.value.selectedReminder!!.documentId.toString())

        }
    }

    fun updateReminder(reminderId: String) {

        repository.updateReminder(
             reminderId,
            _uiState.value.tagList,
            _uiState.value.date,
            _uiState.value.time,
            _uiState.value.message,
            _uiState.value.interval
        ) {isUpdated ->
            _uiState.update { currState ->
                currState.copy (
                    reminderUpdateStatus = true
                )
            }
            Log.d("edit reminder screen addStatus: ", isUpdated.toString() )
        }
    }






}

data class EditReminderState(

    // list of plant tag candidates for the current reminder
    val tagList: List<Journals> =  emptyList(),

    val date: LocalDate = LocalDate.now(),
    val time: LocalTime = LocalTime.now(),
    val message: String = "",
    val interval: String = "",

    val selectedReminder: Reminders? = null,
    val reminderAddedStatus: Boolean = false,
    val reminderUpdateStatus: Boolean = false,

    // current collection of plant journals
    val journalsList: List<Journals> = emptyList(),

)
private fun formatDate(timestamp: Timestamp): String {
    val sdf = SimpleDateFormat("M d yyyy", Locale.getDefault())
    return sdf.format(timestamp.toDate())
}
private fun formatTime(timestamp: Timestamp): String {
    val sdf = SimpleDateFormat("h mm a", Locale.getDefault())
    return sdf.format(timestamp.toDate())
}
