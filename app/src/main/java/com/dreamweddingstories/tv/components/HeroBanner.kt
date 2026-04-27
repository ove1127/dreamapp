@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)

package com.dreamweddingstories.tv.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.focus.onFocusChanged
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
import com.dreamweddingstories.tv.model.WeddingVideo
import com.dreamweddingstories.tv.ui.theme.AccentGold
import com.dreamweddingstories.tv.ui.theme.DarkBackground
import com.dreamweddingstories.tv.ui.theme.DreamAnimation
import com.dreamweddingstories.tv.ui.theme.TextPrimary
import com.dreamweddingstories.tv.ui.theme.TextSecondary
import com.dreamweddingstories.tv.ui.theme.TextTertiary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Full-bleed hero banner — cinematic editorial showcase.
 *
 * Background: blurred + darkened thumbnail with radial vignette.
 * Content: bottom-left anchored with staggered fade-up entrance.
 * Features gold label, enormous couple names, date, location, gold rule, buttons.
 */
@Composable
fun HeroBanner(
    video: WeddingVideo,
    onPlayNow: () -> Unit,
    onDetails: (() -> Unit)? = null,
    onFocus: () -> Unit,
    modifier: Modifier = Modifier
) {
    // ── Staggered entrance animations ──
    val labelAlpha = remember { Animatable(0f) }
    val nameAlpha = remember { Animatable(0f) }
    val nameOffset = remember { Animatable(20f) }
    val metaAlpha = remember { Animatable(0f) }
    val ruleAlpha = remember { Animatable(0f) }
    val buttonAlpha = remember { Animatable(0f) }

    LaunchedEffect(video.id) {
        // Reset
        labelAlpha.snapTo(0f); nameAlpha.snapTo(0f); nameOffset.snapTo(20f)
        metaAlpha.snapTo(0f); ruleAlpha.snapTo(0f); buttonAlpha.snapTo(0f)

        delay(300) // Initial hold

        launch { labelAlpha.animateTo(1f, DreamAnimation.silkTween(DreamAnimation.SLOW)) }
        delay(DreamAnimation.STAGGER_DELAY.toLong())

        launch { nameAlpha.animateTo(1f, DreamAnimation.silkTween(DreamAnimation.SLOW)) }
        launch { nameOffset.animateTo(0f, DreamAnimation.silkTween(DreamAnimation.SLOW)) }
        delay(DreamAnimation.STAGGER_DELAY.toLong())

        launch { metaAlpha.animateTo(1f, DreamAnimation.silkTween()) }
        delay(DreamAnimation.STAGGER_DELAY.toLong())

        launch { ruleAlpha.animateTo(1f, DreamAnimation.silkTween()) }
        delay(DreamAnimation.STAGGER_DELAY.toLong())

        launch { buttonAlpha.animateTo(1f, DreamAnimation.silkTween()) }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(420.dp)
    ) {
        if (video.vimeoVideoId.isNotBlank()) {
            VimeoPreview(
                vimeoVideoId = video.vimeoVideoId,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.6f)
            )
        } else {
            AsyncImage(
                model = video.thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(60.dp)
                    .alpha(0.35f)
            )
        }

        // ── Radial vignette (warm-black, not cool grey) ──
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            DarkBackground.copy(alpha = 0.3f),
                            DarkBackground.copy(alpha = 0.7f),
                            DarkBackground.copy(alpha = 0.95f)
                        ),
                        radius = 1200f
                    )
                )
        )

        // ── Bottom scrim ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, DarkBackground)
                    )
                )
        )

        // ── Left-side content (bottom-left anchored) ──
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 48.dp, bottom = 40.dp, end = 200.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Premium film badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.alpha(labelAlpha.value)
            ) {
                Text(
                    text = "S I G N A T U R E   F I L M",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Couple names — enormous Cormorant Garamond
            Text(
                text = video.coupleNames,
                style = MaterialTheme.typography.displayLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Light,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .alpha(nameAlpha.value)
                    .padding(top = nameOffset.value.dp) // Note: dp conversion is approximate
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Wedding date + location
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.alpha(metaAlpha.value)
            ) {
                if (video.weddingDate.isNotBlank()) {
                    Text(
                        text = video.weddingDate,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AccentGold.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Light
                    )
                }
                if (video.description.isNotBlank()) {
                    Text(
                        text = "·",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextTertiary
                    )
                    Text(
                        text = video.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.alpha(buttonAlpha.value)
            ) {
                PrimaryButton(
                    text = "▶  Play",
                    onClick = onPlayNow,
                    modifier = Modifier
                        .width(180.dp)
                        .onFocusChanged { if (it.isFocused || it.hasFocus) onFocus() }
                )

                if (onDetails != null) {
                    SecondaryButton(
                        text = "More Info",
                        onClick = onDetails,
                        modifier = Modifier.width(160.dp)
                    )
                }
            }
        }
    }
}
