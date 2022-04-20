package com.privo.sdk

import android.content.Context
import com.privo.sdk.internal.AgeGateInternal
import com.privo.sdk.model.*



class PrivoAgeGate(val context: Context) {
    private val ageGate = AgeGateInternal(context)

    fun getAgeStatus(userIdentifier: String?, completionHandler:(AgeGateEvent?) -> Unit) {

        // TODO: add pooling here
        ageGate.getStatusEvent(userIdentifier) { lastEvent ->
            ageGate.storeAgeGateEvent(lastEvent)
            completionHandler(lastEvent)
        }
    }

    fun run(data: CheckAgeData,completionHandler: (AgeGateEvent?) -> Unit) {

        ageGate.getAgeGateEvent(data.userIdentifier) { lastEvent ->

                if (lastEvent != null &&
                    lastEvent.status != AgeGateStatus.ConsentRequired &&
                    lastEvent.status != AgeGateStatus.IdentityVerificationRequired &&
                    lastEvent.status != AgeGateStatus.AgeVerificationRequired
                ) {
                    completionHandler(lastEvent)
                } else {
                    if (data.birthDateYYYYMMDD != null) {
                        ageGate.runAgeGateByBirthDay(data) { event ->
                            ageGate.storeAgeGateEvent(event)
                            completionHandler(event)
                        }
                    } else {
                        ageGate.runAgeGate(data, null) { event ->
                            ageGate.storeAgeGateEvent(event)
                            completionHandler(event)
                        }
                    }
                }
        }
    }
    fun recheck(data: RecheckAgeData,completionHandler: (AgeGateEvent?) -> Unit) {
        ageGate.runAgeGateRecheck(data) { event ->
            ageGate.storeAgeGateEvent(event)
            completionHandler(event)
        }
    }
    fun hide() = ageGate.hide()
}
