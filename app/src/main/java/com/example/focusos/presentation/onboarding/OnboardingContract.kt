package com.example.focusos.presentation.onboarding

sealed interface OnboardingState {
    object Loading: OnboardingState
    object NeedsDefaultHome : OnboardingState
    object NeedsNotification : OnboardingState
    object NeedsUsageStats : OnboardingState
    object Complete : OnboardingState
}

sealed interface OnboardingEvent {
    object CheckPermissions : OnboardingEvent
    object FinishOnboarding : OnboardingEvent
}