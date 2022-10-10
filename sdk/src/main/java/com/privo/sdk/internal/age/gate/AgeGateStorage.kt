package com.privo.sdk.internal.age.gate

import android.content.Context
import android.content.SharedPreferences
import com.privo.sdk.internal.AgeSettingsInternal
import com.privo.sdk.internal.PrivoInternal
import com.privo.sdk.internal.PrivoPreferenceKey
import com.privo.sdk.model.*
import com.privo.sdk.model.AgeGateExpireEvent
import com.privo.sdk.model.AgeGateIsExpireEvent
import com.squareup.moshi.Moshi


internal class AgeGateStorage(val context: Context) {
    private val FP_ID = "PrivoFpId"
    private val AGE_GATE_ID_KEY_PREFIX = "AgeGateID"
    private val AGE_EVENT_KEY_PREFIX = "PrivoAgeGateEvent"

    internal val preferences: SharedPreferences = context.getSharedPreferences(PrivoPreferenceKey, Context.MODE_PRIVATE)
    internal var serviceSettings = AgeSettingsInternal()

    private var lastEvents = mutableMapOf<String, AgeGateExpireEvent>()

    private val moshi: Moshi = Moshi
        .Builder()
        .add(VerificationMethodTypeAdapter())
        .add(AgeGateStatusInternalAdapter())
        .add(AgeGateStatusAdapter())
        .add(AgeGateActionAdapter())
        .build()


    private fun storeValue(value: String?, key: String) {
        value?.let {
            preferences.edit().putString(key, it).apply()
        } ?: run {
            preferences.edit().remove(key).apply()
        }
    }

    internal fun getStoredFpId () = preferences.getString(FP_ID,null)
    internal fun storeFpId (id: String?) = storeValue(id, FP_ID)

    internal fun getFpId(completion: (String) -> Unit) {
        getStoredFpId()?.let {
            completion(it)
        } ?: run {
            val fingerprint = DeviceFingerprintBuilder(context).build()
            PrivoInternal.rest.generateFingerprint(fingerprint) {
                it?.id?.let { fpId ->
                    storeFpId(fpId)
                    completion(fpId)
                }
            }
        }
    }

    private fun getAgIdKey(userIdentifier: String?) = "${AGE_GATE_ID_KEY_PREFIX}-${userIdentifier ?: ""}"


    internal fun storeAgeGateEvent(event: AgeGateEvent?) {

        fun getEventExpiration(interval: Int): Long {
            return if (event?.status == AgeGateStatus.Pending) {
                // Pending Events are always expired and should be re-fetched
                System.currentTimeMillis()
            } else {
                (System.currentTimeMillis() + (interval * 1000))
            }
        }

        if (event !== null && event.status != AgeGateStatus.Canceled) {
            serviceSettings.getSettings { settings ->
                val interval = settings.poolAgeGateStatusInterval
                val expireEvent = AgeGateExpireEvent(event, getEventExpiration(interval))
                val key = event.userIdentifier ?: ""
                lastEvents[key] = expireEvent
            }
        }
        event?.agId?.let {
            val key = getAgIdKey(event.userIdentifier)
            storeValue(it,key);
        }
    }

    internal fun getStoredAgeGateId(userIdentifier: String?, completion: (String?) -> Unit) {
        val key = getAgIdKey(userIdentifier)
        preferences.getString(key,null)?.let {
            completion(it)
        } ?: run {
            // follback. TODO: remove it later (after all users will use a new storage)
            val oldKey = "${AGE_EVENT_KEY_PREFIX}-${userIdentifier ?: '0'}"
            preferences.getString(oldKey,null)?.let { eventString ->
                val adapter = moshi.adapter(AgeGateExpireEvent::class.java)
                val expireEvent = adapter.fromJson(eventString)
                val agId = expireEvent?.event?.agId
                val adIdKey = getAgIdKey(userIdentifier)
                storeValue(agId, adIdKey)
                completion(agId)
            } ?: run {
                completion(null)
            }
        }

    }

    internal fun getStoredAgeGateEvent(userIdentifier: String?): AgeGateIsExpireEvent? {
        val key = userIdentifier ?: ""
        lastEvents[key]?.let {
            return AgeGateIsExpireEvent(it.event, it.expires < System.currentTimeMillis())
        } ?: run {
            return null
        }
    }
}