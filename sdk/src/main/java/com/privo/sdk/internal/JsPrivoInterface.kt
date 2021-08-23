package com.privo.sdk.internal

import android.webkit.JavascriptInterface
import android.webkit.WebView
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
}