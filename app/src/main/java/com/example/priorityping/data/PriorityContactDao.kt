package com.example.priorityping.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.priorityping.model.PriorityContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PriorityContactDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPriorityContact(contact: PriorityContactEntity)

    @Query("SELECT * FROM priority_contacts WHERE identifier = :identifier LIMIT 1")
    suspend fun getPriorityContact(identifier: String): PriorityContactEntity?

    @Query("SELECT * FROM priority_contacts WHERE identifier = :identifier AND appName = :appName LIMIT 1")
    suspend fun getContactByIdentifierAndApp(identifier: String, appName: String): PriorityContactEntity?

    @Query("SELECT * FROM priority_contacts ORDER BY appName, identifier")
    suspend fun getAllContacts(): List<PriorityContactEntity>

    @Query("SELECT * FROM priority_contacts ORDER BY appName, identifier")
    fun observeAllContacts(): Flow<List<PriorityContactEntity>>

    @Delete
    suspend fun deletePriorityContact(contact: PriorityContactEntity)

    @Query("SELECT * FROM priority_contacts WHERE appName = :appName ORDER BY identifier")
    suspend fun getContactsByApp(appName: String): List<PriorityContactEntity>

    @Query("SELECT COUNT(*) FROM priority_contacts WHERE appName = :appName")
    suspend fun getCountByApp(appName: String): Int
}
