package com.bloombook.screens.reminder

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloombook.backend.RemindersRepo
import com.bloombook.backend.Resources
import com.bloombook.models.Reminders
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class RemindersViewModel(
    private val repository: RemindersRepo = RemindersRepo()
): ViewModel() {

    private val _uiState = MutableStateFlow(ReminderState())
    val uiState: StateFlow<ReminderState> = _uiState.asStateFlow()

    fun storage() = repository.storageRef()


    fun getReminders() = viewModelScope.launch {
        repository.getUserReminders().collect{ resource ->

            _uiState.update { currState ->
                currState.copy(remindersList = resource.data.orEmpty())
            }
        }
    }


    fun selectedDate(date: LocalDate) = viewModelScope.launch {
        repository.getRemindersAtDate(date).collect{ resource ->

            Log.d("Calendar Data", resource.data.toString())
            val list = resource.data!!.sortedBy { it.scheduledDateTime }
            _uiState.update { currState ->
                currState.copy(
                    selectedDate = date,
                    remindersCalendar = list)
            }
        }
    }

    fun saveCalendarDate(time: Long) {

    }

    fun deleteReminder() {
        repository.deleteReminder(_uiState.value.selectedReminder!!.documentId) {
            _uiState.update { currState ->
                currState.copy(deleteStatus = it)
            }
        }
    }

    fun resetDeleteStatus (){
        _uiState.update { currState ->
            currState.copy(deleteStatus = false)
        }
    }

    fun setSelectedReminder(reminder: Reminders) {
        _uiState.update { currState ->
            currState.copy(selectedReminder = reminder)
        }
    }

}

data class ReminderState(
    var selectedDate: LocalDate = LocalDate.now(),
    var dateDisplay: String = "",
    var calendarDate:Long = System.currentTimeMillis(),
    val deleteStatus:Boolean = false,
    val remindersCalendar: List<Reminders> = emptyList(),
    val remindersList: List<Reminders> = emptyList(),
    val selectedReminder: Reminders? = null

)
