package com.privo.sdk.model


import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class UserSessionRequest (
    val ext_user_id: String
)