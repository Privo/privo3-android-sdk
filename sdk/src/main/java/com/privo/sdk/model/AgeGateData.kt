package com.privo.sdk.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class CheckAgeStoreData(
    val displayMode: String = "redirect",

    val serviceIdentifier: String,
    val settings: AgeServiceSettings,
    val userIdentifier: String?,
    val countryCode: String?,
    val birthDateYYYYMMDD: String?,
    val redirectUrl: String?,
    val agId: String?,
    val fpId: String?,
)

@JsonClass(generateAdapter = true)
data class CheckAgeData(
    val userIdentifier: String?, // uniq user identifier
    val birthDateYYYYMMDD: String?, // "yyyy-MM-dd" format
    val countryCode: String?, // Alpha-2 country code, e.g US
)

@JsonClass(generateAdapter = true)
data class RecheckAgeData(
    val userIdentifier: String?, // uniq user identifier
    val countryCode: String?, // Alpha-2 country code, e.g US
)