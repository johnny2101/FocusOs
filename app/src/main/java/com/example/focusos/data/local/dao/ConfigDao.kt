package com.example.focusos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.focusos.data.local.entity.AppConfiguration

@Dao
interface ConfigDao {
    @Query("SELECT * FROM app_configurations WHERE packageName = :packageName")
    suspend fun getAppConfig(packageName: String): AppConfiguration?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: AppConfiguration)

    @Query("UPDATE app_configurations SET tier = :tier WHERE packageName = :packageName")
    suspend fun updateTier(packageName: String, tier: Int)

    @Query("SELECT * FROM app_configurations WHERE isHidden = 1")
    suspend fun getHiddenApps(): List<AppConfiguration>
}