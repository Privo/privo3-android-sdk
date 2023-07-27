package com.privo.sdk.internal

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.WindowManager
import android.webkit.WebView
import android.widget.RelativeLayout
import com.privo.sdk.R
import com.privo.sdk.model.WebViewConfig


class PrivoWebViewDialog internal constructor(context: Context, config: WebViewConfig) {
    private val dialog: Dialog

    init {
        val webView = PrivoWebViewBuilder(context, config.url).build()
        val paramsWebView = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.MATCH_PARENT
        )
        val typedValue = TypedValue()
        context.theme.resolveAttribute(R.attr.colorPrimary, typedValue, true)
        val colorPrimary = typedValue.data
        dialog = Dialog(context,  R.style.PrivoDialogStyle)
        dialog.addContentView(webView, paramsWebView)
        dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        dialog.window?.statusBarColor = colorPrimary
        // dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        JsPrivoInterface(webView,config)
        webView.webViewClient = PrivoWebViewClient(config,dialog)
        webView.webChromeClient = PrivoWebChromeClient(dialog)
        webView.settings.mediaPlaybackRequiresUserGesture = false
        dialog.setOnCancelListener { config.onCancel?.invoke() }

    }
    private fun runOnMainThread(completion: () -> Unit) = Handler(Looper.getMainLooper()).post(completion)
    fun show() = runOnMainThread {
        dialog.show()
    }
    fun hide() = runOnMainThread {
        dialog.dismiss()
    }

}