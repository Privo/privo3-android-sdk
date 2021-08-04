package com.privo.sdk

import android.net.Uri
import com.privo.sdk.model.EnvironmentType

internal class Configuration constructor(type: EnvironmentType){
    val helpersUrl: String
    val verificationUrl: String

    init {
        when (type) {
            EnvironmentType.Local -> {
                helpersUrl = "https://helper-svc-dev.privo.com/rest/api/v1.0"
                verificationUrl = "https://verification-dev.privo.com/vw"
            }
            EnvironmentType.Dev -> {
                helpersUrl = "https://helper-svc-dev.privo.com/rest/api/v1.0"
                verificationUrl = "https://verification-dev.privo.com/vw"
            }
            EnvironmentType.Test -> {
                helpersUrl = "https://helper-svc-test.privo.com/rest/api/v1.0"
                verificationUrl = "https://verification-test.privo.com/vw"
            }
            EnvironmentType.Int -> {
                helpersUrl = "https://helper-svc-int.privo.com/rest/api/v1.0"
                verificationUrl = "https://verification-int.privo.com/vw"
            }
            EnvironmentType.Staging -> {
                helpersUrl = "https://helper-svc-staging.privo.com/rest/api/v1.0"
                verificationUrl = "https://verification-staging.privo.com/vw"
            }
            EnvironmentType.Prod -> {
                helpersUrl = "https://helper-svc.privo.com/rest/api/v1.0"
                verificationUrl = "https://verification.privo.com/vw"
            }
        }
    }
}