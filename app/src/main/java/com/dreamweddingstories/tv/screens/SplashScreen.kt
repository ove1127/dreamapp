@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)

package com.dreamweddingstories.tv.screens

import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.dreamweddingstories.tv.R
import com.dreamweddingstories.tv.ui.theme.DarkBackground

/**
 * Splash Screen — Full-screen logo opener video
 *
 * Plays `res/raw/splash_video.mp4` full-screen using ExoPlayer.
 * Navigates to the next screen as soon as the video ends.
 * If the video resource is missing, falls back to navigating immediately.
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val context = LocalContext.current
    val fadeAlpha = remember { Animatable(0f) }
    var videoFinished by remember { mutableStateOf(false) }

    // Build ExoPlayer and attach listener
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            // Check if the raw resource exists using getIdentifier for robustness
            val resId = context.resources.getIdentifier("splash_video", "raw", context.packageName)

            if (resId != 0) {
                val uri = Uri.parse("android.resource://${context.packageName}/$resId")
                setMediaItem(MediaItem.fromUri(uri))
                prepare()
                playWhenReady = true
                volume = 0f  // Muted — change to 1f if you want audio
                repeatMode = Player.REPEAT_MODE_OFF
            } else {
                videoFinished = true // No video found
            }

            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_ENDED) {
                        videoFinished = true
                    }
                }
            })
        }
    }

    // If no video resource, navigate immediately
    LaunchedEffect(Unit) {
        val resId = context.resources.getIdentifier("splash_video", "raw", context.packageName)
        if (resId == 0) {
            onFinished()
        }
        // Fade the player in smoothly
        fadeAlpha.animateTo(1f, tween(600))
    }

    // Navigate when video ends
    LaunchedEffect(videoFinished) {
        if (videoFinished) {
            onFinished()
        }
    }

    // Release player when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .alpha(fadeAlpha.value)
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false          // No playback controls
                    setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
                    resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM // Fill screen
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
