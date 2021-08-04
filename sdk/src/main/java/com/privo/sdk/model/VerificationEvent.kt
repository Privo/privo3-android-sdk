package com.privo.sdk.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

enum class VerificationEventType(val event: String) {
    @Json(name = "verify-initialized") verifyInitialized("verify-initialized"),
    @Json(name = "verify-error") verifyError ("verify-error"),
    @Json(name = "verify-print-preview") verifyPrintPreview ("verify-print-preview"),
    @Json(name = "verify-cancel") verifyCancel ("verify-cancel"),
    @Json(name = "verify-complete") verifyComplete ("verify-complete"),
    @Json(name = "verify-done") verifyDone ("verify-done"),
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