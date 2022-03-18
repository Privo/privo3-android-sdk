package com.privo.sdk.model

import com.privo.sdk.model.adapters.MillisecondsTimeStamp
import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
data class UserVerificationProfile (
    var firstName: String? = null,
    var lastName: String? = null,

    @Deprecated(message = "birthDate field is deprecated, please use birthDateYYYYMMDD instead")
    @MillisecondsTimeStamp
    var birthDate: Date? = null,

    var birthDateYYYYMMDD: String? = null,  // "yyyy-MM-dd"
    var email: String? = null,
    var postalCode: String? = null,
    var phone: String? = null,
    var partnerDefinedUniqueID: String? = null
    )