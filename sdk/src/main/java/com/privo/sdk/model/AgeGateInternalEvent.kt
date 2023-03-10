package com.privo.sdk.model

import com.privo.sdk.model.AgeRange
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class AgeGateEventInternal(
    val status : AgeGateStatusInternal,
    val userIdentifier : String?,
    val nickname : String?,
    val agId : String?,
    val ageRange: AgeRange?,
    val countryCode: String?
)

@Deprecated(message = "We don't store previous events anymore, so we don't need expiration")
@JsonClass(generateAdapter = true)
internal data class AgeGateExpireEvent(
    val event: AgeGateEvent,
    val expires: Long
)
