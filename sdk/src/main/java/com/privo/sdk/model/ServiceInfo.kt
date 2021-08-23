package com.privo.sdk.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ServiceInfo (
    val serviceIdentifier: String,
    val apiKeys: Array<String>?,
    val authMethods: Array<Int>?,
    val p2siteId: Int?,
)