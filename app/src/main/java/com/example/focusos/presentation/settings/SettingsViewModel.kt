package com.example.focusos.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusos.domain.model.AppTier
import com.example.focusos.domain.repository.ConfigRepository
import com.example.focusos.domain.usecase.GetAppConfigUseCase
import com.example.focusos.model.AppConfigItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getAppConfigUseCase: GetAppConfigUseCase,
    private val configRepository: ConfigRepository
) : ViewModel() {

    private val _apps = MutableStateFlow<List<AppConfigItem>>(emptyList())
    val apps: StateFlow<List<AppConfigItem>> = _apps.asStateFlow()

    init {
        loadApps()
    }

    fun loadApps() {
        viewModelScope.launch {
            _apps.value = getAppConfigUseCase()
        }
    }

    fun cycleTier(item: AppConfigItem) {
        viewModelScope.launch {
            val nextTier = when(item.tier) {
                AppTier.UTILITY -> AppTier.STANDARD
                AppTier.STANDARD -> AppTier.DOPAMINE
                AppTier.DOPAMINE -> AppTier.UTILITY
            }

            configRepository.setAppTier(item.packageName, nextTier)

            _apps.value = _apps.value.map {
                if (it.packageName == item.packageName) it.copy(tier = nextTier) else it
            }
        }

    }
}