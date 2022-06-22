package com.privo.sdk.model

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson


internal class AgeVerificationStatusInternalAdapter {
    @ToJson
    fun toJson(enum: AgeVerificationStatusInternal): String {
        return enum.status
    }

    @FromJson
    fun fromJson(enum: String): AgeVerificationStatusInternal {
        return AgeVerificationStatusInternal.values().first { it.status == enum }
    }
}

internal enum class AgeVerificationStatusInternal(val status: String) {
    Undefined("Undefined"),
    Pending("Pending"),
    Declined("Declined"),
    Confirmed("Confirmed"),
    Canceled("Canceled"),
    // Internal statuses
    Closed ("Closed"),
}

internal class AgeVerificationStatusAdapter {
    @ToJson
    fun toJson(enum: AgeVerificationStatus): String {
        return enum.status
    }

    @FromJson
    fun fromJson(enum: String): AgeVerificationStatus {
        return AgeVerificationStatus.values().first { it.status == enum }
    }
}

public enum class AgeVerificationStatus(val status: String) {
    Undefined("Undefined"),
    Pending("Pending"),
    Declined("Declined"),
    Confirmed("Confirmed"),
    Canceled("Canceled"),
}
