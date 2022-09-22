package com.privo.sdk.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class AgeRange (
    val start: Int,
    val end: Int,
    val jurisdiction: String?,
)
