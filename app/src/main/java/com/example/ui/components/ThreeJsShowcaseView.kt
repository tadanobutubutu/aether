package com.example.ui.components

import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun ThreeJsShowcaseView(modifier: Modifier = Modifier) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    
                    // --- SECURITY IMPROVEMENT: WebView Hardening ---
                    // Explicitly disable absolute file and content access to prevent
                    // cross-site file leakage (local code execution) while retaining assets loading.
                    allowFileAccess = false
                    allowContentAccess = false
                    
                    loadWithOverviewMode = true
                    useWideViewPort = true
                }
                webViewClient = WebViewClient()
                loadUrl("file:///android_asset/three_showcase.html")
            }
        },
        modifier = modifier.fillMaxSize()
    )
}
