package com.privo.sdk

import android.content.Context
import com.privo.sdk.internal.AgeGateInternal
import com.privo.sdk.model.*



class PrivoAgeGate(val context: Context) {
    private val ageGate = AgeGateInternal(context)

    fun getAgeStatus(data: CheckAgeData, completionHandler:(AgeGateEvent?) -> Unit) {

        // TODO: add pooling here
        ageGate.getAgeGateEvent { lastEvent ->
            if (lastEvent != null && lastEvent.userIdentifier == data.userIdentifier) {
                completionHandler(lastEvent)
            } else {
                completionHandler(AgeGateEvent(AgeGateStatus.Undefined, agId = null, userIdentifier = data.userIdentifier))
            }
        }
    }
    fun run(data: CheckAgeData,completionHandler: (AgeGateEvent?) -> Unit) {

        fun processEvent(event: AgeGateEvent?) {
            if (event != null) {
                ageGate.storeAgeGateEvent(event)
            }
            completionHandler(event)
        }

        if (data.birthDateYYYYMMDD != null) {
            ageGate.runAgeGateByBirthDay(data) { processEvent(it) }
        } else {
            ageGate.runAgeGate(data) { processEvent(it) }
        }
    }
    fun hide() = ageGate.hide()
}
