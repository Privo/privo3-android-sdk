package com.privo.sdk

import android.content.Context
import com.privo.sdk.internal.AgeGateInternal
import com.privo.sdk.model.*



class PrivoAgeGate(val context: Context) {
    private val ageGate = AgeGateInternal(context)

    fun getAgeStatus(userIdentifier: String?, completionHandler:(AgeGateEvent?) -> Unit) {

        ageGate.getStatusEvent(userIdentifier) { lastEvent ->
            ageGate.storeAgeGateEvent(lastEvent)
            completionHandler(lastEvent)
        }
    }

    fun run(data: CheckAgeData,completionHandler: (AgeGateEvent?) -> Unit) {

        ageGate.getAgeGateEvent(data.userIdentifier) { expireEvent ->
            val lastEvent = expireEvent?.event
                if (lastEvent != null &&
                    lastEvent.status != AgeGateStatus.ConsentRequired &&
                    lastEvent.status != AgeGateStatus.IdentityVerificationRequired &&
                    lastEvent.status != AgeGateStatus.AgeVerificationRequired
                ) {
                    completionHandler(lastEvent)
                } else {
                    if (data.birthDateYYYYMMDD != null || data.birthDateYYYYMM != null || data.birthDateYYYY != null) {
                        ageGate.runAgeGateByBirthDay(data) { event ->
                            ageGate.storeAgeGateEvent(event)
                            completionHandler(event)
                        }
                    } else {
                        ageGate.runAgeGate(data, null, false) { event ->
                            ageGate.storeAgeGateEvent(event)
                            completionHandler(event)
                        }
                    }
                }
        }
    }
    fun recheck(data: CheckAgeData,completionHandler: (AgeGateEvent?) -> Unit) {
        ageGate.getAgeGateEvent(data.userIdentifier) { expireEvent ->
            val event = expireEvent?.event;
            if (event?.agId != null) {
                if (data.birthDateYYYYMMDD != null || data.birthDateYYYYMM != null || data.birthDateYYYY != null) {
                    ageGate.recheckAgeGateByBirthDay(data,event) { newEvent ->
                        ageGate.storeAgeGateEvent(newEvent)
                        completionHandler(newEvent)
                    }
                } else {
                    ageGate.runAgeGate(data,event,true) { newEvent ->
                        ageGate.storeAgeGateEvent(newEvent)
                        completionHandler(newEvent)
                    }
                }
            }
        }
    }
    fun hide() = ageGate.hide()
}
