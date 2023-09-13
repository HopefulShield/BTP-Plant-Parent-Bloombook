package com.bloombook.models

import android.net.Uri
import com.google.firebase.Timestamp

data class Journals (
    val commonName: String = "",
    val nickName: String = "",
    var displayImageUri: String = "",
    val documentId: String = "",
    val timestamp: Timestamp? = null
)