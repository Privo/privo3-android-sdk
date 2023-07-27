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
    internal val helpers = AgeGateHelpers(context, storage.serviceSettings)
    internal val permissions = PermissionsInternal(context)
    private var activePrivoWebViewDialog: PrivoWebViewDialog? = null

    internal fun processStatus(
        userIdentifier: String?,
        nickname: String?,
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
                val event = AgeGateEvent(
                    status = status,
                    userIdentifier = response.extUserId,
                    nickname = nickname,
                    agId = response.agId,
                    ageRange = response.ageRange,
                    countryCode = response.countryCode
                )
                completionHandler(event)
            } else {
                completionHandler(
                    AgeGateEvent(
                        status = AgeGateStatus.Undefined,
                        userIdentifier = userIdentifier,
                        nickname = nickname,
                        agId = agId,
                        ageRange = null,
                        countryCode = null,
                    )
                )
            }
        }
    }


    internal fun getStatusEvent(userIdentifier: String?, nickname: String?, completionHandler:(AgeGateEvent) -> Unit) {

        storage.getStoredAgeGateId(userIdentifier = userIdentifier, nickname = nickname) { agId ->
            storage.getFpId { fpId ->
                if (agId == null && nickname != null) {
                    // for case of a new nickname
                    processStatus(
                        userIdentifier = null,
                        nickname = nickname,
                        agId = null,
                        fpId = fpId,
                        completionHandler = completionHandler
                    )
                } else {
                    processStatus(
                        userIdentifier = userIdentifier,
                        nickname = nickname,
                        agId = agId,
                        fpId = fpId,
                        completionHandler = completionHandler
                    )
                }
            }
        }
    }


    internal fun linkUser(userIdentifier: String, agId: String,nickname: String?,completionHandler:(AgeGateEvent) -> Unit) {

        storage.getAgeGateStoredEntities { entities ->

            val isKnownAgId = entities.any {it.agId == agId}
            if (!isKnownAgId) {
                val warning = AgeGateLinkWarning(description =  "Age Gate Id wasn't found in the store during Age Gate 'link user' call", agIdEntities = entities)

                val adapter = storage.moshi.adapter(AgeGateLinkWarning::class.java)
                adapter.toJson(warning)?.let {
                    val event = AnalyticEvent(serviceIdentifier = PrivoInternal.settings.serviceIdentifier, data = it)
                    PrivoInternal.rest.sendAnalyticEvent(event)
                }
            }
            val record = LinkUserStatusRecord(
                serviceIdentifier = PrivoInternal.settings.serviceIdentifier,
                agId = agId,
                extUserId = userIdentifier
            )
            PrivoInternal.rest.processLinkUser(record) { response ->
                        response?.let { res ->
                            val event = AgeGateEvent(
                                status = res.status.toStatus(),
                                userIdentifier = res.extUserId,
                                nickname = nickname,
                                agId = res.agId ?: agId,
                                ageRange = res.ageRange,
                                countryCode = res.countryCode
                            )
                            completionHandler(event)
                        } ?: run {
                            completionHandler(AgeGateEvent(
                                status = AgeGateStatus.Undefined,
                                userIdentifier = userIdentifier,
                                nickname = nickname,
                                agId = agId,
                                ageRange = null,
                                countryCode = null,
                            ))
                        }
                }
            }
    }

    private fun getAgeGateState(
        userIdentifier: String?,
        nickname: String?,
        completion: (AgeState?) -> Unit)
    {
        storage.getStoredAgeGateId(userIdentifier = userIdentifier, nickname = nickname) { agId ->
            storage.getFpId { fpId ->
                storage.serviceSettings.getSettings { settings ->
                    completion(
                        AgeState(
                            fpId = fpId,
                            agId = agId,
                            settings = settings,
                        )
                    )
                }
            }
        }
    }


    internal fun runAgeGateByBirthDay(
        data: CheckAgeData,
        completionHandler: (AgeGateEvent?) -> Unit
    ) {
        storage.getFpId { fpId ->
            if ((data.birthDateYYYYMMDD != null || data.birthDateYYYYMM != null || data.birthDateYYYY != null || data.age != null)) {
                val record = FpStatusRecord(
                    PrivoInternal.settings.serviceIdentifier,
                    fpId,
                    data.birthDateYYYYMMDD,
                    data.birthDateYYYYMM,
                    data.birthDateYYYY,
                    data.age,
                    data.userIdentifier,
                    data.countryCode
                )
                PrivoInternal.rest.processBirthDate(record,{ response ->
                    if (response != null) {
                        val status = helpers.toStatus(response.action)
                        val event = AgeGateEvent(
                            status = status,
                            userIdentifier = response.extUserId,
                            agId = response.agId,
                            nickname = data.nickname,
                            ageRange = response.ageRange,
                            countryCode = response.countryCode,
                        )

                        if (
                            response.action == AgeGateAction.Consent ||
                            response.action == AgeGateAction.IdentityVerify ||
                            response.action == AgeGateAction.AgeVerify
                        ) {
                            runAgeGate(data,event,null, completionHandler)
                        } else {
                            completionHandler(event)
                        }
                    } else {
                        completionHandler(null)
                    }
                }, {
                    runAgeGate(data,null,AgeGateInternalAction.AgeEstimationRequired,completionHandler)
                })
            }
        }
    }
    internal fun recheckAgeGateByBirthDay(
        data: CheckAgeData,
        completionHandler: (AgeGateEvent?) -> Unit
    ) {
        storage.getStoredAgeGateId(data.userIdentifier, nickname = data.nickname) { agId ->
            if (agId != null && (data.birthDateYYYYMMDD != null || data.birthDateYYYYMM != null || data.birthDateYYYY != null || data.age != null)) {
                val record = RecheckStatusRecord(
                    PrivoInternal.settings.serviceIdentifier,
                    agId,
                    data.birthDateYYYYMMDD,
                    data.birthDateYYYYMM,
                    data.birthDateYYYY,
                    data.age,
                    data.countryCode
                )
                PrivoInternal.rest.processRecheck(record, { response ->
                    if (response != null) {
                        val status = helpers.toStatus(response.action)
                        val event = AgeGateEvent(
                            status = status,
                            userIdentifier = response.extUserId,
                            agId = response.agId,
                            nickname = data.nickname,
                            ageRange = response.ageRange,
                            countryCode = response.countryCode,
                        )
                        if (
                            response.action == AgeGateAction.Consent ||
                            response.action == AgeGateAction.IdentityVerify ||
                            response.action == AgeGateAction.AgeVerify
                        ) {
                            runAgeGate(data,event,null,completionHandler)
                        } else {
                            completionHandler(event)
                        }
                    } else {
                        completionHandler(null)
                    }
                }, {
                    runAgeGate(data,null,AgeGateInternalAction.AgeEstimationRecheckRequired,completionHandler)
                })
            }
        }
    }

    private fun storeState(data: CheckAgeStoreData, completion: (String?) -> Unit ) =
        PrivoInternal.rest.addObjectToTMPStorage(data, CheckAgeStoreData::class.java, completion)



    internal fun runAgeGate(
        data: CheckAgeData,
        prevEvent: AgeGateEvent?,
        requiredAction: AgeGateInternalAction?,
        completion: (AgeGateEvent?) -> Unit
    ) {
        getAgeGateState(data.userIdentifier, data.nickname) { state ->
            val serviceIdentifier = PrivoInternal.settings.serviceIdentifier

            if (state?.settings != null) {
                val fpId = state.fpId
                val agId = state.agId
                val ageGateData = CheckAgeStoreData(
                    serviceIdentifier = serviceIdentifier,
                    settings = state.settings,
                    userIdentifier =  data.userIdentifier,
                    countryCode = data.countryCode,
                    birthDateYYYYMMDD = data.birthDateYYYYMMDD,
                    birthDateYYYYMM = data.birthDateYYYYMM,
                    birthDateYYYY = data.birthDateYYYY,
                    age = data.age,
                    redirectUrl = PrivoInternal.configuration.ageGatePublicUrl.plus("/index.html#/age-gate-loading"),
                    agId = agId,
                    fpId = fpId
                )

                storeState(ageGateData) { stateId ->
                    val ageUrl = "${PrivoInternal.configuration.ageGatePublicUrl}/index.html?privo_age_gate_state_id=${stateId}&service_identifier=${serviceIdentifier}#/${helpers.getStatusTargetPage(prevEvent?.status,requiredAction)}"
                    // val ageUrl = "https://webrtc.github.io/samples/src/content/getusermedia/gum/"
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
                                        publicEvents.forEach {
                                            val event = it.toEvent()
                                            if (event?.status == AgeGateStatus.IdentityVerified || event?.status == AgeGateStatus.AgeVerified) {
                                                // sync status to get Correct Age Range
                                                processStatus(
                                                    userIdentifier = event.userIdentifier,
                                                    nickname = data.nickname,
                                                    agId = event.agId,
                                                    fpId = state.fpId,
                                                    completionHandler = completion
                                                )
                                            } else {
                                                completion(event)
                                            }
                                        }
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

            } else {
                completion(null)
            }
        }
    }

    internal fun showAgeGateIdentifier(userIdentifier: String?, nickname: String?) {
        storage.getStoredAgeGateId(userIdentifier, nickname) { agId ->
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
                        birthDateYYYY = null,
                        age = null
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