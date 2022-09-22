package com.privo.sdk.model

import com.privo.sdk.model.AgeRange
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class AgeGateEventInternal(
    val status : AgeGateStatusInternal,
    val userIdentifier : String?,
    val agId : String?,
    val ageRange: AgeRange?
)

@JsonClass(generateAdapter = true)
internal data class AgeGateExpireEvent(
    val event: AgeGateEvent,
    val expires: Long
)

@JsonClass(generateAdapter = true)
internal data class AgeGateIsExpireEvent(
    val event: AgeGateEvent,
    val isExpire: Boolean
)