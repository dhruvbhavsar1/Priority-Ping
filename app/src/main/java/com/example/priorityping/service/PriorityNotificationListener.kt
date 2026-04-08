package com.example.priorityping.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.service.notification.NotificationListenerService
import android.service.notification.NotificationListenerService.Ranking
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.priorityping.MainActivity
import com.example.priorityping.PriorityPingApplication
import com.example.priorityping.R
import com.example.priorityping.data.AppDatabase
import com.example.priorityping.data.AppPreferences
import com.example.priorityping.model.PriorityContactEntity
import com.example.priorityping.model.PriorityLevel
import com.example.priorityping.model.SupportedApps
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Calendar

class PriorityNotificationListener : NotificationListenerService() {

    private val job = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + job)

    private val database by lazy {
        AppDatabase.getDatabase(applicationContext)
    }

    private val notificationManager by lazy {
        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }

    private val prefs by lazy {
        AppPreferences.getInstance(applicationContext)
    }

    private val wakeLock by lazy {
        (getSystemService(POWER_SERVICE) as PowerManager).newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK, "PriorityPing::NotificationLock"
        )
    }

    private var contactsCache: Map<String, Map<String, PriorityContactEntity>> = emptyMap()
    private var cacheLastUpdated: Long = 0
    private var lastCacheVersion: Long = -1
    private val CACHE_TTL_MS = 60_000L

    private val TAG = "PriorityPing"

    private val SUPPORTED_PACKAGES: Map<String, String> = mapOf(
        "whatsapp" to "com.whatsapp",
        "instagram" to "com.instagram.android",
        "snapchat" to "com.snapchat.android",
        "whatsapp_business" to "com.whatsapp.w4b"
    )

    override fun onListenerConnected() {
        super.onListenerConnected()
        invalidateCache()
    }

    private fun invalidateCache() {
        cacheLastUpdated = 0
        lastCacheVersion = -1
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.isOngoing) return

        val packageName = sbn.packageName
        if (!SUPPORTED_PACKAGES.values.contains(packageName)) return

        val appName = SUPPORTED_PACKAGES.entries.find { it.value == packageName }?.key ?: return
        val extras = sbn.notification.extras
        val rawTitle = extras.getString("android.title")?.trim() ?: return
        
        // Try to capture original sound from the notification channel
        val originalSound = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.getNotificationChannel(sbn.notification.channelId)?.sound
        } else {
            @Suppress("DEPRECATION")
            sbn.notification.sound
        }

        serviceScope.launch {
            try {
                wakeLock.acquire(3000L)
                checkCache()

                // Handle multi-account Instagram titles (e.g., "my_account: sender_name")
                var contact: PriorityContactEntity? = null
                val appContacts = contactsCache[appName]

                if (appContacts != null) {
                    // 1. Try exact match on title
                    contact = appContacts[rawTitle]

                    // 2. If no match and it's Instagram, try splitting by ":"
                    if (contact == null && appName == "instagram" && rawTitle.contains(":")) {
                        val senderPart = rawTitle.substringAfter(":").trim()
                        contact = appContacts[senderPart]
                        
                        // 3. Try the account part just in case
                        if (contact == null) {
                            val accountPart = rawTitle.substringBefore(":").trim()
                            contact = appContacts[accountPart]
                        }
                    }
                }

                if (contact == null || !contact.isActive) {
                    return@launch
                }

                launch(Dispatchers.Main) {
                    handleUserPriority(contact.priorityLevel, contact.vibrationType, contact.identifier, appName, originalSound)
                }
            } finally {
                if (wakeLock.isHeld) wakeLock.release()
            }
        }
    }

    private suspend fun checkCache() {
        val currentVersion = NotificationServiceCache.cacheVersion
        if (System.currentTimeMillis() - cacheLastUpdated > CACHE_TTL_MS || currentVersion != lastCacheVersion) {
            val allContacts = database.priorityContactDao().getAllContacts()
            contactsCache = allContacts.filter { it.isActive }.groupBy { it.appName }
                .mapValues { entry -> entry.value.associateBy { it.identifier } }
            
            cacheLastUpdated = System.currentTimeMillis()
            lastCacheVersion = currentVersion
        }
    }

    private fun handleUserPriority(priority: PriorityLevel, vibration: String, sender: String, appName: String, soundUri: android.net.Uri? = null) {
        val inQuietHours = isQuietHours()

        if (inQuietHours) {
            // Optimization 6: Skip sound/vibration during quiet hours but show notification
            showPriorityAlertNotification(sender, appName, soundUri)
            return
        }

        when (priority) {
            PriorityLevel.HIGH -> {
                forceSoundAndVibration(vibration, soundUri)
                showPriorityAlertNotification(sender, appName, soundUri)
            }
            PriorityLevel.MEDIUM -> {
                triggerVibrationOnly(vibration)
                showPriorityAlertNotification(sender, appName, soundUri)
            }
            PriorityLevel.NORMAL -> {
                // Default system behavior
            }
        }
    }

    private fun isQuietHours(): Boolean {
        if (!prefs.quietHoursEnabled) return false

        val now = Calendar.getInstance()
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        val startParts = prefs.quietHoursStart.split(":")
        val startMinutes = startParts[0].toInt() * 60 + startParts[1].toInt()

        val endParts = prefs.quietHoursEnd.split(":")
        val endMinutes = endParts[0].toInt() * 60 + endParts[1].toInt()

        return if (startMinutes < endMinutes) {
            currentMinutes in startMinutes until endMinutes
        } else {
            // Overlapping midnight
            currentMinutes >= startMinutes || currentMinutes < endMinutes
        }
    }

    private fun triggerVibrationOnly(vibration: String) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as android.os.VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        val pattern = getVibrationPattern(vibration)
        vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
    }

    private fun forceSoundAndVibration(vibration: String, soundUri: android.net.Uri?) {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        
        // 1. Bypass DND if possible (this doesn't change ringer mode)
        if (notificationManager.isNotificationPolicyAccessGranted) {
            val originalFilter = notificationManager.currentInterruptionFilter
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    notificationManager.setInterruptionFilter(originalFilter)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to restore DND filter", e)
                }
            }, 5000)
        }

        // 2. Trigger Custom Vibration
        triggerVibrationOnly(vibration)

        // 3. Manual Sound Playback if not in Normal mode
        // This ensures sound plays even in Silent/Vibrate without changing the system "mood"
        if (audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL) {
            playManualSound(soundUri)
        }
    }

    private fun playManualSound(soundUri: android.net.Uri?) {
        val uri = soundUri ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        try {
            val ringtone = RingtoneManager.getRingtone(applicationContext, uri)
            if (ringtone != null) {
                ringtone.audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM) // Bypasses silent mode if alarm volume is up
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                ringtone.play()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing manual sound", e)
        }
    }

    private fun getVibrationPattern(vibration: String): LongArray {
        return when (vibration) {
            "LIGHT" -> longArrayOf(0, 200, 100, 200)
            "NORMAL" -> longArrayOf(0, 400, 200, 400)
            "HARD" -> longArrayOf(0, 800, 300, 800, 300, 900)
            "VERY_HARD" -> longArrayOf(0, 1000, 300, 1000, 300, 1200)
            else -> longArrayOf(0, 400, 200, 400)
        }
    }

    private fun showPriorityAlertNotification(sender: String, appName: String, soundUri: android.net.Uri? = null) {
        val appDisplayName = SupportedApps.fromAppName(appName)?.displayName ?: appName
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // Use original sound if available, otherwise default notification sound
        val finalSoundUri = soundUri ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(this, PriorityPingApplication.CHANNEL_ALERTS_ID)
            .setSmallIcon(R.drawable.ic_bell_priority)
            .setContentTitle("$appDisplayName: $sender")
            .setContentText("Priority notification detected!")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(Notification.CATEGORY_MESSAGE)
            .setSound(finalSoundUri)
            .setVibrate(null) // Don't use default vibration, we trigger custom patterns manually
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
