package com.example.priorityping.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "priority_contacts")
data class PriorityContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val appName: String,
    val identifier: String,
    val priorityLevel: PriorityLevel,
    val vibrationType: String,
    val addedAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val label: String = ""
)
