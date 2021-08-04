package com.privo.sdk

import android.content.Context
import com.privo.sdk.internal.PrivoInternal
import com.privo.sdk.model.*
import com.privo.sdk.model.WebViewConfig


class PrivoVerification {
    private fun storeState(profile: UserVerificationProfile?, completion: (String?) -> Unit ) {
        val redirectUrl = PrivoInternal.configuration.verificationUrl + "/#/verification-loading";
        PrivoInternal.settings.apiKey?.let { apiKey ->
            val config =  VerificationConfig(apiKey, PrivoInternal.settings.serviceIdentifier)
            val data = VerificationData(profile ?: UserVerificationProfile(), config, redirectUrl)
            PrivoInternal.rest.addObjectToTMPStorage(data, VerificationData::class.java, completion)
        }
    }

    fun showVerificationModal(context: Context, profile: UserVerificationProfile, completion: ((Array<VerificationEvent>) -> Unit)) {
        storeState(profile) { id ->
            val verificationUrl = "${PrivoInternal.configuration.verificationUrl}/index.html?privo_state_id=$id#/intro"
            val config = WebViewConfig(verificationUrl, finishCriteria = "verification-loading", onFinish = { url ->
                url.getQueryParameter("privo_events_id")?.let { eventId ->
                    PrivoInternal.rest.getObjectFromTMPStorage(eventId, Array<VerificationEvent>::class.java) { events ->
                        completion(events ?: arrayOf())
                    }
                }
            })
            PrivoInternal.showWebView(context, config)
        }

    }
}