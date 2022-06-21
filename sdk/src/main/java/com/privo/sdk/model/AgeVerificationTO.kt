package com.privo.sdk.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class AgeVerificationTO (
    val verificationIdentifier: String,
    val status: AgeVerificationStatusInternal,
    val firstName: String,
    val birthDate: String, // "2022-05-24";
    val parentFirstName: String,
    val parentLastName: String,
    val parentEmail: String?,
    val mobilePhone: String?,
    val email: String?,
)