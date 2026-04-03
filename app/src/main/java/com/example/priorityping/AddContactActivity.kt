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
        binding.priorityHighCard.setOnClickListener { updatePrioritySelection(PriorityLevel.HIGH) }
        binding.priorityMediumCard.setOnClickListener { updatePrioritySelection(PriorityLevel.MEDIUM) }
        binding.priorityNormalCard.setOnClickListener { updatePrioritySelection(PriorityLevel.NORMAL) }

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
        binding.radioHigh.isChecked = priority == PriorityLevel.HIGH
        binding.radioMedium.isChecked = priority == PriorityLevel.MEDIUM
        binding.radioNormal.isChecked = priority == PriorityLevel.NORMAL
        
        val highlightColor = ContextCompat.getColor(this, R.color.colorPrimary)
        val defaultColor = ContextCompat.getColor(this, R.color.colorSurfaceVariant)
        
        binding.priorityHighCard.strokeWidth = if (priority == PriorityLevel.HIGH) 4 else 1
        binding.priorityHighCard.setStrokeColor(ColorStateList.valueOf(if (priority == PriorityLevel.HIGH) highlightColor else defaultColor))
        
        binding.priorityMediumCard.strokeWidth = if (priority == PriorityLevel.MEDIUM) 4 else 1
        binding.priorityMediumCard.setStrokeColor(ColorStateList.valueOf(if (priority == PriorityLevel.MEDIUM) highlightColor else defaultColor))
        
        binding.priorityNormalCard.strokeWidth = if (priority == PriorityLevel.NORMAL) 4 else 1
        binding.priorityNormalCard.setStrokeColor(ColorStateList.valueOf(if (priority == PriorityLevel.NORMAL) highlightColor else defaultColor))
    }

    private fun updateVibrationSelection(vibration: String) {
        selectedVibration = vibration
        binding.radioLight.isChecked = vibration == "LIGHT"
        binding.radioNormalVib.isChecked = vibration == "NORMAL"
        binding.radioHard.isChecked = vibration == "HARD"
        binding.radioVeryHard.isChecked = vibration == "VERY_HARD"
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
