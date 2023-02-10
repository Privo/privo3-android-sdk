package com.privo.sdk.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AgeGateLinkWarning(
    val description: String,
    val agIdEntities: Set<AgeGateStoredEntity>,
)
