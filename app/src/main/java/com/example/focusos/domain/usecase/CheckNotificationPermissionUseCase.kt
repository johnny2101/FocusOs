package com.example.focusos.domain.usecase

import com.example.focusos.domain.repository.SystemSettingRepository
import javax.inject.Inject

class CheckNotificationPermissionUseCase @Inject constructor(
    private val repository: SystemSettingRepository
) {
    operator fun invoke(): Boolean = repository.hasNotificationPermission()
}