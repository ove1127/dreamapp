package com.dreamweddingstories.tv.network

import com.dreamweddingstories.tv.model.VimeoVideoResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface VimeoApiService {
    @GET("videos/{video_id}")
    suspend fun getVideoDetails(
        @Path("video_id") videoId: String,
        @Query("h") hash: String? = null
    ): VimeoVideoResponse
}
