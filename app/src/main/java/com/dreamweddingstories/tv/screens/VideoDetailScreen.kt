@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)

package com.dreamweddingstories.tv.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.dreamweddingstories.tv.model.UiState
import com.dreamweddingstories.tv.model.WeddingVideo
import com.dreamweddingstories.tv.ui.theme.CardBackground
import com.dreamweddingstories.tv.ui.theme.DarkBackground
import com.dreamweddingstories.tv.ui.theme.SurfaceDark
import com.dreamweddingstories.tv.ui.theme.ErrorRed
import com.dreamweddingstories.tv.ui.theme.AccentWhite
import com.dreamweddingstories.tv.ui.theme.TextPrimary
import com.dreamweddingstories.tv.ui.theme.TextSecondary

@Composable
fun VideoDetailScreen(
    state: UiState<WeddingVideo>,
    onBack: () -> Unit,
    onPlay: (WeddingVideo) -> Unit,
    onRetry: () -> Unit
) {
    BackHandler(onBack = onBack)

    val playFocusRequester = remember { FocusRequester() }
    val contentVisible = remember { mutableStateOf(false) }

    // Trigger entrance animation
    LaunchedEffect(state) {
        if (state is UiState.Success) {
            contentVisible.value = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(SurfaceDark, DarkBackground))
            )
    ) {
        when (state) {
            UiState.Loading -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(color = AccentWhite, strokeWidth = 3.dp)
                    Text(
                        text = "Loading details...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                }
            }

            is UiState.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(text = state.message, color = ErrorRed, style = MaterialTheme.typography.bodyLarge)
                    Button(
                        onClick = onRetry,
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

            is UiState.Success -> {
                val video = state.data

                // ── Blurred background thumbnail ──
                AsyncImage(
                    model = video.thumbnailUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .matchParentSize()
                        .blur(20.dp)
                )

                // Multi-layer overlay for depth
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color(0xBB0D0D0D))
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xCC1A1A2E), Color.Transparent)
                            )
                        )
                )

                // Auto-focus play button when content loads but wait for animation
                LaunchedEffect(contentVisible.value) {
                    if (contentVisible.value) {
                        try {
                            kotlinx.coroutines.delay(300) // Wait for entrance animation
                            playFocusRequester.requestFocus()
                        } catch (e: IllegalStateException) {
                            // Ignored if still not initialized
                        }
                    }
                }

                // ── Content ──
                AnimatedVisibility(
                    visible = contentVisible.value,
                    enter = fadeIn(tween(500)) + slideInVertically(
                        initialOffsetY = { it / 8 },
                        animationSpec = tween(600)
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 80.dp, bottom = 64.dp, top = 64.dp, end = 80.dp)
                    ) {
                        // ── Info panel on the Left ──
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(0.5f)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Couple name
                            Text(
                                text = video.coupleNames,
                                style = MaterialTheme.typography.displayMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // ── Metadata chips ──
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                MetadataChip(label = "Date", value = video.weddingDate)
                                MetadataChip(label = "Duration", value = video.duration)
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Description
                            if (video.description.isNotBlank()) {
                                Text(
                                    text = video.description,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = TextSecondary,
                                    lineHeight = MaterialTheme.typography.headlineSmall.lineHeight
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(64.dp))

                            // ── Action buttons ──
                            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                                Button(
                                    onClick = { onPlay(video) },
                                    modifier = Modifier
                                        .height(64.dp)
                                        .focusRequester(playFocusRequester),
                                    colors = ButtonDefaults.colors(
                                        containerColor = AccentWhite,
                                        contentColor = DarkBackground,
                                        focusedContainerColor = AccentWhite,
                                        focusedContentColor = DarkBackground
                                    ),
                                    shape = ButtonDefaults.shape(shape = RoundedCornerShape(4.dp)),
                                    border = ButtonDefaults.border(
                                        focusedBorder = androidx.tv.material3.Border(
                                            border = androidx.compose.foundation.BorderStroke(2.dp, AccentWhite),
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                    ),
                                    glow = ButtonDefaults.glow(
                                        focusedGlow = androidx.tv.material3.Glow(
                                            elevationColor = AccentWhite.copy(alpha = 0.5f),
                                            elevation = 20.dp
                                        )
                                    )
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxHeight()) {
                                        Text(
                                            text = "▶  WATCH NOW",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Button(
                                    onClick = onBack,
                                    modifier = Modifier.height(64.dp),
                                    colors = ButtonDefaults.colors(
                                        containerColor = Color.Transparent,
                                        contentColor = TextPrimary,
                                        focusedContainerColor = TextPrimary.copy(alpha = 0.2f),
                                        focusedContentColor = TextPrimary
                                    ),
                                    shape = ButtonDefaults.shape(shape = RoundedCornerShape(4.dp)),
                                    border = ButtonDefaults.border(
                                        focusedBorder = androidx.tv.material3.Border(
                                            border = androidx.compose.foundation.BorderStroke(1.dp, TextPrimary.copy(alpha=0.5f)),
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                    )
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxHeight()) {
                                        Text(
                                            text = "←  GO BACK",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            UiState.Idle -> Unit
        }
    }
}

/* ──────────────────────────────────────────────────────────────
   Metadata Chip — styled label + value
   ────────────────────────────────────────────────────────────── */
@Composable
private fun MetadataChip(label: String, value: String) {
    if (value.isBlank()) return

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(CardBackground.copy(alpha = 0.8f))
            .border(1.dp, AccentWhite.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = AccentWhite
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}
