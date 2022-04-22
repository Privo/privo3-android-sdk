package com.privo.sdk.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class AgeGateEventInternal(
    val status : AgeGateStatusInternal,
    val userIdentifier : String?,
    val agId : String?,
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