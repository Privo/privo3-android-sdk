package com.privo.sdk.api

import android.os.Handler
import android.os.Looper
import com.privo.sdk.internal.PrivoInternal
import com.privo.sdk.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import java.util.Date

class Rest {
    private val client = OkHttpClient()
    private val moshi: Moshi = Moshi
        .Builder()
        .add(Date::class.java, Rfc3339DateJsonAdapter())
        .add(VerificationMethodTypeAdapter())
        .add(VerificationOutcomeAdapter())
        .add(AgeGateActionAdapter())
        .build()
    private val JSON : MediaType = "application/json; charset=utf-8".toMediaType()

    private fun runOnMainThread(completion: () -> Unit) = Handler(Looper.getMainLooper()).post(completion)

    private fun <T>getMoshiCallback(type: Class<T>, completion:(T?) -> Unit): Callback {
        return object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // TODO: Add error completion here
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val json = response.body?.string()
                    if (!json.isNullOrEmpty()) {
                        try {
                            val obj = moshi.adapter(type).fromJson(json)
                            runOnMainThread { completion(obj) }
                            return
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                runOnMainThread {
                    completion(null)
                }
            }
        }
    }

    fun <T>getObjectFromTMPStorage(key: String, clazz:  Class<T>, completion: (T?) -> Unit) {
        val tmpStorageURL = PrivoInternal.configuration.helpersUrl
            .toHttpUrl()
            .newBuilder()
            .addPathSegment("storage")
            .addPathSegment(key)
            .build()
        val request = Request.Builder().url(tmpStorageURL).build()
        val valueAdapter = moshi.adapter(clazz)
        val callback = getMoshiCallback(TmpStringObject::class.java) {
            it?.let { wrapper ->
                val value = valueAdapter.fromJson(wrapper.data)
                completion(value)
            } ?: run {
                completion(null)
            }
        }
        client.newCall(request).enqueue(callback)
    }

    fun <T>addObjectToTMPStorage(value: T, clazz:  Class<T>, completion: ((String?) -> Unit), ttl: Int? = null, ) {
        val valueAdapter = moshi.adapter(clazz)
        val wrapperAdapter = moshi.adapter(TmpStringObject::class.java)

        val tmpStorageURL = PrivoInternal.configuration.helpersUrl
            .toHttpUrl()
            .newBuilder()
            .addPathSegment("storage")
            .addPathSegment("put")
            .build()
        val valueString = valueAdapter.toJson(value)
        val postBody = wrapperAdapter
            .toJson(TmpStringObject(valueString, ttl))
            .toRequestBody(JSON)

        val request = Request.Builder()
            .url(tmpStorageURL)
            .post(postBody)
            .build()

        val callback = getMoshiCallback(TmpStorageResponse::class.java) {
            completion(it?.id)
        }
        client.newCall(request).enqueue(callback)
    }
    fun processAgStatus(data: AgStatusRecord, completion: (AgeGateStatus?) -> Unit) {
        val url = PrivoInternal.configuration.ageGateUrl
            .toHttpUrl()
            .newBuilder()
            .addPathSegments("age-gate/status/ag-id")
            .build()
        val adapter = moshi.adapter(AgStatusRecord::class.java)
        val body = adapter
            .toJson(data)
            .toRequestBody(JSON)
        val request = Request.Builder()
            .url(url)
            .put(body)
            .build()

        val callback = getMoshiCallback(AgeGateStatus::class.java,completion)
        client.newCall(request).enqueue(callback)
    }

    fun processFpStatus(data: FpStatusRecord, completion: (AgeGateStatus?) -> Unit) {
        val url = PrivoInternal.configuration.ageGateUrl
            .toHttpUrl()
            .newBuilder()
            .addPathSegments("age-gate/status/fp-id")
            .build()
        val adapter = moshi.adapter(FpStatusRecord::class.java)
        val body = adapter
            .toJson(data)
            .toRequestBody(JSON)
        val request = Request.Builder()
            .url(url)
            .put(body)
            .build()

        val callback = getMoshiCallback(AgeGateStatus::class.java,completion)
        client.newCall(request).enqueue(callback)
    }
    fun processBirthDate(data: FpStatusRecord, completion: (AgeGateStatus?) -> Unit) {
        val url = PrivoInternal.configuration.ageGateUrl
            .toHttpUrl()
            .newBuilder()
            .addPathSegments("age-gate/birthdate")
            .build()
        val adapter = moshi.adapter(FpStatusRecord::class.java)
        val body = adapter
            .toJson(data)
            .toRequestBody(JSON)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        val callback = getMoshiCallback(AgeGateStatus::class.java,completion)
        client.newCall(request).enqueue(callback)
    }
    fun generateFingerprint(fingerprint: DeviceFingerprint, completion: (DeviceFingerprintResponse?) -> Unit) {
        val url = PrivoInternal.configuration.authUrl
            .toHttpUrl()
            .newBuilder()
            .addPathSegment("fp")
            .build()
        val adapter = moshi.adapter(DeviceFingerprint::class.java)
        val body = adapter
            .toJson(fingerprint)
            .toRequestBody(JSON)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        val callback = getMoshiCallback(DeviceFingerprintResponse::class.java,completion)
        client.newCall(request).enqueue(callback)
    }
}