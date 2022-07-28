package com.privo.sdk.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FpStatusRecord(
    val serviceIdentifier: String,
    val fpId: String,
    val birthDate: String?, // "2021-03-04"
    val birthDateYYYYMM: String?, // "2021-03"
    val birthDateYYYY: String?, // "2021"
    val extUserId: String?,
    val countryCode: String?,
)

@JsonClass(generateAdapter = true)
data class StatusRecord (
    val serviceIdentifier: String,
    val fpId: String,
    val agId: String?,
    val extUserId : String?,
)

@JsonClass(generateAdapter = true)
data class RecheckStatusRecord(
    val serviceIdentifier: String,
    val agId: String,
    val birthDate: String?, // "2021-03-04"
    val birthDateYYYYMM: String?, // "2021-03"
    val birthDateYYYY: String?, // "2021"
    val countryCode : String?,
)
