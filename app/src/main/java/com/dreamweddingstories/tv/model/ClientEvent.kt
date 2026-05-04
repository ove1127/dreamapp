package com.dreamweddingstories.tv.model

/**
 * Represents a single function/event assigned to a client
 * (e.g., "Pre-Wedding Shoot", "Main Wedding", "Baby Shower").
 * Each event contains its own list of videos.
 */
data class ClientEvent(
    val eventName: String = "",
    val eventDate: String = "",
    val thumbnailUrl: String = "",
    val videos: List<WeddingVideo> = emptyList()
) {
    /** The "Main Film" or first video used for the background preview */
    val previewVideo: WeddingVideo?
        get() = videos.firstOrNull { it.category == "Main Film" }
            ?: videos.firstOrNull { it.category == "Trailer" }
            ?: videos.firstOrNull()

    val previewVimeoId: String
        get() = previewVideo?.vimeoVideoId ?: ""
}
