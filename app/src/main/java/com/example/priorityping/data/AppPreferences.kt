package com.example.priorityping.data

import android.content.Context
import android.content.SharedPreferences

class AppPreferences private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var isServiceEnabled: Boolean
        get() = prefs.getBoolean(KEY_SERVICE_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_SERVICE_ENABLED, value).apply()

    var hasCompletedOnboarding: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, value).apply()

    var notificationListenerGranted: Boolean
        get() = prefs.getBoolean(KEY_LISTENER_GRANTED, false)
        set(value) = prefs.edit().putBoolean(KEY_LISTENER_GRANTED, value).apply()

    var batteryOptimizationRequested: Boolean
        get() = prefs.getBoolean(KEY_BATTERY_OPTI_REQUESTED, false)
        set(value) = prefs.edit().putBoolean(KEY_BATTERY_OPTI_REQUESTED, value).apply()

    // Optimization 6: Quiet Hours
    var quietHoursEnabled: Boolean
        get() = prefs.getBoolean(KEY_QUIET_HOURS_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_QUIET_HOURS_ENABLED, value).apply()

    var quietHoursStart: String
        get() = prefs.getString(KEY_QUIET_HOURS_START, "22:00") ?: "22:00"
        set(value) = prefs.edit().putString(KEY_QUIET_HOURS_START, value).apply()

    var quietHoursEnd: String
        get() = prefs.getString(KEY_QUIET_HOURS_END, "07:00") ?: "07:00"
        set(value) = prefs.edit().putString(KEY_QUIET_HOURS_END, value).apply()

    companion object {
        private const val PREFS_NAME = "priority_ping_prefs"
        private const val KEY_SERVICE_ENABLED = "is_service_enabled"
        private const val KEY_ONBOARDING_COMPLETED = "has_completed_onboarding"
        private const val KEY_LISTENER_GRANTED = "notification_listener_granted"
        private const val KEY_BATTERY_OPTI_REQUESTED = "battery_optimization_requested"
        private const val KEY_QUIET_HOURS_ENABLED = "quiet_hours_enabled"
        private const val KEY_QUIET_HOURS_START = "quiet_hours_start"
        private const val KEY_QUIET_HOURS_END = "quiet_hours_end"

        @Volatile
        private var INSTANCE: AppPreferences? = null

        fun getInstance(context: Context): AppPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppPreferences(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
