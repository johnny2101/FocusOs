package com.example.focusos.data.repository

import android.Manifest
import android.app.AppOpsManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.example.focusos.domain.repository.SystemSettingRepository
import com.example.focusos.service.FocusAccessibilityService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SystemSettingRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sharedPreferences: SharedPreferences
): SystemSettingRepository {

    companion object {
        private const val PREF_ONBOARDING_COMPLETE = "pref_onboarding_complete"
    }

    override fun hasUsageStatsPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    override fun isDefaultHomeLauncher(): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }
        val resolveInfo = context.packageManager.resolveActivity(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY
        )

        return resolveInfo?.activityInfo?.packageName == context.packageName
    }

    override fun hasNotificationPermission(): Boolean {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    override fun isOnboardingComplete(): Boolean {
        return sharedPreferences.getBoolean(PREF_ONBOARDING_COMPLETE, false)
    }

    override suspend fun setOnboardingComplete(complete: Boolean) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit(commit = true) { putBoolean(PREF_ONBOARDING_COMPLETE, complete) }
        }
    }

    override fun hasAccessibilityPermission(): Boolean {
        // 1. Check if accessibility is enabled globally
        val accessibilityEnabled = try {
            Settings.Secure.getInt(context.contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED)
        } catch (e: Settings.SettingNotFoundException) {
            0
        }

        if (accessibilityEnabled == 1) {
            // 2. Safely flatten our specific service name (e.g., com.example.focusos/com.example.focusos.service.FocusAccessibilityService)
            val expectedComponent = ComponentName(context, FocusAccessibilityService::class.java).flattenToString()

            // 3. Check if our flattened name exists in the active services string
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )

            return enabledServices?.contains(expectedComponent) == true
        }
        return false
    }
}