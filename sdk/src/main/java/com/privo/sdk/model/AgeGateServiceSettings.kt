package com.privo.sdk.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AgeServiceSettings(
    val verificationApiKey: String,
    val isGeoApiOn: Boolean?,
    val isAllowSelectCountry: Boolean?,
)