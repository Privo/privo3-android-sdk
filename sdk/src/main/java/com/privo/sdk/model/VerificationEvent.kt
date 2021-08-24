package com.privo.sdk.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

enum class VerificationEventType(val event: String) {
    @Json(name = "verify-initialized") VerifyInitialized("verify-initialized"),
    @Json(name = "verify-error") VerifyError ("verify-error"),
    @Json(name = "verify-print-preview") VerifyPrintPreview ("verify-print-preview"),
    @Json(name = "verify-cancel") VerifyCancel ("verify-cancel"),
    @Json(name = "verify-complete") VerifyComplete ("verify-complete"),
    @Json(name = "verify-done") VerifyDone ("verify-done"),
}

@JsonClass(generateAdapter = true)
data class VerificationResult(val serviceId : String?, val verificationResponse: VerificationResponse)

@JsonClass(generateAdapter = true)
data class VerificationEvent(
    val event: VerificationEventType,
    val result: VerificationResult?,
    val data: String?,
    val errorCode: String?,
    val errorMessage: String?
    )