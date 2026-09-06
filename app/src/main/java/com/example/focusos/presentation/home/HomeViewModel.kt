package com.example.focusos.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusos.data.local.entity.EventType
import com.example.focusos.domain.model.AppItem
import com.example.focusos.domain.model.AppTier
import com.example.focusos.domain.model.LaunchResult
import com.example.focusos.domain.repository.AppLauncherRepository
import com.example.focusos.domain.repository.ConfigRepository
import com.example.focusos.domain.repository.UsageRepository
import com.example.focusos.domain.usecase.CheckAccessibilityPermissionUseCase
import com.example.focusos.domain.usecase.LaunchAppUseCase
import com.example.focusos.domain.usecase.TriggerEmergencyOverrideUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val appLauncherRepository: AppLauncherRepository,
    private val launchAppUseCase: LaunchAppUseCase,
    private val usageRepository: UsageRepository,
    private val configRepository: ConfigRepository,
    private val checkAccessibilityPermission: CheckAccessibilityPermissionUseCase,
    private val triggerEmergencyOverride: TriggerEmergencyOverrideUseCase
) : ViewModel() {
    private val _appList = MutableStateFlow<List<AppItem>>(emptyList())

    private val _lockdownApps = MutableStateFlow<List<AppItem>>(emptyList())

    private val _frictionState = MutableStateFlow<AppItem?>(null)
    val frictionState: StateFlow<AppItem?> = _frictionState.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _lockdownRemainingTime = MutableStateFlow(0L)
    val lockdownRemainingTime = _lockdownRemainingTime.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isAccessibilityRevoked = MutableStateFlow(false)
    val isAccessibilityRevoked: StateFlow<Boolean> = _isAccessibilityRevoked.asStateFlow()

    private val _showEmergencyDialog = MutableStateFlow(false)
    val showEmergencyDialog: StateFlow<Boolean> = _showEmergencyDialog.asStateFlow()


    val appList: StateFlow<List<AppItem>> = combine(
        _appList,
        _lockdownApps,
        _lockdownRemainingTime,
        _searchQuery
    ) { all, lockdown, remainingTime, query ->
        val baseList = if (remainingTime > 0) lockdown else all

        if (query.isBlank()) {
            baseList
        } else {
            baseList.filter { it.label.contains(query, ignoreCase = true) }
        }
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
            configRepository.getLockdownEndTimeFlow().collect { endTime ->
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
            when (val result = launchAppUseCase(appItem)) {
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
                usageRepository.logEvent(
                    appItem.packageName,
                    appItem.label,
                    EventType.FRICTION_PASSED
                )
                _frictionState.value = null
            }
        }
    }

    fun onFrictionDismissed() {
        val appItem = _frictionState.value
        if (appItem != null) {
            viewModelScope.launch {
                usageRepository.logEvent(
                    appItem.packageName,
                    appItem.label,
                    EventType.BLOCKED_ATTEMPT
                )
                _frictionState.value = null
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun checkServiceStatus() {
        _isAccessibilityRevoked.value = !checkAccessibilityPermission()
    }

    fun onEmergencyOverrideRequested() {
        _showEmergencyDialog.value = true
    }

    fun onEmergencyOverrideDismissed() {
        _showEmergencyDialog.value = false
    }

    fun onEmergencyOverrideConfirmed() {
        viewModelScope.launch {
            triggerEmergencyOverride()
            _showEmergencyDialog.value = false
            _lockdownRemainingTime.value = 0L
        }
    }
}