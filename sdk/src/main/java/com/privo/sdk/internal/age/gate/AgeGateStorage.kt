package com.privo.sdk.internal.age.gate

import android.content.Context
import android.content.SharedPreferences
import com.privo.sdk.internal.AgeSettingsInternal
import com.privo.sdk.internal.PrivoInternal
import com.privo.sdk.internal.PrivoPreferenceKey
import com.privo.sdk.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types


internal class AgeGateStorage(val context: Context) {
    private val FP_ID = "PrivoFpId"
    private val AGE_GATE_STORED_ENTITY_KEY = "AgeGateStoredEntity"

    internal val preferences: SharedPreferences = context.getSharedPreferences(PrivoPreferenceKey, Context.MODE_PRIVATE)
    internal var serviceSettings = AgeSettingsInternal()

    internal val moshi: Moshi = Moshi
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

    private fun getStoredEntitiesKey (): String {
        return "${AGE_GATE_STORED_ENTITY_KEY}-${PrivoInternal.settings.envType}"
    }
    private fun getFpIdKey (): String {
        return "${FP_ID}-${PrivoInternal.settings.envType}"
    }

    internal fun getStoredFpId () = preferences.getString(getFpIdKey(),null)
    internal fun storeFpId (id: String?) = storeValue(id, getFpIdKey())

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

    internal fun getAgeGateStoredEntities(completion: (Set<AgeGateStoredEntity>) -> Unit) {
        preferences.getString(getStoredEntitiesKey(),null)?.let { jsonString ->
            val adapter = moshi.adapter<Set<AgeGateStoredEntity>>(
                Types.newParameterizedType(Set::class.java, AgeGateStoredEntity::class.java)
            )
            val entities = adapter.fromJson(jsonString)
            completion(entities ?: emptySet())
        } ?: run {
            completion(emptySet())
        }
    }
    internal fun storeAgId(userIdentifier: String?, nickname: String?, agId: String) {
        val newEntity = AgeGateStoredEntity(userIdentifier = userIdentifier, nickname = nickname, agId = agId)
        getAgeGateStoredEntities { entities ->
            val newEntities = entities + newEntity

            val adapter = moshi.adapter<Set<AgeGateStoredEntity>>(
                Types.newParameterizedType(Set::class.java, AgeGateStoredEntity::class.java)
            )
            adapter.toJson(newEntities)?.let {
                storeValue(it, getStoredEntitiesKey());
            }
        }
    }
    internal fun storeInfoFromEvent(event: AgeGateEvent?) {
        event?.agId?.let { agId ->
            storeAgId(userIdentifier = event.userIdentifier, nickname = event.nickname, agId = agId)
        }
    }

    internal fun getStoredAgeGateId(userIdentifier: String?, nickname: String?, completion: (String?) -> Unit) {
        getAgeGateStoredEntities { entities ->
            val ageGateData = entities.firstOrNull { ent ->
                if (userIdentifier != null) {
                    return@firstOrNull ent.userIdentifier == userIdentifier
                } else {
                    return@firstOrNull ent.nickname == nickname
                }
            }
            ageGateData?.let {
                completion(it.agId)
            } ?: run {
                completion(null)

            }
        }
    }
}