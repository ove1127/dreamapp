@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)

package com.dreamweddingstories.tv.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.dreamweddingstories.tv.ui.theme.AccentGold
import com.dreamweddingstories.tv.ui.theme.DarkBackground
import com.dreamweddingstories.tv.ui.theme.DreamAnimation
import com.dreamweddingstories.tv.ui.theme.DreamShapes
import com.dreamweddingstories.tv.ui.theme.PlayerControlBg
import com.dreamweddingstories.tv.ui.theme.TextPrimary
import com.dreamweddingstories.tv.ui.theme.TextSecondary
import java.util.Locale
import kotlin.math.max

/**
 * Cinematic player controls overlay.
 *
 * Top bar: couple names left, DWS logo right.
 * Bottom bar: 1px gold scrub bar, time labels, control buttons.
 * Full-screen dims slightly when visible.
 */
@Composable
fun PlayerControls(
    visible: Boolean,
    title: String,
    isPlaying: Boolean,
    positionMs: Long,
    durationMs: Long,
    onPlayPause: () -> Unit,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // ── Dim overlay when controls visible ──
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(DreamAnimation.silkTween(400)),
        exit = fadeOut(DreamAnimation.silkTween(400)),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x66000000))
        )
    }

    // ── Top bar ──
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(DreamAnimation.silkTween(400)),
        exit = fadeOut(DreamAnimation.silkTween(400)),
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(PlayerControlBg, Color.Transparent)
                    )
                )
                .padding(horizontal = 40.dp, vertical = 20.dp)
        ) {
            // Couple names — Cormorant Garamond, left
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary.copy(alpha = 0.9f),
                fontWeight = FontWeight.Light,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.CenterStart)
            )

            // DWS logo mark — tiny, right
            Text(
                text = "DWS",
                style = MaterialTheme.typography.labelSmall,
                color = AccentGold.copy(alpha = 0.5f),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }

    // ── Bottom bar ──
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(DreamAnimation.silkTween(400)),
        exit = fadeOut(DreamAnimation.silkTween(400)),
        modifier = modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, PlayerControlBg, PlayerControlBg)
                    )
                )
                .padding(horizontal = 40.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Scrub bar — 1px thin, gold fill ──
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .clip(DreamShapes.Sharp)
                        .background(TextPrimary.copy(alpha = 0.1f))
                ) {
                    val fraction = if (durationMs > 0) positionMs.toFloat() / durationMs else 0f
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction.coerceIn(0f, 1f))
                            .height(2.dp)
                            .background(AccentGold)
                    )
                    // White dot scrubber handle
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = (fraction.coerceIn(0f, 1f) * 100).dp) // approximate
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(TextPrimary)
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
                        color = TextSecondary,
                        fontWeight = FontWeight.Light
                    )
                    Text(
                        text = formatTime(durationMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontWeight = FontWeight.Light
                    )
                }
            }

            // ── Control buttons ──
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back
                PlayerControlButton(text = "←", label = "Back", onClick = onBack)

                // Rewind 10s
                PlayerControlButton(text = "⟲", label = "−10s", onClick = onSeekBack)

                // Play / Pause — gold circle
                Button(
                    onClick = onPlayPause,
                    modifier = Modifier.size(56.dp),
                    shape = ButtonDefaults.shape(shape = CircleShape),
                    colors = ButtonDefaults.colors(
                        containerColor = AccentGold,
                        contentColor = DarkBackground,
                        focusedContainerColor = AccentGold,
                        focusedContentColor = DarkBackground
                    ),
                    glow = ButtonDefaults.glow(
                        focusedGlow = androidx.tv.material3.Glow(
                            elevationColor = AccentGold.copy(alpha = 0.4f),
                            elevation = 16.dp
                        )
                    )
                ) {
                    Text(
                        text = if (isPlaying) "⏸" else "▶",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Normal
                    )
                }

                // Forward 10s
                PlayerControlButton(text = "⟳", label = "+10s", onClick = onSeekForward)
            }
        }
    }
}

@Composable
private fun PlayerControlButton(text: String, label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.colors(
            containerColor = TextPrimary.copy(alpha = 0.08f),
            contentColor = TextPrimary,
            focusedContainerColor = AccentGold.copy(alpha = 0.2f),
            focusedContentColor = TextPrimary
        ),
        shape = ButtonDefaults.shape(shape = DreamShapes.Sharp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = text, style = MaterialTheme.typography.titleMedium)
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        }
    }
}

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
