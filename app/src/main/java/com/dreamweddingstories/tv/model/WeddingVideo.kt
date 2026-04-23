package com.dreamweddingstories.tv.model

data class WeddingVideo(
    val id: String = "",
    val vimeoVideoId: String = "",
    val coupleNames: String = "",
    val weddingDate: String = "",
    val description: String = "",
    val thumbnailUrl: String = "",
    val duration: String = "",
    val category: String = "",
    val userId: String = ""   // links to the logged-in user
)
