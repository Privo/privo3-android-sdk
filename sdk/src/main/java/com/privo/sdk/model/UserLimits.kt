package com.privo.sdk.model


import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonClass
import com.squareup.moshi.ToJson

@JsonClass(generateAdapter = true)
class UserLimits (
    val isOverLimit: Boolean,
    val limitType: LimitType,
    val retryAfter: Int?,
)

@JsonClass(generateAdapter = true)
class UserLimitsTO (
    val is_over_limit: Boolean,
    val limit_type: LimitType,
    val retry_after: Int?,
)


class LimitTypeAdapter {
    @ToJson
    fun toJson(enum: LimitType): String {
        return enum.type
    }

    @FromJson
    fun fromJson(enum: String): LimitType {
        return LimitType.values().first { it.type == enum }
    }
}

enum class LimitType(val type: String) {
    IV("IV"),
    Auth("Auth"),
}