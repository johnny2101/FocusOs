package com.example.focusos.service

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.accessibility.AccessibilityEvent
import com.example.focusos.data.local.entity.EventType
import com.example.focusos.domain.model.AnxietyState
import com.example.focusos.domain.repository.ConfigRepository
import com.example.focusos.domain.repository.UsageRepository
import com.example.focusos.domain.usecase.AnalyzeAnxietyUseCase
import com.example.focusos.util.getAppName
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FocusAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var usageRepository: UsageRepository
    @Inject
    lateinit var analyzeAnxietyUseCase: AnalyzeAnxietyUseCase
    @Inject
    lateinit var configRepository: ConfigRepository

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private var lastPackage: String? = null
    private var screenReceiver: BroadcastReceiver? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        registerScreenReceiver()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return

            if (packageName == "com.android.systemui" || packageName == this.packageName) return

            if (packageName != lastPackage) {
                lastPackage = packageName
                logEventAndAnalyze(packageName, EventType.APP_SWITCH)
            }
        }
    }

    override fun onInterrupt() {

    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        unregisterScreenReceiver()
    }

    private fun registerScreenReceiver() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }

        screenReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_SCREEN_ON -> logEventAndAnalyze("Screen", EventType.SCREEN_UNLOCK)
                    Intent.ACTION_SCREEN_OFF -> serviceScope.launch {
                        usageRepository.logEvent("Screen", "System", EventType.SCREEN_OFF)
                    }
                }
            }
        }
        registerReceiver(screenReceiver, filter)
    }

    private fun logEventAndAnalyze(packageName: String, type: EventType) {
        serviceScope.launch {
            usageRepository.logEvent(packageName, getAppName(packageName, packageManager), type)
            val state = analyzeAnxietyUseCase()
            if (state == AnxietyState.LOCKDOWN) {
                handleLockdown()
            }
        }
    }

    private suspend fun handleLockdown() {
        val currentEndTime = configRepository.getLockdownEndTimeFlow().first()
        val now = System.currentTimeMillis()

        if (now > currentEndTime) {
            val lockdownDurationMs = 20 * 60 * 1000L
            configRepository.setLockdownEndTime(now + lockdownDurationMs)

            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(homeIntent)
        }
    }

    private fun unregisterScreenReceiver() {
        screenReceiver?.let {
            unregisterReceiver(it)
            screenReceiver = null
        }
    }
}