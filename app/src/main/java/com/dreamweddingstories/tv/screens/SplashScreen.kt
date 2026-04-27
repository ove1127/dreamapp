@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)

package com.dreamweddingstories.tv.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.dreamweddingstories.tv.ui.theme.AccentGold
import com.dreamweddingstories.tv.ui.theme.DarkBackground
import com.dreamweddingstories.tv.ui.theme.DreamAnimation
import com.dreamweddingstories.tv.ui.theme.GoldDivider
import com.dreamweddingstories.tv.ui.theme.TextPrimary
import com.dreamweddingstories.tv.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Splash Screen — Cinematic Luxury Editorial
 *
 * Full black (#080B14). Centre: thin gold wedding band ring
 * draws itself in 2s stroke animation. Wordmark fades in below.
 * Hold 1.5s. Cross-dissolve to Login.
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    // ── Ring stroke animation (0 → 360 degrees) ──
    val ringSweep = remember { Animatable(0f) }

    // ── Fade-in animations ──
    val ringAlpha = remember { Animatable(0f) }
    val wordmarkAlpha = remember { Animatable(0f) }
    val ruleAlpha = remember { Animatable(0f) }
    val taglineAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Ring draws itself
        launch {
            ringAlpha.animateTo(1f, tween(400, easing = DreamAnimation.SilkEasing))
        }
        launch {
            ringSweep.animateTo(
                360f,
                tween(DreamAnimation.SPLASH_RING, easing = LinearEasing)
            )
        }

        // After ring completes, wordmark appears
        delay(DreamAnimation.SPLASH_RING.toLong() + 200)

        launch {
            wordmarkAlpha.animateTo(1f, DreamAnimation.silkTween(DreamAnimation.SLOW))
        }

        delay(DreamAnimation.STAGGER_DELAY.toLong() * 2)

        launch {
            ruleAlpha.animateTo(1f, DreamAnimation.silkTween())
        }

        delay(DreamAnimation.STAGGER_DELAY.toLong())

        launch {
            taglineAlpha.animateTo(1f, DreamAnimation.silkTween())
        }

        // Hold 1.5s then navigate
        delay(1500)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ── Wedding band ring — thin gold stroke animation ──
            Canvas(
                modifier = Modifier
                    .size(80.dp)
                    .alpha(ringAlpha.value)
            ) {
                drawArc(
                    color = AccentGold,
                    startAngle = -90f,
                    sweepAngle = ringSweep.value,
                    useCenter = false,
                    style = Stroke(width = 1.5f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Wordmark ──
            Text(
                text = "DREAM WEDDING STORIES",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Center,
                letterSpacing = 6.sp,
                modifier = Modifier.alpha(wordmarkAlpha.value)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── Gold horizontal rule ──
            Box(
                modifier = Modifier
                    .alpha(ruleAlpha.value)
                    .width(120.dp)
                    .height(1.dp)
                    .background(GoldDivider)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Tagline ──
            Text(
                text = "YOUR LOVE STORY  ·  BEAUTIFULLY PRESERVED",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(taglineAlpha.value)
            )
        }
    }
}
