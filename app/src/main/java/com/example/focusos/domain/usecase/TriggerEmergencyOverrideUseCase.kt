package com.example.focusos.domain.usecase

import com.example.focusos.data.local.entity.EventType
import com.example.focusos.domain.repository.ConfigRepository
import com.example.focusos.domain.repository.UsageRepository
import javax.inject.Inject

class TriggerEmergencyOverrideUseCase @Inject constructor(
    private val configRepository: ConfigRepository,
    private val usageRepository: UsageRepository
) {
    suspend operator fun invoke() {
        val now = System.currentTimeMillis()

        configRepository.setLockdownEndTime(0L)

        usageRepository.logEvent(
            packageName = "System",
            appName = "Emergency Override",
            type = EventType.EMERGENCY_OVERRIDE
        )
    }
}