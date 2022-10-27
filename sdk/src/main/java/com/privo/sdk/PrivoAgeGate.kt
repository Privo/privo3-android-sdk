package com.privo.sdk

import android.content.Context
import com.privo.sdk.internal.age.gate.AgeGateInternal
import com.privo.sdk.model.*



class PrivoAgeGate(val context: Context) {
    private val ageGate = AgeGateInternal(context)

    @Throws(NoInternetConnectionException::class)
    fun getAgeStatus(userIdentifier: String?, completionHandler:(AgeGateEvent?) -> Unit) {
        ageGate.helpers.checkNetwork()
        ageGate.getStatusEvent(userIdentifier) { lastEvent ->
            completionHandler(lastEvent)
        }
    }

    @Throws(IncorrectDateOfBirthException::class, NoInternetConnectionException::class)
    fun run(data: CheckAgeData,completionHandler: (AgeGateEvent?) -> Unit) {
        ageGate.helpers.checkRequest(data)
        if (data.birthDateYYYYMMDD != null || data.birthDateYYYYMM != null || data.birthDateYYYY != null) {
            ageGate.runAgeGateByBirthDay(data) { event ->
                ageGate.storage.storeAgeGateEvent(event)
                completionHandler(event)
            }
        } else {
            ageGate.runAgeGate(data, null, false) { event ->
                ageGate.storage.storeAgeGateEvent(event)
                completionHandler(event)
            }
        }
    }
    @Throws(IncorrectDateOfBirthException::class, NoInternetConnectionException::class)
    fun recheck(data: CheckAgeData,completionHandler: (AgeGateEvent?) -> Unit) {
        if (data.birthDateYYYYMMDD != null || data.birthDateYYYYMM != null || data.birthDateYYYY != null) {
            ageGate.recheckAgeGateByBirthDay(data) { newEvent ->
                ageGate.storage.storeAgeGateEvent(newEvent)
                completionHandler(newEvent)
            }
        } else {
            ageGate.runAgeGate(data,null,true) { newEvent ->
                ageGate.storage.storeAgeGateEvent(newEvent)
                completionHandler(newEvent)
            }
        }
    }
    @Throws(NoInternetConnectionException::class)
    fun showIdentifierModal(userIdentifier: String?) {
        ageGate.helpers.checkNetwork()
        ageGate.showAgeGateIdentifier(userIdentifier)
    }
    fun hide() = ageGate.hide()
}
