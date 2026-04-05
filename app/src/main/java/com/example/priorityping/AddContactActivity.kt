package com.example.priorityping

import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.priorityping.databinding.ActivityAddContactBinding
import com.example.priorityping.model.PriorityLevel
import com.example.priorityping.model.SupportedApps
import com.example.priorityping.viewmodel.AddContactViewModel
import com.example.priorityping.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class AddContactActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddContactBinding
    private val viewModel: AddContactViewModel by viewModels {
        ViewModelFactory((application as PriorityPingApplication).repository)
    }

    private var selectedPriority = PriorityLevel.NORMAL
    private var selectedVibration = "NORMAL"
    private var appName: String = ""
    private var contactId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appName = intent.getStringExtra("APP_NAME") ?: ""
        contactId = intent.getIntExtra("CONTACT_ID", 0)

        val app = SupportedApps.fromAppName(appName) ?: run {
            finish()
            return
        }

        setupUI(app)
        setupClickListeners()

        if (contactId != 0) {
            viewModel.loadContact(contactId)
            observeContact()
        }
    }

    private fun setupUI(app: com.example.priorityping.model.SupportedApp) {
        binding.appNameText.text = app.displayName
        // Fix 3: Set icon resource and remove programmatically set background color
        binding.appIcon.setImageResource(app.iconRes)
        binding.identifierInputLayout.hint = app.identifierHint
        
        val color = ContextCompat.getColor(this, app.colorRes)
        binding.btnSave.backgroundTintList = ColorStateList.valueOf(color)
        
        // Initial selection
        updatePrioritySelection(PriorityLevel.NORMAL)
        updateVibrationSelection("NORMAL")
    }

    private fun setupClickListeners() {
        binding.priorityHighTab.setOnClickListener { updatePrioritySelection(PriorityLevel.HIGH) }
        binding.priorityMediumTab.setOnClickListener { updatePrioritySelection(PriorityLevel.MEDIUM) }
        binding.priorityNormalTab.setOnClickListener { updatePrioritySelection(PriorityLevel.NORMAL) }

        binding.vibrateLightCard.setOnClickListener { updateVibrationSelection("LIGHT") }
        binding.vibrateNormalCard.setOnClickListener { updateVibrationSelection("NORMAL") }
        binding.vibrateHardCard.setOnClickListener { updateVibrationSelection("HARD") }
        binding.vibrateVeryHardCard.setOnClickListener { updateVibrationSelection("VERY_HARD") }

        binding.btnSave.setOnClickListener {
            val identifier = binding.identifierEditText.text.toString()
            val label = binding.labelEditText.text.toString()

            viewModel.saveContact(
                appName = appName,
                identifier = identifier,
                label = label,
                priority = selectedPriority,
                vibration = selectedVibration,
                id = contactId,
                onSuccess = {
                    Toast.makeText(this, R.string.save_success, Toast.LENGTH_SHORT).show()
                    finish()
                },
                onError = { error ->
                    Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    private fun updatePrioritySelection(priority: PriorityLevel) {
        selectedPriority = priority
        
        // Reset all to default unselected state
        binding.priorityHighTab.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        binding.priorityHighTab.setTextColor(android.graphics.Color.parseColor("#AAAAAA"))
        
        binding.priorityMediumTab.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        binding.priorityMediumTab.setTextColor(android.graphics.Color.parseColor("#AAAAAA"))
        
        binding.priorityNormalTab.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        binding.priorityNormalTab.setTextColor(android.graphics.Color.parseColor("#AAAAAA"))
        
        // Highlight the selected tab
        when (priority) {
            PriorityLevel.HIGH -> {
                binding.priorityHighTab.setBackgroundColor(android.graphics.Color.parseColor("#4CAF50"))
                binding.priorityHighTab.setTextColor(android.graphics.Color.WHITE)
            }
            PriorityLevel.MEDIUM -> {
                binding.priorityMediumTab.setBackgroundColor(android.graphics.Color.parseColor("#4CAF50"))
                binding.priorityMediumTab.setTextColor(android.graphics.Color.WHITE)
            }
            PriorityLevel.NORMAL -> {
                binding.priorityNormalTab.setBackgroundColor(android.graphics.Color.parseColor("#4CAF50"))
                binding.priorityNormalTab.setTextColor(android.graphics.Color.WHITE)
            }
        }
    }

    private fun updateVibrationSelection(vibration: String) {
        selectedVibration = vibration
        
        // Update radio buttons
        binding.radioLight.isChecked = vibration == "LIGHT"
        binding.radioNormalVib.isChecked = vibration == "NORMAL"
        binding.radioHard.isChecked = vibration == "HARD"
        binding.radioVeryHard.isChecked = vibration == "VERY_HARD"

        // Update card appearances (Optional: adds a border to the selected card)
        binding.vibrateLightCard.strokeWidth = if (vibration == "LIGHT") 2 else 0
        binding.vibrateLightCard.strokeColor = android.graphics.Color.parseColor("#4CAF50")
        
        binding.vibrateNormalCard.strokeWidth = if (vibration == "NORMAL") 2 else 0
        binding.vibrateNormalCard.strokeColor = android.graphics.Color.parseColor("#4CAF50")
        
        binding.vibrateHardCard.strokeWidth = if (vibration == "HARD") 2 else 0
        binding.vibrateHardCard.strokeColor = android.graphics.Color.parseColor("#4CAF50")
        
        binding.vibrateVeryHardCard.strokeWidth = if (vibration == "VERY_HARD") 2 else 0
        binding.vibrateVeryHardCard.strokeColor = android.graphics.Color.parseColor("#4CAF50")
    }

    private fun observeContact() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.contact.collect { contact ->
                    contact?.let {
                        binding.identifierEditText.setText(it.identifier)
                        binding.labelEditText.setText(it.label)
                        updatePrioritySelection(it.priorityLevel)
                        updateVibrationSelection(it.vibrationType)
                    }
                }
            }
        }
    }
}
