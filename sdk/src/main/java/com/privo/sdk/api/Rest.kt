package com.privo.sdk.api

import android.os.Handler
import android.os.Looper
import com.privo.sdk.internal.PrivoInternal
import com.privo.sdk.model.TmpStorageResponse
import com.privo.sdk.model.TmpStringObject
import com.privo.sdk.model.VerificationMethodTypeAdapter
import com.privo.sdk.model.VerificationOutcomeAdapter
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
        .build()
    private val JSON : MediaType = "application/json; charset=utf-8".toMediaType()

    private fun runOnMainThread(completion: () -> Unit) = Handler(Looper.getMainLooper()).post(completion)

    fun <T>getObjectFromTMPStorage(key: String, clazz:  Class<T>, completion: (T?) -> Unit) {
        val tmpStorageURL = PrivoInternal.configuration.helpersUrl
            .toHttpUrl()
            .newBuilder()
            .addPathSegment("storage")
            .addPathSegment(key)
            .build()
        val request = Request.Builder().url(tmpStorageURL).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful ) {
                    response.body?.string()?.let { json ->
                        val valueAdapter = moshi.adapter(clazz)
                        val wrapperAdapter = moshi.adapter(TmpStringObject::class.java)

                        wrapperAdapter.fromJson(json)?.let { wrapper ->
                            valueAdapter.fromJson(wrapper.data)?.let { value ->
                                runOnMainThread {
                                    completion(value)
                                }
                                return
                            }
                        }
                    }
                }
                runOnMainThread {
                    completion(null)
                }
            }
        })
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

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let { json ->
                        moshi.adapter(TmpStorageResponse::class.java).fromJson(json)?.let { obj ->
                            runOnMainThread {
                                completion(obj.id)
                            }
                            return
                        }
                    }
                }
                runOnMainThread {
                    completion(null)
                }
            }
        })
    }
}