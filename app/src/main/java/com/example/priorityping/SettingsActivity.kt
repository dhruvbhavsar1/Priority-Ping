package com.example.priorityping

import android.app.NotificationManager
import android.app.TimePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.priorityping.data.AppPreferences
import com.example.priorityping.databinding.ActivitySettingsBinding
import com.example.priorityping.service.PriorityForegroundService
import com.google.android.material.snackbar.Snackbar

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefs: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = AppPreferences.getInstance(this)

        setupToolbar()
        setupListeners()
        setupQuietHoursUI()
        setupSupportUI()
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupListeners() {
        binding.switchService.isChecked = prefs.isServiceEnabled
        binding.switchService.setOnCheckedChangeListener { _, isChecked ->
            prefs.isServiceEnabled = isChecked
            if (isChecked) {
                if (prefs.notificationListenerGranted) {
                    PriorityForegroundService.start(this)
                }
            } else {
                PriorityForegroundService.stop(this)
                Snackbar.make(binding.root, "PriorityPing paused. You will not receive priority alerts.", Snackbar.LENGTH_LONG).show()
            }
        }

        binding.rowNotifPerm.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        binding.rowDndPerm.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
        }

        binding.rowBatteryPerm.setOnClickListener {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:$packageName")
            }
            startActivity(intent)
        }

        binding.rowAboutInfo.setOnClickListener {
            showHowItWorksDialog()
        }

        try {
            val pInfo = packageManager.getPackageInfo(packageName, 0)
            binding.txtVersion.text = pInfo.versionName
        } catch (e: Exception) {
            binding.txtVersion.text = "1.0"
        }
    }

    private fun setupQuietHoursUI() {
        binding.switchQuietHours.isChecked = prefs.quietHoursEnabled
        binding.editQuietStart.setText(prefs.quietHoursStart)
        binding.editQuietEnd.setText(prefs.quietHoursEnd)

        binding.switchQuietHours.setOnCheckedChangeListener { _, isChecked ->
            prefs.quietHoursEnabled = isChecked
        }

        binding.editQuietStart.setOnClickListener {
            showTimePicker(prefs.quietHoursStart) { time ->
                prefs.quietHoursStart = time
                binding.editQuietStart.setText(time)
            }
        }

        binding.editQuietEnd.setOnClickListener {
            showTimePicker(prefs.quietHoursEnd) { time ->
                prefs.quietHoursEnd = time
                binding.editQuietEnd.setText(time)
            }
        }
    }

    private fun setupSupportUI() {
        binding.rowFeedback.setOnClickListener {
            startActivity(Intent(this, FeedbackActivity::class.java))
        }

        binding.rowRate.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${packageName}")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            }
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${packageName}")))
            }
        }
    }

    private fun showTimePicker(currentTime: String, onTimeSelected: (String) -> Unit) {
        val parts = currentTime.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()

        TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
            onTimeSelected(formattedTime)
        }, hour, minute, true).show()
    }

    private fun updateStatus() {
        val isNotificationListenerGranted = NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)
        binding.txtNotifStatus.text = if (isNotificationListenerGranted) "Granted" else "Fix"
        binding.txtNotifStatus.setTextColor(ContextCompat.getColor(this, if (isNotificationListenerGranted) R.color.colorSuccess else R.color.colorError))

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val isDndGranted = notificationManager.isNotificationPolicyAccessGranted
        binding.txtDndStatus.text = if (isDndGranted) "Granted" else "Fix"
        binding.txtDndStatus.setTextColor(ContextCompat.getColor(this, if (isDndGranted) R.color.colorSuccess else R.color.colorError))

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val isBatteryIgnoring = powerManager.isIgnoringBatteryOptimizations(packageName)
        binding.txtBatteryStatus.text = if (isBatteryIgnoring) "Optimized" else "Fix"
        binding.txtBatteryStatus.setTextColor(ContextCompat.getColor(this, if (isBatteryIgnoring) R.color.colorSuccess else R.color.colorWarning))
    }

    private fun showHowItWorksDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.how_it_works_title)
            .setMessage(R.string.how_it_works_desc)
            .setPositiveButton("Got it", null)
            .show()
    }
}
