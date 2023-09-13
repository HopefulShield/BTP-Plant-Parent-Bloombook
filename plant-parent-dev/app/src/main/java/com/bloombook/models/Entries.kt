package com.bloombook.models

import com.google.firebase.Timestamp
data class Entries (
    val journalId: String = "",
    val temperature: String = "",
    val selectedOption: String = "",
    val location: String = "",
    val customLocation: String = "",
    val observations: String = "",
    val documentId: String = "",
    val imageList: List<Map<String, String>> = emptyList(),
    val timestamp: Timestamp = Timestamp.now()

)