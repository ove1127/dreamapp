@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)

package com.dreamweddingstories.tv.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dreamweddingstories.tv.ui.theme.CardBackground
import com.dreamweddingstories.tv.ui.theme.DreamShapes
import com.dreamweddingstories.tv.ui.theme.GoldShimmer
import com.dreamweddingstories.tv.ui.theme.SurfaceDark

/**
 * Gold shimmer effect — a thin horizontal line that sweeps
 * across like a film strip scanning. Replaces all spinners.
 */
@Composable
fun GoldShimmerLine(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "goldShimmer")
    val offsetX by transition.animateFloat(
        initialValue = -300f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOffset"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(SurfaceDark)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            SurfaceDark,
                            GoldShimmer.copy(alpha = 0.6f),
                            GoldShimmer,
                            GoldShimmer.copy(alpha = 0.6f),
                            SurfaceDark
                        ),
                        start = Offset(offsetX, 0f),
                        end = Offset(offsetX + 300f, 0f)
                    )
                )
        )
    }
}

/**
 * Skeleton card — dark elevated surface with slow gold shimmer sweep.
 * Use in place of cards while content loads.
 */
@Composable
fun SkeletonCard(
    modifier: Modifier = Modifier,
    width: Dp = 300.dp
) {
    val transition = rememberInfiniteTransition(label = "skeletonShimmer")
    val offsetX by transition.animateFloat(
        initialValue = -400f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "skeletonOffset"
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            CardBackground,
            GoldShimmer.copy(alpha = 0.15f),
            CardBackground
        ),
        start = Offset(offsetX, 0f),
        end = Offset(offsetX + 400f, 0f)
    )

    Column(
        modifier = modifier.width(width).clip(DreamShapes.Sharp)
    ) {
        // Thumbnail skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(shimmerBrush)
        )

        // Info skeleton
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBackground)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Title line
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(14.dp)
                    .clip(DreamShapes.Badge)
                    .background(shimmerBrush)
            )
            // Date line
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(10.dp)
                    .clip(DreamShapes.Badge)
                    .background(shimmerBrush)
            )
        }
    }
}

/**
 * Loading state for the home screen — shimmer line + skeleton cards.
 */
@Composable
fun LoadingShimmer(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(48.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        // Shimmer line at top
        GoldShimmerLine(modifier = Modifier.padding(horizontal = 48.dp))

        Spacer(modifier = Modifier.height(24.dp))

        // Skeleton card row
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(horizontal = 48.dp)
        ) {
            repeat(4) {
                SkeletonCard()
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Second row
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(horizontal = 48.dp)
        ) {
            repeat(4) {
                SkeletonCard()
            }
        }
    }
}
