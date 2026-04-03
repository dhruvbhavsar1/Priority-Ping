package com.example.priorityping.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.priorityping.data.PriorityContactRepository
import com.example.priorityping.model.PriorityContactEntity
import com.example.priorityping.model.PriorityLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddContactViewModel(private val repository: PriorityContactRepository) : ViewModel() {

    private val _contact = MutableStateFlow<PriorityContactEntity?>(null)
    val contact: StateFlow<PriorityContactEntity?> = _contact.asStateFlow()

    fun loadContact(id: Int) {
        viewModelScope.launch {
            val all = repository.getAllContacts()
            _contact.value = all.find { it.id == id }
        }
    }

    fun saveContact(
        appName: String,
        identifier: String,
        label: String,
        priority: PriorityLevel,
        vibration: String,
        id: Int = 0,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (identifier.isBlank()) {
            onError("Identifier cannot be empty")
            return
        }

        viewModelScope.launch {
            val existing = repository.getContactByIdentifierAndApp(identifier, appName)
            if (existing != null && existing.id != id) {
                onError("A contact with this name already exists for this app")
                return@launch
            }

            val newContact = PriorityContactEntity(
                id = id,
                appName = appName,
                identifier = identifier,
                label = label,
                priorityLevel = priority,
                vibrationType = vibration
            )
            repository.addOrUpdateContact(newContact)
            onSuccess()
        }
    }
}
