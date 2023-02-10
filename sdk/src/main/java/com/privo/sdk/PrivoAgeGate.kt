package com.privo.sdk

import android.content.Context
import com.privo.sdk.internal.age.gate.AgeGateInternal
import com.privo.sdk.model.*



class PrivoAgeGate(val context: Context) {
    private val ageGate = AgeGateInternal(context)

    @Throws(
        NoInternetConnectionException::class,
        NotAllowedEmptyStringUserIdentifierException::class,
        NotAllowedEmptyStringNicknameException::class,
        NotAllowedEmptyStringAgIdException::class,
        NotAllowedMultiUserUsageException::class
    )
    fun getAgeStatus(userIdentifier: String?, nickname: String? = null, completionHandler:(AgeGateEvent?) -> Unit) {
        ageGate.helpers.checkNetwork()
        ageGate.helpers.checkUserData(userIdentifier = userIdentifier, nickname = nickname)
        ageGate.getStatusEvent(userIdentifier = userIdentifier, nickname = nickname) { lastEvent ->
            completionHandler(lastEvent)
        }
    }

    @Throws(
        IncorrectDateOfBirthException::class,
        NoInternetConnectionException::class,
        NotAllowedEmptyStringUserIdentifierException::class,
        NotAllowedEmptyStringNicknameException::class,
        NotAllowedEmptyStringAgIdException::class,
        NotAllowedMultiUserUsageException::class
    )
    fun run(data: CheckAgeData,completionHandler: (AgeGateEvent?) -> Unit) {
        ageGate.helpers.checkRequest(data)
        if (data.birthDateYYYYMMDD != null || data.birthDateYYYYMM != null || data.birthDateYYYY != null) {
            ageGate.runAgeGateByBirthDay(data) { event ->
                ageGate.storage.storeInfoFromEvent(event)
                completionHandler(event)
            }
        } else {
            ageGate.runAgeGate(data, null, false) { event ->
                ageGate.storage.storeInfoFromEvent(event)
                completionHandler(event)
            }
        }
    }
    @Throws(
        IncorrectDateOfBirthException::class,
        NoInternetConnectionException::class,
        NotAllowedEmptyStringUserIdentifierException::class,
        NotAllowedEmptyStringNicknameException::class,
        NotAllowedEmptyStringAgIdException::class,
        NotAllowedMultiUserUsageException::class
    )
    fun recheck(data: CheckAgeData,completionHandler: (AgeGateEvent?) -> Unit) {
        if (data.birthDateYYYYMMDD != null || data.birthDateYYYYMM != null || data.birthDateYYYY != null) {
            ageGate.recheckAgeGateByBirthDay(data) { newEvent ->
                ageGate.storage.storeInfoFromEvent(newEvent)
                completionHandler(newEvent)
            }
        } else {
            ageGate.runAgeGate(data,null,true) { newEvent ->
                ageGate.storage.storeInfoFromEvent(newEvent)
                completionHandler(newEvent)
            }
        }
    }
    @Throws(
        NoInternetConnectionException::class,
        NotAllowedEmptyStringUserIdentifierException::class,
        NotAllowedEmptyStringNicknameException::class,
        NotAllowedEmptyStringAgIdException::class,
        NotAllowedMultiUserUsageException::class
    )
    fun linkUser(userIdentifier: String, agId: String, nickname: String?, completionHandler:(AgeGateEvent) -> Unit) {
        ageGate.helpers.checkNetwork()
        ageGate.helpers.checkUserData(userIdentifier = userIdentifier, nickname = nickname)
        ageGate.linkUser(userIdentifier = userIdentifier, agId = agId, nickname = nickname) { event ->
            ageGate.storage.storeInfoFromEvent(event)
            completionHandler(event)
        }
    }
    @Throws(NoInternetConnectionException::class)
    fun showIdentifierModal(userIdentifier: String?, nickname: String? = null) {
        ageGate.helpers.checkNetwork()
        ageGate.showAgeGateIdentifier(userIdentifier, nickname)
    }
    fun hide() = ageGate.hide()
}
