package com.example.focusos.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.focusos.MainActivity
import com.example.focusos.R
import com.example.focusos.data.local.entity.EventType
import com.example.focusos.domain.model.AnxietyState
import com.example.focusos.domain.repository.ConfigRepository
import com.example.focusos.domain.repository.UsageRepository
import com.example.focusos.domain.usecase.AnalyzeAnxietyUseCase
import com.example.focusos.domain.usecase.CheckAccessibilityPermissionUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.focusos.util.getAppName

@AndroidEntryPoint
class BackgroundMonitorService : Service() {

    @Inject
    lateinit var usageRepository: UsageRepository
    @Inject
    lateinit var analyzeAnxietyUseCase: AnalyzeAnxietyUseCase
    @Inject
    lateinit var configRepository: ConfigRepository

    @Inject
    lateinit var checkAccessibilityPermission: CheckAccessibilityPermissionUseCase

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private var screenReceiver: BroadcastReceiver? = null
    private var lastForegroundPackage: String? = null

    companion object {
        private const val CHANNEL_ID = "focusos_monitor_channel"
        private const val NOTIFICATION_ID = 1001
        private const val POLL_INTERVAL_MS = 1000L
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startScreenReceiver()
        startUsagePoller()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, createNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, createNotification())
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        unregisterScreenReceiver()
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("FocusOs Active")
            .setContentText("Monitoring usage patterns...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "FocusOS Monitor"
            val descriptionText = "Keeps FocusOS active to monitor anxiety patterns"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startScreenReceiver() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }

        screenReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_SCREEN_ON -> logEvent("Screen", EventType.SCREEN_UNLOCK)
                    Intent.ACTION_SCREEN_OFF -> logEvent("Screen", EventType.SCREEN_OFF)
                }
            }
        }
        registerReceiver(screenReceiver, filter)
    }

    private fun unregisterScreenReceiver() {
        screenReceiver?.let {
            unregisterReceiver(it)
            screenReceiver = null
        }
    }

    private fun checkForForegroundApp(usageStatManager: UsageStatsManager) {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 1000

        val usageEvents = usageStatManager.queryEvents(startTime, endTime)
        val event = UsageEvents.Event()

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                val packageName = event.packageName

                if (packageName != lastForegroundPackage && packageName != this@BackgroundMonitorService.packageName) {
                    lastForegroundPackage = packageName
                    logEvent(packageName, EventType.APP_SWITCH)
                }
            }
        }
    }

    private fun logEvent(packageName: String, type: EventType) {
        serviceScope.launch {
            usageRepository.logEvent(packageName, getAppName(packageName, packageManager), type)

            val state = analyzeAnxietyUseCase()

            if (state != AnxietyState.NORMAL) {
                handleAnxietyState(state)
            }

        }
    }

    private suspend fun handleAnxietyState(state: AnxietyState) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = createNotification(state)
        notificationManager.notify(NOTIFICATION_ID, notification)

        if(state == AnxietyState.LOCKDOWN) {
            val currentEndTime = configRepository.getLockdownEndTimeFlow().first()
            val now = System.currentTimeMillis()

            if (now > currentEndTime) {
                val lockdownDurationMs = 1 * 60 * 1000L
                configRepository.setLockdownEndTime(now + lockdownDurationMs)
            }
        }
    }

    private fun createNotification(state: AnxietyState = AnxietyState.NORMAL): Notification {
        val title = when (state) {
            AnxietyState.NORMAL -> "FocusOs Active"
            AnxietyState.WARNING -> "Warning: High Usage"
            AnxietyState.LOCKDOWN -> "LOCKDOWN IMMINENT"
        }

        val text = when (state) {
            AnxietyState.NORMAL -> "Monitoring usage patterns..."
            AnxietyState.WARNING -> "Take a deep breath."
            AnxietyState.LOCKDOWN -> "Stop scrolling."
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun startUsagePoller() {
        val usageStatManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        serviceScope.launch {
            while (isActive) {
                if (!checkAccessibilityPermission()) {
                    checkForForegroundApp(usageStatManager)
                }
                delay(POLL_INTERVAL_MS)
            }
        }
    }


}