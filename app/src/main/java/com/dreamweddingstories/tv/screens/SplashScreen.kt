@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)

package com.dreamweddingstories.tv.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.dreamweddingstories.tv.ui.theme.DarkBackground
import com.dreamweddingstories.tv.ui.theme.SurfaceDark
import com.dreamweddingstories.tv.ui.theme.DividerWhite
import com.dreamweddingstories.tv.ui.theme.AccentWhite
import com.dreamweddingstories.tv.ui.theme.TextPrimary
import com.dreamweddingstories.tv.ui.theme.TextSecondary
import com.dreamweddingstories.tv.utils.Constants
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    // ── Fade + Scale entrance animation ──
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.85f) }
    val subtitleAlpha = remember { Animatable(0f) }
    val dividerAlpha = remember { Animatable(0f) }

    // ── Subtle shimmer pulse on White elements ──
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerAlpha = infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )

    LaunchedEffect(Unit) {
        // Staggered entrance: title fades + scales in, then divider, then subtitle
        launch {
            alpha.animateTo(1f, tween(durationMillis = 1000, easing = FastOutSlowInEasing))
        }
        launch {
            scale.animateTo(1f, tween(durationMillis = 1200, easing = FastOutSlowInEasing))
        }
        delay(600)
        launch {
            dividerAlpha.animateTo(1f, tween(durationMillis = 600))
        }
        delay(300)
        launch {
            subtitleAlpha.animateTo(1f, tween(durationMillis = 800))
        }
        delay(Constants.SPLASH_DELAY_MS)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        SurfaceDark,
                        Color(0xFF121228),
                        DarkBackground
                    ),
                    radius = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Subtle vignette overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color(0x400D0D0D)),
                        radius = 900f
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .alpha(alpha.value)
                .scale(scale.value)
                .padding(48.dp)
        ) {
            // App title
            Text(
                text = "Dream Wedding",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Stories",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Normal,
                color = AccentWhite.copy(alpha = shimmerAlpha.value),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Decorative White divider line
            Box(
                modifier = Modifier
                    .alpha(dividerAlpha.value)
                    .width(120.dp)
                    .height(2.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                AccentWhite.copy(alpha = shimmerAlpha.value),
                                Color.Transparent
                            )
                        )
                    )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Subtitle
            Text(
                text = "Premium Wedding Films",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Normal,
                color = TextSecondary.copy(alpha = subtitleAlpha.value),
                textAlign = TextAlign.Center
            )
        }
    }
}
