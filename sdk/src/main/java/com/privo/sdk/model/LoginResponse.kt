package com.privo.sdk.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


enum class LoginResponseStatus {
    @Json(name = "AccountLocked") AccountLocked,
    @Json(name = "ConsentDeclined") ConsentDeclined,
    @Json(name = "ConsentPending") ConsentPending,
    @Json(name = "ConsentPendingNewGranter") ConsentPendingNewGranter,
    @Json(name = "InvalidCredentials") InvalidCredentials,
    @Json(name = "LoginIsNotAllowed") LoginIsNotAllowed,
    @Json(name = "MoreDataRequired") MoreDataRequired,
    @Json(name = "NewAccount") NewAccount,
    @Json(name = "OIDCConsentRequired") OIDCConsentRequired,
    @Json(name = "OK") OK,
    @Json(name = "ReAuthenticationRequired") ReAuthenticationRequired,
    @Json(name = "UnexpectedError") UnexpectedError,
    @Json(name = "VerificationRequired") VerificationRequired,
}

enum class AVType {
    @Json(name = "Button") Button,
    @Json(name = "Link") Link,
    @Json(name = "NewWindow") NewWindow,
    @Json(name = "Data") Data,
}

enum class AType {
    @Json(name = "Redirect") Redirect,
    @Json(name = "FormSubmit") FormSubmit,
    @Json(name = "NewWindow") NewWindow,
    @Json(name = "Data") Data,
}


@JsonClass(generateAdapter = true)
data class LoginResponseAction(
    val aType: AType,
    val targetUrl: String,
    val isAutoRun: Boolean,
    val view : AVType?
)

@JsonClass(generateAdapter = true)
data class LoginResponse(
    val token: String?,
    val status: LoginResponseStatus,
    val actions: Array<LoginResponseAction>?,
    val error: AppError?,
)