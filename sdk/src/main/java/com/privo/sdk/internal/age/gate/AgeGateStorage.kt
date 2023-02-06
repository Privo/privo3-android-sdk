package com.privo.sdk.internal.age.gate

import android.content.Context
import android.content.SharedPreferences
import com.privo.sdk.internal.AgeSettingsInternal
import com.privo.sdk.internal.PrivoInternal
import com.privo.sdk.internal.PrivoPreferenceKey
import com.privo.sdk.model.*
import com.privo.sdk.model.AgeGateExpireEvent
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types


internal class AgeGateStorage(val context: Context) {
    private val FP_ID = "PrivoFpId"
    private val AGE_GATE_STORED_ENTITY_KEY = "AgeGateStoredEntity"
    private val AGE_GATE_ID_KEY_PREFIX = "AgeGateID"
    private val AGE_EVENT_KEY_PREFIX = "PrivoAgeGateEvent"

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

    internal fun getAgeGateStoredEntities(completion: (Set<AgeGateStoredEntity>) -> Unit) {
        preferences.getString(AGE_GATE_STORED_ENTITY_KEY,null)?.let { jsonString ->
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
                storeValue(it, AGE_GATE_STORED_ENTITY_KEY);
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
                // fallback 1. TODO: remove it later (after all users will use a new storage)
                val oldKey = "${AGE_GATE_ID_KEY_PREFIX}-${userIdentifier ?: ""}"
                preferences.getString(oldKey,null)?.let { agId ->
                    storeAgId(userIdentifier = userIdentifier, nickname = nickname, agId = agId)
                    completion(agId)
                } ?: run {
                    // fallback 2. TODO: remove it later (after all users will use a new storage)
                    val oldKey2 = "${AGE_EVENT_KEY_PREFIX}-${userIdentifier ?: '0'}"
                    preferences.getString(oldKey2,null)?.let { eventString ->
                        val adapter = moshi.adapter(AgeGateExpireEvent::class.java)
                        val expireEvent = adapter.fromJson(eventString)
                        expireEvent?.event?.agId?.let { agId ->
                            storeAgId(
                                userIdentifier = userIdentifier,
                                nickname = nickname,
                                agId = agId
                            )
                            completion(agId)
                        } ?: run {
                            completion(null)
                        }
                    } ?: run {
                        completion(null)
                    }
                }
            }
        }
    }
}