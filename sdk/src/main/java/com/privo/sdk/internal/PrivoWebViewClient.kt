package com.privo.sdk.internal

import android.webkit.WebView
import android.webkit.WebViewClient
import android.app.Dialog
import android.util.Log
import android.webkit.WebResourceRequest
import com.privo.sdk.model.WebViewConfig

internal class PrivoWebViewClient(private val config: WebViewConfig, private val parentDialog: Dialog): WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest): Boolean {
        val url = request.url
        val finishCriteria = config.finishCriteria
        if (finishCriteria != null && url.toString().contains(finishCriteria) ) {
            config.onFinish?.let {
                it(url)
            }
            parentDialog.hide()
            return false
        }
        return true
    }
}