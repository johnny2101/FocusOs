package com.example.focusos.di

import android.content.Context
import androidx.room.Room
import com.example.focusos.data.local.AppDatabase
import com.example.focusos.data.local.dao.ConfigDao
import com.example.focusos.data.local.dao.UsageDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "focusos_db"
        ).build()
    }

    @Provides
    fun provideUsageDao(database: AppDatabase): UsageDao {
        return database.usageDao()
    }

    @Provides
    fun provideConfigDao(database: AppDatabase): ConfigDao {
        return database.configDao()
    }

}