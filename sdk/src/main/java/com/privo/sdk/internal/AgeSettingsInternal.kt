package com.privo.sdk.internal

import android.util.Log
import com.privo.sdk.model.AgeServiceSettings


class AgeSettingsInternal {
    private var lastSettings: Pair<String, AgeServiceSettings>? = null


    init {
        updateSettings {}
    }

    private fun updateSettings(completion:(AgeServiceSettings) -> Unit) {
        val serviceIdentifier = PrivoInternal.settings.serviceIdentifier
        PrivoInternal.rest.getAgeServiceSettings(serviceIdentifier) { settings ->
            if (settings != null) {
                lastSettings = Pair(serviceIdentifier, settings)
                completion(settings)
            } else {
                Log.e("PRIVO Android SDK", "Failed to load Privo service settings")
            }
        }
    }

    fun getSettings(completion: (AgeServiceSettings) -> Unit) {
        val serviceIdentifier = lastSettings?.first
        val settings = lastSettings?.second
        if (serviceIdentifier == PrivoInternal.settings.serviceIdentifier && settings != null) {
            completion(settings)
        } else {
            updateSettings(completion)
        }
    }
}