package com.privo.sdk.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class CheckAgeStoreData(
    val displayMode: String = "redirect",
    val isNativeIntegration: Boolean = true,

    val serviceIdentifier: String,
    val settings: AgeServiceSettings,
    val userIdentifier: String?,
    val countryCode: String?,
    val birthDateYYYYMMDD: String?,
    val birthDateYYYYMM: String?, // "2021-03"
    val birthDateYYYY: String?, // "2021"
    val redirectUrl: String?,
    val agId: String?,
    val fpId: String?,
)

@JsonClass(generateAdapter = true)
data class CheckAgeData(
    val userIdentifier: String?, // uniq user identifier. Can not be an empty string ("").
    val birthDateYYYYMMDD: String?, // "yyyy-MM-dd" format
    val birthDateYYYYMM: String?, // "yyyy-MM" format
    val birthDateYYYY: String?, // "yyyy" format
    val countryCode: String?, // Alpha-2 country code, e.g US
    val nickname: String? // Nickname of user for multi-user integration. Can not be an empty string ("").
)
