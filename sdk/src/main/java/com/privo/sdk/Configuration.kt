package com.privo.sdk

import com.privo.sdk.model.EnvironmentType

internal class Configuration constructor(type: EnvironmentType){
    val helpersUrl: String
    val verificationUrl: String
    val ageGateUrl: String
    val authUrl: String

    init {
        when (type) {
            EnvironmentType.Local -> {
                helpersUrl = "https://helper-svc-dev.privo.com/rest/api/v1.0"
                verificationUrl = "https://verification-dev.privo.com/vw"
                ageGateUrl = "https://agegate-dev.privo.com:443/api/v1.0"
                authUrl = "https://auth-dev.privo.com/api/v1.0"
            }
            EnvironmentType.Dev -> {
                helpersUrl = "https://helper-svc-dev.privo.com/rest/api/v1.0"
                verificationUrl = "https://verification-dev.privo.com/vw"
                ageGateUrl = "https://agegate-dev.privo.com:443/api/v1.0"
                authUrl = "https://auth-dev.privo.com/api/v1.0"
            }
            EnvironmentType.Test -> {
                helpersUrl = "https://helper-svc-test.privo.com/rest/api/v1.0"
                verificationUrl = "https://verification-test.privo.com/vw"
                ageGateUrl = "https://agegate-test.privo.com:443/api/v1.0"
                authUrl = "https://auth-test.privo.com/api/v1.0"
            }
            EnvironmentType.Int -> {
                helpersUrl = "https://helper-svc-int.privo.com/rest/api/v1.0"
                verificationUrl = "https://verification-int.privo.com/vw"
                ageGateUrl = "https://agegate-int.privo.com:443/api/v1.0"
                authUrl = "https://auth-int.privo.com/api/v1.0"
            }
            EnvironmentType.Staging -> {
                helpersUrl = "https://helper-svc-staging.privo.com/rest/api/v1.0"
                verificationUrl = "https://verification-staging.privo.com/vw"
                ageGateUrl = "https://agegate-staging.privo.com:443/api/v1.0"
                authUrl = "https://auth-staging.privo.com/api/v1.0"
            }
            EnvironmentType.Prod -> {
                helpersUrl = "https://helper-svc.privo.com/rest/api/v1.0"
                verificationUrl = "https://verification.privo.com/vw"
                ageGateUrl = "https://agegate.privo.com:443/api/v1.0"
                authUrl = "https://auth.privo.com/api/v1.0"
            }
        }
    }
}