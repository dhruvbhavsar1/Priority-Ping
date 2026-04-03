package com.example.priorityping.service

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.priorityping.MainActivity
import com.example.priorityping.PriorityPingApplication
import com.example.priorityping.R
import kotlinx.coroutines.runBlocking

class PriorityForegroundService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val repository = (application as PriorityPingApplication).repository
        
        val contactCount = runBlocking {
            repository.getAllContacts().size
        }

        val notification: Notification = NotificationCompat.Builder(this, PriorityPingApplication.CHANNEL_STATUS_ID)
            .setSmallIcon(R.drawable.ic_bell_priority)
            .setContentTitle("PriorityPing is active")
            .setContentText("Monitoring $contactCount priority contacts")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        return START_STICKY
    }

    companion object {
        private const val NOTIFICATION_ID = 1001

        fun start(context: Context) {
            val intent = Intent(context, PriorityForegroundService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, PriorityForegroundService::class.java)
            context.stopService(intent)
        }
    }
}
