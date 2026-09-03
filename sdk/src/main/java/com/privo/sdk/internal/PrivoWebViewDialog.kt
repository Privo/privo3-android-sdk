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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
        context.theme.resolveAttribute(androidx.appcompat.R.attr.colorPrimary, typedValue, true)
        val colorPrimary = typedValue.data
        val contentView = RelativeLayout(context)
        contentView.setBackgroundColor(colorPrimary)
        contentView.addView(webView, paramsWebView)
        ViewCompat.setOnApplyWindowInsetsListener(contentView) { view, windowInsets ->
            val insets = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or
                    WindowInsetsCompat.Type.displayCutout() or
                    WindowInsetsCompat.Type.ime()
            )
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            WindowInsetsCompat.CONSUMED
        }
        dialog = Dialog(context,  R.style.PrivoDialogStyle)
        dialog.addContentView(
            contentView,
            RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
            )
        )
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