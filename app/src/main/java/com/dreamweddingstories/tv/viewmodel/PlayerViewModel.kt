package com.dreamweddingstories.tv.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dreamweddingstories.tv.repository.VimeoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val vimeoRepository: VimeoRepository
) : ViewModel() {

    data class PlayerStreamState(
        val isLoading: Boolean = false,
        val streamUrl: String? = null,
        val error: String? = null,
        val title: String = "",
        val videoId: String = ""
    )

    private val _playerState = MutableStateFlow(PlayerStreamState())
    val playerState: StateFlow<PlayerStreamState> = _playerState.asStateFlow()

    private val savedPositions = mutableMapOf<String, Long>()

    fun loadStream(videoId: String, vimeoVideoId: String, title: String) {
        if (_playerState.value.videoId == videoId && _playerState.value.streamUrl != null) return

        viewModelScope.launch {
            _playerState.value = PlayerStreamState(isLoading = true, title = title, videoId = videoId)
            val result = vimeoRepository.getPlayableUrl(vimeoVideoId)
            _playerState.value = result.fold(
                onSuccess = { url ->
                    PlayerStreamState(
                        isLoading = false,
                        streamUrl = url,
                        error = null,
                        title = title,
                        videoId = videoId
                    )
                },
                onFailure = {
                    PlayerStreamState(
                        isLoading = false,
                        streamUrl = null,
                        error = it.message ?: "Unable to play this video",
                        title = title,
                        videoId = videoId
                    )
                }
            )
        }
    }

    fun savePlaybackPosition(videoId: String, positionMs: Long) {
        savedPositions[videoId] = positionMs.coerceAtLeast(0L)
    }

    fun getPlaybackPosition(videoId: String): Long = savedPositions[videoId] ?: 0L

    fun clearError() {
        _playerState.value = _playerState.value.copy(error = null)
    }
}

