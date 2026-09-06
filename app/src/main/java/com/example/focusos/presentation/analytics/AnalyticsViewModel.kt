package com.example.focusos.presentation.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusos.data.local.entity.EventType
import com.example.focusos.domain.repository.ConfigRepository
import com.example.focusos.domain.repository.UsageRepository
import com.example.focusos.domain.usecase.CalculateWeeklyDiffUseCase
import com.example.focusos.domain.usecase.GenerateGitReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val configRepository: ConfigRepository,
    private val calculateWeeklyDiff: CalculateWeeklyDiffUseCase,
    private val generateGitReport: GenerateGitReportUseCase,
    private val usageRepository: UsageRepository
) : ViewModel() {
    val reportState: StateFlow<String?> = configRepository.getLatestWeeklyReportFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun forceCommitReport() {
        viewModelScope.launch {
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

            val reportString = generateGitReport(diffs, emergencyOverrides = overrides, authorName = "System.Admin")
            configRepository.setLatestWeeklyReport(reportString)
        }
    }
}