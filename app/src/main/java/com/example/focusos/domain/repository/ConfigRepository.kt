package com.example.focusos.domain.repository

import com.example.focusos.domain.model.AppTier

interface ConfigRepository {
    suspend fun getAppTier(packageName: String): AppTier
    suspend fun setAppTier(packageName: String, tier: AppTier)
}