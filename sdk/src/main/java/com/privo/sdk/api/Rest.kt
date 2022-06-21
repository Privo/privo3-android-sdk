package com.privo.sdk.api

import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.privo.sdk.internal.PrivoInternal
import com.privo.sdk.model.*
import com.privo.sdk.model.adapters.MillisecondsTimeStampAdapter
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
        .add(MillisecondsTimeStampAdapter())
        .add(VerificationMethodTypeAdapter())
        .add(VerificationOutcomeAdapter())
        .add(AgeGateActionAdapter())
        .add(AgeGateStatusInternalAdapter())
        .add(AgeGateStatusAdapter())
        .add(AgeGateStatusTOAdapter())
        .add(AgeVerificationStatusInternalAdapter())
        .add(AgeVerificationStatusAdapter())
        .build()
    private val JSON : MediaType = "application/json; charset=utf-8".toMediaType()

    private fun runOnMainThread(completion: () -> Unit) = Handler(Looper.getMainLooper()).post(completion)


    private fun handleError (e: Exception, completion:() -> Unit) {
        Log.e("PRIVO Android SDK", "exception",e)
        val event = AnalyticEvent(PrivoInternal.settings.serviceIdentifier, e.toString())
        sendAnalyticEvent(event)
        runOnMainThread { completion() }
    }

    private fun <T>getMoshiCallback(type: Class<T>, completion:(T?) -> Unit): Callback {
        return object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                handleError(e) { completion(null) }
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
                            handleError(e) { completion(null) }
                        }
                    }
                }
                runOnMainThread {
                    completion(null)
                }
            }
        }
    }

    private fun <T> processRequest (request: Request, type: Class<T>, completion:(T?) -> Unit) {
        val callback = getMoshiCallback(type, completion)
        client.newCall(request).enqueue(callback)
    }
    fun getStringFromTMPStorage(key: String, completion: (String?) -> Unit) {
        val tmpStorageURL = PrivoInternal.configuration.helpersUrl
            .toHttpUrl()
            .newBuilder()
            .addPathSegment("storage")
            .addPathSegment(key)
            .build()
        val request = Request.Builder().url(tmpStorageURL).build()
        processRequest(request,TmpStringObject::class.java) {
            completion(it?.data)
        }
    }
    fun <T>getObjectFromTMPStorage(key: String, clazz:  Class<T>, completion: (T?) -> Unit) {
        val adapter = moshi.adapter(clazz)
        getStringFromTMPStorage(key) {
            it?.let { wrapper ->
                try {
                    val value = adapter.fromJson(wrapper)
                    completion(value)
                } catch (e: Exception) {
                    handleError(e) { completion(null) }
                }
            } ?: run {
                completion(null)
            }
        }
    }
    fun addStringToTMPStorage(value: String, completion: ((String?) -> Unit), ttl: Int? = null, ) {
        val adapter = moshi.adapter(TmpStringObject::class.java)

        val tmpStorageURL = PrivoInternal.configuration.helpersUrl
            .toHttpUrl()
            .newBuilder()
            .addPathSegment("storage")
            .addPathSegment("put")
            .build()
        val postBody = adapter
            .toJson(TmpStringObject(value, ttl))
            .toRequestBody(JSON)

        val request = Request.Builder()
            .url(tmpStorageURL)
            .post(postBody)
            .build()

        processRequest(request,TmpStorageResponse::class.java) {
            completion(it?.id)
        }
    }
    fun <T>addObjectToTMPStorage(value: T, clazz:  Class<T>, completion: ((String?) -> Unit), ttl: Int? = null, ) {
        val valueAdapter = moshi.adapter(clazz)
        val valueString = valueAdapter.toJson(value)
        addStringToTMPStorage(valueString,completion)
    }

    fun processStatus(data: StatusRecord, completion: (AgeGateStatusTO?) -> Unit) {
        val url = PrivoInternal.configuration.ageGateBaseUrl
            .toHttpUrl()
            .newBuilder()
            .addPathSegments("age-gate/status")
            .build()
        val adapter = moshi.adapter(StatusRecord::class.java)
        val body = adapter
            .toJson(data)
            .toRequestBody(JSON)
        val request = Request.Builder()
            .url(url)
            .put(body)
            .build()

        processRequest(request,AgeGateStatusTO::class.java,completion)
    }

    fun processBirthDate(data: FpStatusRecord, completion: (AgeGateResponse?) -> Unit) {
        val url = PrivoInternal.configuration.ageGateBaseUrl
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

        processRequest(request,AgeGateResponse::class.java,completion)
    }

    fun processRecheck(data: RecheckStatusRecord, completion: (AgeGateRecheckResponse?) -> Unit) {
        val url = PrivoInternal.configuration.ageGateBaseUrl
            .toHttpUrl()
            .newBuilder()
            .addPathSegments("age-gate/recheck")
            .build()
        val adapter = moshi.adapter(RecheckStatusRecord::class.java)
        val body = adapter
            .toJson(data)
            .toRequestBody(JSON)
        val request = Request.Builder()
            .url(url)
            .put(body)
            .build()

        processRequest(request,AgeGateRecheckResponse::class.java,completion)
    }

    fun getAgeServiceSettings(serviceIdentifier: String, completion: (AgeServiceSettings?) -> Unit) {
        val url = PrivoInternal.configuration.ageGateBaseUrl
            .toHttpUrl()
            .newBuilder()
            .addPathSegments("age-gate/settings")
            .addQueryParameter("service_identifier",serviceIdentifier)
            .build()
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        processRequest(request,AgeServiceSettings::class.java,completion)
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

        processRequest(request,DeviceFingerprintResponse::class.java,completion)
    }
    fun getServiceInfo(serviceIdentifier: String, completion: (ServiceInfo?) -> Unit) {
        val url = PrivoInternal.configuration.authUrl
            .toHttpUrl()
            .newBuilder()
            .addPathSegments("info/svc")
            .addQueryParameter("service_identifier",serviceIdentifier)
            .build()
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        processRequest(request,ServiceInfo::class.java,completion)
    }

    internal fun getAgeVerification(verificationIdentifier: String, completion: (AgeVerificationTO?) -> Unit) {
        val url = PrivoInternal.configuration.ageVerificationBaseUrl
            .toHttpUrl()
            .newBuilder()
            .addPathSegments("age-verification")
            .addQueryParameter("verification_identifier",verificationIdentifier)
            .build()
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        processRequest(request,AgeVerificationTO::class.java,completion)
    }

    fun getAuthSessionId(completion:(String?) -> Unit) {
        val url = "${PrivoInternal.configuration.authUrl}/privo/authorize?client_id=mobile&redirect_uri="
            .toHttpUrl()
            .newBuilder()
            .build()
        val sessionIdKey = "session_id"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                handleError(e) { completion(null) }
            }

            override fun onResponse(call: Call, response: Response) {
                val redirectUrl = response.request.url.toString()
                val redirectUri = Uri.parse(redirectUrl)
                val sessionId = redirectUri.getQueryParameter(sessionIdKey)
                runOnMainThread { completion(sessionId) }
            }
        })

    }

    fun renewToken(oldToken: String, sessionId: String, completion: (LoginResponse?) -> Unit) {
        val url = "${PrivoInternal.configuration.authUrl}/privo/login/token?session_id=${sessionId}"
            .toHttpUrl()
            .newBuilder()
            .build()
        val body = oldToken.toRequestBody(JSON)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        processRequest(request,LoginResponse::class.java,completion)
    }

    fun sendAnalyticEvent(event: AnalyticEvent) {
        try {
            val url = "${PrivoInternal.configuration.helpersUrl}/metrics"
                .toHttpUrl()
                .newBuilder()
                .build()
            val adapter = moshi.adapter(AnalyticEvent::class.java)
            val body = adapter
                .toJson(event)
                .toRequestBody(JSON)
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()
            processRequest(request,Unit::class.java){}
        } catch(e: java.lang.Exception) {
            Log.e("PRIVO Android SDK", "exception",e)
        }
    }
}