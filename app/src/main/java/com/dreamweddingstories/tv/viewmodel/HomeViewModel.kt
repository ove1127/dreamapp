package com.dreamweddingstories.tv.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dreamweddingstories.tv.model.ClientEvent
import com.dreamweddingstories.tv.model.UiState
import com.dreamweddingstories.tv.model.User
import com.dreamweddingstories.tv.model.WeddingVideo
import com.dreamweddingstories.tv.repository.VideoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val videoRepository: VideoRepository
) : ViewModel() {

    private val _videosState = MutableStateFlow<UiState<List<WeddingVideo>>>(UiState.Loading)
    val videosState: StateFlow<UiState<List<WeddingVideo>>> = _videosState.asStateFlow()

    private val _selectedVideoState = MutableStateFlow<UiState<WeddingVideo>>(UiState.Idle)
    val selectedVideoState: StateFlow<UiState<WeddingVideo>> = _selectedVideoState.asStateFlow()

    // ── Events state — populated once user logs in ──
    private val _eventsState = MutableStateFlow<UiState<List<ClientEvent>>>(UiState.Idle)
    val eventsState: StateFlow<UiState<List<ClientEvent>>> = _eventsState.asStateFlow()

    /**
     * Called after login — loads the events from the User object directly.
     * If the user has events, expose them. Otherwise fall back to loading flat videos.
     */
    fun loadEvents(user: User) {
        if (user.events.isNotEmpty()) {
            _eventsState.value = UiState.Success(user.events)
        } else {
            // No events structure — treat each video as its own "event" or skip to home
            _eventsState.value = UiState.Error("no_events")
        }
    }

    /**
     * Load only the videos for a specific event (called when user selects an event).
     */
    fun loadVideosForEvent(event: ClientEvent) {
        if (event.videos.isNotEmpty()) {
            _videosState.value = UiState.Success(event.videos)
        } else {
            _videosState.value = UiState.Error("No videos in this event yet")
        }
    }

    /**
     * Legacy loader — loads all assigned videos flat (used as fallback).
     */
    fun loadAssignedVideos(user: User) {
        viewModelScope.launch {
            _videosState.value = UiState.Loading
            val result = videoRepository.getVideosByIds(user.assignedVideoIds)
            _videosState.value = result.fold(
                onSuccess = { videos ->
                    if (videos.isEmpty()) {
                        UiState.Error("No videos are assigned to this account yet")
                    } else {
                        UiState.Success(videos)
                    }
                },
                onFailure = {
                    UiState.Error(it.message ?: "Unable to load videos")
                }
            )
        }
    }

    fun loadVideoById(videoId: String) {
        // 1. Try to find it in the currently loaded videos list first (instant)
        val currentVideos = (_videosState.value as? UiState.Success)?.data
        val cachedVideo = currentVideos?.find { it.id == videoId }
        
        if (cachedVideo != null) {
            _selectedVideoState.value = UiState.Success(cachedVideo)
            return
        }

        // 2. If not found in flat list, check all events
        val currentEvents = (_eventsState.value as? UiState.Success)?.data
        val videoInEvents = currentEvents?.flatMap { it.videos }?.find { it.id == videoId }
        
        if (videoInEvents != null) {
            _selectedVideoState.value = UiState.Success(videoInEvents)
            return
        }

        // 3. Fallback to repository if not found in memory
        viewModelScope.launch {
            _selectedVideoState.value = UiState.Loading
            val result = videoRepository.getVideoById(videoId)
            _selectedVideoState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Unable to load video details") }
            )
        }
    }

    fun resetSelectedVideo() {
        _selectedVideoState.value = UiState.Idle
    }
}
