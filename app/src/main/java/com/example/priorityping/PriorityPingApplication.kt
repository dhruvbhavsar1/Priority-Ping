package com.example.priorityping

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.priorityping.data.AppDatabase
import com.example.priorityping.data.PriorityContactRepository

class PriorityPingApplication : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { PriorityContactRepository(database) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val statusChannel = NotificationChannel(
                CHANNEL_STATUS_ID,
                "PriorityPing Service",
                NotificationManager.IMPORTANCE_MIN // Optimization 1: Importance MIN
            ).apply {
                description = "Shows that PriorityPing is monitoring notifications"
            }

            val alertChannel = NotificationChannel(
                CHANNEL_ALERTS_ID,
                "Priority Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts for priority contacts"
                enableVibration(true)
                setShowBadge(true)
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(statusChannel)
            manager.createNotificationChannel(alertChannel)
        }
    }

    companion object {
        const val CHANNEL_STATUS_ID = "priority_ping_status"
        const val CHANNEL_ALERTS_ID = "priority_alerts"
    }
}
