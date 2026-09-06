package com.example.focusos.domain.repository

import com.example.focusos.data.local.entity.AppUsageEvent
import com.example.focusos.data.local.entity.EventType
import com.example.focusos.model.PackageUsageStat

interface UsageRepository {
    suspend fun logEvent(packageName: String, appName: String, type: EventType)

    suspend fun getEventsSince(timestamp: Long): List<AppUsageEvent>

    suspend fun getUsageStatsInRange(startTime: Long, endTime: Long): List<PackageUsageStat>

    suspend fun getEventCountByTypeInRange(eventType: EventType, startTime: Long, endTime: Long): Int

}