package com.example.priorityping.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.priorityping.data.PriorityContactRepository
import com.example.priorityping.model.PriorityContactEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ContactListViewModel(private val repository: PriorityContactRepository) : ViewModel() {

    val allContacts: StateFlow<List<PriorityContactEntity>> = repository.allContactsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteContact(contact: PriorityContactEntity) {
        viewModelScope.launch {
            repository.deleteContact(contact)
        }
    }

    fun toggleContactActive(contact: PriorityContactEntity) {
        viewModelScope.launch {
            repository.addOrUpdateContact(contact.copy(isActive = !contact.isActive))
        }
    }
}
