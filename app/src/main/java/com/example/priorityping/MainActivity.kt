package com.example.priorityping

import android.animation.ObjectAnimator
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.priorityping.data.AppPreferences
import com.example.priorityping.databinding.ActivityMainBinding
import com.example.priorityping.model.SupportedApps
import com.example.priorityping.service.PriorityForegroundService
import com.example.priorityping.viewmodel.ContactListViewModel
import com.example.priorityping.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: ContactListViewModel by viewModels {
        ViewModelFactory((application as PriorityPingApplication).repository)
    }
    private lateinit var prefs: AppPreferences
    private var pulseAnimator: ObjectAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        prefs = AppPreferences.getInstance(this)

        if (!prefs.hasCompletedOnboarding) {
            startActivity(Intent(this, PermissionCheckActivity::class.java))
            finish()
            return
        }

        setupClickListeners()
        observeContacts()
        setupPulseAnimation()
    }

    override fun onResume() {
        super.onResume()
        updateStatusCard()
        if (prefs.isServiceEnabled && prefs.notificationListenerGranted) {
            PriorityForegroundService.start(this)
        }
    }

    private fun setupPulseAnimation() {
        pulseAnimator = ObjectAnimator.ofFloat(binding.statusIndicator, "alpha", 0.4f, 1.0f).apply {
            duration = 1200
            repeatMode = ObjectAnimator.REVERSE
            repeatCount = ObjectAnimator.INFINITE
        }
    }

    private fun setupClickListeners() {
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.btnAddContactGlobal.setOnClickListener {
            launchAddContact(SupportedApps.WHATSAPP.appName)
        }

        binding.btnAddWhatsapp.setOnClickListener { launchAddContact(SupportedApps.WHATSAPP.appName) }
        binding.btnAddInstagram.setOnClickListener { launchAddContact(SupportedApps.INSTAGRAM.appName) }
        binding.btnAddSnapchat.setOnClickListener { launchAddContact(SupportedApps.SNAPCHAT.appName) }

        binding.cardWhatsapp.setOnClickListener { launchContactList(SupportedApps.WHATSAPP.appName) }
        binding.cardInstagram.setOnClickListener { launchContactList(SupportedApps.INSTAGRAM.appName) }
        binding.cardSnapchat.setOnClickListener { launchContactList(SupportedApps.SNAPCHAT.appName) }

        binding.btnFixStatus.setOnClickListener {
            startActivity(Intent(this, PermissionCheckActivity::class.java))
        }
    }

    private fun launchAddContact(appName: String) {
        val intent = Intent(this, AddContactActivity::class.java).apply {
            putExtra("APP_NAME", appName)
        }
        startActivity(intent)
    }

    private fun launchContactList(appName: String) {
        val intent = Intent(this, ContactListActivity::class.java).apply {
            putExtra("APP_NAME", appName)
        }
        startActivity(intent)
    }

    private fun updateStatusCard() {
        val isNotificationListenerGranted = NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)
        val isActive = isNotificationListenerGranted && prefs.isServiceEnabled

        if (!isActive) {
            binding.statusBackground.setBackgroundResource(R.drawable.bg_status_inactive)
            binding.statusIndicator.backgroundTintList = ContextCompat.getColorStateList(this, R.color.colorError)
            binding.statusTitle.text = getString(R.string.status_inactive)
            binding.statusSubtitle.text = if (!isNotificationListenerGranted) "Notification access required" else "Service is paused"
            binding.btnFixStatus.visibility = if (!isNotificationListenerGranted) View.VISIBLE else View.GONE
            pulseAnimator?.cancel()
            binding.statusIndicator.alpha = 1.0f
        } else {
            binding.statusBackground.setBackgroundResource(R.drawable.bg_status_active)
            binding.statusIndicator.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.transparent)
            binding.statusTitle.text = getString(R.string.status_active)
            binding.statusSubtitle.text = "Monitoring Active"
            binding.btnFixStatus.visibility = View.GONE
            if (pulseAnimator?.isRunning == false) {
                pulseAnimator?.start()
            }
        }

        if (!prefs.isServiceEnabled && isNotificationListenerGranted) {
            binding.statusTitle.text = getString(R.string.service_paused)
        }
    }

    private fun observeContacts() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allContacts.collect { contacts ->
                    val whatsappCount = contacts.count { it.appName == SupportedApps.WHATSAPP.appName || it.appName == SupportedApps.WHATSAPP_BUSINESS.appName }
                    val instagramCount = contacts.count { it.appName == SupportedApps.INSTAGRAM.appName }
                    val snapchatCount = contacts.count { it.appName == SupportedApps.SNAPCHAT.appName }

                    binding.txtWhatsappCount.text = "$whatsappCount priority contacts"
                    binding.txtInstagramCount.text = "$instagramCount priority contacts"
                    binding.txtSnapchatCount.text = "$snapchatCount priority contacts"
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_feedback -> {
                startActivity(Intent(this, FeedbackActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        pulseAnimator?.cancel()
    }
}
