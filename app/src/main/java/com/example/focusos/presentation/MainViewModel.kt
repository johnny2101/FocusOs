package com.example.focusos.presentation

import androidx.lifecycle.ViewModel
import com.example.focusos.domain.usecase.GetOnboardingStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    getOnboardingStatus: GetOnboardingStatusUseCase
)  : ViewModel() {

    val startDestination: String = if (getOnboardingStatus()) {
        "home"
    } else {
        "onboarding"
    }
}