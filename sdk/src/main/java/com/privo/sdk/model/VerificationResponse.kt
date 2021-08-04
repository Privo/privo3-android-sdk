package com.privo.sdk.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.*

enum class VerificationMethodType(val method: Int) {
    CreditCard(1),
    DriversLicense(2),
    Phone(4),
    SSN(5),
    CorporateEmail(13),
    PrintForm(15),
    PayPal(10)
}
enum class VerificationOutcome(val outcome: Int) {
    Pass(1),
    Pending(2),
    Fail(3),
    Declined(4),
    Purged(5),
}

@JsonClass(generateAdapter = true)
data class VerificationResponse(
    val verified: Boolean,
    val requestID: String,
    val transactionID: String,
    val verificationMethod: VerificationMethodType,
    val matchOutcome: VerificationOutcome,
    val requestTimestamp: Date,
    val locale: String,
    val matchCode: String?,
    val redirectUrl: String?,
    val message: String?,
    val partnerDefinedUniqueID: String?,
    //Applicable to offline methods only
    val identificationNumber: String?,
    val attemptId: Int?)