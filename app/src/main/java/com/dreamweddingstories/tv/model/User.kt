package com.dreamweddingstories.tv.model

data class User(
    val uid: String = "",          // same as accessCode (used as identifier)
    val accessCode: String = "",   // the 4-char wedding code (e.g. "V4N2")
    val email: String = "",        // kept for backward compat, not used for login
    val displayName: String = "",
    val assignedVideoIds: List<String> = emptyList()
)
