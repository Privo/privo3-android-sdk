package com.privo.sdk.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AgeGateResponse(
    val action: AgeGateAction,
    val ageGateIdentifier: String?
)