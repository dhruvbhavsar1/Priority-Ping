package com.example.priorityping.data

import com.example.priorityping.model.PriorityContactEntity
import com.example.priorityping.service.NotificationServiceCache
import kotlinx.coroutines.flow.Flow

class PriorityContactRepository(private val database: AppDatabase) {
    private val dao = database.priorityContactDao()

    val allContactsFlow: Flow<List<PriorityContactEntity>> = dao.observeAllContacts()

    suspend fun addOrUpdateContact(contact: PriorityContactEntity) {
        dao.insertPriorityContact(contact)
        NotificationServiceCache.invalidate()
    }

    suspend fun deleteContact(contact: PriorityContactEntity) {
        dao.deletePriorityContact(contact)
        NotificationServiceCache.invalidate()
    }

    suspend fun getAllContacts(): List<PriorityContactEntity> = dao.getAllContacts()

    suspend fun getContactsByApp(appName: String): List<PriorityContactEntity> = dao.getContactsByApp(appName)

    suspend fun getContactByIdentifierAndApp(identifier: String, appName: String): PriorityContactEntity? =
        dao.getContactByIdentifierAndApp(identifier, appName)

    suspend fun getCountByApp(appName: String): Int = dao.getCountByApp(appName)
}
