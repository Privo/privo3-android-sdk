package com.privo.sdk.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class AgeVerificationStoreData (
    val displayMode: String = "redirect",

    val serviceIdentifier: String,
    val redirectUrl: String?,
    val profile: AgeVerificationProfile?,
)