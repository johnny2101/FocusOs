package com.example.focusos.presentation.home

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusos.data.local.entity.EventType
import com.example.focusos.domain.model.AppItem
import com.example.focusos.domain.model.AppTier
import com.example.focusos.domain.model.LaunchResult
import com.example.focusos.domain.repository.AppLauncherRepository
import com.example.focusos.domain.repository.ConfigRepository
import com.example.focusos.domain.repository.UsageRepository
import com.example.focusos.domain.usecase.LaunchAppUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val appLauncherRepository: AppLauncherRepository,
    private val launchAppUseCase: LaunchAppUseCase,
    private val usageRepository: UsageRepository,
    private val configRepository: ConfigRepository
) : ViewModel() {
    private val _appList = MutableStateFlow<List<AppItem>>(emptyList())

    private val _lockdownApps = MutableStateFlow<List<AppItem>>(emptyList())

    private val _frictionState = MutableStateFlow<AppItem?>(null)
    val frictionState: StateFlow<AppItem?> = _frictionState.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _lockdownRemainingTime = MutableStateFlow(0L)
    val lockdownRemainingTime = _lockdownRemainingTime.asStateFlow()


    val appList: StateFlow<List<AppItem>> = combine(
        _appList,
        _lockdownApps,
        _lockdownRemainingTime
    ) { all, lockdown, remainingTime ->
        if (remainingTime > 0) lockdown else all
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        startLockDownTicker()
        loadApps()
    }

    private fun startLockDownTicker() {
        viewModelScope.launch {
            configRepository.getLockdownEndTimeFlow().collect() { endTime ->
                while (true) {
                    val remaining = endTime - System.currentTimeMillis()
                    if (remaining > 0) {
                        _lockdownRemainingTime.value = remaining
                        delay(1000)
                    } else {
                        _lockdownRemainingTime.value = 0L
                        break
                    }
                }
            }
        }
    }

    private fun loadApps() {
        viewModelScope.launch {
            _isLoading.value = true

            val apps = appLauncherRepository.getInstalledApps()
            _appList.value = apps

            val safeApps = mutableListOf<AppItem>()
            for (app in apps) {
                if (configRepository.getAppTier(app.packageName) == AppTier.UTILITY) {
                    safeApps.add(app)
                }
            }

            _lockdownApps.value = safeApps
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