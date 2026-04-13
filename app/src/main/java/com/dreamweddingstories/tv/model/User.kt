package com.dreamweddingstories.tv.model

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val assignedVideoIds: List<String> = emptyList()
)
