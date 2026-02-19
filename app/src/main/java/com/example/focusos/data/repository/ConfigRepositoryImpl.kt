package com.example.focusos.data.repository

import android.content.SharedPreferences
import com.example.focusos.data.local.dao.ConfigDao
import com.example.focusos.data.local.entity.AppConfiguration
import com.example.focusos.domain.model.AppTier
import com.example.focusos.domain.repository.ConfigRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import androidx.core.content.edit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

class ConfigRepositoryImpl @Inject constructor(
    private val configDao: ConfigDao,
    private val sharedPreferences: SharedPreferences
) : ConfigRepository {

    companion object {
        private const val KEY_LOCKDOWN_END = "lockdown_end_time"
    }

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

    override suspend fun setLockdownEndTime(timeInMillis: Long) {
        sharedPreferences.edit { putLong(KEY_LOCKDOWN_END, timeInMillis) }
    }

    override fun getLockdownEndTimeFlow(): Flow<Long> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            if (key == KEY_LOCKDOWN_END) {
                trySend(prefs.getLong(key, 0))
            }
        }

        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)

        trySend(sharedPreferences.getLong(KEY_LOCKDOWN_END, 0L))

        awaitClose {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

}