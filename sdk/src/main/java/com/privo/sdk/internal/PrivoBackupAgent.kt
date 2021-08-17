package com.privo.sdk.internal

import android.os.ParcelFileDescriptor

import android.app.backup.BackupDataInput

import android.app.backup.BackupDataOutput

import android.content.SharedPreferences

import android.app.backup.SharedPreferencesBackupHelper

import android.app.backup.BackupAgentHelper
import okio.IOException


const val PrivoPreferenceKey = "privo-preferences"

const val PrivoPreferenceBackupKey = "privo-preferences-backup"

class PrivoBackupAgent : BackupAgentHelper() {
    override fun onCreate() {
        // Allocate a helper and add it to the backup agent
        SharedPreferencesBackupHelper(this, PrivoPreferenceKey).also {
            addHelper(PrivoPreferenceBackupKey, it)
        }
    }
}