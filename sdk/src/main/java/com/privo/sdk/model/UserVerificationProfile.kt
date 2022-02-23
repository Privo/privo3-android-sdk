package com.privo.sdk.model

import com.privo.sdk.model.adapters.MillisecondsTimeStamp
import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
data class UserVerificationProfile (
    var firstName: String? = null,
    var lastName: String? = null,
    @MillisecondsTimeStamp var birthDate: Date? = null,
    var email: String? = null,
    var postalCode: String? = null,
    var phone: String? = null,
    var partnerDefinedUniqueID: String? = null
    )