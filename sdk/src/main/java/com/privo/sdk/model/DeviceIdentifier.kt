package com.privo.sdk.model

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import com.privo.sdk.internal.PrivoPreferenceKey
import java.util.*
import android.app.backup.BackupManager

data class DeviceIdentifier(val context: Context) {
    private val preferences: SharedPreferences = context.getSharedPreferences(PrivoPreferenceKey, Context.MODE_PRIVATE)
    private val backupManager = BackupManager(context)
    private val storeKey = "privo-device-identifier"

    @SuppressLint("HardwareIds")
    private fun getNewIdentifier(): String {
        val id = Settings.Secure.getString(context.contentResolver,Settings.Secure.ANDROID_ID)
        return if (!id.isNullOrBlank() && !id.contains("0000000")) {
            id
        } else {
            UUID.randomUUID().toString()
        }
    }
    @SuppressLint("ApplySharedPref")
    fun getIdentifier()  = preferences.getString(storeKey, null) ?: run {
        val uniqueID = getNewIdentifier()
        preferences.edit().putString(storeKey,uniqueID).commit()
        backupManager.dataChanged()
        uniqueID
    }
}