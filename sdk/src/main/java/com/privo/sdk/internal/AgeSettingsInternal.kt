package com.privo.sdk.internal

import android.util.Log
import com.privo.sdk.model.AgeServiceSettings
import com.privo.sdk.model.EnvironmentType


class AgeSettingsInternal {
    private var lastSettings: Triple<String, EnvironmentType,AgeServiceSettings>? = null


    init {
        updateSettings {}
    }

    private fun updateSettings(completion:(AgeServiceSettings) -> Unit) {
        val serviceIdentifier = PrivoInternal.settings.serviceIdentifier
        val envType = PrivoInternal.settings.envType
        PrivoInternal.rest.getAgeServiceSettings(serviceIdentifier) { settings ->
            if (settings != null) {
                lastSettings = Triple(serviceIdentifier,envType, settings)
                completion(settings)
            } else {
                Log.e("PRIVO Android SDK", "Failed to load Privo service settings")
            }
        }
    }

    fun getSettings(completion: (AgeServiceSettings) -> Unit) {
        val serviceIdentifier = lastSettings?.first
        val envType = lastSettings?.second
        val settings = lastSettings?.third

        if (settings != null && serviceIdentifier == PrivoInternal.settings.serviceIdentifier && envType == PrivoInternal.settings.envType) {
            completion(settings)
        } else {
            updateSettings(completion)
        }
    }
}