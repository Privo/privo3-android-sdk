package com.privo.sdk.internal

import android.R.style.Theme_Translucent_NoTitleBar_Fullscreen
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.webkit.WebView
import android.widget.RelativeLayout
import com.privo.sdk.Configuration
import com.privo.sdk.api.Rest
import com.privo.sdk.model.PrivoSettings
import com.privo.sdk.model.WebViewConfig

internal class PrivoInternal private constructor() {
    companion object {
        private var _configuration: Configuration? = null
        private var _settings: PrivoSettings? = null

        val configuration: Configuration
            get() = _configuration ?: throw UninitializedPropertyAccessException("Privo SDK is not initialized, call Privo.initialize first")
        val settings: PrivoSettings
            get() = _settings ?: throw UninitializedPropertyAccessException("Privo SDK is not initialized, call Privo.initialize first")

        fun initialize (settings: PrivoSettings) {
            _settings = settings;
            _configuration = Configuration(settings.envType)
        }
        val rest = Rest();

        fun showWebView(context: Context, config: WebViewConfig) {
            val webView = WebView(context)
            webView.settings.loadWithOverviewMode = true
            webView.settings.useWideViewPort = false
            webView.settings.setSupportZoom(false)
            webView.settings.javaScriptEnabled = true
            webView.settings.databaseEnabled = true
            webView.settings.domStorageEnabled = true
            webView.setBackgroundColor(Color.TRANSPARENT)
            webView.loadUrl(config.url)

            val paramsWebView = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
            )
            val dialog = Dialog(context, Theme_Translucent_NoTitleBar_Fullscreen)
            dialog.addContentView(webView, paramsWebView)
            webView.webViewClient = PrivoWebViewClient(config,dialog)
            dialog.show()
        }
    }
}