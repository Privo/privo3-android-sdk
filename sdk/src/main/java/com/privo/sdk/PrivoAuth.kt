package com.privo.sdk

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.auth0.android.jwt.JWT
import com.privo.sdk.components.LoadingDialog
import com.privo.sdk.internal.PrivoInternal
import com.privo.sdk.internal.PrivoPreferenceKey
import com.privo.sdk.internal.PrivoWebViewDialog
import com.privo.sdk.model.TokenStatus
import com.privo.sdk.model.WebViewConfig
import java.lang.Exception
import java.util.*

class PrivoAuth(val context: Context) {
    private val accessIdKey = "accessId"
    private val siteIdKey = "siteId"
    private val tokenKey = PrivoInternal.configuration.tokenStorageKey
    private var activePrivoWebViewDialog: PrivoWebViewDialog? = null
    private val preferences: SharedPreferences = context.getSharedPreferences(PrivoPreferenceKey, Context.MODE_PRIVATE)
    private  val loadingDialog = LoadingDialog(context)

    fun showLogin(completion: (String?) -> Unit) {
        loadingDialog.show()
        val url = "${PrivoInternal.configuration.authUrl}/privo/authorize?client_id=mobile&redirect_uri="
        val config = WebViewConfig(
            url,
            onPrivoEvent = { event ->
            event?.get(accessIdKey)?.let { accessId ->
                PrivoInternal.rest.getStringFromTMPStorage(accessId) { token ->
                    if (token != null) {
                        preferences.edit().putString(tokenKey,token).apply()
                    }
                    activePrivoWebViewDialog?.hide()
                    completion(token)
                }
            }},
            onLoad = {
                loadingDialog.hide()
            }
        )
        activePrivoWebViewDialog = PrivoWebViewDialog(context, config)
        activePrivoWebViewDialog?.show()

    }
    fun showRegister(completion: (dialog: PrivoWebViewDialog) -> Unit) {
        loadingDialog.show()
        val serviceIdentifier = PrivoInternal.settings.serviceIdentifier
        PrivoInternal.rest.getServiceInfo(serviceIdentifier) { serviceInfo ->
            serviceInfo?.p2siteId?.let { siteId ->
                val registerUrl  = "${PrivoInternal.configuration.lgsRegistrationUrl}?${siteIdKey}=${siteId}"
                val config = WebViewConfig(
                    registerUrl,
                    finishCriteria = "step=complete",
                    onFinish = {
                        activePrivoWebViewDialog?.let {
                            completion(it)
                        }},
                    onLoad = {
                        loadingDialog.hide()
                    }
                )
                activePrivoWebViewDialog = PrivoWebViewDialog(context, config)
                activePrivoWebViewDialog?.show()
            } ?: run {
                loadingDialog.hide()
            }
        }
    }

    fun getToken(): String? {
        preferences.getString(tokenKey,null)?.let { token ->
            try {
                val jwt = JWT(token)
                jwt.expiresAt?.let { exp ->
                    if (exp.after(Date())) {
                        return token
                    }
                } ?: run {
                    return token
                }
            } catch (e: Exception) {
                Log.e("PRIVO Android SDK", "exception",e)
            }
        }
        return null
    }

    fun renewToken(completion: (TokenStatus?) -> Unit) {
        getToken()?.let { oldToken ->
            PrivoInternal.rest.getAuthSessionId { sId ->
                sId?.let { sessionId ->
                    PrivoInternal.rest.renewToken(oldToken,sessionId) { response ->
                        val token = response?.token
                        if (token != null) {
                            preferences.edit().putString(tokenKey,token).apply()
                            val status = TokenStatus(token, true)
                            completion(status)
                        } else {
                            val status = TokenStatus(oldToken, false)
                            completion(status)
                        }
                    }
                } ?: run {
                    completion(null)
                }
            }
        } ?: run {
            completion(null)
        }

    }
    fun logout() {
        preferences.edit().remove(tokenKey).apply()
    }
}