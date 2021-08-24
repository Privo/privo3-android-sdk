package com.privo.sdk.internal

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class PrivoMessageConverter {
    private val eventAdapter = Moshi
        .Builder()
        .build()
        .adapter<Map<String, String>>(
            Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
        )

    fun getPrivoEvent(rawMessage: String): Map<String,String>? {
        return eventAdapter.fromJson(rawMessage)
    }
}