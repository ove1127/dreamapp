@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)

package com.dreamweddingstories.tv.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.dreamweddingstories.tv.ui.theme.AccentGold
import com.dreamweddingstories.tv.ui.theme.DarkBackground
import com.dreamweddingstories.tv.ui.theme.SurfaceDark
import com.dreamweddingstories.tv.ui.theme.DreamAnimation
import com.dreamweddingstories.tv.ui.theme.DreamShapes
import com.dreamweddingstories.tv.ui.theme.TextPrimary

/**
 * Primary button. Netflix style (White background, Black text).
 * On focus: scales slightly or glows.
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = 56.dp,
    onFocusChange: ((Boolean) -> Unit)? = null
) {
    var isFocused by remember { mutableStateOf(false) }

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(height)
            .onFocusChanged {
                isFocused = it.isFocused || it.hasFocus
                onFocusChange?.invoke(isFocused)
            },
        shape = ButtonDefaults.shape(shape = DreamShapes.Sharp),
        colors = ButtonDefaults.colors(
            containerColor = Color.White,
            contentColor = Color.Black,
            focusedContainerColor = Color.White,
            focusedContentColor = Color.Black,
            disabledContainerColor = Color.White.copy(alpha = 0.3f),
            disabledContentColor = Color.Black.copy(alpha = 0.5f)
        ),
        border = ButtonDefaults.border(
            focusedBorder = androidx.tv.material3.Border(
                border = androidx.compose.foundation.BorderStroke(2.dp, Color.White),
                shape = DreamShapes.Sharp
            )
        ),
        glow = ButtonDefaults.glow(
            focusedGlow = androidx.tv.material3.Glow(
                elevationColor = Color.White.copy(alpha = 0.4f),
                elevation = 16.dp
            )
        )
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Secondary button. Netflix style (Dark grey translucent background, White text).
 * On focus: becomes lighter or white border.
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = 56.dp,
    onFocusChange: ((Boolean) -> Unit)? = null
) {
    var isFocused by remember { mutableStateOf(false) }

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(height)
            .onFocusChanged {
                isFocused = it.isFocused || it.hasFocus
                onFocusChange?.invoke(isFocused)
            },
        shape = ButtonDefaults.shape(shape = DreamShapes.Sharp),
        colors = ButtonDefaults.colors(
            containerColor = Color(0x66404040), // translucent grey
            contentColor = Color.White,
            focusedContainerColor = Color(0x99404040), // lighter grey on focus
            focusedContentColor = Color.White
        ),
        border = ButtonDefaults.border(
            focusedBorder = androidx.tv.material3.Border(
                border = androidx.compose.foundation.BorderStroke(2.dp, Color.White),
                shape = DreamShapes.Sharp
            )
        ),
        glow = ButtonDefaults.glow(
            focusedGlow = androidx.tv.material3.Glow(
                elevationColor = Color.White.copy(alpha = 0.25f),
                elevation = 12.dp
            )
        )
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}
