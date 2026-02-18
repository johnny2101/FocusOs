package com.example.focusos.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusos.data.local.entity.EventType
import com.example.focusos.domain.model.AppItem
import com.example.focusos.domain.model.LaunchResult
import com.example.focusos.domain.repository.AppLauncherRepository
import com.example.focusos.domain.repository.UsageRepository
import com.example.focusos.domain.usecase.LaunchAppUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val appLauncherRepository: AppLauncherRepository,
    private val launchAppUseCase: LaunchAppUseCase,
    private val usageRepository: UsageRepository
) : ViewModel() {
    private val _appList = MutableStateFlow<List<AppItem>>(emptyList())
    val appList: StateFlow<List<AppItem>> = _appList.asStateFlow()

    private val _frictionState = MutableStateFlow<AppItem?>(null)
    val frictionState: StateFlow<AppItem?> = _frictionState.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadApps()
    }

    private fun loadApps() {
        viewModelScope.launch {
            _isLoading.value = true
            _appList.value = appLauncherRepository.getInstalledApps()
            _isLoading.value = false
        }
    }

    fun onAppClick(appItem: AppItem) {
        viewModelScope.launch {
            when(val result = launchAppUseCase(appItem)) {
                is LaunchResult.Allowed -> {
                }
                is LaunchResult.Blocked -> {
                    _frictionState.value = result.appItem
                }
            }
        }
    }

    fun onFrictionPassed() {
        val appItem = _frictionState.value
        if (appItem != null) {
            viewModelScope.launch {
                appLauncherRepository.launchApp(appItem.packageName)
                usageRepository.logEvent(appItem.packageName, appItem.label, EventType.FRICTION_PASSED)
                _frictionState.value = null
            }
        }
    }

    fun onFrictionDismissed() {
        val appItem = _frictionState.value
        if (appItem != null) {
            viewModelScope.launch {
                usageRepository.logEvent(appItem.packageName, appItem.label, EventType.BLOCKED_ATTEMPT)
                _frictionState.value = null
            }
        }
    }
}