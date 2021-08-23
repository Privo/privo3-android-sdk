package com.privo.sdk.internal

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.webkit.WebView

@SuppressLint("SetJavaScriptEnabled")
class PrivoWebViewBuilder(context: Context, url: String) {
    private val webView: WebView
    init {
        webView = WebView(context)
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = false
        webView.settings.setSupportZoom(false)
        webView.settings.javaScriptEnabled = true
        webView.settings.databaseEnabled = true
        webView.settings.domStorageEnabled = true
        webView.setBackgroundColor(Color.TRANSPARENT)
        webView.loadUrl(url)
    }
    fun build() = webView
}