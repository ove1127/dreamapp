package com.dreamweddingstories.tv.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

// ═══════════════════════════════════════════════════════
// CINEMATIC LUXURY EDITORIAL — Shapes
// ═══════════════════════════════════════════════════════
// Luxury brands use sharp corners. No rounded edges.

object DreamShapes {
    /** Standard corners — used for buttons, cards, containers */
    val Sharp = RoundedCornerShape(4.dp)

    /** Minimal radius — used for input fields only */
    val Subtle = RoundedCornerShape(2.dp)

    /** Small radius — used sparingly for badges */
    val Badge = RoundedCornerShape(3.dp)
}
