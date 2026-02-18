package com.example.focusos.domain.usecase

import android.util.Log
import com.example.focusos.data.local.entity.EventType
import com.example.focusos.domain.model.AppItem
import com.example.focusos.domain.model.AppTier
import com.example.focusos.domain.repository.AppLauncherRepository
import com.example.focusos.domain.repository.ConfigRepository
import javax.inject.Inject

import com.example.focusos.domain.model.LaunchResult
import com.example.focusos.domain.repository.UsageRepository


class LaunchAppUseCase @Inject constructor(
    private val launcherRepository: AppLauncherRepository,
    private val configRepository: ConfigRepository,
    private val usageRepository: UsageRepository
) {
    suspend operator fun invoke(appItem: AppItem): LaunchResult {
        val tier = configRepository.getAppTier(appItem.packageName)

        return when (tier) {
            AppTier.UTILITY, AppTier.STANDARD -> {
                launcherRepository.launchApp(appItem.packageName)
                usageRepository.logEvent(appItem.packageName, appItem.label, EventType.APP_LAUNCH)
                LaunchResult.Allowed
            }
            AppTier.DOPAMINE -> {
                LaunchResult.Blocked(appItem)
            }
        }
    }
}