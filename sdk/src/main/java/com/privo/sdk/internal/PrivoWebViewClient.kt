package com.privo.sdk.internal

import android.webkit.WebView
import android.webkit.WebViewClient
import android.app.Dialog
import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.util.Log
import android.view.View
import android.webkit.WebResourceRequest
import com.privo.sdk.internal.PrivoInternal.Companion.getWebView
import com.privo.sdk.model.WebViewConfig

internal class PrivoWebViewClient(private val config: WebViewConfig, private val parentDialog: Dialog): WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest): Boolean {
        val url = request.url.toString()
        val finishCriteria = config.finishCriteria
        if (finishCriteria != null && url.contains(finishCriteria) ) {
            config.onFinish?.let {
                it(request.url)
            }
            parentDialog.hide()
            return false
        }
        val printCriteria = config.printCriteria
        if (printCriteria != null && url.contains(printCriteria) ) {
            view?.let{
                printContent(it, url)
            }
        }
        return true
    }

    private fun printContent(parentView: WebView, url: String) {
        val webView = getWebView(parentView.context, url)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                (parentView.context.getSystemService(Context.PRINT_SERVICE) as? PrintManager)?.let { printManager ->
                    val jobName = "PRIVO Print Form"
                    val printAdapter = webView.createPrintDocumentAdapter(jobName)
                    printManager.print(
                        jobName,
                        printAdapter,
                        PrintAttributes.Builder().build()
                    )
                }
                parentView.removeView(webView)
            }
        }
        webView.visibility = View.INVISIBLE
        parentView.addView(webView)
    }
}