@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)

package com.dreamweddingstories.tv.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
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
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.dreamweddingstories.tv.model.UiState
import com.dreamweddingstories.tv.model.User
import com.dreamweddingstories.tv.ui.theme.AccentWhite
import com.dreamweddingstories.tv.ui.theme.DarkBackground
import com.dreamweddingstories.tv.ui.theme.DividerWhite
import com.dreamweddingstories.tv.ui.theme.ErrorRed
import com.dreamweddingstories.tv.ui.theme.TextPrimary
import com.dreamweddingstories.tv.ui.theme.TextSecondary

private const val CODE_LENGTH = 4

@Composable
fun LoginScreen(
    authState: UiState<User>,
    onCodeSignIn: (String) -> Unit,
    onDemoSignIn: () -> Unit,
    onDismissError: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    // One state string for the full code
    var code by remember { mutableStateOf("") }

    // 4 individual FocusRequesters — one per box
    val focusRequesters = remember { List(CODE_LENGTH) { FocusRequester() } }

    // Navigate on success
    LaunchedEffect(authState) {
        if (authState is UiState.Success) onLoginSuccess()
    }

    // Auto-focus first box
    LaunchedEffect(Unit) {
        focusRequesters[0].requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
    ) {
        Row(modifier = Modifier.fillMaxSize()) {

            // ── Left: Branding ────────────────────────────────────────────────
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
                    Text(
                        text = "✦",
                        fontSize = 36.sp,
                        color = AccentWhite.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Dream Wedding\nStories",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = AccentWhite,
                        textAlign = TextAlign.Center,
                        lineHeight = 52.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your cinematic memories,\nalways with you.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 26.sp
                    )
                }
            }

            // ── Right: Code Entry ─────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color(0xFF0F0F0F)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(420.dp)
                ) {
                    // Heading
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Enter Your Code",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Use the 4-character code shared with you by your photographer.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            lineHeight = 22.sp
                        )
                    }

                    // ── 4-Box OTP input ──────────────────────────────────────
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (index in 0 until CODE_LENGTH) {
                            val char = code.getOrNull(index)?.toString() ?: ""
                            val isFocused = remember { mutableStateOf(false) }

                            val borderColor by animateColorAsState(
                                targetValue = when {
                                    authState is UiState.Error -> ErrorRed
                                    isFocused.value -> AccentWhite
                                    char.isNotEmpty() -> AccentWhite.copy(alpha = 0.6f)
                                    else -> DividerWhite
                                },
                                animationSpec = tween(200),
                                label = "border_$index"
                            )

                            OutlinedTextField(
                                value = char,
                                onValueChange = { newVal ->
                                    onDismissError()
                                    // Only accept alphanumeric
                                    val filtered = newVal.filter { it.isLetterOrDigit() }
                                        .uppercase()
                                        .take(1)

                                    val codeChars = code.padEnd(CODE_LENGTH).toMutableList()
                                    if (filtered.isNotEmpty()) {
                                        codeChars[index] = filtered[0]
                                        code = codeChars.joinToString("").trimEnd()
                                        // Advance focus
                                        if (index < CODE_LENGTH - 1) {
                                            focusRequesters[index + 1].requestFocus()
                                        }
                                    } else if (filtered.isEmpty() && char.isNotEmpty()) {
                                        // Backspace — clear this box and move back
                                        codeChars[index] = ' '
                                        code = codeChars.joinToString("").trimEnd()
                                        if (index > 0) focusRequesters[index - 1].requestFocus()
                                    }
                                },
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    color = TextPrimary,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
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
                                    cursorColor = AccentWhite,
                                    focusedBorderColor = borderColor,
                                    unfocusedBorderColor = borderColor,
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(80.dp)
                                    .focusRequester(focusRequesters[index])
                                    .onFocusChanged { isFocused.value = it.isFocused }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // ── Submit button ────────────────────────────────────────
                    Button(
                        onClick = { onCodeSignIn(code) },
                        enabled = authState !is UiState.Loading && code.length == CODE_LENGTH,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = ButtonDefaults.shape(shape = RoundedCornerShape(4.dp)),
                        colors = ButtonDefaults.colors(
                            containerColor = AccentWhite,
                            contentColor = DarkBackground,
                            focusedContainerColor = AccentWhite.copy(alpha = 0.85f),
                            focusedContentColor = DarkBackground,
                            disabledContainerColor = AccentWhite.copy(alpha = 0.3f),
                            disabledContentColor = DarkBackground.copy(alpha = 0.5f)
                        )
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            if (authState is UiState.Loading) {
                                CircularProgressIndicator(
                                    color = DarkBackground,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "View My Wedding",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // ── Demo button ──────────────────────────────────────────
                    Button(
                        onClick = onDemoSignIn,
                        enabled = authState !is UiState.Loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = ButtonDefaults.shape(shape = RoundedCornerShape(4.dp)),
                        colors = ButtonDefaults.colors(
                            containerColor = Color.Transparent,
                            contentColor = AccentWhite,
                            focusedContainerColor = AccentWhite.copy(alpha = 0.15f),
                            focusedContentColor = AccentWhite
                        )
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Try Demo",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // ── Error message ─────────────────────────────────────────
                    if (authState is UiState.Error) {
                        Text(
                            text = authState.message,
                            color = ErrorRed,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
