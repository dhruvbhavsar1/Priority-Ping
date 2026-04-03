package com.example.priorityping

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.priorityping.data.AppPreferences
import com.example.priorityping.databinding.ActivityPermissionCheckBinding
import com.example.priorityping.service.PriorityForegroundService

class PermissionCheckActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPermissionCheckBinding
    private lateinit var prefs: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPermissionCheckBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = AppPreferences.getInstance(this)

        binding.btnGrantNotif.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        binding.btnGrantDnd.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
        }

        binding.btnGrantBattery.setOnClickListener {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:$packageName")
            }
            startActivity(intent)
        }

        binding.btnContinue.setOnClickListener {
            prefs.hasCompletedOnboarding = true
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        updatePermissionsStatus()
    }

    private fun updatePermissionsStatus() {
        val isNotificationListenerGranted = NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)
        prefs.notificationListenerGranted = isNotificationListenerGranted
        
        binding.statusNotif.text = if (isNotificationListenerGranted) "GRANTED" else "REQUIRED"
        binding.statusNotif.setTextColor(ContextCompat.getColor(this, if (isNotificationListenerGranted) R.color.colorSuccess else R.color.colorError))
        binding.btnGrantNotif.visibility = if (isNotificationListenerGranted) android.view.View.GONE else android.view.View.VISIBLE

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val isDndGranted = notificationManager.isNotificationPolicyAccessGranted
        binding.statusDnd.text = if (isDndGranted) "GRANTED" else "REQUIRED"
        binding.statusDnd.setTextColor(ContextCompat.getColor(this, if (isDndGranted) R.color.colorSuccess else R.color.colorError))
        binding.btnGrantDnd.visibility = if (isDndGranted) android.view.View.GONE else android.view.View.VISIBLE

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val isBatteryIgnoring = powerManager.isIgnoringBatteryOptimizations(packageName)
        binding.statusBattery.text = if (isBatteryIgnoring) "OPTIMIZED" else "OPTIMIZING"
        binding.statusBattery.setTextColor(ContextCompat.getColor(this, if (isBatteryIgnoring) R.color.colorSuccess else R.color.colorWarning))
        binding.btnGrantBattery.visibility = if (isBatteryIgnoring) android.view.View.GONE else android.view.View.VISIBLE

        binding.btnContinue.isEnabled = isNotificationListenerGranted
    }
}
