package com.example.priorityping

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.priorityping.adapter.ContactListAdapter
import com.example.priorityping.databinding.ActivityContactListBinding
import com.example.priorityping.model.SupportedApps
import com.example.priorityping.viewmodel.ContactListViewModel
import com.example.priorityping.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class ContactListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactListBinding
    private val viewModel: ContactListViewModel by viewModels {
        ViewModelFactory((application as PriorityPingApplication).repository)
    }
    private lateinit var adapter: ContactListAdapter
    private var appFilter: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appFilter = intent.getStringExtra("APP_NAME")
        
        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        observeContacts()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
        
        appFilter?.let {
            val app = SupportedApps.fromAppName(it)
            supportActionBar?.title = "${app?.displayName ?: it} Contacts"
        }
    }

    private fun setupRecyclerView() {
        adapter = ContactListAdapter(
            onToggleActive = { viewModel.toggleContactActive(it) },
            onDelete = { showDeleteConfirmation(it) },
            onClick = { launchEditContact(it.id, it.appName) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, AddContactActivity::class.java).apply {
                putExtra("APP_NAME", appFilter ?: SupportedApps.WHATSAPP.appName)
            }
            startActivity(intent)
        }
    }

    private fun observeContacts() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allContacts.collect { allContacts ->
                    val filteredList = if (appFilter != null) {
                        allContacts.filter { it.appName == appFilter }
                    } else {
                        allContacts
                    }
                    
                    adapter.submitList(filteredList)
                    binding.emptyState.visibility = if (filteredList.isEmpty()) View.VISIBLE else View.GONE
                    binding.recyclerView.visibility = if (filteredList.isEmpty()) View.GONE else View.VISIBLE
                }
            }
        }
    }

    private fun showDeleteConfirmation(contact: com.example.priorityping.model.PriorityContactEntity) {
        AlertDialog.Builder(this)
            .setTitle("Delete Contact")
            .setMessage("Are you sure you want to delete ${contact.identifier}?")
            .setPositiveButton("Delete") { _, _ -> viewModel.deleteContact(contact) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun launchEditContact(id: Int, appName: String) {
        val intent = Intent(this, AddContactActivity::class.java).apply {
            putExtra("CONTACT_ID", id)
            putExtra("APP_NAME", appName)
        }
        startActivity(intent)
    }
}
