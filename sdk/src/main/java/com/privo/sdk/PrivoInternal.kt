package com.privo.sdk

import com.privo.sdk.model.PrivoSettings

internal class PrivoInternal private constructor() {
    companion object {
        private var _configuration: Configuration? = null
        private var _settings: PrivoSettings? = null

        val configuration: Configuration
            get() = _configuration ?: throw UninitializedPropertyAccessException("Privo SDK is not initialized, call Privo.initialize first")
        val settings: PrivoSettings
            get() = _settings ?: throw UninitializedPropertyAccessException("Privo SDK is not initialized, call Privo.initialize first")

        fun initialize (settings: PrivoSettings) {
            this._settings = settings;
            this._configuration = Configuration(settings.envType)
        }
    }
}