package com.privo.sdk.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AgeGateStoredEntity (
    val userIdentifier: String?,
    val nickname: String?,
    val agId: String
    )