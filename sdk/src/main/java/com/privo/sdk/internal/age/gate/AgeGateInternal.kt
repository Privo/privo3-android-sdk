package com.privo.sdk.internal.age.gate

import android.content.Context
import com.privo.sdk.extensions.toEvent
import com.privo.sdk.extensions.toStatus
import com.privo.sdk.internal.*
import com.privo.sdk.internal.PrivoInternal
import com.privo.sdk.model.*
import java.util.*


internal class AgeGateInternal(val context: Context) {

    internal val storage = AgeGateStorage(context)
    internal val helpers = AgeGateHelpers(context)
    private var activePrivoWebViewDialog: PrivoWebViewDialog? = null


    internal fun processStatus(
        userIdentifier: String?,
        agId: String?,
        fpId: String,
        completionHandler:(AgeGateEvent) -> Unit) {

        val record = StatusRecord(
            PrivoInternal.settings.serviceIdentifier,
            fpId,
            agId,
            userIdentifier
        )
        PrivoInternal.rest.processStatus(record) { response ->
            if (response != null) {
                val status = response.status.toStatus()
                val event = AgeGateEvent(status, userIdentifier, response.agId, response.ageRange)
                completionHandler(event)
            } else {
                completionHandler(
                    AgeGateEvent(
                        AgeGateStatus.Undefined,
                        userIdentifier,
                        agId,
                        null
                    )
                )
            }
        }
    }


    internal fun getStatusEvent(userIdentifier: String?, completionHandler:(AgeGateEvent) -> Unit) {
        val expireEvent = storage.getStoredAgeGateEvent(userIdentifier)
        if (expireEvent?.isExpire == false) {
            expireEvent.event.let {
                // Force return event if we found non-expired one
                completionHandler(it)
                return
            }
        }
        storage.getStoredAgeGateId(userIdentifier) { agId ->
            storage.getFpId { fpId ->
                processStatus(
                    userIdentifier,
                    agId,
                    fpId
                ) { event ->
                    storage.storeAgeGateEvent(event)
                    completionHandler(event)
                }
            }
        }
    }

    private fun getCurrentAgeState(
        userIdentifier: String?,
        prevEvent: AgeGateEvent?,
        completion: (AgeState?) -> Unit)
    {
        storage.getStoredAgeGateId(userIdentifier) { agId ->
            storage.getFpId { fpId ->

                var settings: AgeServiceSettings? = null
                var event: AgeGateEvent? = null
                val group = DispatchGroup(2) {
                    settings?.let { settings ->
                        completion(AgeState(
                            fpId,
                            agId,
                            settings,
                            event
                        ))
                    } ?: run {
                        completion(null)
                    }
                }
                storage.serviceSettings.getSettings {
                    settings = it
                    group.leave()
                }
                if (prevEvent != null) {
                    event = prevEvent
                    group.leave()
                } else {
                    processStatus(
                        userIdentifier,
                        agId,
                        fpId
                    ) {
                        event = it
                        group.leave()
                    }
                }
            }
        }
    }


    internal fun runAgeGateByBirthDay(
        data: CheckAgeData,
        completionHandler: (AgeGateEvent?) -> Unit
    ) {
        storage.getFpId { fpId ->
            if ((data.birthDateYYYYMMDD != null || data.birthDateYYYYMM != null || data.birthDateYYYY != null)) {
                val record = FpStatusRecord(
                    PrivoInternal.settings.serviceIdentifier,
                    fpId,
                    data.birthDateYYYYMMDD,
                    data.birthDateYYYYMM,
                    data.birthDateYYYY,
                    data.userIdentifier,
                    data.countryCode
                )
                PrivoInternal.rest.processBirthDate(record) { response ->
                    if (response != null) {
                        val status = helpers.toStatus(response.action)
                        val event = AgeGateEvent(status,data.userIdentifier,response.agId, response.ageRange)

                        if (
                            response.action == AgeGateAction.Consent ||
                            response.action == AgeGateAction.IdentityVerify ||
                            response.action == AgeGateAction.AgeVerify
                        ) {
                            runAgeGate(data,event,false,completionHandler)
                        } else {
                            completionHandler(event)
                        }
                    } else {
                        completionHandler(null)
                    }
                }
            }
        }
    }
    internal fun recheckAgeGateByBirthDay(
        data: CheckAgeData,
        completionHandler: (AgeGateEvent?) -> Unit
    ) {
        storage.getStoredAgeGateId(data.userIdentifier) { agId ->
            if (agId != null && (data.birthDateYYYYMMDD != null || data.birthDateYYYYMM != null || data.birthDateYYYY != null)) {
                val record = RecheckStatusRecord(
                    PrivoInternal.settings.serviceIdentifier,
                    agId,
                    data.birthDateYYYYMMDD,
                    data.birthDateYYYYMM,
                    data.birthDateYYYY,
                    data.countryCode
                )
                PrivoInternal.rest.processRecheck(record) { response ->
                    if (response != null) {
                        val status = helpers.toStatus(response.action)
                        val event = AgeGateEvent(status,data.userIdentifier,response.agId, response.ageRange)
                        if (
                            response.action == AgeGateAction.Consent ||
                            response.action == AgeGateAction.IdentityVerify ||
                            response.action == AgeGateAction.AgeVerify
                        ) {
                            runAgeGate(data,event,false,completionHandler)
                        } else {
                            completionHandler(event)
                        }
                    } else {
                        completionHandler(null)
                    }
                }
            }
        }
    }

    private fun storeState(data: CheckAgeStoreData, completion: (String?) -> Unit ) =
        PrivoInternal.rest.addObjectToTMPStorage(data, CheckAgeStoreData::class.java, completion)



    internal fun runAgeGate(
        data: CheckAgeData,
        prevEvent: AgeGateEvent?,
        recheckRequired: Boolean,
        completion: (AgeGateEvent?) -> Unit
    ) {
        getCurrentAgeState(data.userIdentifier, prevEvent) { state ->
            val serviceIdentifier = PrivoInternal.settings.serviceIdentifier

            if (state?.settings != null) {
                val fpId = state.fpId
                val agId = state.agId
                val status = state.event?.status
                val ageGateData = CheckAgeStoreData(
                    serviceIdentifier = serviceIdentifier,
                    settings = state.settings,
                    userIdentifier =  data.userIdentifier,
                    countryCode = data.countryCode,
                    birthDateYYYYMMDD = data.birthDateYYYYMMDD,
                    birthDateYYYYMM = data.birthDateYYYYMM,
                    birthDateYYYY = data.birthDateYYYY,
                    redirectUrl = PrivoInternal.configuration.ageGatePublicUrl.plus("/index.html#/age-gate-loading"),
                    agId = agId,
                    fpId = fpId
                )

                storeState(ageGateData) { stateId ->
                    val ageUrl = "${PrivoInternal.configuration.ageGatePublicUrl}/index.html?privo_age_gate_state_id=${stateId}&service_identifier=${serviceIdentifier}#/${helpers.getStatusTargetPage(status,recheckRequired)}"
                    val config = WebViewConfig(
                        url = ageUrl,
                        finishCriteria = "age-gate-loading",
                        onFinish = { url ->
                            url.getQueryParameter("privo_age_gate_events_id")?.let { eventId ->
                                PrivoInternal.rest.getObjectFromTMPStorage(eventId, Array<AgeGateEventInternal>::class.java) { events ->
                                    activePrivoWebViewDialog?.hide()
                                    val nonCanceledEvents = events?.filter { it.status != AgeGateStatusInternal.Canceled && it.status != AgeGateStatusInternal.Closed } ?: emptyList()
                                    val publicEvents = nonCanceledEvents.ifEmpty { events?.toList() }
                                    if (!publicEvents.isNullOrEmpty()) {
                                        publicEvents.forEach {completion(it.toEvent())}
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
        }
    }

    internal fun showAgeGateIdentifier(userIdentifier: String?) {
        storage.getStoredAgeGateId(userIdentifier) { agId ->
            storage.getFpId { fpId ->
                storage.serviceSettings.getSettings { settings ->

                    val serviceIdentifier = PrivoInternal.settings.serviceIdentifier
                    val ageGateData = CheckAgeStoreData(
                        serviceIdentifier = serviceIdentifier,
                        settings = settings,
                        userIdentifier =  userIdentifier,
                        agId = agId,
                        fpId = fpId,
                        redirectUrl = null,
                        countryCode = null,
                        birthDateYYYYMMDD = null,
                        birthDateYYYYMM = null,
                        birthDateYYYY = null
                    )

                    storeState(ageGateData) { stateId ->
                        val ageUrl =
                            "${PrivoInternal.configuration.ageGatePublicUrl}/index.html?privo_age_gate_state_id=${stateId}&service_identifier=${serviceIdentifier}#/age-gate-identifier"
                        val config = WebViewConfig(
                            url = ageUrl,
                            finishCriteria = "identifier-closed",
                            onFinish = {
                                activePrivoWebViewDialog?.hide()
                            }
                        )
                        activePrivoWebViewDialog = PrivoWebViewDialog(context, config)
                        activePrivoWebViewDialog?.show()
                    }
                }
            }
        }
    }
    internal fun hide() = activePrivoWebViewDialog?.hide()
}