package com.dreamweddingstories.tv.model

data class VimeoVideoResponse(
    val name: String = "",
    val description: String? = null,
    val duration: Int = 0,
    val pictures: Pictures = Pictures(),
    val play: Play? = null
)

data class Pictures(
    val sizes: List<Size> = emptyList()
)

data class Size(
    val width: Int = 0,
    val height: Int = 0,
    val link: String = ""
)

data class Play(
    val hls: Hls? = null,
    val progressive: List<Progressive>? = null
)

data class Hls(
    val link: String = ""
)

data class Progressive(
    val link: String = "",
    val width: Int = 0,
    val height: Int = 0
)
