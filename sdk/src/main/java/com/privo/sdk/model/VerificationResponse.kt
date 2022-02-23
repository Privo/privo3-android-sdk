package com.privo.sdk.model

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonClass
import com.squareup.moshi.ToJson

class VerificationMethodTypeAdapter {
    @ToJson
    fun toJson(enum: VerificationMethodType): Int {
        return enum.method
    }

    @FromJson
    fun fromJson(type: Int): VerificationMethodType {
        return VerificationMethodType.values().first { it.method == type }
    }
}

enum class VerificationMethodType(val method: Int) {
    CreditCard(1),
    DriversLicense(2),
    Phone(4),
    SSN(5),
    CorporateEmail(6),
    PrintForm(7),
    PayPal(10)
}

class VerificationOutcomeAdapter {
    @ToJson
    fun toJson(enum: VerificationOutcome): Int {
        return enum.outcome
    }

    @FromJson
    fun fromJson(type: Int): VerificationOutcome {
        return VerificationOutcome.values().first { it.outcome == type }
    }
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
    val requestIdentifier: String,
    val verified: Boolean,
    val requestID: String,
    val transactionID: String,
    val verificationMethod: VerificationMethodType,
    val matchOutcome: VerificationOutcome,
    val requestTimestamp: Long,
    val locale: String,
    val matchCode: String?,
    val redirectUrl: String?,
    val message: String?,
    val partnerDefinedUniqueID: String?,
    //Applicable to offline methods only
    val identificationNumber: String?,
    val attemptId: Int?)