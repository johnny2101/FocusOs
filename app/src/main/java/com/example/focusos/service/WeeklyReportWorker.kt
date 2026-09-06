package com.example.focusos.service

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.focusos.data.local.entity.EventType
import com.example.focusos.domain.repository.ConfigRepository
import com.example.focusos.domain.repository.UsageRepository
import com.example.focusos.domain.usecase.CalculateWeeklyDiffUseCase
import com.example.focusos.domain.usecase.GenerateGitReportUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class WeeklyReportWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val calculateWeeklyDiff: CalculateWeeklyDiffUseCase,
    private val generateGitReport: GenerateGitReportUseCase,
    private val configRepository: ConfigRepository,
    private val usageRepository: UsageRepository
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        return try {
            val now = System.currentTimeMillis()
            val oneWeekMs = 7 * 24 * 60 * 60 * 1000L
            val currentWeekStart = now - oneWeekMs
            val previousWeekStart = currentWeekStart - oneWeekMs

            val diffs = calculateWeeklyDiff(currentWeekStart, previousWeekStart)

            val overrides = usageRepository.getEventCountByTypeInRange(
                EventType.EMERGENCY_OVERRIDE,
                currentWeekStart,
                now
            )

            val reportString = generateGitReport(diffs, emergencyOverrides = overrides)

            configRepository.setLatestWeeklyReport(reportString)

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}