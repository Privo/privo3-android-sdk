package com.privo.sdk

import com.privo.sdk.model.EnvironmentType

internal class Configuration constructor(type: EnvironmentType) {
    val urlPrefix: String
    val helpersUrl: String
    val commonUrl: String
    val verificationUrl: String
    val ageGateBaseUrl: String
    val ageGatePublicUrl: String
    val ageVerificationBaseUrl: String
    val ageVerificationPublicUrl: String
    val authUrl: String
    val lgsRegistrationUrl: String
    val tokenStorageKey: String

    init {
        when (type) {
            EnvironmentType.Local -> {
                urlPrefix = "-dev"
                helpersUrl = "https://helper-svc-dev.privo.com/rest/api/v1.0"
                commonUrl = "https://common-svc-dev.privo.com/api/v1.0"
                verificationUrl = "https://verification-dev.privo.com/vw"
                ageGateBaseUrl = "https://agegate-dev.privo.com/api/v1.0"
                ageGatePublicUrl = "https://age-dev.privo.com/gate"
                ageVerificationBaseUrl = "https://ageverification-dev.privo.com/api/v1.0"
                ageVerificationPublicUrl = "https://age-dev.privo.com/verification"
                authUrl = "https://auth-dev.privo.com"
                lgsRegistrationUrl = "https://privohub-dev.privo.com/lgs"
                tokenStorageKey = "privo-token-local"
            }
            EnvironmentType.Dev -> {
                urlPrefix = "-dev"
                helpersUrl = "https://helper-svc-dev.privo.com/rest/api/v1.0"
                commonUrl = "https://common-svc-dev.privo.com/api/v1.0"
                verificationUrl = "https://verification-dev.privo.com/vw"
                ageGateBaseUrl = "https://agegate-dev.privo.com/api/v1.0"
                ageGatePublicUrl = "https://age-dev.privo.com/gate"
                ageVerificationBaseUrl = "https://ageverification-dev.privo.com/api/v1.0"
                ageVerificationPublicUrl = "https://age-dev.privo.com/verification"
                authUrl = "https://auth-dev.privo.com"
                lgsRegistrationUrl = "https://privohub-dev.privo.com/lgs"
                tokenStorageKey = "privo-token-dev"
            }
            EnvironmentType.Test -> {
                urlPrefix = "-test"
                helpersUrl = "https://helper-svc-test.privo.com/rest/api/v1.0"
                commonUrl = "https://common-svc-test.privo.com/api/v1.0"
                verificationUrl = "https://verification-test.privo.com/vw"
                ageGateBaseUrl = "https://agegate-test.privo.com/api/v1.0"
                ageGatePublicUrl = "https://age-test.privo.com/gate"
                ageVerificationBaseUrl = "https://ageverification-test.privo.com/api/v1.0"
                ageVerificationPublicUrl = "https://age-test.privo.com/verification"
                authUrl = "https://auth-test.privo.com"
                lgsRegistrationUrl = "https://privohub-test.privo.com/lgs"
                tokenStorageKey = "privo-token-test"
            }
            EnvironmentType.Int -> {
                urlPrefix = "-int"
                helpersUrl = "https://helper-svc-int.privo.com/rest/api/v1.0"
                commonUrl = "https://common-svc-int.privo.com/api/v1.0"
                verificationUrl = "https://verification-int.privo.com/vw"
                ageGateBaseUrl = "https://agegate-int.privo.com/api/v1.0"
                ageGatePublicUrl = "https://age-int.privo.com/gate"
                ageVerificationBaseUrl = "https://ageverification-int.privo.com/api/v1.0"
                ageVerificationPublicUrl = "https://age-int.privo.com/verification"
                authUrl = "https://auth-int.privo.com"
                lgsRegistrationUrl = "https://privohub-int.privo.com/lgs"
                tokenStorageKey = "privo-token-int"
            }
            EnvironmentType.Staging -> {
                urlPrefix = "-staging"
                helpersUrl = "https://helper-svc-staging.privo.com/rest/api/v1.0"
                commonUrl = "https://common-svc-staging.privo.com/api/v1.0"
                verificationUrl = "https://verification-staging.privo.com/vw"
                ageGateBaseUrl = "https://agegate-staging.privo.com/api/v1.0"
                ageGatePublicUrl = "https://age-staging.privo.com/gate"
                ageVerificationBaseUrl = "https://ageverification-staging.privo.com/api/v1.0"
                ageVerificationPublicUrl = "https://age-staging.privo.com/verification"
                authUrl = "https://auth-staging.privo.com"
                lgsRegistrationUrl = "https://privohub-staging.privo.com/lgs"
                tokenStorageKey = "privo-token-staging"
            }
            EnvironmentType.Prod -> {
                urlPrefix = ""
                helpersUrl = "https://helper-svc.privo.com/rest/api/v1.0"
                commonUrl = "https://common-svc.privo.com/api/v1.0"
                verificationUrl = "https://verification.privo.com/vw"
                ageGateBaseUrl = "https://agegate.privo.com/api/v1.0"
                ageGatePublicUrl = "https://age.privo.com/gate"
                ageVerificationBaseUrl = "https://ageverification.privo.com/api/v1.0"
                ageVerificationPublicUrl = "https://age.privo.com/verification"
                authUrl = "https://auth.privo.com"
                lgsRegistrationUrl = "https://privohub.privo.com/lgs"
                tokenStorageKey = "privo-token"
            }
        }
    }
}