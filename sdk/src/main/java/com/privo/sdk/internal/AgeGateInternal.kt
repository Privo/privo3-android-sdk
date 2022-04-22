package com.privo.sdk.internal

import android.content.Context
import android.content.SharedPreferences
import com.privo.sdk.extensions.toEvent
import com.privo.sdk.extensions.toStatus
import com.privo.sdk.model.*
import com.privo.sdk.model.AgeGateEventInternal
import com.privo.sdk.model.CheckAgeStoreData
import com.privo.sdk.model.WebViewConfig
import com.squareup.moshi.Moshi


internal class AgeGateInternal(val context: Context) {
    private val FP_ID = "PrivoFpId"
    private val AGE_EVENT_KEY_PREFIX = "PrivoAgeGateEvent"
    private val preferences: SharedPreferences = context.getSharedPreferences(PrivoPreferenceKey, Context.MODE_PRIVATE)
    private var serviceSettings = AgeSettingsInternal()
    private val moshi: Moshi = Moshi
        .Builder()
        .add(VerificationMethodTypeAdapter())
        .add(AgeGateStatusInternalAdapter())
        .add(AgeGateStatusAdapter())
        .add(AgeGateActionAdapter())
        .build()
    private var activePrivoWebViewDialog: PrivoWebViewDialog? = null

    init {
    }

    private fun storeValue(value: String?, key: String) {
        value?.let {
            preferences.edit().putString(key, it).apply()
        } ?: run {
            preferences.edit().remove(key).apply()
        }
    }
    private fun getStoredFpId () = preferences.getString(FP_ID,null)
    private fun storeFpId (id: String?) = storeValue(id, FP_ID)

    private fun getFpId(completion: (String?) -> Unit) {
        getStoredFpId()?.let {
            completion(it)
        } ?: run {
            try {
                val fingerprint = DeviceFingerprintBuilder(context).build()
                PrivoInternal.rest.generateFingerprint(fingerprint) {
                    val fpId = it?.id
                    fpId?.let { id ->
                        storeFpId(id)
                    }
                    completion(fpId)
                }
            } catch (e: Exception) {
                completion(null)
            }
        }
    }

    internal fun storeAgeGateEvent(event: AgeGateEvent?) {

        fun getEventExpiration(interval: Int): Long {
            return if (event?.status == AgeGateStatus.Pending) {
                // Pending Events are always expired and should be re-fetched
                System.currentTimeMillis()
            } else {
                (System.currentTimeMillis() + (interval * 1000))
            }
        };

        if (event !== null && event.status != AgeGateStatus.Canceled && event.status != AgeGateStatus.Undefined) {
            serviceSettings.getSettings { settings ->
                val interval = settings.poolAgeGateStatusInterval
                val expireEvent = AgeGateExpireEvent(event, getEventExpiration(interval))

                val eventAdapter = moshi.adapter(AgeGateExpireEvent::class.java)
                val eventString = eventAdapter.toJson(expireEvent)
                val key = "${AGE_EVENT_KEY_PREFIX}-${event.userIdentifier ?: '0'}"
                storeValue(eventString, key)
            }
        }
    }
    internal fun getAgeGateEvent(userIdentifier: String?, completion: (AgeGateIsExpireEvent?) -> Unit) {
        val key = "${AGE_EVENT_KEY_PREFIX}-${userIdentifier ?: '0'}"
        preferences.getString(key,null)?.let { eventString ->
            val adapter = moshi.adapter(AgeGateExpireEvent::class.java)
            val expireEvent = adapter.fromJson(eventString)

            if (expireEvent != null) {
                val event = AgeGateIsExpireEvent(expireEvent.event, expireEvent.expires < System.currentTimeMillis())
                completion(event);
            }else {
                completion(null)
            }
        } ?: run {
            completion(null)
        }
    }
    private fun toStatus(action: AgeGateAction?): AgeGateStatus {
        when (action) {
            AgeGateAction.Allow -> {
                return AgeGateStatus.Allowed
            }
            AgeGateAction.Block -> {
                return AgeGateStatus.Blocked
            }
            AgeGateAction.Consent -> {
                return AgeGateStatus.ConsentRequired
            }
            AgeGateAction.IdentityVerify -> {
                return AgeGateStatus.IdentityVerificationRequired
            }
            else -> {
                return AgeGateStatus.Undefined
            }
        }
    }

    internal fun getStatusEvent(userIdentifier: String?, completionHandler:(AgeGateEvent) -> Unit) {
        getAgeGateEvent(userIdentifier) { expireEvent ->
            if (expireEvent != null && !expireEvent?.isExpire) {
                // Force return event if we found non-expired one
                completionHandler(expireEvent.event)
                return@getAgeGateEvent
            }
            val lastEvent = expireEvent?.event
            getFpId { fpId->
                val agId = lastEvent?.agId;
                if (agId != null && fpId != null) {
                    val record = StatusRecord(
                        PrivoInternal.settings.serviceIdentifier,
                        fpId,
                        agId,
                        userIdentifier
                    )
                    PrivoInternal.rest.processStatus(record) { response ->
                        if (response != null) {
                            val event = AgeGateEvent(response.toStatus(), userIdentifier, agId)
                            completionHandler(event)
                        } else {
                            completionHandler(
                                AgeGateEvent(
                                    AgeGateStatus.Undefined,
                                    userIdentifier,
                                    agId
                                )
                            )
                        }
                    }
                } else {
                    completionHandler(
                        AgeGateEvent(
                            lastEvent?.status ?: AgeGateStatus.Undefined,
                            userIdentifier,
                            lastEvent?.agId
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
        getFpId { fpId ->
            val birthDateYYYMMDD = data.birthDateYYYYMMDD
            if (fpId != null && birthDateYYYMMDD != null) {
                val record = FpStatusRecord(
                    PrivoInternal.settings.serviceIdentifier,
                    fpId,
                    birthDateYYYMMDD,
                    data.userIdentifier,
                    data.countryCode
                )
                PrivoInternal.rest.processBirthDate(record) { response ->
                    if (response != null) {
                        val status = toStatus(response.action)
                        val event = AgeGateEvent(status,data.userIdentifier,response.ageGateIdentifier)

                        if (
                            response.action == AgeGateAction.Consent ||
                            response.action == AgeGateAction.IdentityVerify ||
                            response.action == AgeGateAction.AgeVerify
                        ) {
                            runAgeGate(data,event,completionHandler)
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
    private fun prepareSettings(completionHandler: (Pair<AgeServiceSettings?,String??>) -> Unit) {
        var settings: AgeServiceSettings? = null
        var fpId: String? = null

        val group = DispatchGroup(2) {
            completionHandler(Pair(settings,fpId))
        }
        serviceSettings.getSettings { s ->
            settings = s
            group.leave()
        }
        getFpId { r ->
            fpId = r
            group.leave()
        }
    }

    private fun storeState(data: CheckAgeStoreData, completion: (String?) -> Unit ) =
        PrivoInternal.rest.addObjectToTMPStorage(data, CheckAgeStoreData::class.java, completion)


    private fun getStatusTargetPage(status: AgeGateStatus?): String {
        return when (status) {
            AgeGateStatus.Pending -> {
                "verification-pending"
            }
            AgeGateStatus.Blocked -> {
                "sorry"
            }
            AgeGateStatus.ConsentRequired -> {
                "request-consent"
            }
            AgeGateStatus.AgeVerificationRequired -> {
                "request-consent"
            }
            AgeGateStatus.IdentityVerificationRequired -> {
                "request-verification"
            } else -> {
                "dob"
            }
        }
    };

    internal fun runAgeGate(
        data: CheckAgeData,
        lastEvent: AgeGateEvent?,
        completion: (AgeGateEvent?) -> Unit
    ) {
        prepareSettings { pSettings ->

            val settings = pSettings.first
            val fpId = pSettings.second

            if (settings != null) {
                val agId = lastEvent?.agId
                val status = lastEvent?.status
                val ageGateData = CheckAgeStoreData(
                    serviceIdentifier = PrivoInternal.settings.serviceIdentifier,
                    settings = settings,
                    userIdentifier =  data.userIdentifier,
                    countryCode = data.countryCode,
                    birthDateYYYYMMDD = data.birthDateYYYYMMDD,
                    redirectUrl = PrivoInternal.configuration.ageGatePublicUrl.plus("/index.html#/age-gate-loading"),
                    agId = agId,
                    fpId = fpId
                )

                storeState(ageGateData) { stateId ->
                    val ageUrl = "${PrivoInternal.configuration.ageGatePublicUrl}/index.html?privo_age_gate_state_id=${stateId}#/${getStatusTargetPage(status)}"
                    val config = WebViewConfig(
                        url = ageUrl,
                        finishCriteria = "age-gate-loading",
                        onFinish = { url ->
                            url.getQueryParameter("privo_age_gate_events_id")?.let { eventId ->
                                PrivoInternal.rest.getObjectFromTMPStorage(eventId, Array<AgeGateEventInternal>::class.java) { events ->
                                    activePrivoWebViewDialog?.hide()
                                    events?.mapNotNull { it.toEvent() }?.forEach { event ->
                                        completion(event)
                                    } ?: run {
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

    internal fun runAgeGateRecheck(
        data: CheckAgeData,
        completion: (AgeGateEvent?) -> Unit
    ) {

        getAgeGateEvent(data.userIdentifier) { expireEvent ->
            prepareSettings { pSettings ->

                val settings = pSettings.first
                val fpId = pSettings.second

                if (settings != null) {
                    val agId = expireEvent?.event?.agId
                    val ageGateData = CheckAgeStoreData(
                        serviceIdentifier = PrivoInternal.settings.serviceIdentifier,
                        settings = settings,
                        userIdentifier = data.userIdentifier,
                        countryCode = data.countryCode,
                        birthDateYYYYMMDD = null,
                        redirectUrl = PrivoInternal.configuration.ageGatePublicUrl.plus("/index.html#/age-gate-loading"),
                        agId = agId,
                        fpId = fpId
                    )

                    storeState(ageGateData) { stateId ->
                        val ageUrl =
                            "${PrivoInternal.configuration.ageGatePublicUrl}/?privo_age_gate_state_id=${stateId}#/recheck"
                        val config = WebViewConfig(
                            url = ageUrl,
                            finishCriteria = "age-gate-loading",
                            onFinish = { url ->
                                url.getQueryParameter("privo_age_gate_events_id")?.let { eventId ->
                                    PrivoInternal.rest.getObjectFromTMPStorage(
                                        eventId,
                                        Array<AgeGateEventInternal>::class.java
                                    ) { events ->
                                        activePrivoWebViewDialog?.hide()
                                        events?.mapNotNull { it.toEvent() }?.forEach { event ->
                                            completion(event)
                                        } ?: run {
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
    }
    internal fun hide() = activePrivoWebViewDialog?.hide()
}