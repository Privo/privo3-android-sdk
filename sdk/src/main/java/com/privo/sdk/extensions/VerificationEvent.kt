package com.privo.sdk.extensions

import com.privo.sdk.model.VerificationEvent
import com.privo.sdk.model.VerificationEventType

internal fun createVerificationEvent (event: VerificationEventType) = VerificationEvent(event = event, result = null, data = null, errorCode = null, errorMessage = null)