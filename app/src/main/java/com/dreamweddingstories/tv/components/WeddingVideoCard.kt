@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)

package com.dreamweddingstories.tv.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.dreamweddingstories.tv.model.WeddingVideo
import com.dreamweddingstories.tv.ui.theme.AccentGold
import com.dreamweddingstories.tv.ui.theme.AccentGoldGlow
import com.dreamweddingstories.tv.ui.theme.CardBackground
import com.dreamweddingstories.tv.ui.theme.DreamAnimation
import com.dreamweddingstories.tv.ui.theme.DreamShapes
import com.dreamweddingstories.tv.ui.theme.TextPrimary
import com.dreamweddingstories.tv.ui.theme.TextSecondary
import com.dreamweddingstories.tv.ui.theme.WarmBorder

/**
 * 16:9 video card with cinematic Netflix-style focus behavior.
 *
 * Focus: scale(1.06), 2px white border, title brightens.
 * Default: subtle warm border.
 * All transitions use 600ms silk easing.
 */
@Composable
fun WeddingVideoCard(
    video: WeddingVideo,
    onClick: () -> Unit,
    onFocus: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.06f else 1f,
        animationSpec = DreamAnimation.silkTween(),
        label = "cardScale"
    )

    Surface(
        onClick = onClick,
        modifier = modifier
            .width(300.dp)
            .scale(scale)
            .onFocusChanged {
                isFocused = it.isFocused || it.hasFocus
                if (isFocused) onFocus()
            },
        shape = ClickableSurfaceDefaults.shape(shape = DreamShapes.Sharp),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = CardBackground,
            focusedContainerColor = CardBackground,
            contentColor = TextPrimary,
            focusedContentColor = TextPrimary
        ),
        border = ClickableSurfaceDefaults.border(
            border = androidx.tv.material3.Border(
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Transparent),
                shape = DreamShapes.Sharp
            ),
            focusedBorder = androidx.tv.material3.Border(
                border = androidx.compose.foundation.BorderStroke(2.dp, Color.White),
                shape = DreamShapes.Sharp
            )
        ),
        glow = ClickableSurfaceDefaults.glow(
            focusedGlow = androidx.tv.material3.Glow(
                elevationColor = AccentGoldGlow,
                elevation = 20.dp
            )
        )
    ) {
        Column {
            // ── Thumbnail (16:9) ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(DreamShapes.Sharp)
            ) {
                AsyncImage(
                    model = video.thumbnailUrl,
                    contentDescription = video.coupleNames,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (isFocused && video.vimeoVideoId.isNotBlank()) {
                    VimeoPreview(
                        vimeoVideoId = video.vimeoVideoId,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Subtle darken overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x26080B14))
                )

                // Duration badge
                if (video.duration.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .clip(DreamShapes.Badge)
                            .background(Color(0xE6000000))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = video.duration,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // ── Info below image — editorial spacing ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBackground)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = video.description.ifBlank { video.category },
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isFocused) Color.White else TextPrimary.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium
                )

                Text(
                    text = video.weddingDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isFocused) Color.White else TextSecondary,
                    maxLines = 1
                )
            }
        }
    }
}
