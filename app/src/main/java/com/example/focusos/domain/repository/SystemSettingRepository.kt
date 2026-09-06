package com.example.focusos.domain.repository

interface SystemSettingRepository {

    fun hasUsageStatsPermission(): Boolean

    fun isDefaultHomeLauncher(): Boolean

    fun isOnboardingComplete(): Boolean

    suspend fun setOnboardingComplete(complete: Boolean)

    fun hasAccessibilityPermission(): Boolean
    fun hasNotificationPermission(): Boolean

}