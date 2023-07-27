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
    Blocked("Blocked"),
    Allowed("Allowed"),
    Pending("Pending"),
    ConsentRequired("ConsentRequired"),
    ConsentApproved("ConsentApproved"),
    ConsentDenied("ConsentDenied"),
    IdentityVerificationRequired("IdentityVerificationRequired"),
    IdentityVerified("IdentityVerified"),
    AgeVerificationRequired("AgeVerificationRequired"),
    AgeVerified("AgeVerified"),
    AgeBlocked("AgeBlocked"),
    Canceled("Canceled"),
    MultiUserBlocked("MultiUserBlocked"),
    AgeEstimationBlocked("AgeEstimationBlocked"),

    // Internal statuses
    Closed ("Closed"),
}

internal class AgeGateStatusTOAdapter {
    @ToJson
    fun toJson(enum: AgeGateStatusTO): Int {
        return enum.status
    }

    @FromJson
    fun fromJson(enum: Int): AgeGateStatusTO {
        return AgeGateStatusTO.values().first { it.status == enum }
    }
}
enum class AgeGateStatusTO(val status: Int) {
    Undefined(0),
    Pending(1),
    Allowed(2),
    Blocked(3),
    ConsentRequired(4),
    ConsentApproved(5),
    ConsentDenied(6),
    IdentityVerificationRequired(7),
    IdentityVerified(8),
    AgeVerificationRequired(9),
    AgeVerified(10),
    AgeBlocked(11),
    MultiUserBlocked(12),
    AgeEstimationBlocked(13),
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
    Pending("Pending"),
    ConsentRequired("ConsentRequired"),
    ConsentApproved("ConsentApproved"),
    ConsentDenied("ConsentDenied"),
    IdentityVerificationRequired("IdentityVerificationRequired"),
    IdentityVerified("IdentityVerified"),
    AgeVerificationRequired("AgeVerificationRequired"),
    AgeVerified("AgeVerified"),
    AgeBlocked("AgeBlocked"),
    AgeEstimationBlocked("AgeEstimationBlocked"),
    Canceled("Canceled"),
    MultiUserBlocked("MultiUserBlocked")
}
