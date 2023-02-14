package com.privo.sdk.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AgeGateActionResponse(
    val action: AgeGateAction,
    val agId: String,
    val ageRange: AgeRange?,
    val extUserId: String?,
    val countryCode: String?
)

@JsonClass(generateAdapter = true)
data class AgeGateStatusResponse(
    val status: AgeGateStatusTO,
    val agId: String?,
    val ageRange: AgeRange?,
    val extUserId: String?,
    val countryCode: String?
)