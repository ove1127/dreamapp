@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)

package com.dreamweddingstories.tv.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.items
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.dreamweddingstories.tv.model.UiState
import com.dreamweddingstories.tv.model.User
import com.dreamweddingstories.tv.model.WeddingVideo
import com.dreamweddingstories.tv.ui.theme.CardBackground
import com.dreamweddingstories.tv.ui.theme.DarkBackground
import com.dreamweddingstories.tv.ui.theme.SurfaceDark
import com.dreamweddingstories.tv.ui.theme.ErrorRed
import com.dreamweddingstories.tv.ui.theme.FocusGlowWhite
import com.dreamweddingstories.tv.ui.theme.AccentWhite
import com.dreamweddingstories.tv.ui.theme.TextPrimary
import com.dreamweddingstories.tv.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    user: User,
    videosState: UiState<List<WeddingVideo>>,
    onVideoSelected: (String) -> Unit,
    onLogout: () -> Unit,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        when (videosState) {
            UiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(color = AccentWhite, strokeWidth = 3.dp)
                        Text(
                            text = "Loading your cinematic memories...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary
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
                            text = videosState.message,
                            color = ErrorRed,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Button(
                            onClick = onRetry,
                            colors = ButtonDefaults.colors(
                                containerColor = AccentWhite,
                                contentColor = DarkBackground
                            )
                        ) {
                            Text("Retry", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            is UiState.Success -> {
                val videos = videosState.data
                val featured = videos.firstOrNull()

                if (featured != null) {
                    var focusedVideo by remember { mutableStateOf(featured) }

                    // Cinematic Dynamic Background
                    Box(modifier = Modifier.fillMaxSize()) {
                        Crossfade(
                            targetState = focusedVideo.thumbnailUrl,
                            animationSpec = tween(1200),
                            label = "dynamic_bg"
                        ) { url ->
                            AsyncImage(
                                model = url,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .alpha(0.4f)
                                    .blur(100.dp)
                            )
                        }
                        
                        // Scrim to ensure content is always readable
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            DarkBackground.copy(alpha = 0.7f),
                                            DarkBackground.copy(alpha = 0.95f),
                                            DarkBackground
                                        )
                                    )
                                )
                        )
                    }

                    // Foreground Content
                    TvLazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 64.dp)
                    ) {
                        // ── Top Bar ──
                        item {
                            TopBar(user = user, onLogout = onLogout)
                        }

                        // ── Hero Banner ──
                        item {
                            HeroBanner(
                                video = featured,
                                onPlayNow = { onVideoSelected(featured.id) },
                                onFocus = { focusedVideo = featured },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 40.dp)
                            )
                            Spacer(modifier = Modifier.height(32.dp))
                        }

                        // ── Row 1: Your Wedding Films ──
                        item {
                            VideoRow(
                                title = "Your Wedding Films",
                                videos = videos,
                                onVideoSelected = onVideoSelected,
                                onFocus = { focusedVideo = it }
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        // ── Row 2: Cinematic Highlights ──
                        item {
                            // Dummy shuffle to create a distinct row look without backend changes
                            VideoRow(
                                title = "Cinematic Highlights",
                                videos = videos.shuffled(),
                                onVideoSelected = onVideoSelected,
                                onFocus = { focusedVideo = it }
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                        
                        // ── Row 3: Trending Ceremonies ──
                        item {
                            VideoRow(
                                title = "Trending Ceremonies",
                                videos = videos.shuffled(),
                                onVideoSelected = onVideoSelected,
                                onFocus = { focusedVideo = it }
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        // ── Row 4: Recently Added ──
                        item {
                            VideoRow(
                                title = "Recently Added",
                                videos = videos.reversed(),
                                onVideoSelected = onVideoSelected,
                                onFocus = { focusedVideo = it }
                            )
                        }
                    }
                }
            }

            UiState.Idle -> Unit
        }
    }
}

@Composable
private fun VideoRow(
    title: String,
    videos: List<WeddingVideo>,
    onVideoSelected: (String) -> Unit,
    onFocus: (WeddingVideo) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // ── Section Title ──
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = AccentWhite.copy(alpha = 0.9f),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 48.dp)
        )

        // ── Horizontal video row ──
        TvLazyRow(
            contentPadding = PaddingValues(horizontal = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(videos, key = { it.id }) { video ->
                VideoCard(
                    video = video,
                    onClick = { onVideoSelected(video.id) },
                    onFocus = { onFocus(video) }
                )
            }
        }
    }
}

/* ──────────────────────────────────────────────────────────────
   Top App Bar — logo left, user info + logout right
   ────────────────────────────────────────────────────────────── */
@Composable
private fun TopBar(user: User, onLogout: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp, vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo
        Text(
            text = "Dream Wedding Stories",
            style = MaterialTheme.typography.headlineSmall,
            color = AccentWhite,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.weight(1f))

        // User avatar circle
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(AccentWhite.copy(alpha = 0.2f))
                .border(1.dp, AccentWhite.copy(alpha = 0.5f), CircleShape)
        ) {
            val initial = (user.displayName.ifBlank { user.email }).take(1).uppercase()
            Text(
                text = initial,
                style = MaterialTheme.typography.labelLarge,
                color = AccentWhite,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = user.displayName.ifBlank { user.email },
            color = AccentWhite.copy(alpha = 0.8f),
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.width(24.dp))

        Button(
            onClick = onLogout,
            colors = ButtonDefaults.colors(
                containerColor = Color.Transparent,
                contentColor = AccentWhite.copy(alpha = 0.6f),
                focusedContainerColor = AccentWhite.copy(alpha = 0.15f),
                focusedContentColor = AccentWhite
            ),
            shape = ButtonDefaults.shape(shape = RoundedCornerShape(10.dp))
        ) {
            Text("Logout", style = MaterialTheme.typography.labelLarge)
        }
    }
}

/* ──────────────────────────────────────────────────────────────
   Hero Banner — full-bleed featured video
   ────────────────────────────────────────────────────────────── */
@Composable
private fun HeroBanner(
    video: WeddingVideo,
    onPlayNow: () -> Unit,
    onFocus: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(380.dp)
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, AccentWhite.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
    ) {
        // Background thumbnail
        AsyncImage(
            model = video.thumbnailUrl,
            contentDescription = video.coupleNames,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Horizontal gradient scrim (left-to-right)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFA000000), // Heavy black on left
                            Color(0xCC000000),
                            Color(0x80000000),
                            Color.Transparent
                        )
                    )
                )
        )

        // Bottom scrim for text readability
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xCC000000), Color(0xFA000000))
                    )
                )
        )

        // Content overlay
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .padding(start = 48.dp, top = 48.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Wedding date badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(AccentWhite)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "NEW RELEASE",
                        style = MaterialTheme.typography.labelSmall,
                        color = DarkBackground,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = video.coupleNames,
                    style = MaterialTheme.typography.displayMedium,
                    color = AccentWhite,
                    fontWeight = FontWeight.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )

                if (video.description.isNotBlank()) {
                    Text(
                        text = video.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = AccentWhite.copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(0.5f)
                    )
                }
            }

            // Action buttons
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = onPlayNow,
                    modifier = Modifier.onFocusChanged { if (it.isFocused || it.hasFocus) onFocus() },
                    colors = ButtonDefaults.colors(
                        containerColor = AccentWhite,
                        contentColor = DarkBackground,
                        focusedContainerColor = AccentWhite,
                        focusedContentColor = DarkBackground
                    ),
                    shape = ButtonDefaults.shape(shape = RoundedCornerShape(12.dp)),
                    glow = ButtonDefaults.glow(
                        focusedGlow = androidx.tv.material3.Glow(
                            elevationColor = AccentWhite.copy(alpha = 0.5f),
                            elevation = 16.dp
                        )
                    )
                ) {
                    Text(
                        text = "▶  WATCH NOW",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

/* ──────────────────────────────────────────────────────────────
   Video Card — focus-aware with scale + cinematic glow
   ────────────────────────────────────────────────────────────── */
@Composable
private fun VideoCard(video: WeddingVideo, onClick: () -> Unit, onFocus: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.08f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "cardScale"
    )

    Surface(
        onClick = onClick,
        modifier = Modifier
            .width(300.dp)
            .scale(scale)
            .onFocusChanged { 
                isFocused = it.isFocused || it.hasFocus 
                if (isFocused) onFocus()
            },
        shape = ClickableSurfaceDefaults.shape(shape = RoundedCornerShape(16.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = CardBackground,
            focusedContainerColor = CardBackground,
            contentColor = TextPrimary,
            focusedContentColor = TextPrimary
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = androidx.tv.material3.Border(
                border = androidx.compose.foundation.BorderStroke(3.dp, FocusGlowWhite),
                shape = RoundedCornerShape(16.dp)
            )
        ),
        glow = ClickableSurfaceDefaults.glow(
            focusedGlow = androidx.tv.material3.Glow(
                elevationColor = AccentWhite.copy(alpha = 0.4f),
                elevation = 20.dp
            )
        )
    ) {
        Column {
            // Thumbnail (16:9)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                AsyncImage(
                    model = video.thumbnailUrl,
                    contentDescription = video.coupleNames,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Subtly darken the thumbnail
                Box(modifier = Modifier.fillMaxSize().background(Color(0x33000000)))

                // Duration badge
                if (video.duration.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xE6000000))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = video.duration,
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Info section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0A0A0A))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = video.coupleNames,
                    style = MaterialTheme.typography.titleMedium,
                    color = AccentWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = video.weddingDate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AccentWhite.copy(alpha = 0.6f),
                    maxLines = 1
                )
            }
        }
    }
}
