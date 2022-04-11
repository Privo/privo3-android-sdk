package com.privo.sdk.internal

import android.content.Context
import android.content.SharedPreferences
import com.privo.sdk.extensions.toEvent
import com.privo.sdk.model.*
import com.privo.sdk.model.AgeGateEventInternal
import com.privo.sdk.model.CheckAgeStoreData
import com.privo.sdk.model.WebViewConfig
import com.squareup.moshi.Moshi


internal class AgeGateInternal(val context: Context) {
    private val FP_ID = "PrivoFpId"
    private val EVENT_ID = "PrivoAgeEventId"
    private val preferences: SharedPreferences = context.getSharedPreferences(PrivoPreferenceKey, Context.MODE_PRIVATE)
    private val moshi: Moshi = Moshi
        .Builder()
        .add(VerificationMethodTypeAdapter())
        .add(AgeGateStatusInternalAdapter())
        .add(AgeGateStatusAdapter())
        .add(AgeGateActionAdapter())
        .build()
    private var activePrivoWebViewDialog: PrivoWebViewDialog? = null
    private var serviceSettings: AgeServiceSettings? = null

    init {
        PrivoInternal.rest.getAgeServiceSettings(PrivoInternal.settings.serviceIdentifier) { s ->
            serviceSettings = s
        }
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

    internal fun storeAgeGateEvent(event: AgeGateEvent) {
        val eventAdapter = moshi.adapter(AgeGateEvent::class.java)
        val eventString = eventAdapter.toJson(event)
        storeValue(eventString, EVENT_ID)
    }
    internal fun getAgeGateEvent(completion: (AgeGateEvent?) -> Unit) {
        preferences.getString(EVENT_ID,null)?.let { eventString ->
            val adapter = moshi.adapter(AgeGateEvent::class.java)
            val event = adapter.fromJson(eventString)
            completion(event)
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
                return AgeGateStatus.Blocked
            }
            AgeGateAction.Verify -> {
                return AgeGateStatus.Pending
            }
            else -> {
                return AgeGateStatus.Undefined
            }
        }
    }
    internal fun runAgeGateByBirthDay(data: CheckAgeData, completionHandler: (AgeGateEvent?) -> Unit) {
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
                        completionHandler(event)
                    } else {
                        completionHandler(null)
                    }
                }
            }
        }
    }
    private fun prepareSettings(completionHandler: (Triple<AgeServiceSettings?,String?, AgeGateEvent?>) -> Unit) {
        var settings: AgeServiceSettings? = serviceSettings
        var fpId: String? = null
        var lastEvent: AgeGateEvent? = null


        val group = DispatchGroup(3) {
            completionHandler(Triple(settings,fpId,lastEvent))
        }
        if (settings == null) {
            PrivoInternal.rest.getAgeServiceSettings(PrivoInternal.settings.serviceIdentifier) { s ->
                settings = s
                group.leave()
            }
        } else {
            group.leave()
        }
        getFpId { r ->
            fpId = r
            group.leave()
        }
        getAgeGateEvent { event ->
            lastEvent = event
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
            else -> {
                "dob"
            }
        }
    };

    internal fun runAgeGate(data: CheckAgeData, completion: (AgeGateEvent?) -> Unit) {

        prepareSettings { pSettings ->

            val settings = pSettings.first
            val fpId = pSettings.second
            val lastEvent = pSettings.third

            if (settings != null) {
                val agId = if (lastEvent?.userIdentifier ==  data.userIdentifier)  lastEvent?.agId else null
                val status = if (lastEvent?.userIdentifier == data.userIdentifier) lastEvent?.status else null

                val ageGateData = CheckAgeStoreData(
                    serviceIdentifier = PrivoInternal.settings.serviceIdentifier,
                    settings = settings,
                    userIdentifier =  data.userIdentifier,
                    countryCode = data.countryCode,
                    redirectUrl = PrivoInternal.configuration.ageGatePublicUrl.plus("/#/age-gate-loading"),
                    agId = agId,
                    fpId = fpId
                )

                storeState(ageGateData) { stateId ->
                    val ageUrl = "${PrivoInternal.configuration.ageGatePublicUrl}/?privo_state_id=${stateId}#/${getStatusTargetPage(status)}"
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
    internal fun hide() = activePrivoWebViewDialog?.hide()
}