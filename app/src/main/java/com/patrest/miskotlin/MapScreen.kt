package com.patrest.miskotlin

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController




@SuppressLint("ClickableViewAccessibility")
@Composable
fun MapScreen(
    viewModel: MediaViewModel, // Pass the ViewModel
    navController: NavHostController
) {
    // Collect mediaItems state from the ViewModel
    val mediaItems by viewModel.mediaItems.collectAsState() // Use collectAsState() instead of directly accessing value

    // Map screen UI and logic
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                webViewClient = WebViewClient()
                webChromeClient = WebChromeClient()
                loadUrl("file:///map.html")

                // Pass media items to the JavaScript function
                evaluateJavascript(
                    "javascript: onMediaItemsLoaded(" +
                            "[" + mediaItems.joinToString(", ") { item ->
                        "{ latitude: ${item.latitude}, longitude: ${item.longitude}, title: '${item.title}' }"
                    } + "]" +
                            ")",
                    null
                )
            }
        }
    )
}
