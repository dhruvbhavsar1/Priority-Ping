package com.example.priorityping

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.priorityping.databinding.ActivityFeedbackBinding
import com.google.android.material.snackbar.Snackbar

class FeedbackActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFeedbackBinding
    private var selectedCategory: String = "General Feedback"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupChips()
        setupListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupChips() {
        binding.chipBugReport.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedCategory = "Bug Report"
                binding.chipFeatureRequest.isChecked = false
                binding.chipGeneral.isChecked = false
            } else if (!binding.chipFeatureRequest.isChecked && !binding.chipGeneral.isChecked) {
                binding.chipBugReport.isChecked = true
            }
        }

        binding.chipFeatureRequest.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedCategory = "Feature Request"
                binding.chipBugReport.isChecked = false
                binding.chipGeneral.isChecked = false
            } else if (!binding.chipBugReport.isChecked && !binding.chipGeneral.isChecked) {
                binding.chipFeatureRequest.isChecked = true
            }
        }

        binding.chipGeneral.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedCategory = "General Feedback"
                binding.chipBugReport.isChecked = false
                binding.chipFeatureRequest.isChecked = false
            } else if (!binding.chipBugReport.isChecked && !binding.chipFeatureRequest.isChecked) {
                binding.chipGeneral.isChecked = true
            }
        }
    }

    private fun setupListeners() {
        binding.deviceInfoCheckBox.setOnCheckedChangeListener { _, isChecked ->
            binding.deviceInfoHintText.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        binding.sendButton.setOnClickListener {
            val message = binding.messageInput.text.toString().trim()
            if (message.isEmpty()) {
                binding.messageInput.error = "Please describe your feedback before sending"
                return@setOnClickListener
            }
            binding.messageInput.error = null
            sendFeedbackEmail()
        }
    }

    private fun buildEmailSubject(): String {
        val subjectText = binding.subjectInput.text.toString().trim()
        return if (subjectText.isNotEmpty()) {
            "[PriorityPing] [$selectedCategory] — $subjectText"
        } else {
            "[PriorityPing] [$selectedCategory]"
        }
    }

    private fun buildEmailBody(): String {
        val message = binding.messageInput.text.toString().trim()
        val body = StringBuilder(message)

        if (binding.deviceInfoCheckBox.isChecked) {
            body.append("\n\n--- Device Info ---")
            body.append("\nApp Version: ").append(BuildConfig.VERSION_NAME)
            body.append("\nAndroid Version: ").append(Build.VERSION.RELEASE)
            body.append("\nAPI Level: ").append(Build.VERSION.SDK_INT)
            body.append("\nDevice: ").append(Build.MANUFACTURER).append(" ").append(Build.MODEL)
        }

        return body.toString()
    }

    private fun sendFeedbackEmail() {
        val developerEmail = "feedbacktopriorityping@gmail.com"
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(developerEmail))
            putExtra(Intent.EXTRA_SUBJECT, buildEmailSubject())
            putExtra(Intent.EXTRA_TEXT, buildEmailBody())
        }

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Snackbar.make(binding.root, R.string.feedback_no_email_app, Snackbar.LENGTH_LONG)
                .setAction("OK") { }
                .show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
