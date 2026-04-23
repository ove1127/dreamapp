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
        val videoId: String = "",
        val vimeoVideoId: String = "",
        val isFallbackWebView: Boolean = false,
        val webViewUrl: String? = null
    )

    private val _playerState = MutableStateFlow(PlayerStreamState())
    val playerState: StateFlow<PlayerStreamState> = _playerState.asStateFlow()

    private val savedPositions = mutableMapOf<String, Long>()

    fun loadStream(videoId: String, vimeoVideoId: String, title: String) {
        // Guard: don't reload if already loaded for this video
        if (_playerState.value.videoId == videoId && 
            (_playerState.value.streamUrl != null || _playerState.value.isFallbackWebView)) return

        viewModelScope.launch {
            _playerState.value = PlayerStreamState(isLoading = true, title = title, videoId = videoId)
            
            // Try Vimeo API first for native ExoPlayer streaming
            val result = vimeoRepository.getPlayableUrl(vimeoVideoId)
            android.util.Log.e("PlayerViewModel", "API Result: success=${result.isSuccess}")
            
            _playerState.value = result.fold(
                onSuccess = { url ->
                    android.util.Log.e("PlayerViewModel", "Got stream URL: $url")
                    PlayerStreamState(
                        isLoading = false,
                        streamUrl = url,
                        error = null,
                        title = title,
                        videoId = videoId,
                        vimeoVideoId = vimeoVideoId,
                        isFallbackWebView = false
                    )
                },
                onFailure = { err ->
                    android.util.Log.e("PlayerViewModel", "API failed: ${err.message}", err)
                    if (err is kotlinx.coroutines.CancellationException) throw err
                    
                    // Fall back to WebView embed
                    val parts = vimeoVideoId.split("?h=")
                    val vidId = parts[0]
                    val hashParam = if (parts.size > 1) "?h=${parts[1]}&" else "?"
                    val embedUrl = "https://player.vimeo.com/video/$vidId${hashParam}badge=0&autopause=0&autoplay=1&player_id=0&app_id=58479"
                    android.util.Log.e("PlayerViewModel", "Falling back to WebView: $embedUrl")
                    
                    PlayerStreamState(
                        isLoading = false,
                        streamUrl = null,
                        error = null,
                        title = title,
                        videoId = videoId,
                        vimeoVideoId = vimeoVideoId,
                        isFallbackWebView = true,
                        webViewUrl = embedUrl
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

