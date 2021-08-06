package com.privo.sdk.internal

import android.R.style.Theme_Translucent_NoTitleBar_Fullscreen
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.webkit.WebView
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import com.privo.sdk.Configuration
import com.privo.sdk.api.Rest
import com.privo.sdk.model.PrivoSettings
import com.privo.sdk.model.WebViewConfig
import android.content.pm.ResolveInfo

import android.content.pm.PackageManager





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

        @SuppressLint("SetJavaScriptEnabled")
        fun getWebView(context: Context, url: String): WebView {
            val webView = WebView(context)
            webView.settings.loadWithOverviewMode = true
            webView.settings.useWideViewPort = false
            webView.settings.setSupportZoom(false)
            webView.settings.javaScriptEnabled = true
            webView.settings.databaseEnabled = true
            webView.settings.domStorageEnabled = true
            webView.setBackgroundColor(Color.TRANSPARENT)
            webView.loadUrl(url)
            return webView
        }

        fun shareFile(context: Context, uri: Uri, title: String, mimeType: String) {
            val shareIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, uri)
                type = mimeType
            }

            val chooser = Intent.createChooser(shareIntent, title)
            val resInfoList: List<ResolveInfo> = context
                .packageManager
                .queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY)

            for (resolveInfo in resInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                context.grantUriPermission(
                    packageName,
                    uri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            startActivity(context,chooser, null)
        }

        fun showWebView(context: Context, config: WebViewConfig) {
            val webView = getWebView(context, config.url)
            val paramsWebView = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
            )
            val dialog = Dialog(context, Theme_Translucent_NoTitleBar_Fullscreen)
            dialog.addContentView(webView, paramsWebView)

            if (config.allowPdfDownload) {
                JsPdfDownload(webView)
            }
            webView.webViewClient = PrivoWebViewClient(config,dialog)
            dialog.show()
        }
    }
}