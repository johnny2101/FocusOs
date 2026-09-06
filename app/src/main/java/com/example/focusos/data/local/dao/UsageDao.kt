package com.example.focusos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.focusos.data.local.entity.AppUsageEvent
import com.example.focusos.data.local.entity.EventType
import com.example.focusos.model.PackageUsageStat

@Dao
interface UsageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: AppUsageEvent)

    @Query("SELECT * FROM app_usage_events WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    suspend fun getEventsInRange(startTime: Long, endTime: Long): List<AppUsageEvent>

    @Query("SELECT * FROM app_usage_events WHERE timestamp >= :startTime ORDER BY  timestamp DESC")
    suspend fun getEventsSince(startTime: Long): List<AppUsageEvent>

    @Query("""
        SELECT packageName, appName, COUNT(id) as eventCount
        FROM app_usage_events
        WHERE timestamp BETWEEN :startTime and :endTime
        AND eventType IN ('APP_LAUNCH', 'APP_SWITCH', 'SCREEN_UNLOCK')
        GROUP BY packageName
        ORDER BY eventCount DESC
    """)
    suspend fun getUsageStatsInRange(startTime: Long, endTime: Long): List<PackageUsageStat>

    @Query("SELECT COUNT(id) FROM app_usage_events WHERE eventType = :eventType AND timestamp BETWEEN :startTime AND :endTime")
    suspend fun getEventCountByTypeInRange(eventType: EventType, startTime: Long, endTime: Long): Int

}