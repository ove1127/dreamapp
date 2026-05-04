@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)

package com.dreamweddingstories.tv.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.itemsIndexed
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.dreamweddingstories.tv.components.LoadingShimmer
import com.dreamweddingstories.tv.components.VimeoPreview
import com.dreamweddingstories.tv.model.ClientEvent
import com.dreamweddingstories.tv.model.UiState
import com.dreamweddingstories.tv.model.User
import com.dreamweddingstories.tv.ui.theme.AccentGold
import com.dreamweddingstories.tv.ui.theme.CardBackground
import com.dreamweddingstories.tv.ui.theme.DarkBackground
import com.dreamweddingstories.tv.ui.theme.DreamAnimation
import com.dreamweddingstories.tv.ui.theme.ErrorAmber
import com.dreamweddingstories.tv.ui.theme.TextPrimary
import com.dreamweddingstories.tv.ui.theme.TextSecondary
import com.dreamweddingstories.tv.ui.theme.TextTertiary

/* ════════════════════════════════════════════════════════════
   Netflix-style Event / Function Selection Screen
   ════════════════════════════════════════════════════════════ */
@Composable
fun EventSelectionScreen(
    user: User,
    eventsState: UiState<List<ClientEvent>>,
    onEventSelected: (ClientEvent) -> Unit,
    onLogout: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        when (eventsState) {
            UiState.Loading -> LoadingShimmer()

            is UiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        val errorMessage = if (eventsState.message == "no_events") {
                            "No events found for this code.\nPlease contact your filmmaker."
                        } else {
                            eventsState.message
                        }

                        Text(
                            text = errorMessage,
                            color = if (eventsState.message == "no_events") TextSecondary else ErrorAmber,
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                            lineHeight = 32.sp,
                            fontWeight = FontWeight.Light
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        com.dreamweddingstories.tv.components.PrimaryButton(
                            text = "Go Back",
                            onClick = onLogout,
                            modifier = Modifier.width(200.dp)
                        )
                    }
                }
            }

            is UiState.Success -> {
                val events = eventsState.data
                var focusedIndex by remember { mutableIntStateOf(0) }
                val focusedEvent = events.getOrNull(focusedIndex)

                // ── Full-screen cinematic video background (right panel) ──
                if (focusedEvent != null && focusedEvent.previewVimeoId.isNotBlank()) {
                    AnimatedContent(
                        targetState = focusedEvent.previewVimeoId,
                        transitionSpec = {
                            fadeIn(tween(700, easing = DreamAnimation.SilkEasing)) togetherWith
                                    fadeOut(tween(400, easing = DreamAnimation.SilkEasing))
                        },
                        label = "vimeo_preview"
                    ) { vimeoId ->
                        VimeoPreview(
                            vimeoVideoId = vimeoId,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else if (focusedEvent?.thumbnailUrl?.isNotBlank() == true) {
                    Crossfade(
                        targetState = focusedEvent.thumbnailUrl,
                        animationSpec = tween(800, easing = DreamAnimation.SilkEasing),
                        label = "thumb_bg"
                    ) { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().alpha(0.5f)
                        )
                    }
                }

                // Dark gradient over the video — heavier on the left for readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    DarkBackground,
                                    DarkBackground.copy(alpha = 0.92f),
                                    DarkBackground.copy(alpha = 0.6f),
                                    Color.Transparent
                                ),
                                startX = 0f,
                                endX = 1400f
                            )
                        )
                )

                // Also a top scrim for the header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(DarkBackground.copy(alpha = 0.85f), Color.Transparent)
                            )
                        )
                )

                // ── Main Content Row ──
                Row(modifier = Modifier.fillMaxSize()) {

                    // ───────── LEFT PANEL — Event List ─────────
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(520.dp)
                            .padding(start = 56.dp, top = 0.dp, end = 32.dp, bottom = 56.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Header
                        Column(modifier = Modifier.padding(top = 52.dp)) {
                            // Logo
                            AsyncImage(
                                model = "https://res.cloudinary.com/doy1jic8d/image/upload/v1774727598/dream-wedding-assets/umnjmt9ucxwled1c3rq4.png",
                                contentDescription = "Dream Wedding Stories",
                                modifier = Modifier.height(24.dp),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.height(28.dp))

                            Text(
                                text = "Welcome back",
                                color = TextTertiary,
                                style = MaterialTheme.typography.labelMedium,
                                letterSpacing = 3.sp,
                                fontWeight = FontWeight.Light
                            )
                            Text(
                                text = user.displayName,
                                color = TextPrimary,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp,
                                lineHeight = 36.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Select a function to watch",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodyMedium,
                                fontStyle = FontStyle.Italic
                            )
                        }

                        // ── Event cards ──
                        TvLazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .padding(top = 32.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            itemsIndexed(events) { index, event ->
                                EventCard(
                                    event = event,
                                    isFocused = focusedIndex == index,
                                    onFocused = { focusedIndex = index },
                                    onClick = { onEventSelected(event) }
                                )
                            }
                        }

                        // Logout hint
                        Text(
                            text = "LOGOUT  ·  ${user.accessCode}",
                            color = TextTertiary.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 2.sp,
                            modifier = Modifier.padding(top = 24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))
                }

                // Event info overlay (bottom-right over the video) for focused event
                if (focusedEvent != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(end = 56.dp, bottom = 56.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = focusedEvent.eventName.uppercase(),
                                color = TextPrimary.copy(alpha = 0.85f),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 4.sp,
                                fontSize = 26.sp
                            )
                            if (focusedEvent.eventDate.isNotBlank()) {
                                Text(
                                    text = focusedEvent.eventDate,
                                    color = TextSecondary.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.bodyMedium,
                                    letterSpacing = 1.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${focusedEvent.videos.size} film${if (focusedEvent.videos.size != 1) "s" else ""}",
                                color = AccentGold.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.labelMedium,
                                letterSpacing = 2.sp
                            )
                        }
                    }
                }
            }

            UiState.Idle -> Unit
        }
    }
}

/* ════════════════════════════════════════════════════════════
   Individual Event Card — Netflix profile card style
   ════════════════════════════════════════════════════════════ */
@Composable
private fun EventCard(
    event: ClientEvent,
    isFocused: Boolean,
    onFocused: () -> Unit,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.04f else 1.0f,
        animationSpec = tween(250, easing = DreamAnimation.SilkEasing),
        label = "card_scale"
    )
    val borderWidth by animateDpAsState(
        targetValue = if (isFocused) 2.5.dp else 0.dp,
        animationSpec = tween(250, easing = DreamAnimation.SilkEasing),
        label = "border_width"
    )
    val bgAlpha by animateFloatAsState(
        targetValue = if (isFocused) 1f else 0.75f,
        animationSpec = tween(200),
        label = "bg_alpha"
    )

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp)
            .scale(scale)
            .border(borderWidth, Color.White, RoundedCornerShape(12.dp))
            .onFocusChanged { if (it.isFocused || it.hasFocus) onFocused() }
            .focusable(),
        shape = androidx.tv.material3.ClickableSurfaceDefaults.shape(
            shape = RoundedCornerShape(12.dp)
        ),
        colors = androidx.tv.material3.ClickableSurfaceDefaults.colors(
            containerColor = CardBackground.copy(alpha = bgAlpha),
            focusedContainerColor = CardBackground.copy(alpha = 0.95f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .size(width = 120.dp, height = 80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.DarkGray)
            ) {
                if (event.thumbnailUrl.isNotBlank()) {
                    AsyncImage(
                        model = event.thumbnailUrl,
                        contentDescription = event.eventName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                // Gradient overlay for readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, DarkBackground.copy(alpha = 0.3f))
                            )
                        )
                )
                // Play icon hint when focused
                if (isFocused) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(Color.White.copy(alpha = 0.85f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("▶", color = Color.Black, fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = event.eventName,
                    color = if (isFocused) TextPrimary else TextSecondary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (event.eventDate.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = event.eventDate,
                        color = TextTertiary,
                        style = MaterialTheme.typography.labelMedium,
                        letterSpacing = 0.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${event.videos.size} film${if (event.videos.size != 1) "s" else ""}",
                    color = if (isFocused) AccentGold.copy(alpha = 0.9f) else TextTertiary.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.sp
                )
            }

            // Right arrow indicator
            if (isFocused) {
                Text(
                    text = "›",
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Light,
                    modifier = Modifier.padding(end = 16.dp)
                )
            }
        }
    }
}
