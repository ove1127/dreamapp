package com.dreamweddingstories.tv.repository

import com.dreamweddingstories.tv.model.VimeoVideoResponse
import com.dreamweddingstories.tv.network.VimeoApiService
import com.dreamweddingstories.tv.utils.Constants
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VimeoRepository @Inject constructor(
    private val vimeoApiService: VimeoApiService
) {
    suspend fun getVideoDetails(vimeoVideoId: String): Result<VimeoVideoResponse> = runCatching {
        val parts = vimeoVideoId.split("?h=")
        vimeoApiService.getVideoDetails(videoId = parts[0], hash = parts.getOrNull(1))
    }

    suspend fun getPlayableUrl(vimeoVideoId: String): Result<String> = runCatching {
        // --- Fallback for Demo Mode (if no token provided) ---
        if (Constants.VIMEO_ACCESS_TOKEN == "VIMEO_ACCESS_TOKEN" || Constants.VIMEO_ACCESS_TOKEN.isBlank()) {
            return@runCatching "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
        }

        val parts = vimeoVideoId.split("?h=")
        val response = vimeoApiService.getVideoDetails(videoId = parts[0], hash = parts.getOrNull(1))
        response.play?.hls?.link
            ?: response.play?.progressive?.maxByOrNull { it.width * it.height }?.link
            ?: throw IllegalStateException("No playable Vimeo stream found")
    }

    suspend fun getLargestThumbnail(vimeoVideoId: String): Result<String> = runCatching {
        val parts = vimeoVideoId.split("?h=")
        val response = vimeoApiService.getVideoDetails(videoId = parts[0], hash = parts.getOrNull(1))
        response.pictures.sizes.maxByOrNull { it.width * it.height }?.link
            ?: throw IllegalStateException("No Vimeo thumbnail available")
    }
}
