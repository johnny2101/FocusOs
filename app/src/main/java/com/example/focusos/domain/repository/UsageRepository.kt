package com.example.focusos.domain.repository

import com.example.focusos.data.local.entity.EventType

interface UsageRepository {
    suspend fun logEvent(packageName: String, appName: String, type: EventType)
}