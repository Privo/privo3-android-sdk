package com.privo.sdk.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class AnalyticEvent(
    val serviceIdentifier: String?,
    val data: String?,
    val sid: String? = null,
    val tid: String? = null,
) {
    val svc = 63 // PrivoAndroidSDK
    val event = 299 // MetricUnexpectedError
}
