package com.example.focusos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.focusos.data.local.entity.AppUsageEvent
import java.sql.Time

@Dao
interface UsageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: AppUsageEvent)

    @Query("SELECT * FROM app_usage_events WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    suspend fun getEventsInRange(startTime: Long, endTime: Long): List<AppUsageEvent>
}