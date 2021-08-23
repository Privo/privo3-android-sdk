package com.privo.sdk.model

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonClass
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
    Verify(2),
    Allow(3)
}

@JsonClass(generateAdapter = true)
data class AgeGateStatus(val action: AgeGateAction, val ageGateIdentifier: String)