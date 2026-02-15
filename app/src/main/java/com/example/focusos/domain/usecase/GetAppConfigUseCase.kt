package com.example.focusos.domain.usecase

import com.example.focusos.domain.repository.AppLauncherRepository
import com.example.focusos.domain.repository.ConfigRepository
import com.example.focusos.model.AppConfigItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetAppConfigUseCase @Inject constructor(
    private val launcherRepository: AppLauncherRepository,
    private val configRepository: ConfigRepository
) {
    suspend operator fun invoke(): List<AppConfigItem> = withContext(Dispatchers.IO) {
        val apps = launcherRepository.getInstalledApps()

        apps.map { app ->
            async {
                val tier = configRepository.getAppTier(app.packageName)
                AppConfigItem(
                    packageName = app.packageName,
                    label = app.label,
                    tier = tier
                )
            }
        }.awaitAll().sortedBy { it.label }
    }
}