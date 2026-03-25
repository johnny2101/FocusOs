package com.example.focusos.domain.usecase

import com.example.focusos.domain.repository.SystemSettingRepository
import javax.inject.Inject

class CompleteOnboardingUseCase @Inject constructor(
    private val repository: SystemSettingRepository
) {
    suspend operator fun invoke(){
        repository.setOnboardingComplete(true)
    }
}