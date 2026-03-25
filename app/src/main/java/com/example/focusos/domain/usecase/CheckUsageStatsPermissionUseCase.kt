package com.example.focusos.domain.usecase

import com.example.focusos.domain.repository.SystemSettingRepository
import javax.inject.Inject

class CheckUsageStatsPermissionUseCase @Inject constructor(
    private val repository: SystemSettingRepository
){
    operator fun invoke(): Boolean = repository.hasUsageStatsPermission()
}