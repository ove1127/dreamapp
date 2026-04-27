@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)

package com.dreamweddingstories.tv.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
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
import androidx.tv.material3.MaterialTheme
import coil.compose.AsyncImage
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.dreamweddingstories.tv.components.HeroBanner
import com.dreamweddingstories.tv.components.PrimaryButton
import com.dreamweddingstories.tv.components.LoadingShimmer
import com.dreamweddingstories.tv.components.WeddingVideoCard
import com.dreamweddingstories.tv.model.UiState
import com.dreamweddingstories.tv.model.User
import com.dreamweddingstories.tv.model.WeddingVideo
import com.dreamweddingstories.tv.ui.theme.AccentGold
import com.dreamweddingstories.tv.ui.theme.CardBackground
import com.dreamweddingstories.tv.ui.theme.DarkBackground
import com.dreamweddingstories.tv.ui.theme.DreamAnimation
import com.dreamweddingstories.tv.ui.theme.DreamShapes
import com.dreamweddingstories.tv.ui.theme.ErrorAmber
import com.dreamweddingstories.tv.ui.theme.GoldDivider
import com.dreamweddingstories.tv.ui.theme.TextPrimary
import com.dreamweddingstories.tv.ui.theme.TextSecondary
import com.dreamweddingstories.tv.ui.theme.TextTertiary

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
                LoadingShimmer()
            }

            is UiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Text(
                            text = videosState.message,
                            color = ErrorAmber,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        PrimaryButton(
                            text = "Retry",
                            onClick = onRetry,
                            modifier = Modifier.width(180.dp)
                        )
                    }
                }
            }

            is UiState.Success -> {
                val videos = videosState.data
                val featured = videos.firstOrNull { it.category == "Trailer" } ?: videos.firstOrNull()

                if (featured != null) {
                    var focusedVideo by remember { mutableStateOf(featured) }

                    // ── Cinematic dynamic background ──
                    Box(modifier = Modifier.fillMaxSize()) {
                        Crossfade(
                            targetState = focusedVideo.thumbnailUrl,
                            animationSpec = tween(1200, easing = DreamAnimation.SilkEasing),
                            label = "dynamic_bg"
                        ) { url ->
                            AsyncImage(
                                model = url,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .alpha(0.25f)
                                    .blur(100.dp)
                            )
                        }

                        // Warm-black gradient scrim (not cool grey)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            DarkBackground.copy(alpha = 0.6f),
                                            DarkBackground.copy(alpha = 0.9f),
                                            DarkBackground
                                        )
                                    )
                                )
                        )
                    }

                    // ── Foreground content ──
                    TvLazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 64.dp)
                    ) {
                        // ── Top bar ──
                        item {
                            TopBar(user = user, onLogout = onLogout)
                        }

                        // ── Hero banner ──
                        item {
                            HeroBanner(
                                video = featured,
                                onPlayNow = { onVideoSelected(featured.id) },
                                onDetails = { onVideoSelected(featured.id) },
                                onFocus = { focusedVideo = featured },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 40.dp)
                            )
                            Spacer(modifier = Modifier.height(40.dp))
                        }

                        // ── Categorized rows ──
                        val mainFilms = videos.filter { it.category == "Main Film" }
                        val reels = videos.filter { it.category == "Reel" }
                        val uncategorized = videos.filter {
                            it.category != "Trailer" && it.category != "Main Film" && it.category != "Reel"
                        }
                        val isCategorized = mainFilms.isNotEmpty() || reels.isNotEmpty()

                        if (isCategorized) {
                            if (mainFilms.isNotEmpty()) {
                                item {
                                    VideoRow(
                                        title = "Your Cinematic Films",
                                        videos = mainFilms,
                                        onVideoSelected = onVideoSelected,
                                        onFocus = { focusedVideo = it }
                                    )
                                    Spacer(modifier = Modifier.height(36.dp))
                                }
                            }
                            if (reels.isNotEmpty()) {
                                item {
                                    VideoRow(
                                        title = "Social Media Reels",
                                        videos = reels,
                                        onVideoSelected = onVideoSelected,
                                        onFocus = { focusedVideo = it }
                                    )
                                    Spacer(modifier = Modifier.height(36.dp))
                                }
                            }
                            if (uncategorized.isNotEmpty()) {
                                item {
                                    VideoRow(
                                        title = "More Memories",
                                        videos = uncategorized,
                                        onVideoSelected = onVideoSelected,
                                        onFocus = { focusedVideo = it }
                                    )
                                }
                            }
                        } else {
                            item {
                                VideoRow(
                                    title = "Your Wedding Films",
                                    videos = videos,
                                    onVideoSelected = onVideoSelected,
                                    onFocus = { focusedVideo = it }
                                )
                                Spacer(modifier = Modifier.height(32.dp))
                            }
                            item {
                                VideoRow(
                                    title = "Cinematic Highlights",
                                    videos = videos.shuffled(),
                                    onVideoSelected = onVideoSelected,
                                    onFocus = { focusedVideo = it }
                                )
                                Spacer(modifier = Modifier.height(32.dp))
                            }
                            item {
                                VideoRow(
                                    title = "Trending Ceremonies",
                                    videos = videos.shuffled(),
                                    onVideoSelected = onVideoSelected,
                                    onFocus = { focusedVideo = it }
                                )
                                Spacer(modifier = Modifier.height(32.dp))
                            }
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
            }

            UiState.Idle -> Unit
        }
    }
}

/* ──────────────────────────────────────────────────────────────
   Video Row — section label with gold left-border accent
   ────────────────────────────────────────────────────────────── */
@Composable
private fun VideoRow(
    title: String,
    videos: List<WeddingVideo>,
    onVideoSelected: (String) -> Unit,
    onFocus: (WeddingVideo) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Section label
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 48.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }

        // ── Horizontal card row ──
        TvLazyRow(
            contentPadding = PaddingValues(horizontal = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(videos, key = { it.id }) { video ->
                WeddingVideoCard(
                    video = video,
                    onClick = { onVideoSelected(video.id) },
                    onFocus = { onFocus(video) }
                )
            }
        }
    }
}

/* ──────────────────────────────────────────────────────────────
   Top Bar — ultra-minimal, logo left, user avatar right
   ────────────────────────────────────────────────────────────── */
@Composable
private fun TopBar(user: User, onLogout: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Company Logo
        AsyncImage(
            model = "https://res.cloudinary.com/doy1jic8d/image/upload/v1774727598/dream-wedding-assets/umnjmt9ucxwled1c3rq4.png",
            contentDescription = "Dream Wedding Stories",
            modifier = Modifier.height(28.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.weight(1f))

        // User initial avatar — white ring border
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.DarkGray)
                .border(1.dp, Color.White, CircleShape)
        ) {
            val initial = (user.displayName.ifBlank { user.accessCode }).take(1).uppercase()
            Text(
                text = initial,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = user.displayName.ifBlank { user.accessCode },
            color = TextSecondary,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.width(20.dp))

        Button(
            onClick = onLogout,
            colors = ButtonDefaults.colors(
                containerColor = Color.Transparent,
                contentColor = TextSecondary,
                focusedContainerColor = Color.White.copy(alpha = 0.2f),
                focusedContentColor = Color.White
            ),
            shape = ButtonDefaults.shape(shape = DreamShapes.Sharp)
        ) {
            Text("LOGOUT", style = MaterialTheme.typography.labelSmall)
        }
    }
}
