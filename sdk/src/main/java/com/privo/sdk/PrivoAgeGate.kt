package com.privo.sdk

import android.content.Context
import android.content.SharedPreferences
import com.privo.sdk.internal.PrivoInternal
import com.privo.sdk.internal.PrivoPreferenceKey
import com.privo.sdk.model.*
import java.text.SimpleDateFormat
import java.util.*

class PrivoAgeGate(val context: Context) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val ageGate = InternalAgeGate(context)
    private val verification = PrivoVerification(context)

    fun getAgeStatus(extUserId: String?, countryCode: String?, completion:(AgeGateStatus?) -> Unit) {
        ageGate.getStoredAgId()?.let { agId ->
            val record = AgStatusRecord(PrivoInternal.settings.serviceIdentifier,agId, extUserId, countryCode)
            PrivoInternal.rest.processAgStatus(record) {
                val id = it?.ageGateIdentifier
                ageGate.storeAgId(id)
                if (!id.isNullOrEmpty()) {
                    completion(it)
                } else {
                    ageGate.getFpStatus(extUserId,countryCode,completion)
                }
            }
        } ?: run {
            ageGate.getFpStatus(extUserId,countryCode,completion)
        }
    }
    fun getAgeStatusByBirthDate(birthDate: Date, extUserId: String?, countryCode: String?, completion:(AgeGateStatus?) -> Unit) {
        val textDate = dateFormat.format(birthDate)
        ageGate.getFpId { fpId ->
            fpId?.let { id ->
                val record = FpStatusRecord(PrivoInternal.settings.serviceIdentifier,id,textDate, extUserId, countryCode)
                PrivoInternal.rest.processBirthDate(record) {
                    it?.ageGateIdentifier?.let { agId ->
                        ageGate.storeAgId(agId)
                    }
                    completion(it)
                }
            }  ?: run {
                completion(null)
            }
        }
    }
    fun verifyStatus(ageGateIdentifier: String, completion: (AgeGateStatus?) -> Unit) {
        val profile = UserVerificationProfile(partnerDefinedUniqueID = "AG:$ageGateIdentifier")
        verification.showVerification(profile) { events ->
            val status = ageGate.getVerificationStatus(events,ageGateIdentifier)
            completion(status)
        }
    }
}

internal class InternalAgeGate(val context: Context) {
    internal val AG_ID = "privoAgId"
    internal val FP_ID = "privoFpId"
    private val preferences: SharedPreferences = context.getSharedPreferences(PrivoPreferenceKey, Context.MODE_PRIVATE)

    internal fun getFpId(completion: (String?) -> Unit) {
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
    internal fun getFpStatus(extUserId: String?, countryCode: String?, completion: (AgeGateStatus?) -> Unit) {
        getFpId {
            it?.let { fpId ->
                val record = FpStatusRecord(PrivoInternal.settings.serviceIdentifier, fpId, null, extUserId, countryCode)
                PrivoInternal.rest.processFpStatus(record) { status ->
                    status?.ageGateIdentifier?.let { id ->
                        storeAgId(id)
                    }
                    completion(status)
                }
            } ?: run {
                completion(null)
            }

        }
    }
    internal fun getVerificationStatus(events: Array<VerificationEvent>, ageGateIdentifier: String): AgeGateStatus? {
        val acceptedVerification = events.firstOrNull {it.result?.verificationResponse?.matchOutcome == VerificationOutcome.Pass};
        return if (acceptedVerification != null) {
            AgeGateStatus(AgeGateAction.Allow, ageGateIdentifier)
        } else {
            null
        }
    }
    internal fun getStoredAgId () = preferences.getString(AG_ID,null)
    internal fun getStoredFpId () = preferences.getString(FP_ID,null);
    internal fun storeAgId (id: String?) = storeId(id, AG_ID)
    internal fun storeFpId (id: String?) = storeId(id, FP_ID)
    private fun storeId(id: String?, key: String) {
        id?.let {
            preferences.edit().putString(key, it).apply()
        } ?: run {
            preferences.edit().remove(key).apply()
        }
    }
}