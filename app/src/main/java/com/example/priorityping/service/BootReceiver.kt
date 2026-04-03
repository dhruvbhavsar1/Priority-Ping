package com.example.priorityping.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.priorityping.data.AppPreferences

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val prefs = AppPreferences.getInstance(context)
            if (prefs.isServiceEnabled && prefs.notificationListenerGranted) {
                PriorityForegroundService.start(context)
            }
        }
    }
}
