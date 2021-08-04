package com.privo.sdk.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VerificationConfig(
    val apiKey: String,
    val siteIdentifier: String,
    val displayMode: String = "redirect"
)

@JsonClass(generateAdapter = true)
data class VerificationData(
    val profile: UserVerificationProfile,
    val config: VerificationConfig,
    val redirectUrl: String? = null,
    val sourceOrigin: String? = null
)