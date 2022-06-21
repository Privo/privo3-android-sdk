package com.privo.sdk.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AgeVerificationEvent(
    val status : AgeVerificationStatus,
    val profile : AgeVerificationProfile?
)

@JsonClass(generateAdapter = true)
internal data class AgeVerificationEventInternal (
    val status: AgeVerificationStatusInternal,
    val profile: AgeVerificationProfile?,
    val ageVerificationId: String?
)