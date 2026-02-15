package com.example.focusos.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusos.domain.model.AppItem
import com.example.focusos.domain.model.LaunchResult
import com.example.focusos.domain.repository.AppLauncherRepository
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
    private val launchAppUseCase: LaunchAppUseCase
) : ViewModel() {
    private val _appList = MutableStateFlow<List<AppItem>>(emptyList())
    val appList: StateFlow<List<AppItem>> = _appList.asStateFlow()

    private val _frictionState = MutableStateFlow<String?>(null)
    val frictionState: StateFlow<String?> = _frictionState.asStateFlow()

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

    fun onAppClick(packageName: String) {
        viewModelScope.launch {
            when(val result = launchAppUseCase(packageName)) {
                is LaunchResult.Allowed -> {
                }
                is LaunchResult.Blocked -> {
                    _frictionState.value = result.packageName
                }
            }
        }
    }

    fun onFrictionPassed() {
        val packageName = _frictionState.value
        if (packageName != null) {
            appLauncherRepository.launchApp(packageName)
            _frictionState.value = null
        }
    }

    fun onFrictionDismissed() {
        _frictionState.value = null
    }
}