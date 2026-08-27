package com.lifetrace.execute.core.cloud

import android.content.Context
import android.os.Build
import java.util.UUID

class DeviceIdentityStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun deviceId(): String {
        preferences.getString(KEY_DEVICE_ID, null)?.takeIf { it.isNotBlank() }?.let { return it }
        val created = UUID.randomUUID().toString()
        preferences.edit().putString(KEY_DEVICE_ID, created).apply()
        return created
    }

    fun deviceName(): String {
        val manufacturer = Build.MANUFACTURER.orEmpty().trim()
        val model = Build.MODEL.orEmpty().trim()
        return listOf(manufacturer, model)
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString(" ")
            .ifBlank { "Android Device" }
    }

    companion object {
        private const val PREFS_NAME = "lifetrace_device_identity"
        private const val KEY_DEVICE_ID = "installation_id"
    }
}
