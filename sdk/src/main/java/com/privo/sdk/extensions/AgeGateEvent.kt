package com.privo.sdk.extensions

import com.privo.sdk.model.AgeGateEvent
import com.privo.sdk.model.AgeGateEventInternal
import com.privo.sdk.model.AgeGateStatus
import com.privo.sdk.model.AgeGateStatusInternal

internal fun AgeGateEventInternal.toEvent(): AgeGateEvent? {
    return if (this.status == AgeGateStatusInternal.Closed) {
        // Skip internal statuses
        null
    } else {
        val status = AgeGateStatus.valueOf(this.status.status)
        AgeGateEvent(status = status, userIdentifier = this.userIdentifier, nickname = this.nickname, agId = this.agId, ageRange = this.ageRange)
    }
}