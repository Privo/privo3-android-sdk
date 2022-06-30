package com.privo.sdk.internal

import android.content.Intent
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.core.content.ContextCompat.startActivity
import com.privo.sdk.model.WebViewConfig
import java.io.IOException

internal class JsPrivoInterface(private val webView: WebView, val config: WebViewConfig) {
    private var jsPdfDownload: JsPdfDownload? = null
    private val privoMessageConverter = PrivoMessageConverter()

    init {
        webView.addJavascriptInterface(this, "Android");
        if (config.allowPdfDownload) {
            jsPdfDownload = JsPdfDownload(webView)
        }
    }

    @Throws(IOException::class)
    @JavascriptInterface
    fun getBase64FromBlobData(base64Data: String) {
        jsPdfDownload?.convertBase64StringToPdfAndStoreIt(base64Data)
    }

    @JavascriptInterface
    fun postPrivoMessage(message: String) {
        config.onPrivoEvent?.let {
            it(privoMessageConverter.getPrivoEvent(message))
        }
    }

    @JavascriptInterface // <- this decorator is what exposes the method
    fun nativeShare(title: String, text: String, url: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TITLE, title)
            putExtra(Intent.EXTRA_TEXT, "$text $url")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        val mContext = webView.context
        startActivity(mContext, shareIntent, null)
    }
}