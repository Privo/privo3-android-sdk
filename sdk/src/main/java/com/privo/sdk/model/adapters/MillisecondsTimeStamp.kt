package com.privo.sdk.model.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.ToJson
import java.util.*

@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class MillisecondsTimeStamp

/** Converts Date to   */
class MillisecondsTimeStampAdapter {
    @ToJson
    fun toJson(@MillisecondsTimeStamp date: Date): Long {
        return date.time
    }
    @FromJson @MillisecondsTimeStamp fun fromJson(date: Long): Date {
        return Date(date)
    }
}