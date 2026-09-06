package com.example.focusos.data.repository

import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.focusos.data.local.dao.ConfigDao
import com.example.focusos.data.local.entity.AppConfiguration
import com.example.focusos.domain.model.AppTier
import com.example.focusos.domain.repository.ConfigRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class ConfigRepositoryImpl @Inject constructor(
    private val configDao: ConfigDao,
    private val sharedPreferences: SharedPreferences
) : ConfigRepository {

    companion object {
        private const val KEY_LOCKDOWN_END = "lockdown_end_time"
        private const val KEY_LATEST_REPORT = "latest_weekly_report"
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

    override suspend fun setLatestWeeklyReport(report: String) {
        sharedPreferences.edit { putString(KEY_LATEST_REPORT, report) }
    }

    override fun getLatestWeeklyReportFlow(): Flow<String?> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            if (key == KEY_LATEST_REPORT) {
                trySend(prefs.getString(key, null))
            }
        }

        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        trySend(sharedPreferences.getString(KEY_LATEST_REPORT, null))
        awaitClose {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

}