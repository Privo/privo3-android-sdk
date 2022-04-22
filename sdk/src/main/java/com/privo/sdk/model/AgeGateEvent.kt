package com.privo.sdk.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AgeGateEvent(
    val status : AgeGateStatus,
    val userIdentifier : String?,
    val agId : String?,
)