package com.privo.sdk.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AgeServiceSettings(
    val isGeoApiOn: Boolean,
    val isAllowSelectCountry: Boolean,
    val isProvideUserId: Boolean,
    val isShowStatusUi: Boolean,
    val poolAgeGateStatusInterval: Int,
    val verificationApiKey: String?,
    val p2SiteId: Int?
)