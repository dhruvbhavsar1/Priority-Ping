package com.example.priorityping.model

enum class PriorityLevel(val displayName: String, val description: String) {
    HIGH("High Priority", "Overrides Silent and DND mode"),
    MEDIUM("Medium Priority", "Alerts even in vibrate mode"),
    NORMAL("Normal", "Default system behavior")
}