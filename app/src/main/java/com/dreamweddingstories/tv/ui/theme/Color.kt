package com.dreamweddingstories.tv.ui.theme

import androidx.compose.ui.graphics.Color

// ═══════════════════════════════════════════════════════
// NETFLIX AESTHETIC — Color Tokens
// ═══════════════════════════════════════════════════════

// ── Backgrounds ──
val DarkBackground = Color(0xFF000000)          // Pure Black
val SurfaceDark = Color(0xFF141414)             // Netflix Dark Grey
val CardBackground = Color(0xFF181818)          // Elevated surface
val CardBackgroundElevated = Color(0xFF202020)  // Higher elevation

// ── Accent ──
val AccentGold = Color(0xFFFFFFFF)              // Pure White for accents
val AccentGoldDeep = Color(0xFFE5E5E5)          // Hover / pressed state for white
val AccentGoldSubtle = Color(0x33FFFFFF)        // 20% white
val AccentGoldGlow = Color(0x40FFFFFF)          // 25% white glow

// ── Text ──
val TextPrimary = Color(0xFFFFFFFF)             // Pure White
val TextSecondary = Color(0xFFB3B3B3)           // Light Grey
val TextTertiary = Color(0xFF808080)            // Medium Grey

// ── Focus ──
val FocusGold = Color(0xFFFFFFFF)               // Pure White ring
val FocusDimOverlay = Color(0x99000000)         // 60% black — dims siblings

// ── Feedback ──
val ErrorAmber = Color(0xFFE50914)              // Netflix Red for errors
val SuccessGold = Color(0xFF2ECC71)             // Standard Green

// ── Overlays & Scrims ──
val SurfaceDim = Color(0xD9000000)              // 85% alpha
val GradientOverlay = Color(0xE6000000)         // 90% alpha
val PlayerControlBg = Color(0xB3000000)         // 70% alpha

// ── Decorative ──
val GoldDivider = Color(0x33FFFFFF)             // Subtle white divider
val GoldShimmer = Color(0x80FFFFFF)             // White shimmer highlight
val WarmBorder = Color(0x33FFFFFF)              // White border

// ── Legacy aliases (keep imports working during migration) ──
@Deprecated("Use AccentGold", replaceWith = ReplaceWith("AccentGold"))
val AccentWhite = AccentGold
@Deprecated("Use GoldDivider", replaceWith = ReplaceWith("GoldDivider"))
val DividerWhite = GoldDivider
@Deprecated("Use GoldShimmer", replaceWith = ReplaceWith("GoldShimmer"))
val ShimmerWhite = GoldShimmer
@Deprecated("Use FocusGold", replaceWith = ReplaceWith("FocusGold"))
val FocusGlowWhite = FocusGold
@Deprecated("Use ErrorAmber", replaceWith = ReplaceWith("ErrorAmber"))
val ErrorRed = ErrorAmber
