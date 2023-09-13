package com.bloombook.models

import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.LocalTime

data class Reminders(
    val documentId: String = "",
    val tagList: List<Journals> =  emptyList(),
    val timestamp: Timestamp? = null,
    var scheduledDate:  Timestamp? = null,
    var scheduledDateTime: Timestamp? = null,
    var nextDueDate:  Timestamp? = null,
    var nextDueDateTime:  Timestamp? = null,
    val message: String = "",
    val interval: String = "",
    val lastCompletedDate: Timestamp? = null
)