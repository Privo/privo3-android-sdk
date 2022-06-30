package com.privo.sdk.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AgeVerificationProfile(
    val userIdentifier: String?,
    val firstName: String?,
    val email: String?,
    val birthDateYYYYMMDD: String?, // "yyyy-MM-dd" format
    val phoneNumber: String?, // in the full international format (E.164, e.g. “+17024181234”)
)