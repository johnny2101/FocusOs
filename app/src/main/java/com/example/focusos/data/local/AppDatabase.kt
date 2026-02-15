package com.example.focusos.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.focusos.data.local.dao.ConfigDao
import com.example.focusos.data.local.dao.UsageDao
import com.example.focusos.data.local.entity.AppConfiguration
import com.example.focusos.data.local.entity.AppUsageEvent

@Database(entities = [AppUsageEvent::class, AppConfiguration::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usageDao(): UsageDao
    abstract fun configDao(): ConfigDao
}
