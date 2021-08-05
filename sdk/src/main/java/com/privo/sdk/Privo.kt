package com.privo.sdk

import com.privo.sdk.internal.PrivoInternal
import com.privo.sdk.model.PrivoSettings


class Privo private constructor() {
    companion object {
        fun initialize (settings: PrivoSettings) = PrivoInternal.initialize(settings)
        fun getSettings () = PrivoInternal.settings
        val verification = PrivoVerification()
    }
}