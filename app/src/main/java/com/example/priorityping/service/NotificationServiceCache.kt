package com.example.priorityping.service

object NotificationServiceCache {
    @Volatile
    var cacheVersion: Long = 0
        private set

    fun invalidate() {
        cacheVersion++
    }
}
