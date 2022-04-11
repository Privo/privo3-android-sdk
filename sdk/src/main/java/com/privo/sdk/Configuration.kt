package com.privo.sdk

import com.privo.sdk.model.EnvironmentType

internal class Configuration constructor(type: EnvironmentType){
    val helpersUrl: String
    val verificationUrl: String
    val ageGateBaseUrl: String
    val ageGatePublicUrl: String
    val authUrl: String
    val lgsRegistrationUrl: String
    val tokenStorageKey: String

    init {
        when (type) {
            EnvironmentType.Local -> {
                helpersUrl = "https://helper-svc-dev.privo.com/rest/api/v1.0"
                verificationUrl = "https://verification-dev.privo.com/vw"
                ageGateBaseUrl = "https://agegate-dev.privo.com/api/v1.0"
                ageGatePublicUrl = "https://age-dev.privo.com/gate"
                authUrl = "https://auth-dev.privo.com/api/v1.0"
                lgsRegistrationUrl = "https://privohub-dev.privo.com/lgs"
                tokenStorageKey = "privo-token-local"
            }
            EnvironmentType.Dev -> {
                helpersUrl = "https://helper-svc-dev.privo.com/rest/api/v1.0"
                verificationUrl = "https://verification-dev.privo.com/vw"
                ageGateBaseUrl = "https://agegate-dev.privo.com/api/v1.0"
                ageGatePublicUrl = "https://age-dev.privo.com/gate"
                authUrl = "https://auth-dev.privo.com/api/v1.0"
                lgsRegistrationUrl = "https://privohub-dev.privo.com/lgs"
                tokenStorageKey = "privo-token-dev"
            }
            EnvironmentType.Test -> {
                helpersUrl = "https://helper-svc-test.privo.com/rest/api/v1.0"
                verificationUrl = "https://verification-test.privo.com/vw"
                ageGateBaseUrl = "https://agegate-test.privo.com/api/v1.0"
                ageGatePublicUrl = "https://age-test.privo.com/gate"
                authUrl = "https://auth-test.privo.com/api/v1.0"
                lgsRegistrationUrl = "https://privohub-test.privo.com/lgs"
                tokenStorageKey = "privo-token-test"
            }
            EnvironmentType.Int -> {
                helpersUrl = "https://helper-svc-int.privo.com/rest/api/v1.0"
                verificationUrl = "https://verification-int.privo.com/vw"
                ageGateBaseUrl = "https://agegate-int.privo.com/api/v1.0"
                ageGatePublicUrl = "https://age-int.privo.com/gate"
                authUrl = "https://auth-int.privo.com/api/v1.0"
                lgsRegistrationUrl = "https://privohub-int.privo.com/lgs"
                tokenStorageKey = "privo-token-int"
            }
            EnvironmentType.Staging -> {
                helpersUrl = "https://helper-svc-staging.privo.com/rest/api/v1.0"
                verificationUrl = "https://verification-staging.privo.com/vw"
                ageGateBaseUrl = "https://agegate-staging.privo.com/api/v1.0"
                ageGatePublicUrl = "https://age-staging.privo.com/gate"
                authUrl = "https://auth-staging.privo.com/api/v1.0"
                lgsRegistrationUrl = "https://privohub-staging.privo.com/lgs"
                tokenStorageKey = "privo-token-staging"
            }
            EnvironmentType.Prod -> {
                helpersUrl = "https://helper-svc.privo.com/rest/api/v1.0"
                verificationUrl = "https://verification.privo.com/vw"
                ageGateBaseUrl = "https://agegate.privo.com/api/v1.0"
                ageGatePublicUrl = "https://age.privo.com/gate"
                authUrl = "https://auth.privo.com/api/v1.0"
                lgsRegistrationUrl = "https://privohub.privo.com/lgs"
                tokenStorageKey = "privo-token"
            }
        }
    }
}