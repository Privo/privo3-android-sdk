package com.privo.sdk.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TmpStringObject(val data: String, val ttl: Int? = null)

@JsonClass(generateAdapter = true)
data class TmpStorageObject<T>(val data: T, val ttl: Int? = null)

@JsonClass(generateAdapter = true)
data class TmpStorageResponse(val id: String)

