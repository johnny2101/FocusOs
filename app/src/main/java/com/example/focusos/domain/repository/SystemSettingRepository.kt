package com.example.focusos.domain.repository

interface SystemSettingRepository {

    fun hasUsageStatsPermission(): Boolean

    fun isDefaultHomeLauncher(): Boolean

    fun hasNotificationPermission(): Boolean

    fun isOnboardingComplete(): Boolean

    suspend fun setOnboardingComplete(complete: Boolean)
}