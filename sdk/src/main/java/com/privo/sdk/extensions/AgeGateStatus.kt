package com.privo.sdk.extensions

import com.privo.sdk.model.*

internal fun AgeGateStatusTO.toStatus(): AgeGateStatus {
    when (this) {
        AgeGateStatusTO.Pending -> {
            return AgeGateStatus.Pending
        }
        AgeGateStatusTO.Allowed -> {
            return AgeGateStatus.Allowed
        }
        AgeGateStatusTO.Blocked -> {
            return AgeGateStatus.Blocked
        }
        AgeGateStatusTO.ConsentRequired -> {
            return AgeGateStatus.ConsentRequired
        }
        AgeGateStatusTO.ConsentApproved -> {
            return AgeGateStatus.ConsentApproved
        }
        AgeGateStatusTO.ConsentDenied -> {
            return AgeGateStatus.ConsentDenied
        }
        AgeGateStatusTO.IdentityVerificationRequired -> {
            return AgeGateStatus.IdentityVerificationRequired
        }
        AgeGateStatusTO.IdentityVerified -> {
            return AgeGateStatus.IdentityVerified
        }
        AgeGateStatusTO.AgeVerificationRequired -> {
            return AgeGateStatus.AgeVerificationRequired
        }
        AgeGateStatusTO.AgeVerified -> {
            return AgeGateStatus.AgeVerified
        }
        AgeGateStatusTO.AgeBlocked -> {
            return AgeGateStatus.AgeBlocked
        }
        AgeGateStatusTO.MultiUserBlocked -> {
            return AgeGateStatus.MultiUserBlocked
        }
        else -> {
            return AgeGateStatus.Undefined
        }
    }
}
