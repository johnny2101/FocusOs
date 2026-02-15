package com.example.focusos.domain.usecase

import android.util.Log
import com.example.focusos.domain.model.AppTier
import com.example.focusos.domain.repository.AppLauncherRepository
import com.example.focusos.domain.repository.ConfigRepository
import javax.inject.Inject

import com.example.focusos.domain.model.LaunchResult


class LaunchAppUseCase @Inject constructor(
    private val launcherRepository: AppLauncherRepository,
    private val configRepository: ConfigRepository
) {
    suspend operator fun invoke(packageName: String): LaunchResult {
        val tier = configRepository.getAppTier(packageName)

        return when (tier) {
            AppTier.UTILITY, AppTier.STANDARD -> {
                launcherRepository.launchApp(packageName)
                LaunchResult.Allowed
            }
            AppTier.DOPAMINE -> {
                LaunchResult.Blocked(packageName)
            }
        }
    }
}