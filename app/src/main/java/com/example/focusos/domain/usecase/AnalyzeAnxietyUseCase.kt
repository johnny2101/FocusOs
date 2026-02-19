package com.example.focusos.domain.usecase

import com.example.focusos.data.local.entity.EventType
import com.example.focusos.domain.model.AnxietyState
import com.example.focusos.domain.repository.UsageRepository
import javax.inject.Inject



class AnalyzeAnxietyUseCase @Inject constructor(
    private val usageRepository: UsageRepository
) {

    companion object {
        private const val WINDOW_UNLOCKS_MS = 10 * 60 * 1000L
        private const val THRESHOLD_UNLOCKS = 5

        private const val WINDOW_SWITCHES_MS = 2 * 60 * 1000L
        private const val THRESHOLD_SWITCHES = 12
    }

    suspend operator fun invoke(): AnxietyState {
        val now = System.currentTimeMillis()
        val lookBackWindow = now - WINDOW_UNLOCKS_MS

        val recentEvents = usageRepository.getEventsSince(lookBackWindow)

        val unlockCount = recentEvents.count {
            it.eventType == EventType.SCREEN_UNLOCK
        }

        val switchWindow = now - WINDOW_SWITCHES_MS
        val switchCount = recentEvents.count {
            it.eventType == EventType.APP_SWITCH && it.timestamp >= switchWindow
        }

        return when {
            unlockCount > THRESHOLD_UNLOCKS || switchCount > THRESHOLD_SWITCHES -> {
                AnxietyState.LOCKDOWN
            }
            unlockCount >= (THRESHOLD_UNLOCKS - 2) || switchCount >= (THRESHOLD_SWITCHES - 3) -> {
                AnxietyState.WARNING
            }
            else -> AnxietyState.NORMAL
        }
    }
}