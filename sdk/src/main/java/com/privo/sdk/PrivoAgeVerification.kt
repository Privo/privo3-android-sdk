package com.privo.sdk

import android.content.Context
import com.privo.sdk.extensions.toEvent
import com.privo.sdk.internal.AgeVerificationInternal
import com.privo.sdk.model.AgeVerificationEvent
import com.privo.sdk.model.AgeVerificationProfile
import com.privo.sdk.model.AgeVerificationStatus

class PrivoAgeVerification(val context: Context) {
    private val ageVerification = AgeVerificationInternal(context)

    fun getStatus(userIdentifier: String?, completion: (AgeVerificationEvent) -> Unit) {
        ageVerification.getLastEvent(userIdentifier, completion)
    }
    fun run(profile: AgeVerificationProfile?, completion: (AgeVerificationEvent?) -> Unit) {

        ageVerification.getLastEvent(profile?.userIdentifier) { event ->

            if (event.status != AgeVerificationStatus.Undefined &&
                event.status != AgeVerificationStatus.Canceled
            ) {
                completion(event)
            } else {
                ageVerification.runAgeVerification(profile) { verificationEvent ->
                    ageVerification.saveVerificationIdentifier(profile?.userIdentifier, verificationEvent?.ageVerificationId)
                    val publicEvent = verificationEvent?.toEvent()
                    completion(publicEvent)
                }
            }
        }
    }
    fun hide() = ageVerification.hide()
}