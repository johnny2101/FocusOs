package com.example.focusos.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusos.domain.usecase.CheckDefaultHomeRoleUseCase
import com.example.focusos.domain.usecase.CheckNotificationPermissionUseCase
import com.example.focusos.domain.usecase.CheckUsageStatsPermissionUseCase
import com.example.focusos.domain.usecase.CompleteOnboardingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val checkDefaultHomeRole: CheckDefaultHomeRoleUseCase,
    private val checkUsageStatsPermission: CheckUsageStatsPermissionUseCase,
    private val checkNotificationPermission: CheckNotificationPermissionUseCase,
    private val completeOnboarding: CompleteOnboardingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<OnboardingState>(OnboardingState.Loading)
    val uiState: StateFlow<OnboardingState> = _uiState.asStateFlow()

    init {
        verifyPermissions()
    }

    fun onEvent(event: OnboardingEvent) {
        when (event) {
            is OnboardingEvent.CheckPermissions -> verifyPermissions()
            is OnboardingEvent.FinishOnboarding -> finalizeOnboarding()
        }
    }

    fun verifyPermissions() {
        if (!checkDefaultHomeRole()) {
            _uiState.value = OnboardingState.NeedsDefaultHome
            return
        }

        if (!checkUsageStatsPermission()) {
            _uiState.value = OnboardingState.NeedsUsageStats
            return
        }

        if (!checkNotificationPermission()) {
            _uiState.value = OnboardingState.NeedsNotification
            return
        }

        _uiState.value = OnboardingState.Complete
    }

    fun finalizeOnboarding() {
        viewModelScope.launch {
            completeOnboarding()

            _uiState.value = OnboardingState.Complete
        }

    }
}
