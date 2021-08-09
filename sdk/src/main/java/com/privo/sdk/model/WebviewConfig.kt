package com.privo.sdk.model

import android.net.Uri

internal data class WebViewConfig(
    val url: String,
    val allowPdfDownload: Boolean,
    var printCriteria: String? = null,
    var finishCriteria: String? = null,
    val onPrivoEvent: ((String) -> Unit)? = null,
    val onFinish: ((Uri) -> Unit)? = null
)
