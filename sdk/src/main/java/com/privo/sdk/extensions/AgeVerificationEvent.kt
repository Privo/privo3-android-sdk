package com.privo.sdk.extensions

import com.privo.sdk.model.AgeVerificationEvent
import com.privo.sdk.model.AgeVerificationEventInternal
import com.privo.sdk.model.AgeVerificationStatus
import com.privo.sdk.model.AgeVerificationStatusInternal

internal fun AgeVerificationEventInternal.toEvent(): AgeVerificationEvent? {
    return if (this.status == AgeVerificationStatusInternal.Closed) {
        // Skip internal statuses
        null
    } else {
        val status = AgeVerificationStatus.valueOf(this.status.status)
        AgeVerificationEvent(status, this.profile)
    }
}