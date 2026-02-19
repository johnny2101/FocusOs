package com.example.focusos.domain.repository

import com.example.focusos.domain.model.AppTier
import kotlinx.coroutines.flow.Flow

interface ConfigRepository {
    suspend fun getAppTier(packageName: String): AppTier
    suspend fun setAppTier(packageName: String, tier: AppTier)

    suspend fun setLockdownEndTime(timeInMillis: Long)
    fun getLockdownEndTimeFlow(): Flow<Long>
}