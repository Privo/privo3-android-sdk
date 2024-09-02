package com.privo.sdk

import android.content.Context
import com.privo.sdk.extensions.createVerificationEvent
import com.privo.sdk.internal.PermissionsInternal
import com.privo.sdk.internal.PrivoInternal
import com.privo.sdk.internal.PrivoWebViewDialog
import com.privo.sdk.model.*
import com.privo.sdk.model.WebViewConfig


class PrivoVerification(val context: Context) {
    private val eventsKey = "privo_events_id"
    private val stateKey = "privo_state_id"
    private var activePrivoWebViewDialog: PrivoWebViewDialog? = null
    private val permissions = PermissionsInternal(context)
    private val userSessions = mutableMapOf<String,String>()

    private fun storeState(profile: UserVerificationProfile?, completion: (String?) -> Unit ) {
        val redirectUrl = PrivoInternal.configuration.verificationUrl + "/#/verification-loading";
        PrivoInternal.settings.apiKey?.let { apiKey ->
            val config =  VerificationConfig(apiKey, PrivoInternal.settings.serviceIdentifier)
            val data = VerificationData(profile ?: UserVerificationProfile(), config, redirectUrl)
            PrivoInternal.rest.addObjectToTMPStorage(data, VerificationData::class.java, completion)
        }
    }
    private fun getCancelEvents(): Array<VerificationEvent> = arrayOf(
            createVerificationEvent(VerificationEventType.VerifyInitialized),
            createVerificationEvent(VerificationEventType.VerifyCancel)
        )

    fun showVerification(profile: UserVerificationProfile, completion: ((Array<VerificationEvent>) -> Unit)) {
        storeState(profile) { id ->
            val serviceIdentifier = PrivoInternal.settings.serviceIdentifier
            val verificationUrl = "${PrivoInternal.configuration.verificationUrl}/index.html?$stateKey=$id&service_identifier=$serviceIdentifier#/intro"
            val config = WebViewConfig(
                verificationUrl,
                true,
                "/print",
                "verification-loading",
                onFinish = { url ->
                    url.getQueryParameter(eventsKey)?.let { eventId ->
                        PrivoInternal.rest.getObjectFromTMPStorage(eventId, Array<VerificationEvent>::class.java) { events ->
                            activePrivoWebViewDialog?.hide()
                            completion(events ?: arrayOf())
                        }
                    }
                },
                onCancel = {
                    val events = getCancelEvents()
                    completion(events)
                }
            )
            activePrivoWebViewDialog = PrivoWebViewDialog(context, config)
            activePrivoWebViewDialog?.show()
        }

    }
    fun checkRuntimePermissions(activity: android.app.Activity) = permissions.checkCameraPermission(activity)


    fun getUserLimits (externalUserId: String, completion: (UserLimits?) -> Unit) {
        val serviceIdentifier = PrivoInternal.settings.serviceIdentifier
        val sessionIdentifier = userSessions[externalUserId]
        if (sessionIdentifier != null) {
            PrivoInternal.rest.checkUserLimits(serviceIdentifier, sessionIdentifier, LimitType.IV, completion)
        } else {
            PrivoInternal.rest.createUserSession(serviceIdentifier, externalUserId) { sessionIdentifier ->
                if (sessionIdentifier != null) {
                    userSessions[externalUserId] = sessionIdentifier
                    PrivoInternal.rest.checkUserLimits(serviceIdentifier, sessionIdentifier, LimitType.IV, completion)
                } else {
                    completion(null)
                }
            }
        }
    }
}