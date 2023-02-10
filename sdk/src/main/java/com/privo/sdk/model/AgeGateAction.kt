package com.privo.sdk.model

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

class AgeGateActionAdapter {
    @ToJson
    fun toJson(enum: AgeGateAction): Int {
        return enum.action
    }

    @FromJson
    fun fromJson(type: Int): AgeGateAction {
        return AgeGateAction.values().first { it.action == type }
    }
}

enum class AgeGateAction(val action: Int) {
    Block(0),
    Consent(1),
    IdentityVerify(2),
    AgeVerify(3),
    Allow(4),
    MultiUserBlock(5)
}