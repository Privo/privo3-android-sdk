package com.privo.sdk.internal

import android.webkit.WebView
import android.webkit.WebViewClient
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintManager
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import com.privo.sdk.components.LoadingDialog
import com.privo.sdk.model.WebViewConfig

internal class PrivoWebViewClient(private val config: WebViewConfig, private val parentDialog: Dialog): WebViewClient() {
    val loading = LoadingDialog(parentDialog.context)
    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        config.onLoad?.invoke()
    }

    override fun onLoadResource(view: WebView?, url: String?) {
        super.onLoadResource(view, url)
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest): Boolean {
        val url = request.url.toString()
        val printCriteria = config.printCriteria
        if (printCriteria != null && url.contains(printCriteria) ) {
            view?.let{
                printContent(it, url)
            }
            return true
        }
        return false
    }

    override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
        super.doUpdateVisitedHistory(view, url, isReload)
        val finishCriteria = config.finishCriteria
        if (finishCriteria != null && url != null && url.contains(finishCriteria) ) {
            config.onFinish?.invoke(Uri.parse(url))
        }
    }

    private fun printContent(parentView: WebView, url: String) {
        val webView = PrivoWebViewBuilder(parentView.context, url).build()
        loading.show()
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                loading.hide()
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