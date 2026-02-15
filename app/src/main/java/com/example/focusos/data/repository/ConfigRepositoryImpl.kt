package com.example.focusos.data.repository

import com.example.focusos.data.local.dao.ConfigDao
import com.example.focusos.data.local.entity.AppConfiguration
import com.example.focusos.domain.model.AppTier
import com.example.focusos.domain.repository.ConfigRepository
import javax.inject.Inject

class ConfigRepositoryImpl @Inject constructor(
    private val configDao: ConfigDao
) : ConfigRepository {
    override suspend fun getAppTier(packageName: String): AppTier {
        val config = configDao.getAppConfig(packageName)
        return if (config != null) {
            AppTier.fromId(config.tier)
        } else {
            AppTier.STANDARD
        }
    }

    override suspend fun setAppTier(
        packageName: String,
        tier: AppTier
    ) {
        val config = AppConfiguration(packageName = packageName, tier = tier.id)
        configDao.insertConfig(config)
    }

}