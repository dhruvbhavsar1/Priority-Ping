package com.example.priorityping.data

import androidx.room.TypeConverter
import com.example.priorityping.model.PriorityLevel

class PriorityConverters {

    @TypeConverter
    fun fromPriority(level: PriorityLevel): String {
        return level.name
    }

    @TypeConverter
    fun toPriority(value: String): PriorityLevel {
        return PriorityLevel.valueOf(value)
    }
}
