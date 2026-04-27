@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)

package com.dreamweddingstories.tv.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.dreamweddingstories.tv.components.PrimaryButton
import com.dreamweddingstories.tv.components.SecondaryButton
import com.dreamweddingstories.tv.components.GoldShimmerLine
import com.dreamweddingstories.tv.model.UiState
import com.dreamweddingstories.tv.model.User
import com.dreamweddingstories.tv.ui.theme.AccentGold
import com.dreamweddingstories.tv.ui.theme.AccentGoldSubtle
import com.dreamweddingstories.tv.ui.theme.DarkBackground
import com.dreamweddingstories.tv.ui.theme.DreamAnimation
import com.dreamweddingstories.tv.ui.theme.DreamShapes
import com.dreamweddingstories.tv.ui.theme.ErrorAmber
import com.dreamweddingstories.tv.ui.theme.GoldDivider
import com.dreamweddingstories.tv.ui.theme.SurfaceDark
import com.dreamweddingstories.tv.ui.theme.TextPrimary
import com.dreamweddingstories.tv.ui.theme.TextSecondary
import com.dreamweddingstories.tv.ui.theme.TextTertiary
import com.dreamweddingstories.tv.ui.theme.WarmBorder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val CODE_LENGTH = 4

/**
 * Login Screen — Cinematic Luxury Editorial
 *
 * Full-bleed dark bokeh background. Centre frosted-glass card.
 * 4-character code entry with gold bottom-border animation.
 * Sharp-cornered gold CTA. Warm amber error states.
 */
@Composable
fun LoginScreen(
    authState: UiState<User>,
    onCodeSignIn: (String) -> Unit,
    onDemoSignIn: () -> Unit,
    onDismissError: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    val focusRequesters = remember { List(CODE_LENGTH) { FocusRequester() } }

    // ── Entrance animation ──
    val contentAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(200)
        contentAlpha.animateTo(1f, DreamAnimation.silkTween(DreamAnimation.SLOW))
    }

    // Navigate on success
    LaunchedEffect(authState) {
        if (authState is UiState.Success) onLoginSuccess()
    }

    // Auto-focus first box
    LaunchedEffect(Unit) {
        delay(600)
        focusRequesters[0].requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // ── Abstract bokeh background (CSS/canvas-generated) ──
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1A1E3A).copy(alpha = 0.4f),
                            Color(0xFF0F1623).copy(alpha = 0.6f),
                            DarkBackground
                        ),
                        radius = 900f
                    )
                )
        )
        // Gold-tinted bokeh circles (simulated)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            AccentGold.copy(alpha = 0.03f),
                            Color.Transparent
                        ),
                        radius = 600f,
                        center = androidx.compose.ui.geometry.Offset(300f, 200f)
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .alpha(contentAlpha.value)
        ) {
            // ── Left: Branding ──
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(64.dp)
                ) {
                    // Company Logo
                    AsyncImage(
                        model = "https://res.cloudinary.com/doy1jic8d/image/upload/v1774727598/dream-wedding-assets/umnjmt9ucxwled1c3rq4.png",
                        contentDescription = "Dream Wedding Stories",
                        modifier = Modifier.height(120.dp),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Gold rule
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(1.dp)
                            .background(GoldDivider)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "YOUR LOVE STORY\nBEAUTIFULLY PRESERVED",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextTertiary,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            }

            // ── Right: Code Entry (frosted glass card) ──
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(SurfaceDark.copy(alpha = 0.7f))
                    .border(
                        width = 1.dp,
                        color = WarmBorder,
                        shape = DreamShapes.Sharp
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(420.dp)
                ) {
                    // ── Heading ──
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Enter Your Code",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Light,
                            color = TextPrimary,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "Use the 4-character code shared with you\nby your filmmaker.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            lineHeight = 24.sp,
                            fontWeight = FontWeight.Light
                        )
                    }

                    // ── 4-Box code input with gold bottom borders ──
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (index in 0 until CODE_LENGTH) {
                            val char = code.getOrNull(index)?.toString() ?: ""
                            val isFocused = remember { mutableStateOf(false) }

                            val borderColor by animateColorAsState(
                                targetValue = when {
                                    authState is UiState.Error -> ErrorAmber
                                    isFocused.value -> AccentGold
                                    char.isNotEmpty() -> AccentGold.copy(alpha = 0.5f)
                                    else -> TextTertiary.copy(alpha = 0.4f)
                                },
                                animationSpec = DreamAnimation.silkTween(),
                                label = "border_$index"
                            )

                            OutlinedTextField(
                                value = char,
                                onValueChange = { newVal ->
                                    onDismissError()
                                    val filtered = newVal.filter { it.isLetterOrDigit() }
                                        .uppercase()
                                        .take(1)

                                    val codeChars = code.padEnd(CODE_LENGTH).toMutableList()
                                    if (filtered.isNotEmpty()) {
                                        codeChars[index] = filtered[0]
                                        code = codeChars.joinToString("").trimEnd()
                                        if (index < CODE_LENGTH - 1) {
                                            focusRequesters[index + 1].requestFocus()
                                        }
                                    } else if (filtered.isEmpty() && char.isNotEmpty()) {
                                        codeChars[index] = ' '
                                        code = codeChars.joinToString("").trimEnd()
                                        if (index > 0) focusRequesters[index - 1].requestFocus()
                                    }
                                },
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    color = TextPrimary,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Light,
                                    textAlign = TextAlign.Center,
                                    letterSpacing = 0.sp
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    capitalization = KeyboardCapitalization.Characters,
                                    imeAction = if (index == CODE_LENGTH - 1) ImeAction.Done else ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (code.length == CODE_LENGTH) onCodeSignIn(code)
                                    },
                                    onNext = {
                                        if (index < CODE_LENGTH - 1)
                                            focusRequesters[index + 1].requestFocus()
                                    }
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    cursorColor = AccentGold,
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedContainerColor = SurfaceDark.copy(alpha = 0.5f),
                                    unfocusedContainerColor = SurfaceDark.copy(alpha = 0.3f)
                                ),
                                shape = DreamShapes.Sharp,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(80.dp)
                                    .focusRequester(focusRequesters[index])
                                    .onFocusChanged { isFocused.value = it.isFocused }
                                    // Gold bottom border only
                                    .border(
                                        width = 1.dp,
                                        brush = Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color.Transparent, borderColor),
                                            startY = 0f,
                                            endY = Float.MAX_VALUE
                                        ),
                                        shape = DreamShapes.Sharp
                                    )
                            )
                        }
                    }

                    // ── Loading shimmer ──
                    if (authState is UiState.Loading) {
                        GoldShimmerLine(modifier = Modifier.fillMaxWidth())
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // ── Submit button — gold, sharp corners ──
                    PrimaryButton(
                        text = if (authState is UiState.Loading) "Verifying..." else "View My Wedding",
                        onClick = { onCodeSignIn(code) },
                        enabled = authState !is UiState.Loading && code.length == CODE_LENGTH,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // ── Demo button — outlined ──
                    SecondaryButton(
                        text = "Try Demo",
                        onClick = onDemoSignIn,
                        enabled = authState !is UiState.Loading,
                        modifier = Modifier.fillMaxWidth(),
                        height = 48.dp
                    )

                    // ── Error message — warm amber ──
                    if (authState is UiState.Error) {
                        Text(
                            text = authState.message,
                            color = ErrorAmber,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
