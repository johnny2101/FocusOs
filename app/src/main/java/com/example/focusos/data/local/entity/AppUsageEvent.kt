package com.example.focusos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class EventType {
    APP_LAUNCH,
    BLOCKED_ATTEMPT,
    FRICTION_PASSED
}

@Entity(tableName = "app_usage_events")
data class AppUsageEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val appName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: EventType
)