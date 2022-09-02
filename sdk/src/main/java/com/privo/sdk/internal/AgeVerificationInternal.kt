package com.privo.sdk.internal

import android.content.Context
import android.content.SharedPreferences
import com.privo.sdk.extensions.toEvent
import com.privo.sdk.model.*
import com.privo.sdk.model.AgeVerificationEventInternal
import com.privo.sdk.model.AgeVerificationTO

internal class AgeVerificationInternal(val context: Context) {

    private val AGE_VERIFICATION_EVENT_KEY = "AgeVerificationEvent"
    private val PRIVO_STATE_ID = "privo_state_id"

    private val preferences: SharedPreferences = context.getSharedPreferences(PrivoPreferenceKey, Context.MODE_PRIVATE)
    private var activePrivoWebViewDialog: PrivoWebViewDialog? = null

    init {
    }

    fun toInternalEvent(from: AgeVerificationTO, userIdentifier: String?): AgeVerificationEventInternal {
        val status = from.status
        val profile = AgeVerificationProfile(
            userIdentifier = userIdentifier,
            firstName =  from. firstName,
            email = from.email,
            birthDateYYYYMMDD = from.birthDate,
            phoneNumber = from.mobilePhone
        )
        return AgeVerificationEventInternal(
            status = status,
            profile = profile,
            ageVerificationId = from.verificationIdentifier
        )
    }
    fun saveVerificationIdentifier(userIdentifier: String?, verificationIdentifier: String?) {
        val key = "${AGE_VERIFICATION_EVENT_KEY}-${userIdentifier ?: ""}"
        verificationIdentifier?.let {
            preferences.edit().putString(key, it).apply()
        } ?: run {
            preferences.edit().remove(key).apply()
        }
    }
    fun getLastEvent(userIdentifier: String?, completion: (AgeVerificationEvent) -> Unit) {
        val key = "${AGE_VERIFICATION_EVENT_KEY}-${userIdentifier ?: ""}"
        preferences.getString(key, null)?.let { verificationIdentifier ->
            PrivoInternal.rest.getAgeVerification(verificationIdentifier) {
                it?.let { verification ->
                    toInternalEvent(verification, userIdentifier).toEvent()?.let(completion)
                } ?: run {
                    completion(AgeVerificationEvent(AgeVerificationStatus.Undefined, null))
                }
            }
        } ?: run {
            completion(AgeVerificationEvent(AgeVerificationStatus.Undefined, null))
        }
    }
    private fun storeState(data: AgeVerificationStoreData, completion: (String?) -> Unit ) =
        PrivoInternal.rest.addObjectToTMPStorage(data, AgeVerificationStoreData::class.java, completion)

    fun runAgeVerification(profile: AgeVerificationProfile?, completion: (AgeVerificationEventInternal?) -> Unit) {
        val serviceIdentifier = PrivoInternal.settings.serviceIdentifier
        val ageVerificationData = AgeVerificationStoreData(
            serviceIdentifier = serviceIdentifier,
            redirectUrl =   PrivoInternal.configuration.ageVerificationPublicUrl.plus("/index.html#/age-verification-loading"),
            profile = profile
        )
        storeState(ageVerificationData) { stateId ->
            val ageVerificationUrl = "${PrivoInternal.configuration.ageVerificationPublicUrl}/index.html?${PRIVO_STATE_ID}=${stateId}&service_identifier=${serviceIdentifier}#/intro"
            val config = WebViewConfig(
                url = ageVerificationUrl,
                finishCriteria = "age-verification-loading",
                onFinish = { url ->
                    url.getQueryParameter("privo_age_verification_events_id")?.let { eventId ->
                        PrivoInternal.rest.getObjectFromTMPStorage(eventId, Array<AgeVerificationEventInternal>::class.java) { events ->
                            activePrivoWebViewDialog?.hide()
                            val nonCanceledEvents = events?.filter { it.status != AgeVerificationStatusInternal.Canceled && it.status != AgeVerificationStatusInternal.Closed } ?: emptyList()
                            val publicEvents = nonCanceledEvents.ifEmpty { events?.toList() }
                            if (!publicEvents.isNullOrEmpty()) {
                                publicEvents.forEach(completion)
                            } else {
                                completion(null)
                            }
                        }
                    } ?: run {
                        completion(null)
                    }
                }
            )
            activePrivoWebViewDialog = PrivoWebViewDialog(context, config)
            activePrivoWebViewDialog?.show()
        }
    }
    internal fun hide() = activePrivoWebViewDialog?.hide()

}