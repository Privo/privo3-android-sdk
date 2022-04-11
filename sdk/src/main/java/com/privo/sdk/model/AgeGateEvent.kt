package com.privo.sdk.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class AgeGateEventInternal(
    val status : AgeGateStatusInternal,
    val userIdentifier : String?,
    val agId : String?,
)

@JsonClass(generateAdapter = true)
data class AgeGateEvent(
    val status : AgeGateStatus,
    val userIdentifier : String?,
    val agId : String?,
)