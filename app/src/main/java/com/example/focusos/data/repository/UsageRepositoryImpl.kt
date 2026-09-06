package com.example.focusos.data.repository

import com.example.focusos.data.local.dao.UsageDao
import com.example.focusos.data.local.entity.AppUsageEvent
import com.example.focusos.data.local.entity.EventType
import com.example.focusos.domain.repository.UsageRepository
import com.example.focusos.model.PackageUsageStat
import javax.inject.Inject

class UsageRepositoryImpl @Inject constructor(
    private val usageDao: UsageDao
) : UsageRepository {
    override suspend fun logEvent(packageName: String, appName: String, type: EventType) {
        val event = AppUsageEvent(
            packageName = packageName,
            appName = appName,
            eventType = type,
            timestamp = System.currentTimeMillis()
        )
        usageDao.insertEvent(event)
    }

    override suspend fun getEventsSince(timestamp: Long): List<AppUsageEvent> {
        return usageDao.getEventsSince(timestamp)
    }

    override suspend fun getUsageStatsInRange(
        startTime: Long,
        endTime: Long
    ): List<PackageUsageStat> {
        return usageDao.getUsageStatsInRange(startTime, endTime)
    }

    override suspend fun getEventCountByTypeInRange(
        eventType: EventType,
        startTime: Long,
        endTime: Long
    ): Int {
        return usageDao.getEventCountByTypeInRange(eventType, startTime, endTime)
    }
}