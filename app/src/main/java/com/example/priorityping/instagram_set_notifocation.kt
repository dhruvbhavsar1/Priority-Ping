package com.example.priorityping

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.priorityping.data.AppDatabase
import com.example.priorityping.model.PriorityContactEntity
import com.example.priorityping.model.PriorityLevel
import kotlinx.coroutines.launch


class instagram_set_notification : AppCompatActivity() {

    private lateinit var usernameInput: EditText
    private lateinit var prioritySwitch: Switch
    private lateinit var vibrationGroup: RadioGroup
    private lateinit var saveButton: Button

    private val database by lazy {
        AppDatabase.getDatabase(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instagram_set_notifocation)

        usernameInput = findViewById(R.id.usernameInput)
        prioritySwitch = findViewById(R.id.prioritySwitch)
        vibrationGroup = findViewById(R.id.vibrationGroup)
        saveButton = findViewById(R.id.saveButton)

        saveButton.setOnClickListener {

            val username = usernameInput.text.toString()

            if (username.isEmpty()) {
                Toast.makeText(this, "Enter username", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val priority = if (prioritySwitch.isChecked)
                PriorityLevel.HIGH
            else
                PriorityLevel.NORMAL

            val vibration = when (vibrationGroup.checkedRadioButtonId) {

                R.id.vibrationLight -> "LIGHT"
                R.id.vibrationNormal -> "NORMAL"
                R.id.vibrationHard -> "HARD"
                R.id.vibrationVeryHard -> "VERY_HARD"

                else -> "NORMAL"
            }

            val contact = PriorityContactEntity(
                appName = "instagram",
                identifier = username,
                priorityLevel = priority,
                vibrationType = vibration
            )

            lifecycleScope.launch {

                database.priorityContactDao()
                    .insertPriorityContact(contact)

                Toast.makeText(
                    this@instagram_set_notification,
                    "Saved Successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}