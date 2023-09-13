package com.bloombook.models

data class Users (
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val profilePicture: String = "",
    val imageList: List<String> = emptyList()
)