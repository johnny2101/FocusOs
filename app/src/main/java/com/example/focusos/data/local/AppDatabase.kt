package com.example.focusos.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.focusos.data.local.dao.UsageDao
import com.example.focusos.data.local.entity.AppUsageEvent

@Database(entities = [AppUsageEvent::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usageDao(): UsageDao
}
