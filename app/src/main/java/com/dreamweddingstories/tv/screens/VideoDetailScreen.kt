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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.dreamweddingstories.tv.components.PrimaryButton
import com.dreamweddingstories.tv.components.SecondaryButton
import com.dreamweddingstories.tv.components.GoldShimmerLine
import com.dreamweddingstories.tv.model.UiState
import com.dreamweddingstories.tv.model.WeddingVideo
import com.dreamweddingstories.tv.ui.theme.AccentGold
import com.dreamweddingstories.tv.ui.theme.CardBackground
import com.dreamweddingstories.tv.ui.theme.DarkBackground
import com.dreamweddingstories.tv.ui.theme.DreamAnimation
import com.dreamweddingstories.tv.ui.theme.DreamShapes
import com.dreamweddingstories.tv.ui.theme.ErrorAmber
import com.dreamweddingstories.tv.ui.theme.GoldDivider
import com.dreamweddingstories.tv.ui.theme.SurfaceDark
import com.dreamweddingstories.tv.ui.theme.SurfaceDim
import com.dreamweddingstories.tv.ui.theme.TextPrimary
import com.dreamweddingstories.tv.ui.theme.TextSecondary
import com.dreamweddingstories.tv.ui.theme.TextTertiary
import com.dreamweddingstories.tv.ui.theme.WarmBorder

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

    LaunchedEffect(state) {
        if (state is UiState.Success) {
            contentVisible.value = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        when (state) {
            UiState.Loading -> {
                // Gold shimmer loading
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        GoldShimmerLine(modifier = Modifier.width(300.dp))
                        Text(
                            text = "LOADING DETAILS",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextTertiary
                        )
                    }
                }
            }

            is UiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = state.message,
                            color = ErrorAmber,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        PrimaryButton(
                            text = "Retry",
                            onClick = onRetry,
                            modifier = Modifier.width(180.dp)
                        )
                        SecondaryButton(
                            text = "Go Back",
                            onClick = onBack,
                            modifier = Modifier.width(180.dp)
                        )
                    }
                }
            }

            is UiState.Success -> {
                val video = state.data

                // ── Blurred background (40px blur, 85% darkened) ──
                AsyncImage(
                    model = video.thumbnailUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .matchParentSize()
                        .blur(40.dp)
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(SurfaceDim)
                )
                // Warm horizontal gradient
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    DarkBackground.copy(alpha = 0.5f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // ── Content — two-column editorial layout ──
                AnimatedVisibility(
                    visible = contentVisible.value,
                    enter = fadeIn(DreamAnimation.silkTween(DreamAnimation.SLOW)) +
                            slideInVertically(
                                initialOffsetY = { it / 12 },
                                animationSpec = DreamAnimation.silkTween(DreamAnimation.SLOW)
                            )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 80.dp, vertical = 64.dp),
                        horizontalArrangement = Arrangement.spacedBy(60.dp)
                    ) {
                        // ── Left column (45%): Film poster ──
                        Column(
                            modifier = Modifier
                                .weight(0.45f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Poster with gold frame
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, GoldDivider, DreamShapes.Sharp)
                                    .padding(1.dp)
                            ) {
                                AsyncImage(
                                    model = video.thumbnailUrl,
                                    contentDescription = video.coupleNames,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(DreamShapes.Sharp)
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Metadata below poster
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (video.duration.isNotBlank()) {
                                    MetadataLabel(label = "RUNTIME", value = video.duration)
                                }
                                if (video.weddingDate.isNotBlank()) {
                                    MetadataLabel(label = "DATE", value = video.weddingDate)
                                }
                            }
                        }

                        // ── Right column (55%): Details ──
                        Column(
                            modifier = Modifier
                                .weight(0.55f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Section tag
                            Text(
                                text = "WEDDING FILM",
                                style = MaterialTheme.typography.labelMedium,
                                color = AccentGold,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Couple names — Cormorant, 52sp, thin
                            Text(
                                text = video.coupleNames,
                                style = MaterialTheme.typography.displayMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Light,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Gold rule
                            Box(
                                modifier = Modifier
                                    .width(160.dp)
                                    .height(1.dp)
                                    .background(GoldDivider)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Description
                            if (video.description.isNotBlank()) {
                                Text(
                                    text = video.description,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Light,
                                    lineHeight = 28.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(48.dp))

                            // Auto-focus play button
                            LaunchedEffect(Unit) {
                                kotlinx.coroutines.delay(700)
                                try { playFocusRequester.requestFocus() } catch (_: Exception) { }
                            }

                            // ── Action buttons — sharp corners ──
                            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                                PrimaryButton(
                                    text = "▶  Watch Now",
                                    onClick = { onPlay(video) },
                                    modifier = Modifier
                                        .height(60.dp)
                                        .width(220.dp)
                                        .focusRequester(playFocusRequester)
                                )

                                SecondaryButton(
                                    text = "←  Go Back",
                                    onClick = onBack,
                                    modifier = Modifier
                                        .height(60.dp)
                                        .width(180.dp)
                                )
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
   Metadata Label — Inter Light caps, gold accent
   ────────────────────────────────────────────────────────────── */
@Composable
private fun MetadataLabel(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = AccentGold.copy(alpha = 0.6f),
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Light
        )
    }
}
