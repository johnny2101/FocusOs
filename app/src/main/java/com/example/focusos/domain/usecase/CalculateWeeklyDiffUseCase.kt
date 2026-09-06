package com.example.focusos.domain.usecase

import com.example.focusos.domain.repository.UsageRepository
import com.example.focusos.model.AppUsageDiff
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CalculateWeeklyDiffUseCase @Inject constructor(
    private val usageRepository: UsageRepository
) {
    suspend operator fun invoke(currentWeekStart: Long, previousWeekStart: Long): List<AppUsageDiff> =
        withContext(Dispatchers.Default) {
            val currentWeekEnd = currentWeekStart + (7 * 24 * 60 * 60 * 1000L)
            val previousWeekEnd = currentWeekStart

            val currentStats = usageRepository.getUsageStatsInRange(currentWeekStart, currentWeekEnd)
                .filter { it.packageName != "Screen" }
            val previousStats = usageRepository.getUsageStatsInRange(previousWeekStart, previousWeekEnd)
                .filter { it.packageName != "Screen" }
            val previousStatsMap = previousStats.associateBy { it.packageName }

            currentStats.map { current ->
                val prevCount = previousStatsMap[current.packageName]?.eventCount ?: 0
                AppUsageDiff(
                    packageName = current.packageName,
                    appName = current.appName,
                    currentWeekCount = current.eventCount,
                    previousWeekCount = prevCount,
                    diff = current.eventCount - prevCount
                )
            }.sortedByDescending { it.diff }
        }
}