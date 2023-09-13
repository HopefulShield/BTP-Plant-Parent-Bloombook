package com.bloombook.screens.home

import com.bloombook.backend.Resources
import com.bloombook.models.Journals
import com.bloombook.models.Reminders

data class HomeScreenState (
    val journalsList: List<Journals> = emptyList(),
    val dailyRemindersList: List<Reminders> = emptyList(),

    val deleteStatus: Boolean = false,
    val selectedReminder: Reminders? = null
)