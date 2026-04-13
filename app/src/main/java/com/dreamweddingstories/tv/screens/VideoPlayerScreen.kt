@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)

package com.dreamweddingstories.tv.screens

import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.dreamweddingstories.tv.ui.theme.DarkBackground
import com.dreamweddingstories.tv.ui.theme.ErrorRed
import com.dreamweddingstories.tv.ui.theme.PlayerControlBg
import com.dreamweddingstories.tv.ui.theme.AccentWhite
import com.dreamweddingstories.tv.ui.theme.TextPrimary
import com.dreamweddingstories.tv.ui.theme.TextSecondary
import com.dreamweddingstories.tv.utils.Constants
import com.dreamweddingstories.tv.viewmodel.PlayerViewModel
import kotlinx.coroutines.delay
import java.util.Locale
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
            }
            player.addListener(listener)
            onDispose {
                viewModel.savePlaybackPosition(videoId, player.currentPosition)
                player.removeListener(listener)
                player.release()
            }
        }
    }

    // MediaSession integration for TV remote media buttons
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

                    // Show controls on any key
                    controlsVisible = true

                    // Handle media keys
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
        // ── Video Surface ──
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
        }

        // ── Buffering indicator ──
        if (state.isLoading || isBuffering) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        color = AccentWhite,
                        modifier = Modifier.size(48.dp),
                        strokeWidth = 3.dp
                    )
                    Text(
                        text = "Loading...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }

        // ── Error state ──
        if (!state.error.isNullOrBlank()) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = state.error ?: "Playback failed",
                    color = ErrorRed,
                    style = MaterialTheme.typography.bodyLarge
                )
                Button(
                    onClick = {
                        controlsVisible = true
                        viewModel.loadStream(videoId, vimeoVideoId, title)
                    },
                    colors = ButtonDefaults.colors(containerColor = AccentWhite, contentColor = DarkBackground)
                ) {
                    Text("Retry", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.colors(
                        containerColor = Color.Transparent,
                        contentColor = TextSecondary,
                        focusedContainerColor = AccentWhite.copy(alpha = 0.15f),
                        focusedContentColor = AccentWhite
                    )
                ) {
                    Text("Back")
                }
            }
        }

        // ── Custom Playback Controls ──
        AnimatedVisibility(
            visible = controlsVisible && state.error.isNullOrBlank(),
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, PlayerControlBg, PlayerControlBg)
                        )
                    )
                    .padding(horizontal = 32.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Video title
                Text(
                    text = if (state.title.isBlank()) title else state.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // ── Seek bar ──
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Progress track
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(TextPrimary.copy(alpha = 0.15f))
                    ) {
                        val fraction = if (durationMs > 0) positionMs.toFloat() / durationMs else 0f
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(AccentWhite, AccentWhite.copy(alpha = 0.7f))
                                    )
                                )
                        )
                    }

                    // Time labels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatTime(positionMs),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Text(
                            text = formatTime(durationMs),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }

                // ── Control buttons ──
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back
                    ControlButton(
                        text = "←",
                        label = "Back",
                        onClick = {
                            viewModel.savePlaybackPosition(videoId, positionMs)
                            onBack()
                        }
                    )

                    // Rewind 10s
                    ControlButton(
                        text = "↻",
                        label = "-10s",
                        onClick = {
                            exoPlayer?.seekTo(max(0L, positionMs - Constants.SEEK_INTERVAL_MS))
                        }
                    )

                    // Play / Pause (primary)
                    Button(
                        onClick = {
                            exoPlayer?.let { p ->
                                if (p.isPlaying) p.pause() else p.play()
                            }
                        },
                        modifier = Modifier.size(56.dp),
                        shape = ButtonDefaults.shape(shape = CircleShape),
                        colors = ButtonDefaults.colors(
                            containerColor = AccentWhite,
                            contentColor = DarkBackground,
                            focusedContainerColor = AccentWhite.copy(alpha = 0.85f),
                            focusedContentColor = DarkBackground
                        )
                    ) {
                        Text(
                            text = if (isPlaying) "⏸" else "▶",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Forward 10s
                    ControlButton(
                        text = "↺",
                        label = "+10s",
                        onClick = {
                            exoPlayer?.seekTo(positionMs + Constants.SEEK_INTERVAL_MS)
                        }
                    )
                }
            }
        }

        // ── Title overlay at top when controls visible ──
        AnimatedVisibility(
            visible = controlsVisible && state.error.isNullOrBlank(),
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(PlayerControlBg, Color.Transparent)
                        )
                    )
                    .padding(horizontal = 32.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Dream Wedding Stories",
                    style = MaterialTheme.typography.labelLarge,
                    color = AccentWhite.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/* ──────────────────────────────────────────────────────────────
   Control Button — translucent with icon + label
   ────────────────────────────────────────────────────────────── */
@Composable
private fun ControlButton(text: String, label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.colors(
            containerColor = TextPrimary.copy(alpha = 0.1f),
            contentColor = TextPrimary,
            focusedContainerColor = TextPrimary.copy(alpha = 0.25f),
            focusedContentColor = TextPrimary
        ),
        shape = ButtonDefaults.shape(shape = RoundedCornerShape(12.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = text, style = MaterialTheme.typography.titleMedium)
            Text(text = label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

/* ──────────────────────────────────────────────────────────────
   Time formatter — ms → "mm:ss" or "hh:mm:ss"
   ────────────────────────────────────────────────────────────── */
private fun formatTime(ms: Long): String {
    if (ms <= 0) return "0:00"
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%d:%02d", minutes, seconds)
    }
}
