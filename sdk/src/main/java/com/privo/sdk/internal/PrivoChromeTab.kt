package com.privo.sdk.internal

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import androidx.browser.customtabs.CustomTabsIntent
import com.privo.sdk.model.WebViewConfig


class PrivoChromeTab internal constructor(val context: Context) {
    private val accessIdKey = "accessId"
    private val accessIdRestKey = "access_id"
    private var config: WebViewConfig? = null

    internal fun show(config: WebViewConfig) {
        this.config = config
        val customTabsHeight = Resources.getSystem().displayMetrics.heightPixels / 0.75 // 75% height
        val builder = CustomTabsIntent.Builder()
        val customTabsIntent = builder.setInitialActivityHeightPx(customTabsHeight.toInt()).build()
        customTabsIntent.launchUrl(context, Uri.parse(config.url))

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            // TODO: onLoad on real criteria
            this.config?.onLoad?.invoke()
        }, 5000)
    }

    fun handleReturnIntent (intent: Intent) {
        val data = intent.data
        val accessId = data?.getQueryParameter(accessIdRestKey)
        accessId?.let { accessId ->
            val event = mapOf(accessIdKey to accessId)
            config?.onPrivoEvent?.invoke(event)
        }

    }
}