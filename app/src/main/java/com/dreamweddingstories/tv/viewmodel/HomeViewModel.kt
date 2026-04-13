package com.dreamweddingstories.tv.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

