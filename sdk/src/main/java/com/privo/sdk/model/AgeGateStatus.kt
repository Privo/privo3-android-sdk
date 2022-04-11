package com.privo.sdk.model

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson


internal class AgeGateStatusInternalAdapter {
    @ToJson
    fun toJson(enum: AgeGateStatusInternal): String {
        return enum.status
    }

    @FromJson
    fun fromJson(enum: String): AgeGateStatusInternal {
        return AgeGateStatusInternal.values().first { it.status == enum }
    }
}

internal enum class AgeGateStatusInternal(val status: String) {
    Undefined("Undefined"),
    Blocked ("Blocked"),
    Allowed ("Allowed"),
    Canceled ("Canceled"),
    ConsentApproved ("Consent Approved"),
    ConsentDeclined ("Consent Declined"),

    // Internal statuses
    OpenVerification ("open-verification-widget"),
    CloseAgeGate ("close-age-gate-widget"),
}

class AgeGateStatusAdapter {
    @ToJson
    fun toJson(enum: AgeGateStatus): String {
        return enum.status
    }

    @FromJson
    fun fromJson(enum: String): AgeGateStatus {
        return AgeGateStatus.values().first { it.status == enum }
    }
}

enum class AgeGateStatus(val status: String) {
    Undefined("Undefined"),
    Blocked("Blocked"),
    Allowed("Allowed"),
    Canceled("Canceled"),
    Pending("Pending"),
    ConsentApproved("Consent Approved"),
    ConsentDeclined("Consent Declined"),
}
