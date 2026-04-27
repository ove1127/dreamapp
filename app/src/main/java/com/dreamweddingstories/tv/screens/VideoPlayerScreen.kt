@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)

package com.dreamweddingstories.tv.screens

import android.widget.FrameLayout
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.dreamweddingstories.tv.components.PrimaryButton
import com.dreamweddingstories.tv.components.SecondaryButton
import com.dreamweddingstories.tv.components.GoldShimmerLine
import com.dreamweddingstories.tv.components.PlayerControls
import com.dreamweddingstories.tv.ui.theme.DarkBackground
import com.dreamweddingstories.tv.ui.theme.DreamAnimation
import com.dreamweddingstories.tv.ui.theme.ErrorAmber
import com.dreamweddingstories.tv.ui.theme.TextPrimary
import com.dreamweddingstories.tv.ui.theme.TextSecondary
import com.dreamweddingstories.tv.ui.theme.TextTertiary
import com.dreamweddingstories.tv.utils.Constants
import com.dreamweddingstories.tv.viewmodel.PlayerViewModel
import kotlinx.coroutines.delay
import kotlin.math.max

@Composable
fun VideoPlayerScreen(
    videoId: String,
    vimeoVideoId: String,
    title: String,
    viewModel: PlayerViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.playerState.collectAsState()

    var controlsVisible by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(true) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var positionMs by remember { mutableLongStateOf(0L) }

    BackHandler {
        viewModel.savePlaybackPosition(videoId, positionMs)
        onBack()
    }

    LaunchedEffect(videoId, vimeoVideoId, title) {
        viewModel.loadStream(videoId = videoId, vimeoVideoId = vimeoVideoId, title = title)
    }

    // Auto-hide controls after 3s
    LaunchedEffect(controlsVisible) {
        if (controlsVisible) {
            delay(Constants.PLAYER_CONTROLS_HIDE_DELAY_MS)
            controlsVisible = false
        }
    }

    val streamUrl = state.streamUrl
    val exoPlayer = remember(streamUrl) {
        if (streamUrl.isNullOrBlank()) return@remember null
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(streamUrl))
            prepare()
            seekTo(viewModel.getPlaybackPosition(videoId))
            playWhenReady = true
        }
    }

    // Player listener
    DisposableEffect(exoPlayer) {
        val player = exoPlayer
        if (player == null) {
            onDispose { }
        } else {
            val listener = object : Player.Listener {
                override fun onIsPlayingChanged(playing: Boolean) {
                    isPlaying = playing
                }
                override fun onPlaybackStateChanged(playbackState: Int) {
                    isBuffering = playbackState == Player.STATE_BUFFERING
                }
                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    android.util.Log.e("VideoPlayerScreen", "Player Error: ${error.message}", error)
                    viewModel.clearError()
                }
            }
            player.addListener(listener)
            onDispose {
                viewModel.savePlaybackPosition(videoId, player.currentPosition)
                player.removeListener(listener)
                player.release()
            }
        }
    }

    // MediaSession integration
    DisposableEffect(exoPlayer) {
        val player = exoPlayer ?: return@DisposableEffect onDispose { }
        val mediaSession = androidx.media3.session.MediaSession.Builder(context, player)
            .setId("DreamWeddingPlayer_$videoId")
            .build()
        onDispose {
            mediaSession.release()
        }
    }

    // Position polling
    LaunchedEffect(exoPlayer) {
        while (exoPlayer != null) {
            positionMs = max(0L, exoPlayer.currentPosition)
            durationMs = max(0L, exoPlayer.duration)
            delay(300)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .focusable()
            .onPreviewKeyEvent { event ->
                if (event.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN) {
                    val keyCode = event.nativeKeyEvent.keyCode
                    controlsVisible = true

                    when (keyCode) {
                        android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
                        android.view.KeyEvent.KEYCODE_DPAD_CENTER -> {
                            exoPlayer?.let { p ->
                                if (p.isPlaying) p.pause() else p.play()
                            }
                            true
                        }
                        android.view.KeyEvent.KEYCODE_MEDIA_PLAY -> {
                            exoPlayer?.play()
                            true
                        }
                        android.view.KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                            exoPlayer?.pause()
                            true
                        }
                        android.view.KeyEvent.KEYCODE_MEDIA_REWIND,
                        android.view.KeyEvent.KEYCODE_DPAD_LEFT -> {
                            exoPlayer?.seekTo(max(0L, positionMs - Constants.SEEK_INTERVAL_MS))
                            true
                        }
                        android.view.KeyEvent.KEYCODE_MEDIA_FAST_FORWARD,
                        android.view.KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            exoPlayer?.seekTo(positionMs + Constants.SEEK_INTERVAL_MS)
                            true
                        }
                        android.view.KeyEvent.KEYCODE_BACK -> {
                            viewModel.savePlaybackPosition(videoId, positionMs)
                            onBack()
                            true
                        }
                        else -> false
                    }
                } else false
            }
    ) {
        // ── Video Surface or WebView Fallback ──
        if (streamUrl != null && exoPlayer != null) {
            AndroidView(
                factory = {
                    PlayerView(it).apply {
                        useController = false
                        layoutParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                        )
                        player = exoPlayer
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else if (state.isFallbackWebView && state.webViewUrl != null) {

            AndroidView(
                factory = { ctx ->
                    object : WebView(ctx) {
                        override fun dispatchKeyEvent(event: android.view.KeyEvent): Boolean {
                            if (event.action == android.view.KeyEvent.ACTION_DOWN) {
                                when (event.keyCode) {
                                    android.view.KeyEvent.KEYCODE_DPAD_CENTER,
                                    android.view.KeyEvent.KEYCODE_ENTER,
                                    android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                                        evaluateJavascript("""
                                            (function(){
                                              var v=document.querySelector('video');
                                              if(v){if(v.paused)v.play();else v.pause();}
                                            })();
                                        """.trimIndent(), null)
                                        return true
                                    }
                                    android.view.KeyEvent.KEYCODE_DPAD_LEFT,
                                    android.view.KeyEvent.KEYCODE_MEDIA_REWIND -> {
                                        evaluateJavascript("""
                                            (function(){var v=document.querySelector('video');
                                            if(v)v.currentTime=Math.max(0,v.currentTime-10);})();
                                        """.trimIndent(), null)
                                        return true
                                    }
                                    android.view.KeyEvent.KEYCODE_DPAD_RIGHT,
                                    android.view.KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> {
                                        evaluateJavascript("""
                                            (function(){var v=document.querySelector('video');
                                            if(v)v.currentTime=Math.min(v.duration,v.currentTime+10);})();
                                        """.trimIndent(), null)
                                        return true
                                    }
                                    android.view.KeyEvent.KEYCODE_MEDIA_PLAY -> {
                                        evaluateJavascript("document.querySelector('video')?.play();", null)
                                        return true
                                    }
                                    android.view.KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                                        evaluateJavascript("document.querySelector('video')?.pause();", null)
                                        return true
                                    }
                                    android.view.KeyEvent.KEYCODE_BACK -> {
                                        return super.dispatchKeyEvent(event)
                                    }
                                }
                            }
                            return super.dispatchKeyEvent(event)
                        }
                    }.apply {
                        layoutParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                        )
                        setBackgroundColor(android.graphics.Color.BLACK)
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.mediaPlaybackRequiresUserGesture = false
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true
                        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        settings.allowContentAccess = true
                        settings.allowFileAccess = true
                        // Desktop Chrome UA — Vimeo serves the full player to desktop browsers
                        settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"

                        webViewClient = object : android.webkit.WebViewClient() {
                            override fun shouldInterceptRequest(
                                view: WebView?,
                                request: android.webkit.WebResourceRequest?
                            ): android.webkit.WebResourceResponse? {
                                // Let all requests through — no interception needed
                                return null
                            }
                            override fun onPageFinished(view: WebView?, url: String?) {
                                android.util.Log.d("VimeoWebView", "PAGE_FINISHED: $url")
                                // Force play in case autoplay was blocked
                                view?.evaluateJavascript("""
                                    (function(){
                                      var v = document.querySelector('video');
                                      if(v && v.paused) { v.play(); }
                                    })();
                                """.trimIndent(), null)
                            }
                            override fun onReceivedSslError(
                                view: WebView?,
                                handler: android.webkit.SslErrorHandler?,
                                error: android.net.http.SslError?
                            ) {
                                handler?.proceed()
                            }
                            override fun onReceivedError(
                                view: WebView?,
                                request: android.webkit.WebResourceRequest?,
                                error: android.webkit.WebResourceError?
                            ) {
                                android.util.Log.e("VimeoWebView", "ERROR: ${error?.errorCode} ${error?.description} for ${request?.url}")
                            }
                        }
                        webChromeClient = object : WebChromeClient() {
                            override fun onConsoleMessage(msg: android.webkit.ConsoleMessage?): Boolean {
                                android.util.Log.d("VimeoWebView", "JS: ${msg?.message()}")
                                return true
                            }
                        }

                        isFocusable = true
                        isFocusableInTouchMode = true
                        requestFocus()

                        // Load the Vimeo player URL with Referer header pointing to our
                        // registered domain. This is a real navigation (no ORB blocking)
                        // and Vimeo's embed validation accepts it because of the Referer.
                        val playerUrl = state.webViewUrl ?: return@apply
                        android.util.Log.d("VimeoWebView", "Loading: $playerUrl")
                        loadUrl(playerUrl, mapOf("Referer" to "https://dreamweddingstories.com"))
                    }
                },
                update = { },
                modifier = Modifier.fillMaxSize()
            )
        }

        // ── Buffering indicator — gold shimmer, not spinner ──
        if (state.isLoading || isBuffering) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    GoldShimmerLine(modifier = Modifier.width(200.dp))
                    Text(
                        text = "LOADING",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                }
            }
        }

        // ── Error state — warm amber ──
        if (!state.error.isNullOrBlank()) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = state.error ?: "Playback failed",
                    color = ErrorAmber,
                    style = MaterialTheme.typography.bodyLarge
                )
                PrimaryButton(
                    text = "Retry",
                    onClick = {
                        controlsVisible = true
                        viewModel.loadStream(videoId, vimeoVideoId, title)
                    },
                    modifier = Modifier.width(180.dp)
                )
                SecondaryButton(
                    text = "Back",
                    onClick = onBack,
                    modifier = Modifier.width(180.dp)
                )
            }
        }

        // ── Cinematic Player Controls (ExoPlayer only) ──
        if (state.error.isNullOrBlank() && !state.isFallbackWebView) {
            // Top controls
            Box(modifier = Modifier.align(Alignment.TopCenter)) {
                PlayerControls(
                    visible = controlsVisible,
                    title = if (state.title.isBlank()) title else state.title,
                    isPlaying = isPlaying,
                    positionMs = positionMs,
                    durationMs = durationMs,
                    onPlayPause = {
                        exoPlayer?.let { p -> if (p.isPlaying) p.pause() else p.play() }
                    },
                    onSeekBack = {
                        exoPlayer?.seekTo(max(0L, positionMs - Constants.SEEK_INTERVAL_MS))
                    },
                    onSeekForward = {
                        exoPlayer?.seekTo(positionMs + Constants.SEEK_INTERVAL_MS)
                    },
                    onBack = {
                        viewModel.savePlaybackPosition(videoId, positionMs)
                        onBack()
                    }
                )
            }

            // Bottom controls (need separate alignment)
            Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                PlayerControls(
                    visible = controlsVisible,
                    title = if (state.title.isBlank()) title else state.title,
                    isPlaying = isPlaying,
                    positionMs = positionMs,
                    durationMs = durationMs,
                    onPlayPause = {
                        exoPlayer?.let { p -> if (p.isPlaying) p.pause() else p.play() }
                    },
                    onSeekBack = {
                        exoPlayer?.seekTo(max(0L, positionMs - Constants.SEEK_INTERVAL_MS))
                    },
                    onSeekForward = {
                        exoPlayer?.seekTo(positionMs + Constants.SEEK_INTERVAL_MS)
                    },
                    onBack = {
                        viewModel.savePlaybackPosition(videoId, positionMs)
                        onBack()
                    }
                )
            }
        }
    }
}
