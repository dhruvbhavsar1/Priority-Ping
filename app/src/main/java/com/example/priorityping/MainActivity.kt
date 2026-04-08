package com.example.priorityping

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
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

        // setSupportActionBar(binding.toolbar)

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
        /*
        pulseAnimator = ObjectAnimator.ofFloat(binding.statusIndicator, "alpha", 0.4f, 1.0f).apply {
            duration = 1200
            repeatMode = ObjectAnimator.REVERSE
            repeatCount = ObjectAnimator.INFINITE
        }
        */
    }

    private fun setupClickListeners() {
        binding.btnInfo.setOnClickListener {
            showHowToUseDialog()
        }

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

        /*
        binding.btnFixStatus.setOnClickListener {
            startActivity(Intent(this, PermissionCheckActivity::class.java))
        }
        */
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
            binding.statusTitle.text = getString(R.string.status_inactive)
            binding.statusSubtitle.text = if (!isNotificationListenerGranted) "Notification access required" else "Service is paused"
            pulseAnimator?.cancel()
        } else {
            binding.statusTitle.text = getString(R.string.status_active)
            binding.statusSubtitle.text = "Monitoring Active"
            if (pulseAnimator?.isRunning == false) {
                pulseAnimator?.start()
            }
        }

        if (!prefs.isServiceEnabled && isNotificationListenerGranted) {
            binding.statusTitle.text = getString(R.string.service_paused)
        }
    }

    private fun showHowToUseDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_how_to_use, null)
        val dialog = AlertDialog.Builder(this, R.style.Theme_PriorityPing_Dialog)
            .setView(dialogView)
            .create()

        dialogView.findViewById<View>(R.id.btnDismiss).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
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
