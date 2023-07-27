package com.privo.sdk.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class CustomRestError (
    val code: Int,
    val msg: String?
)
