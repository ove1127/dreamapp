package com.dreamweddingstories.tv.components

import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Builds the Vimeo embed URL from a vimeoVideoId (e.g. "1183011948?h=f46b555875").
 * Returns a muted, autoplay, loop player URL with no controls.
 */
fun buildVimeoEmbedUrl(vimeoVideoId: String, muted: Boolean = true, loop: Boolean = true): String {
    val parts = vimeoVideoId.split("?h=")
    val vidId = parts[0]
    val hashParam = if (parts.size > 1) "?h=${parts[1]}&" else "?"
    val muteParam = if (muted) "&muted=1" else ""
    val loopParam = if (loop) "&loop=1" else ""
    return "https://player.vimeo.com/video/$vidId${hashParam}background=1&autoplay=1$muteParam$loopParam&badge=0&autopause=0&player_id=0&app_id=58479"
}

/**
 * A muted, autoplay Vimeo video preview rendered in a WebView.
 * Uses Vimeo's `background=1` mode which hides all controls and plays silently.
 */
@Composable
fun VimeoPreview(
    vimeoVideoId: String,
    modifier: Modifier = Modifier
) {
    val embedUrl = remember(vimeoVideoId) { buildVimeoEmbedUrl(vimeoVideoId) }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(android.graphics.Color.BLACK)
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                settings.cacheMode = WebSettings.LOAD_DEFAULT
                settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                webViewClient = object : android.webkit.WebViewClient() {
                    override fun onReceivedSslError(
                        view: WebView?, handler: android.webkit.SslErrorHandler?,
                        error: android.net.http.SslError?
                    ) { handler?.proceed() }
                }
                webChromeClient = WebChromeClient()
                // Not focusable — preview only, shouldn't steal focus from TV navigation
                isFocusable = false
                isFocusableInTouchMode = false
                loadUrl(embedUrl)
            }
        },
        modifier = modifier
    )
}
