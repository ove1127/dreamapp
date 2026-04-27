package com.dreamweddingstories.tv.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut

// ═══════════════════════════════════════════════════════
// CINEMATIC LUXURY EDITORIAL — Animation Tokens
// ═══════════════════════════════════════════════════════
// All motion flows like silk. No snap, no bounce.

object DreamAnimation {

    /** The signature easing — fast decelerate, slow settle */
    val SilkEasing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

    // ── Durations ──
    const val FAST = 300
    const val STANDARD = 600
    const val SLOW = 800
    const val SCREEN_TRANSITION = 800
    const val SPLASH_RING = 2000
    const val STAGGER_DELAY = 150

    // ── Reusable tween specs ──
    fun <T> silkTween(durationMillis: Int = STANDARD, delayMillis: Int = 0) =
        tween<T>(durationMillis = durationMillis, delayMillis = delayMillis, easing = SilkEasing)

    fun <T> fastSilkTween(delayMillis: Int = 0) =
        tween<T>(durationMillis = FAST, delayMillis = delayMillis, easing = SilkEasing)

    // ── Screen transitions — cross-dissolve only ──
    fun screenEnter(): EnterTransition = fadeIn(
        animationSpec = tween(SCREEN_TRANSITION, easing = SilkEasing)
    )

    fun screenExit(): ExitTransition = fadeOut(
        animationSpec = tween(SCREEN_TRANSITION, easing = SilkEasing)
    )

    fun playerEnter(): EnterTransition = fadeIn(
        animationSpec = tween(200, easing = SilkEasing)
    )

    fun playerExit(): ExitTransition = fadeOut(
        animationSpec = tween(200, easing = SilkEasing)
    )
}
