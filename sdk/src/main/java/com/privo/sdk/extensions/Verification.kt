package com.privo.sdk.extensions

import com.privo.sdk.model.VerificationEvent
import com.privo.sdk.model.VerificationMethodTypeAdapter
import com.privo.sdk.model.VerificationOutcomeAdapter
import com.privo.sdk.model.VerificationResult
import com.squareup.moshi.Moshi

fun VerificationEvent.formattedResult(): String? {
    this.result?.let {
        val moshi = Moshi
            .Builder()
            .add(VerificationMethodTypeAdapter())
            .add(VerificationOutcomeAdapter())
            .build();
        return moshi
            .adapter(VerificationResult::class.java)
            .indent("  ")
            .toJson(it)
    } ?: run {
        return null
    }
}