package com.privo.sdk

import com.privo.sdk.model.PrivoSettings

class Privo private constructor() {
    companion object {
        fun initialize (settings: PrivoSettings) = PrivoInternal.initialize(settings)
        fun getSettings () = PrivoInternal.settings
        //TODO: add modules (auth,verification,age-gate) here
    }
}