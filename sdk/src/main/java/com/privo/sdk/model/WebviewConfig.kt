package com.privo.sdk.model

import android.net.Uri

internal data class WebViewConfig(
    val url: String,
    val allowPdfDownload: Boolean = false,
    var printCriteria: String? = null,
    var finishCriteria: String? = null,
    val onPrivoEvent: ((Map<String,String>?) -> Unit)? = null,
    val onLoad: (() -> Unit)? = null,
    val onFinish: ((Uri) -> Unit)? = null,
)
