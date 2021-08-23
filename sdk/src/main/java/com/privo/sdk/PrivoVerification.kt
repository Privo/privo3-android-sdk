package com.privo.sdk

import android.content.Context
import com.privo.sdk.internal.PrivoInternal
import com.privo.sdk.internal.PrivoWebViewDialog
import com.privo.sdk.model.*
import com.privo.sdk.model.WebViewConfig


class PrivoVerification(val context: Context) {
    private val eventsKey = "privo_events_id"
    private val stateKey = "privo_state_id"
    private var activePrivoWebViewDialog: PrivoWebViewDialog? = null

    private fun storeState(profile: UserVerificationProfile?, completion: (String?) -> Unit ) {
        val redirectUrl = PrivoInternal.configuration.verificationUrl + "/#/verification-loading";
        PrivoInternal.settings.apiKey?.let { apiKey ->
            val config =  VerificationConfig(apiKey, PrivoInternal.settings.serviceIdentifier)
            val data = VerificationData(profile ?: UserVerificationProfile(), config, redirectUrl)
            PrivoInternal.rest.addObjectToTMPStorage(data, VerificationData::class.java, completion)
        }
    }

    fun showVerification(profile: UserVerificationProfile, completion: ((Array<VerificationEvent>) -> Unit)) {
        storeState(profile) { id ->
            val verificationUrl = "${PrivoInternal.configuration.verificationUrl}/index.html?$stateKey=$id#/intro"
            val config = WebViewConfig(verificationUrl, true, "/print","verification-loading", onFinish = { url ->
                url.getQueryParameter(eventsKey)?.let { eventId ->
                    PrivoInternal.rest.getObjectFromTMPStorage(eventId, Array<VerificationEvent>::class.java) { events ->
                        activePrivoWebViewDialog?.hide()
                        completion(events ?: arrayOf())
                    }
                }
            })
            activePrivoWebViewDialog = PrivoWebViewDialog(context, config)
            activePrivoWebViewDialog?.show()
        }

    }
}